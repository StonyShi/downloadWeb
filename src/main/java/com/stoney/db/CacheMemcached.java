package com.stoney.db;

import net.rubyeye.xmemcached.*;
import net.rubyeye.xmemcached.exception.*;
import net.rubyeye.xmemcached.impl.*;
import net.rubyeye.xmemcached.transcoders.SerializingTranscoder;
import net.rubyeye.xmemcached.utils.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-30
 * Time: 上午11:10
 * To change this template use File | Settings | File Templates.
 */
public class CacheMemcached {

    private static  MemcachedClient memcachedClient = null;
    static {
        //节点权重
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(
                AddrUtil.getAddresses("localhost:11211 localhost:11212"), new int[]{1, 3});
//        AddrUtil.getAddressMap("localhost:11211,localhost:11212 localhost:11211,localhost:11212");
        //Consistent Hash(一致性哈希)
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        //Election Hash(选举散列)
        //builder.setSessionLocator(new ElectionMemcachedSessionLocator());

        //数据压缩 阀值默认是16K
//        memcachedClient.getTranscoder()).setCompressionThreshold(1024);
//        ((SerializingTranscoder)memcachedClient.getTranscoder()).setCompressionThreshold(1024);
//        memcachedClient.getTranscoder()).setPackZeros(false);
        try {
            memcachedClient = builder.build();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }



    public static void main(String[] args) {
        try {
            memcachedClient.set("hello", 0, "Hello,xmemcached");
            String value = memcachedClient.get("hello");
            System.out.println("hello=" + value);
            memcachedClient.delete("hello");
            value = memcachedClient.get("hello");
            System.out.println("hello=" + value);

            for (int i = 0 ; i < 10; i++)
                memcachedClient.set("key" + i, i, i);

            KeyIterator it = memcachedClient.getKeyIterator(AddrUtil.getOneAddress("localhost:11211"));
            while(it.hasNext()){
                String key = it.next();
                System.out.println("key = " + key + ", value = " + memcachedClient.get(key));
            }

            Map<InetSocketAddress, Map<String,String>> result = memcachedClient.getStats();

            System.out.println(result);
            Iterator address = result.keySet().iterator();
            while(address.hasNext()){
                InetSocketAddress addr = (InetSocketAddress) address.next();
                System.out.println("address = " + addr + ", value = " + result.get(addr));
            }


        } catch (MemcachedException e) {
            System.err.println("MemcachedClient operation fail");
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.err.println("MemcachedClient operation timeout");
            e.printStackTrace();
        } catch (InterruptedException e) {
            // ignore
        }
        try {
            //close memcached client
            memcachedClient.shutdown();
        } catch (IOException e) {
            System.err.println("Shutdown MemcachedClient fail");
            e.printStackTrace();
        }
    }
}

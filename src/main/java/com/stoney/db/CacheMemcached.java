package com.stoney.db;

import net.rubyeye.xmemcached.*;
import net.rubyeye.xmemcached.exception.*;
import net.rubyeye.xmemcached.impl.*;
import net.rubyeye.xmemcached.utils.*;
import java.io.IOException;
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
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(
                AddrUtil.getAddresses("localhost:11211 localhost:11212"));
        //Consistent Hash(一致性哈希)
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        //Election Hash(选举散列)
        //builder.setSessionLocator(new ElectionMemcachedSessionLocator());
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

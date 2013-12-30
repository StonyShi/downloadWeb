package com.stoney.db;

import redis.clients.jedis.JedisPool;

import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-30
 * Time: 上午11:08
 * To change this template use File | Settings | File Templates.
 */
public class CacheRedis {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private  static JedisPool jedisPool = null;
    private static String host = "localhost";
    static{
        init();
    }
    private static void init(){
        jedisPool = new JedisPool(host);
    }

    public static void main(String[] args) {

    }
}

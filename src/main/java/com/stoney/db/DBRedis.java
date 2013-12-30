package com.stoney.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.stoney.spider.SpiderAbstractHeadler;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-30
 * Time: 上午10:50
 * To change this template use File | Settings | File Templates.
 */
public class DBRedis {

    static class logger extends SpiderAbstractHeadler{
        public static void debug(String string) {
            print(string);
        }
        public static void error(Exception e) {
            e.printStackTrace();
        }
        public static void error(String msg) {
            print(msg);
        }
    }
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private  static JedisPool jedisPool = null;
    private static String host = "127.0.0.1";
    static{
        logger.debug("|--->....... init .........");
        init();
    }
    private static void startRedis(){
        try{
            //启动Redis 服务
            InitRedisCache.startRedis();
        }catch(Exception e){
        }
    }
    private static void init(){
        jedisPool = new JedisPool(host);
    }
    /**
     * 获取Pool
     * @return JedisPool
     */
    public static JedisPool getJedisPool(){
        if(jedisPool == null){
            return newJedisPool();
        }
        return jedisPool;
    }
    private static JedisPool newJedisPool(){
        return new JedisPool(host);
    }
    /**
     * 获取一个Jedis实例
     * @return Jedis
     */
    public static Jedis getJedis(){
        return jedisPool==null?getJedisPool().getResource():jedisPool.getResource();
    }
    /**
     * 释放对象池
     * @param jedis
     */
    public static void returnResource(Jedis jedis){
        try{
            if(jedis != null)
                jedisPool.returnResource(jedis);
        }catch(Exception e){
            logger.error("释放Jedis对象错误:" + e);
        }
    }
    /**
     * 销毁连接
     * @param jedis
     */
    public static void returnBrokenResource(Jedis jedis){
        try{
            if(jedis != null)
                jedisPool.returnBrokenResource(jedis);
        }catch(Exception e){
            logger.error("销毁Jedis连接错误：" + e);
        }

    }
    /**
     * Redis 是否有效
     * @return
     */
    public static boolean jedisValid(){
        return jedisPool != null;
    }
    /**
     * KEY是否存在
     * @param key
     * @return
     */
    public static boolean exists(String key){
        boolean exists = false;
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                exists = jedis.exists(key);
            } catch(Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
        return exists;
    }

    /**
     * 获取DB大小
     * @return
     */
    public static long getDBsize(){
        long size = 0L;
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                size = jedis.dbSize();
            } catch(Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
        return size;
    }
    /**
     * 保存 字符串
     * @param k
     * @param v
     */
    public static void setStr(String k, String v){
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                jedis.set(k, v);
            } catch(Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
    }
    /**
     * 保存 字符串
     * @param k
     * @param v
     * @param seconds
     */
    public static void setStr(String k, String v, int seconds){
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                jedis.setex(k, seconds, v);
            } catch(Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
    }
    public static String getStr(String key){
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                return jedis.get(key);
            } catch(Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
        return null;
    }
    /**
     * 保存数据 List
     * @param key
     * @param value
     */
    public static void setList(String key, String value) {
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                jedis.rpush(key, value);
            } catch(Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }

    }
    /**
     * 根据key取出全部list
     * @param key
     * @return
     */
    public static List<String> getList(String key) {
        return getListRange(key, 0, -1);
    }
    /**
     * 取列表第一个元素
     * @param key
     * @return
     */
    public static List<String> getListLeft(String key) {
        return getListRange(key, 0, 0);
    }
    /**
     * 判断集合第一个元素是否有效
     * @param key
     * @return
     */
    public static boolean isLeftDataInValid(String key){
        List<String> list = getListLeft(key);
        return (list != null && list.size() > 0 && !"-1".equals(list.get(0)))?true:false;
    }
    /**
     * 判断KEY 是否符合删除条件
     * @param key
     * @return
     */
    public static boolean isDeleteKey(String key){
        List<String> list = getListLeft(key);
        //System.out.println("isDeleteKey ： " +  (list != null && list.size() > 0 && "-1".equals(list.get(0))));
        return (list != null && list.size() > 0 && "-1".equals(list.get(0)))?true:false;
    }
    /**
     * 根据key 和范围取出数据
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static List<String> getListRange(String key, int start, int end) {
        List<String> list = new ArrayList<String>();
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                list = jedis.lrange(key, start, end);
            } catch (Exception e) {
                list = null;
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
        return list;
    }
    /**
     * 重新设置第一个元素值
     * @param key
     * @param value
     */
    public static void setListTop(String key, String value) {
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                jedis.lpush(key, value);
            } catch(Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }

    }
    /**
     * 删除指定的KEY
     * @param key
     */
    public static void delList(String key) {
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                jedis.del(key);
                //System.out.println("finish del key ： " + key);
            } catch (Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
    }

    /**
     * 移除并返回列表 key 的头元素
     * @param key
     */
    public static void lpop(String key) {
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                jedis.lpop(key);
                //System.out.println("finish lpop key ： " + key);
            } catch (Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
    }
    /**
     * 移除并返回列表 key 的尾元素
     * @param key
     */
    public static void rpop(String key) {
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                jedis.rpop(key);
                //System.out.println("finish rpop key ： " + key);
            } catch (Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
    }

    /**
     * 设置多长时间过期（秒为单位时间）
     * @param key
     */
    public static void setExpireTime(String key){
        setExpireTime(key, 259200);
    }
    /**
     * * 设置多长时间过期（秒为单位时间）
     * @param key
     * @param time 秒
     */
    public static void setExpireTime(String key,int time){
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                jedis.expire(key, time);
            } catch (Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
    }
    /**
     * 获取key集合
     * @param pattern  可以使用通配符*
     * @return
     */
    public static Set<String> getKeys(String pattern){
        Set<String>  keys = null;
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                keys = jedis.keys(pattern);
            } catch (Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
        return keys;
    }
    /**
     * 获取生命时间
     * @param key
     * @return 生命时间 秒
     */
    public static long getLifeTime(String key){
        long lifeTime = 0L;
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                lifeTime = jedis.ttl(key);
            } catch (Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
        return lifeTime;
    }
    /**
     * 删除全部过期数据
     * @param time 时间 秒
     */
    public static void DeleteAllExpiredData(long time){
        deleteExpiredData(time, "*");
    }
    /**
     * 删除指定时间内 指定keys 过期数据
     * @param time 时间 秒
     * @param pattern 可以使用通配符* (keys *)
     */
    public static void deleteExpiredData(long time,String pattern){
        Set<String> keys = getKeys(pattern);
        for(String key : keys){
            if(getLifeTime(key) < time){
                delList(key);
            }
        }
    }
    /**
     * 删除已重置无效数据   忽略是否重置
     * @param pattern 可以使用通配符 （* OR W*）
     */
    public static void deleteInvalidData(String pattern){
        deleteInvalidData(pattern, true);
    }
    /**
     * 删除已重置无效数据
     * @param pattern 可以使用通配符 （* OR W*）
     * @param isReset true 为忽略是否重置
     */
    public static void deleteInvalidData(String pattern,boolean isReset){
        Set<String> keys = getKeys(pattern);
        for(String key : keys){
            if(isReset || isDeleteKey(key)){
                delList(key);
            }
        }
    }
    /**
     * 删除所有数据库中的所有key
     */
    public static void deleteAllData(){
        if (jedisValid()) {
            Jedis jedis = null;
            try {
                jedis = getJedis();
                jedis.flushAll();
            } catch(Exception e) {
                returnBrokenResource(jedis);
                logger.error(e);
            }finally{
                returnResource(jedis);
            }
        }
    }
    /**
     * 删除指定时间内KEYS 忽略重置
     * @param start
     * @param end
     * @param keys  多个KEY 用,号分割
     */
    public static void deleteDataByDate(String start,String end,String keys){
        deleteDataByDate(start, end, keys, true);
    }
    /**
     * 删除指定时间内KEYS 是否忽略重置
     * @param start
     * @param end
     * @param keys
     * @param isReset true 忽略重置 默认忽略
     */
    public static void deleteDataByDate(String start,String end,String keys,boolean isReset){

        try {
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            long time = endDate.getTime() - startDate.getTime();
            int minute = (int) (time / 1000 / 60);
            deleteDataByDate(startDate, minute, keys.split(","),isReset);
        } catch (ParseException e) {
            logger.error("格式化日期错误：" + e );
        }
    }
    /**
     * 删除指定时间内KEYS
     * @param startDate
     * @param minute
     * @param keys
     * @param isReset true 忽略重置 默认忽略
     */
    public static void deleteDataByDate(Date startDate ,int minute ,String[] keys, boolean isReset){
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        String key = "";
        String[] dateList = getDateList(c, minute);
        for(int i = 0 ,len = keys.length; i<len ;i++){
            for(int j=0,len2=dateList.length;j<len2;j++){
                key = keys[i] + dateList[j];
                //System.out.println("get KEY : " + key);
                if(isReset || isDeleteKey(key)){
                    //System.out.println("start del key ： " + key);
                    delList(key);
                }
            }
        }
    }

    /**
     * 时间集合
     * @param c
     * @param minute
     * @return
     */
    public static String[] getDateList(Calendar c,int minute){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        StringBuilder dateList = new StringBuilder();
        for(int i=0;i<=minute;i++){
            dateList.append(sdf.format(c.getTime()));
            if(i<minute){
                dateList.append(",");
            }
            c.add(Calendar.MINUTE, 1);
        }
        return dateList.toString().split(",");
    }
    public static List<String> getDateList2(Calendar c,int minute){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        List<String> list = new ArrayList<String>();
        for(int i=0;i<=minute;i++){
            list.add(sdf.format(c.getTime()));
            c.add(Calendar.MINUTE, 1);
        }
        return list;
    }

    /**
     * 删除 Keys
     * @param start
     * @param end
     * @param keys
     */
    public static void deleteKeyMultiLine(String start,String end,String keys){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            long time = endDate.getTime() - startDate.getTime();
            int minute = (int) (time / 1000 / 60);
            deleteKeyMultiLine(startDate, minute, keys.split(","));
        } catch (ParseException e) {
            logger.error("格式化日期错误：" + e );
        }
    }
    public static void deleteKeyMultiLine(Date startDate ,int minute ,String[] keys){
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        List<String> dateList = getDateList2(c, minute);
        for(int i = 0 ,len = keys.length; i<len ;i++){
            System.out.println("|------->index " + i  + " handle  start processor key  " + keys[i] +"  ........");
            deleteKeyHandle(dateList, keys[i]);
        }
    }
    private static void deleteKeyHandle(List<String> dateList, String key) {
        new Thread(new deleteKeyRunnable(dateList, key)).start();
    }
    static class deleteKeyRunnable implements Runnable{
        private List<String> dateList;
        private String key;
        public deleteKeyRunnable(List<String> dateList, String key) {
            super();
            this.dateList = dateList;
            this.key = key;
        }
        public void run() {
            for(int i=0, len = dateList.size(); i < len; i++){
                String newKey = key + dateList.get(i);
//				System.out.println("get KEY : " + newKey);
                if(isDeleteKey(newKey)){
                    //System.out.println("start del key ： " + newKey);
                    delList(newKey);
                }
            }
        }

    }
    public static void main(String[] args) throws Exception {
        setStr("shi", "hui");
        System.out.println(getStr("shi"));
        System.out.println(getKeys("*"));
    }
}

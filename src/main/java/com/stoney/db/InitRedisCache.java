package com.stoney.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-30
 * Time: 上午10:52
 * To change this template use File | Settings | File Templates.
 */
public class InitRedisCache {

    public static void main(String[] args) {
        try {
            Process ps = Runtime.getRuntime().exec("D:\\Develop_Tools\\redis-2.4.5-win32-win64\\64bit\\redis-server");
            InputStream in = ps.getInputStream();
            int c;
            while ((c = in.read()) != -1) {
                System.out.print(c);// 如果你不需要看输出，这行可以注销掉
            }
            in.close();
            ps.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void startRedis(){
        if(checkServer("redis-server")){
            System.out.println("Redis已启动.......");
        }else{
            startRedis("D:\\Develop_Tools\\redis-2.4.5-win32-win64\\64bit\\redis-server");
        }
    }
    public static void startRedis(final String path){
        if(checkServer("redis-server")){
            System.out.println("Redis已启动.......");
        }else{
            System.out.println("开始执行初始化启动Redis");
            try{
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try{
//							Runtime.getRuntime().exec("redis-server");
                            Runtime.getRuntime().exec(path);
                        } catch (Exception e) {
                            System.out.println("执行初始化启动Redis 错误 ： " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
                t.setDaemon(true);
                t.start();
            } catch (Exception e) {
                System.out.println("初始化启动Redis 错误 ： " + e.getMessage());
            }
        }
    }
    public static void startMemcached(){
        if(checkServer("memcached")){
            System.out.println("Memcached已启动.......");
        }else{
            System.out.println("开始执行初始化启动Memcached");
            try{
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try{
                            Runtime.getRuntime().exec("memcached -p 11211");
                        } catch (Exception e) {
                            System.out.println("执行初始化启动Memcached 错误 ： " + e.getMessage());
                        }
                    }
                });
                t.setDaemon(true);
                t.start();
            } catch (Exception e) {
                System.out.println("初始化启动Memcached 错误 ： " + e.getMessage());
            }
        }
    }
    public static boolean checkServer(String server){
        BufferedReader reader = null;
        boolean falg = false;
        try{
            try{
                reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("cmd /k tasklist /FI \"IMAGENAME eq "+server+".exe\"").getInputStream()));
                String line;
                while((line = reader.readLine()) != null){
                    if(line.indexOf(server) >= 0)
                        falg = true;
                }
            }finally{
                if(reader != null)
                    reader.close();
            }
        }catch(Exception e){
            System.out.println("效验服务错误");
        }
        return falg;
    }
}

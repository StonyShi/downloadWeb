package com.stoney.spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-30
 * Time: 上午10:58
 * To change this template use File | Settings | File Templates.
 */
public class SpiderStaticsHeadler extends SpiderHeadler{

    static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    @Override
    public void process(String url) {
        // TODO Auto-generated method stub
        ExecutorService service = null;
        try{
            service = Executors.newFixedThreadPool(3);
            service.execute(new ProcessStaticsTask(queue));
            service.shutdown();
        }catch(Exception e){
        }
    }
    public static void run(){
        ExecutorService service = null;
        try{
            service = Executors.newFixedThreadPool(5);
            service.execute(new ProcessStaticsTask(queue));
            service.shutdown();
        }catch(Exception e){
        }
    }
    public static void put(String url){
        try {
            queue.put(url);
        } catch (InterruptedException e) {
        }
    }
    static class ProcessStaticsTask implements Runnable{
        private BlockingQueue<String> queue;
        public ProcessStaticsTask(BlockingQueue<String> queue) {
            super();
            this.queue = queue;
        }
        public void run() {
            try {
                while(true){
                    String url = queue.take();
                    if(checkUrlRedis(url)){
                        SpiderStaticsHeadler.saveStatic(url);
                        saveUrlToRedis(url);
                    }
                }
            } catch (InterruptedException e) {
                print(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public static void saveStatic(String url) {
        print("save static : %s", url);
        String path = getStaticPath(url);
        HttpClient httpclient = null;
        HttpContext localContext = null;
        //System.out.println("-------path :  " + path);
        String error = "";
        try{
            if (isNotEmpty(path)) {
                File file = new File(SAVE_DIR + path);
                if (!file.exists())
                    file.mkdirs();
                String imgName = getStaticName(url);
                HttpGet httpget = null;
                HttpResponse response;
                try {
                    File imageFile = new File(SAVE_DIR + path + "/" + imgName);
                    if (!imageFile.exists()) {
                        httpclient = new DefaultHttpClient();
                        localContext = new BasicHttpContext();

                        httpget = new HttpGet(url);
                        response = httpclient.execute(httpget, localContext);
                        HttpEntity entity = response.getEntity();

                        FileOutputStream outStream = new FileOutputStream(imageFile);
                        outStream.write(EntityUtils.toByteArray(entity));
                        outStream.close();
                        EntityUtils.consume(entity);
                    }
                } catch (ClientProtocolException e) {
                    error += e.getMessage();
                    error += ",---- ";
                    e.printStackTrace();
                } catch (IOException e) {
                    error += e.getMessage();
                    error += ",---- ";
                    e.printStackTrace();
                }
            }
        }finally {
            closeHttpClient(httpclient);
            if(isNotEmpty(error)){
                error += ", url = " + url;
                print(error);
                try {
                    failQueue.put(url);
                } catch (InterruptedException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    static String getStaticPath(String url) {
        //System.out.println("getImgPath : " + url);
        if (url.startsWith("http:")) {
            url = url.substring(url.indexOf("//") + 1);
            url = url.substring(url.indexOf("/") + 1);
            url = url.substring(0, url.lastIndexOf("/"));
            return url;
        } else {
            return null;
        }
    }
    static String getStaticName(String url) {
        //System.out.println("getImgName : " + url);
        url = url.substring(url.lastIndexOf("/") + 1);
        if(url.lastIndexOf("?") != -1 ){
            url = url.substring(0, url.lastIndexOf("?"));
        }
        return url;
    }
}

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
 * Time: 上午10:57
 * To change this template use File | Settings | File Templates.
 */
public class SpiderImagesHeadler extends SpiderHeadler{

    static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    public static void main(String[] args) {
        saveImg("http://www.keenthemes.com/preview/metronic_admin/assets/fonts/cJZKeOuBrn4kERxqtaUH3T8E0i7KZn-EPnyo3HZu7kw.woff");
        saveImg("http://www.keenthemes.com/preview/metronic_admin/assets/fonts/DXI1ORHCpsQm3Vp6mXoaTXhCUOGz7vYGh680lGh-uXM.woff");
        saveImg("http://www.keenthemes.com/preview/metronic_admin/assets/plugins/font-awesome/font/fontawesome-webfont.woff?v=3.2.1");
        saveImg("http://www.keenthemes.com/preview/metronic_admin/assets/fonts/k3k702ZOKiLJc3WVjuplzHhCUOGz7vYGh680lGh-uXM.woff");
        saveImg("http://www.keenthemes.com/preview/metronic_admin/assets/fonts/MTP_ySUJH_bn48VBG8sNSnhCUOGz7vYGh680lGh-uXM.woff");
    }

    @Override
    public void process(String url) {
        // TODO Auto-generated method stub
        ExecutorService service = null;
        try{
            service = Executors.newFixedThreadPool(3);
            service.execute(new ProcessImgTask(queue));
            service.shutdown();
        }catch(Exception e){
        }
    }
    static void put(String url){
        try {
            queue.put(url);
        } catch (InterruptedException e) {
        }
    }
    static class ProcessImgTask implements Runnable{
        private BlockingQueue<String> queue;
        public ProcessImgTask(BlockingQueue<String> queue) {
            super();
            this.queue = queue;
        }
        public void run() {
            try {
                while(true){
                    String url = queue.take();
                    SpiderImagesHeadler.save(url);
                    saveUrlToRedis(url);
                }
            } catch (InterruptedException e) {
                print(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public static void save(String url) {
        saveImg(url);
    }
    public static void saveImg(String url) {
        print("save src : %s", url);
        String path = getImgPath(url);
        HttpClient httpclient = null;
        HttpContext localContext = null;
        //System.out.println("-------path :  " + path);
        try{
            if (isNotEmpty(path)) {
                File file = new File(SAVE_DIR + path);
                if (!file.exists())
                    file.mkdirs();
                String imgName = getImgName(url);
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
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }finally {
            closeHttpClient(httpclient);
        }
    }
    static String getImgPath(String url) {
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

    static String getImgName(String url) {
        //System.out.println("getImgName : " + url);
        url = url.substring(url.lastIndexOf("/") + 1);
        if(url.lastIndexOf("?") != -1 ){
            url = url.substring(0, url.lastIndexOf("?"));
        }
        return url;
    }
}

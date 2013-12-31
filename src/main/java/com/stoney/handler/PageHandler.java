package com.stoney.handler;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.helper.HttpConnection;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Stony on 13-12-29.
 */
public class PageHandler extends BaseHandler{

    private static AtomicInteger index = new AtomicInteger(0);

    public static void process(String url){
        print("StaticHandler process %s.", url);
        String path = SAVE_DIR + "/" + extaFileDir(url) + "/" + extaFilePath(url);
        File outFile = new File(path + "/" + extaFileName(url) + "_" + extaFileQuery(url) + ".html");
        boolean flag = true;
        if(isSaveFile(outFile)){
            checkDir(path);
            try {
                download(new URL(url), outFile);
            } catch (Exception e) {
                flag = false;
                index.incrementAndGet();
                e.printStackTrace();
            } finally {
                if(flag){
                    addSucceed(url);
                    print("[%d] Save Page succeed.", index.get(), url, outFile.getAbsoluteFile());
                } else{
                    putMedia(url);
                    print("[%d] Save Page <%s>|##|<%s> failed.", index.get(), url, outFile.getAbsoluteFile());
                }
                reStart(url);
            }
        }
    }

    public static boolean savePage(HttpURLConnection conn, File outfile) throws Exception {
        boolean flag = false;
        if(conn.getResponseCode() == 200) {
            BufferedReader reader = null;
            BufferedWriter writer = null;
            InputStream in = null;
            try {
                in = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                writer = new BufferedWriter(new FileWriter(outfile));
//                char buf[] = new char[1024];
//                while(reader.read(buf) != -1){
//                    writer.write(buf);
//                }
                int b = 0;
                while ((b = reader.read()) != -1) {
                    writer.write(b);
                }
                writer.flush();
                flag = true;
            } catch (Exception e){
                flag = false;
                e.printStackTrace();
            } finally {
                close(reader);
                close(in);
                close(writer);
            }
        }
        return flag;
    }
    public static void process2(String url) {
        print("PageHandler process %s.", url);
        String path = SAVE_DIR + "/" + extaFileDir(url) + "/" + extaFilePath(url);
        File outFile = new File(path + "/" + extaFileName(url) + "_" + extaFileQuery(url) + ".html");
        if(isSaveFile(outFile)){
            checkDir(path);
//            CloseableHttpClient httpClient = (CloseableHttpClient) createHttpClient();
            DefaultHttpClient httpClient = new DefaultHttpClient();
            try {
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                if(savePage(response, outFile)){
                    addSucceed(url);
                    print("[%d] Save Page <%s>|##|<%s> succeed.", index.get(), url, outFile.getAbsoluteFile());
                } else{
                    putPage(url);
                    print("[%d] Save Page <%s>|##|<%s> failed.", index.get(), url, outFile.getAbsoluteFile());
                }
            } catch (Exception e) {
                addFailed(url);
                putPage(url);
                print("[%d] Get Page <%s> error.", index.get() ,url);
                index.incrementAndGet();
                e.printStackTrace();
            } finally {
                httpClient.getConnectionManager().shutdown();
                try {
                    BootHandler.start(url);
                } catch (IOException e) {
                }
            }
        }
    }

    public static boolean savePage(HttpResponse response, File outfile) throws Exception {
        HttpEntity httpEntity = response.getEntity();
        boolean flag = false;
        if(response.getStatusLine().getStatusCode() == 200 && httpEntity != null) {
            BufferedReader reader = null;
            BufferedWriter writer = null;
            InputStream in = null;
            try {
                httpEntity = new BufferedHttpEntity(httpEntity);
                in = httpEntity.getContent();
                reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                writer = new BufferedWriter(new FileWriter(outfile));
                char buf[] = new char[1024 * 1024 * 5];
                while(reader.read(buf) != -1){
                    writer.write(buf);
                }
                writer.flush();
                flag = true;
            } catch (Exception e){
                flag = false;
                e.printStackTrace();
            } finally {
                close(reader);
                close(in);
                close(writer);
            }
        }
        return flag;
    }

}

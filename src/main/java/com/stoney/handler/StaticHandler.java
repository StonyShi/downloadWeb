package com.stoney.handler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Stony on 13-12-29.
 */
public class StaticHandler extends BaseHandler{


    private static AtomicInteger index = new AtomicInteger(0);

    public static void process(String url){
        print("StaticHandler process %s.", url);
        String path = SAVE_DIR + "/" + extaFileDir(url) + "/" + extaFilePath(url);
        File outFile = new File(path + "/" + extaFileName(url));
        if(isSaveFile(outFile)){
            checkDir(path);
            CloseableHttpClient httpClient = (CloseableHttpClient) createHttpClient();
            try {
                HttpGet httpGet = new HttpGet(url);
                setTimeOut(httpGet);
                HttpResponse response = httpClient.execute(httpGet);
                if(saveMedia(response, outFile)){
                    addSucceed(url);
                    print("[%d] Save Media succeed.", index.get(), url, outFile.getAbsoluteFile());
                } else{
                    putMedia(url);
                    print("[%d] Save Media <%s>|##|<%s> failed.", index.get(), url, outFile.getAbsoluteFile());
                }
            } catch (Exception e) {
                addFailed(url);
                putMedia(url);
                print("[%d] Get Media <%s> error.", index.get() ,url);
                index.incrementAndGet();
                e.printStackTrace();
            } finally {
                close(httpClient);
                try {
                    BootHandler.start(url);
                } catch (IOException e) {
                }
            }
        }
    }

    public static boolean saveMedia(HttpResponse response, File outfile) throws Exception {
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
                writer = new BufferedWriter(new FileWriter(outfile, true));
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

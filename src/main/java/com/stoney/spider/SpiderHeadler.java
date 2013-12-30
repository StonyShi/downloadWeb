package com.stoney.spider;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.stoney.db.DBRedis;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Element;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-30
 * Time: 上午10:48
 * To change this template use File | Settings | File Templates.
 */
public abstract class SpiderHeadler extends SpiderAbstractHeadler {
    public abstract void process(String url);

    public static String SAVE_DIR = SpiderConfig.getSaveDir();
    public static String BASE_URL = SpiderConfig.getBaseURL();
    public static String INDEX_URL = SpiderConfig.getIndexURL();
    public static String MATH_URL = SpiderConfig.getItems("MATCH_STR");
    public static String COOKIES = "dotcom_user=StonyShi; logged_in=yes; tz=Asia%2FShanghai; __utma=1.1407264730.1386914106.1387510236.1387528465.18; __utmb=1.10.10.1387528465; __utmc=1; __utmz=1.1387510236.17.12.utmcsr=qingbob.com|utmccn=(referral)|utmcmd=referral|utmcct=/blog/%E6%8A%98%E8%85%BE%EF%BC%9A%E5%88%A9%E7%94%A8Node.js%20express%E6%A1%86%E5%AE%9E%E7%8E%B0%E5%9B%BE%E7%89%87%E4%B8%8A%E4%BC%A0; user_session=v91PLFVRwv59Ci0aYV3vPEbxotBPGqajaOu3U9EeLARpvOR5; spy_user=StonyShi; _gh_sess=BAh7CDoPc2Vzc2lvbl9pZCIlNzhhYTNjMGZkYTZkNDQyMDAzODUzODllNzJmMTUzZmI6DGNvbnRleHRJIgYvBjoGRUY6EF9jc3JmX3Rva2VuSSIxVk9GZmo4eGZIVXFab0tld2ZuS3dteWNuWFh3Q1VZWjdRc2Z6bElXSzlBRT0GOwdG--7a48a2e48ecef1b07b1dc01276d5153b235830be";
    public static String regex =
            "([\\s]*)(url[\\s]*[\\(]{1}[\\s]*[\"|']?)([a-zA-Z0-9:\\/\\.\\-\\_\\?\\-\\=\\#\\&]+)([\\s]*[\"|']?[\\)]{1})";
    private final static String[] FOOT_SUFFIX = {".eot",".woff",".ttf",".svg"};

    public static boolean isfoot(String src){
        for(String suffix : FOOT_SUFFIX){
            if(src.endsWith(suffix)){
                return true;
            }
            if(src.lastIndexOf(src) > -1){
                return true;
            }
        }
        return false;
    }

    final static Hashtable<String, String> table = new Hashtable<String, String>();
    final static Map<String, String> URLS = new HashMap<String, String>();

    static BlockingQueue<String> failQueue = new LinkedBlockingQueue<String>();
    public static boolean isMathUrl(String url){
        return url.matches(MATH_URL) || url.indexOf(MATH_URL) != -1;
    }
    public static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width - 1) + ".";
        else
            return s;
    }
    public static void closeHttpEntity(HttpEntity entity){
        if(entity != null){
            try {
                EntityUtils.consume(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void closeReader(Reader in){
        if(in != null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void closeWrite(Writer out){
        if(out != null){
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void closeInputStream(InputStream in){
        if(in != null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void closeOutputStream(OutputStream out){
        if(out != null){
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    static void closeHttpClient(HttpClient httpclient){
        try{
            if(httpclient != null && httpclient.getConnectionManager() != null){
                httpclient.getConnectionManager().shutdown();
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static void saveUrl(String url){
        DBRedis.setStr(MD5(url), url);
    }
    public static void saveUrlToRedis(String url){
        DBRedis.setStr(MD5(url), url);
    }
    public static boolean checkUrlRedis(String url){
        if(isEmpty(getUrlFromRedis(url)))
            return true;
        return false;
    }
    public static String getUrlFromRedis(String url){
        return DBRedis.getStr(MD5(url));
    }
    public static String MD5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return s;
        }
    }

    static String getHtml(String url) {
        HttpClient httpclient;
        CookieStore cookieStore;
        HttpContext localContext;
        httpclient = new DefaultHttpClient();
        cookieStore = new BasicCookieStore();
        localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        String html = "";
        try {
            int timeout = 80000;
            /** set client connection parameters **/
            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
            httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);

            response = httpclient.execute(httpget, localContext);
            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY) || (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) ||
                    (statusCode == HttpStatus.SC_SEE_OTHER) || (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
                String newUri = response.getLastHeader("Location").getValue();
                print("Location new url is %s.", newUri);
            }else{
                html = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            closeHttpClient(httpclient);
        }
        return html;
    }
    public static String getFileHtml(String name){
        StringBuffer info = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(name)), "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                info.append(line);
//				System.out.println(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info.toString();
    }
    static String getHref(Element link, String prefix) {
        String href = "";
        if (isEmpty(link.attr("abs:href"))) {
            href = link.attr("href");
        } else {
            href = link.attr("abs:href");
        }
        if (!href.startsWith("http:")) {
            href = prefix + href;
        }
        return href;
    }

    static String getHref(Element link) {
        return getHref(link, null);
    }

    public static String getBaseUrl(String str){
        String baseUrlName = "";
        try {
            URI uri  = new URI(str);
            baseUrlName = uri.getScheme() + "://" + uri.getAuthority() + "/";
        } catch (URISyntaxException e) {
        }
        return baseUrlName;
    }
}

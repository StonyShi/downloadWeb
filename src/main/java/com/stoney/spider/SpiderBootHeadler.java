package com.stoney.spider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-30
 * Time: 上午10:59
 * To change this template use File | Settings | File Templates.
 */
public class SpiderBootHeadler extends SpiderHeadler{
    static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

    @Override
    public void process(String url) {

    }
    public static void main(String[] args) throws Exception {
        String index = "http://themes.semicolonweb.com/html/coworker/index.php";
        index = "http://www.keenthemes.com/preview/metronic_admin/index.html";
        index = "http://suggeelson.com/themes/supr/dashboard.html";
        index = "http://runjs.cn/code";
        index = "http://runjs.cn/js/all.js?2012121305";
        index = "http://hmelius.com/avant/index.php";
        index = INDEX_URL;
        boot();

    }

    public static void boot(){
        parse(INDEX_URL);
        saveUrlToRedis(INDEX_URL);
        processStatics();
        run();
        procesFailQueue();
    }
    public static void processStatics(){
        SpiderStaticsHeadler.run();
    }
    public static void run(){
        ExecutorService service = null;
        try{
            service = Executors.newFixedThreadPool(5);
            service.execute(new ProcessUrlsTask(queue));
            service.shutdown();
        }catch(Exception e){
        }
    }
    public static void procesFailQueue(){
        String url;
        try {
            while(true){
                url = failQueue.take();
                save(url);
                saveUrlToRedis(url);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static class ProcessUrlsTask implements Runnable{
        private BlockingQueue<String> queue;
        public ProcessUrlsTask(BlockingQueue<String> queue) {
            super();
            this.queue = queue;
        }
        public void run() {
            try {
                while(true){
                    String url = queue.take();
                    if(checkUrlRedis(url)){
                        SpiderBootHeadler.parse(url);
                        saveUrlToRedis(url);
                    }
                }
            } catch (InterruptedException e) {
                print(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public static String absUrl(String baseUri, String relUrl) {
        URL base;
        try {
            try {
                base = new URL(baseUri);
            } catch (MalformedURLException e) {
                // the base is unsuitable, but the attribute may be abs on its own, so try that
                URL abs = new URL(relUrl);
                return abs.toExternalForm();
            }
            // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
            if (relUrl.startsWith("?"))
                relUrl = base.getPath() + relUrl;
            URL abs = new URL(base, relUrl);
            return abs.toExternalForm();
        } catch (MalformedURLException e) {
            return "";
        }
    }
    public static void put(String url){
        try {
            queue.put(url);
        } catch (InterruptedException e) {
        }
    }
    static void putStatics(String url){
        SpiderStaticsHeadler.put(url);
    }
    static void parse(String url) {
        if(isEmpty(url)) return;
        Document doc = null;
        try {
            doc = Jsoup.connect(url).header("Cookie", COOKIES).get();

            Elements links = doc.select("a[href]");
            Elements media = doc.select("[src]");
            Elements imports = doc.select("link[href]");
            print("\nMedia: (%d)", media.size());
            for (Element src : media) {
                if (src.tagName().equals("img")){
                    print(" * %s: <%s> %sx%s (%s)",src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),trim(src.attr("alt"), 20));
                    putStatics(src.attr("abs:src"));
                }else{
                    putStatics(src.attr("abs:src"));
                    print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
                }
            }
            print("\nCss Imports: (%d)", imports.size());
            for (Element link : imports) {
                String tagName = link.tagName();
                if(tagName.endsWith(".png") || tagName.endsWith(".gif") || tagName.endsWith(".jpg") || tagName.endsWith(".ico")){
                    putStatics(link.attr("abs:href"));
                    print(" * %s: <%s>", tagName, link.attr("abs:href"));
                }else{
                    print(" * %s <%s> (%s)", tagName,link.attr("abs:href"), link.attr("rel"));
                    putStatics(link.attr("abs:href"));
                    parseCss(link.attr("abs:href"));
                }
            }
            print("\nA Links: (%d)", links.size());
            for (Element link : links) {
                if("a".equals(link.tagName())){
                    print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
                    put(link.attr("abs:href"));
                }else{
                    print(" * %s: <%s>", link.tagName(), link.attr("abs:href"));
                    put(link.attr("abs:href"));
                }
            }
            //------- save page
            savePage(url, doc.html());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static boolean checkStr(String src, String index){
        return src.indexOf(index) != -1;
    }
    static boolean checkNotCss(String url){
        if(url.endsWith(".png") || url.endsWith(".gif") || url.endsWith(".jpg") || url.endsWith(".ico")){
            print(" * %s: <%s>", "img", url);
            save(url);
            return true;
        }
        if(url.endsWith(".ttf") || url.endsWith(".woff") || url.endsWith(".eot") || url.endsWith(".svg")){
            print(" * %s: <%s>", "font", url);
            save(url);
            return true;
        }
        if(checkStr(url, ".ttf") || checkStr(url, ".woff") || checkStr(url, ".eot") || checkStr(url, ".svg")){
            print(" * %s: <%s>", "font", url);
            save(url);
            return true;
        }
        return false;
    }

    static void parseCss(String url){
        if(isEmpty(url)) return;
        if(checkNotCss(url)) return;
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
            String baseUri = doc.baseUri();
            java.util.regex.Pattern pat = Pattern.compile(regex);
            Matcher mat = pat.matcher(doc.text());
            while(mat.find()){
                String src = mat.group(3);
                if(src.startsWith("http:") || src.startsWith("https:")){
                }else{
                    src = absUrl(baseUri, src);
                }
                if(src.startsWith("http:") || src.startsWith("https:")){
                    if(isfoot(src)){
                        save(src);
                        continue;
                    }
                }
                print(" * %s: <%s>", "group font", src);
                putStatics(src);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                failQueue.put(url);
            } catch (InterruptedException ee) {
                ee.printStackTrace();
            }
        }
    }
    public static void save(String url) {
        SpiderImagesHeadler.save(url);
    }
    static void savePage(String url, String html){
        String fpath = getPagePath(url);
        String fname = getPageName(url);
        File dir = new File(SAVE_DIR + fpath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File pageFile = new File(SAVE_DIR + fpath + "/" + fname);
        savePage(pageFile, html);
    }
    static void savePage(File pageFile, String html){
        if(pageFile.exists()) return;
        FileWriter fw = null;
        try{
            fw = new FileWriter(pageFile);
            fw.write(html);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            closeWrite(fw);
        }
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
        return getHref(link, "");
    }
    static String getPagePath(String url) {
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
    static String getPageName(String url) {
        //System.out.println("getImgName : " + url);
        url = url.substring(url.lastIndexOf("/") + 1);
        if(url.lastIndexOf("?") != -1 ){
            url = url.substring(0, url.lastIndexOf("?"));
        }
        return url;
    }
}

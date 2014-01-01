package com.stoney.handler;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.params.CoreConnectionPNames;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Stony on 13-12-29.
 */
public class BaseHandler {

    static final String MATH_URL = "templatation.com";
    static final String INDEX_URL = "http://templatation.com/justshop/layout2/index.php";
    static final String BASE_DIR = "d:/Website/";
    static String SAVE_DIR = BASE_DIR + MATH_URL;

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    static int MaxTotal = 30;
    static int MaxPer = 10;
    public static ArrayBlockingQueue mediaQueue;
    public static ArrayBlockingQueue pageQueue;
    public static Map failed;
    public static Map succeed;

    public static boolean Must_Math = true;
    private final static int timeOut = 20000;

    static {
        initDir();
        initQueue();
    }

    public static void checkDir(String path){
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
    }
    public static void reStart(String url){
        try {
            BootHandler.start(url);
        } catch (IOException e) {
        }
    }
    public static void saveHtml(String url){
        int timeoutMillis = 20000;
        String path = SAVE_DIR + "/" + extaFileDir(url) + "/" + extaFilePath(url);
        File outFile = new File(path + "/" + extaFileName(url) + "_" + extaFileQuery(url) + ".html");
        BufferedWriter writer = null;
        if(isSaveFile(outFile)){
            checkDir(path);
            try {
                Document doc = Jsoup.connect(url).timeout(timeoutMillis).get();
                writer = new BufferedWriter(new FileWriter(outFile));
                writer.write(doc.html());
                writer.flush();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                close(writer);
            }
        }
    }


    public static void putMedia(String url){
        try {
            if(isUrl(url)) mediaQueue.put(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void putPage(String url){
        try {
            if(isUrl(url)) pageQueue.put(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static String extaUrl(String url){
        return url.substring(0,url.lastIndexOf("?"));
    }
    public static String extaImgeType(String img){
        return img.substring(img.lastIndexOf(".") + 1);
    }
    public static String extaFileQuery(String url){
        try {
            String name = new URL(url).getQuery();
            if(isEmpty(name) || name.equalsIgnoreCase("/")){
                return "index";
            }else{
                name = name.replaceAll("/", "##");
            }
            return name;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "index";
    }
    public static String extaFileName(String url){
        try {
            String name = new URL(url).getPath();
            return name.substring(name.lastIndexOf("/") + 1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String extaFilePath(String url){
        try {
            String name = new URL(url).getPath();
            return name.substring(0,name.lastIndexOf("/") + 1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String extaFileDir(String url){
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean isSaveUrl(String url){
        return isEmpty((String) succeed.get(getMD5(url)));
    }
    public static boolean isSaveFile(String file){
        return isSaveFile(new File(file));
    }
    public static boolean isSaveFile(File file){
        return !file.exists();
    }
    public static void addFailed(String url){
        failed.put(getMD5(url),url);
    }
    public static void addSucceed(String url){
        if(isSaveUrl(url)) succeed.put(getMD5(url),url);
    }
    public static void delSucceed(String url){
        if(!isSaveUrl(url)) succeed.remove(getMD5(url));
    }
    public static void initQueue(){
        mediaQueue = new ArrayBlockingQueue(20000);
        pageQueue = new ArrayBlockingQueue(20000);
        failed = new ConcurrentHashMap(10000);
        succeed = new ConcurrentHashMap(10000);
    }

    public static void initDir(){
        File dir = new File(SAVE_DIR);
        if(!dir.exists()){
            dir.mkdirs();
        }
        if(dir.exists()){
            dir.canExecute();
            dir.canRead();
            dir.canWrite();
        }
    }




    public static void close(OutputStream s){
        try{
            if(s != null) s.close();
        }catch (IOException e){}
    }
    static void close(InputStream s){
        try{
            if(s != null) s.close();
        }catch (IOException e){}
    }
    static void close(Reader s){
        try{
            if(s != null) s.close();
        }catch (IOException e){}
    }
    static void close(Writer s){
        try{
            if(s != null) s.close();
        }catch (IOException e){}
    }

    public static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }
    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }

    public static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    public static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }
    public static boolean isMath(String url){
        if(!Must_Math) return true;
        return url.indexOf(MATH_URL) != -1;
    }
    public static boolean isUrl(String url){
        return url.startsWith("http:") || url.startsWith("https:");
    }



    public static void downloadFile(InputStream in, File file) throws IOException {
        InputStream is = null;
        OutputStream os = null;

        try {
            byte[] tempbytes = new byte[DEFAULT_BUFFER_SIZE];
            is = new BufferedInputStream(in);
            os = new BufferedOutputStream(new FileOutputStream(file));

            // I/O 读写
            int byteread = 0;
            while ((byteread = is.read(tempbytes)) != -1)
                os.write(tempbytes, 0, byteread);

            os.flush();
        } finally {
            close(is);
            close(os);
        }
    }

    private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f' };// 用来将字节转换成16进制表示的字符
    public static String getMD5(String source) {
        return getMD5(source.getBytes());
    }
    public static String getMD5(byte[] source) {
        String s = null;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(source);
            byte tmp[] = md.digest();// MD5 的计算结果是一个 128 位的长整数，
            // 用字节表示就是 16 个字节
            char str[] = new char[16 * 2];// 每个字节用 16 进制表示的话，使用两个字符， 所以表示成 16
            // 进制需要 32 个字符
            int k = 0;// 表示转换结果中对应的字符位置
            for (int i = 0; i < 16; i++) {// 从第一个字节开始，对 MD5 的每一个字节// 转换成 16
                // 进制字符的转换
                byte byte0 = tmp[i];// 取第 i 个字节
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];// 取字节中高 4 位的数字转换,// >>>
                // 为逻辑右移，将符号位一起右移
                str[k++] = hexDigits[byte0 & 0xf];// 取字节中低 4 位的数字转换

            }
            s = new String(str);// 换后的结果转换为字符串

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return s.toUpperCase();
    }

    public static void download(String url, String toPath) throws IOException {
        download(new URL(url), new File(toPath));
    }
    public static void download(String url, File to) throws IOException {
        download(new URL(url), to);
    }
    public static void download(URL url, File to) throws IOException {
        if(to.exists()) {
            print("save before src : %s", url);
        } else {
            com.google.common.io.Files.copy(com.google.common.io.Resources.asByteSource(url), to);
//        print("Successed download file %s.",to.getName());
            print("save src : %s", url);
        }
    }

}

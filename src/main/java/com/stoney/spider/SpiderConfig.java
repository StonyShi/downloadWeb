package com.stoney.spider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-30
 * Time: 上午10:53
 * To change this template use File | Settings | File Templates.
 */
public class SpiderConfig extends SpiderAbstractHeadler{

    protected static final String SAVE_DIR;
    protected static final String BASE_URL;
    protected static final String INDEX_URL;



    private static Map<String,String> hash = new HashMap<String,String>();

    static {
        refresh();
        SAVE_DIR = getSaveDir();
        BASE_URL = getBaseURL();
        INDEX_URL = getIndexURL();
    }
    public static void refresh(){
        print("|-->>>SpiderHeaderConfig refresh()");

        try {
            hash.clear();
            Properties props = new Properties();
            props.load(SpiderConfig.class.getResourceAsStream("./spider.properties"));
            Iterator<?> it = props.keySet().iterator();
            while(it.hasNext()){
                String keyName = (String)it.next();
                String value = props.getProperty(keyName,"");
                print("|-->>>  key:  " + keyName + "   ,value: " + value);
                if(!isEmpty(value)){
                    try{
                        hash.put(keyName, value);
                    }catch(Exception exp){
                        print(exp);
                        print("|-->>>读取配置KEY: " + keyName + " 失败！");
                    }
                }
            }
        } catch (Exception exp) {
            print(exp);
        } finally{
            initIndexUrl();
        }
    }
    private static void initIndexUrl(){
        try {
            URL url = new URL(getIndexURL());
            hash.put("host", url.getHost());
            hash.put("query", url.getQuery());
            hash.put("path", url.getPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        System.out.println(getSaveDir());
        System.out.println(getBaseURL());
    }
    public static String getItems(String keyName) {
        String value =(String)hash.get(keyName);
        print("|-->>>getItems('" + keyName + "')" + " is : " + value);
        return value;
    }

    public static String getSaveDir() {
        String dir = getItems("SAVE_DIR");
        if(!dir.endsWith("/")){
            dir = dir + "/";
        }
        dir += hash.get("host");
        if(!dir.endsWith("/")){
            dir = dir + "/";
        }
        return dir;
    }

    public static String getBaseURL() {
        return getItems("BASE_URL");
    }
    public static String getIndexURL(){
        return getItems("INDEX_URL");
    }
}

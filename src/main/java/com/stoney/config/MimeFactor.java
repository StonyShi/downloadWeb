package com.stoney.config;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by Stony on 13-12-29.
 */
public class MimeFactor {
    private static HashMap hash = new HashMap();
    static {

        Properties pro = new Properties();
        BufferedReader reader = null;
        try {
            System.out.println();
            File file = new File((MimeFactor.class.getResource("./").getPath() + "mime.properties").replace("%20", " "));
            reader = new BufferedReader(new FileReader(file));
            pro.load(reader);
            Iterator it = pro.keySet().iterator();
            while(it.hasNext()){
                String key = (String) it.next();
                String value = pro.getProperty(key, "text/html");
                hash.put(key, value);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(reader);
        }
    }
    public static String getItem(String key){
        return (String) hash.get(key);
    }
    static void close(Reader s){
        try{
            if(s != null) s.close();
        } catch (IOException e){}
    }


}

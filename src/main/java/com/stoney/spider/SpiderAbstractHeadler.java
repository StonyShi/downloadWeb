package com.stoney.spider;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-30
 * Time: 上午10:46
 * To change this template use File | Settings | File Templates.
 */
public abstract class SpiderAbstractHeadler {

    static void print(String line) {
        System.out.println(line);
    }

    static void print(Object obj) {
        System.out.println(obj);
    }
    public static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    static boolean isNotEmpty(String str) {
        return !isEmpty(str);
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

}

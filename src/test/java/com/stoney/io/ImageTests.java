package com.stoney.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Stony on 13-12-29.
 */
public class ImageTests {
    public static void main(String[] args) throws IOException {
        BufferedImage image = null;
        URL url = new URL("http://static.blog.csdn.net/images/top.png");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(2000);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.connect();
        image = ImageIO.read(conn.getInputStream());
        ImageIO.write(image, "png", new File("d:/top.png"));

    }

    public static String extaImgeType(String img){
        return img.substring(img.lastIndexOf(".") + 1);
    }
}

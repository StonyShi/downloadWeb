package com.stoney.handler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Stony on 13-12-29.
 */
public class BootHandler extends BaseHandler{


    public static void main(String[] args) throws IOException {
        print(INDEX_URL);
        start(INDEX_URL);
        saveHtml(INDEX_URL);
        ExecutorService service = Executors.newFixedThreadPool(10);
        service.execute(new StaticHandlerTask(mediaQueue));
        service.execute(new PageHandlerTask(pageQueue));
        service.shutdown();
    }

    static class StaticHandlerTask implements Runnable{
        private ArrayBlockingQueue queue;

        public StaticHandlerTask(ArrayBlockingQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while(true){
                String url = (String) queue.poll();
                if(isNotEmpty(url))
                    StaticHandler.process(url);
            }
        }
    }
    static class PageHandlerTask implements Runnable{
        private ArrayBlockingQueue queue;

        public PageHandlerTask(ArrayBlockingQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while(true){
                String url = (String) queue.poll();
                if(isNotEmpty(url))
                    PageHandler.process(url);
            }
        }
    }
    public static void start(String uri) throws IOException {
        int timeoutMillis = 20000;
        Document doc = Jsoup.connect(uri).timeout(timeoutMillis).get();
//        System.out.println(doc.html());

        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");

        print("\nMedia: (%d)", media.size());
        for (Element src : media) {
            String href = src.attr("abs:src");
            if (src.tagName().equals("img"))
                print(" * %s: <%s> %sx%s (%s)",
                        src.tagName(), href, src.attr("width"), src.attr("height"),
                        trim(src.attr("alt"), 20));
            else
                print(" * %s: <%s>", src.tagName(), href);
            if(isNotEmpty(href)) putMedia(href);
        }

        print("\nImports: (%d)", imports.size());
        for (Element link : imports) {
            String href = link.attr("abs:href");
            print(" * %s <%s> (%s)", link.tagName(),href, link.attr("rel"));
            if(isNotEmpty(href)) putMedia(href);
        }

        print("\nLinks: (%d)", links.size());
        for (Element link : links) {
            String href = link.attr("abs:href");
            print(" * a: <%s>  (%s)", href, trim(link.text(), 35));
            if(isNotEmpty(href)) putPage(href);
        }
    }


}

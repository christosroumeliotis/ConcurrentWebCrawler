package org.example;

import org.example.Entity.Crawler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {

        int processors = Runtime.getRuntime().availableProcessors();

        System.out.println("Available processors: " + processors);

        Crawler crawler = new Crawler();
        ExecutorService executorService = Executors.newCachedThreadPool();
        ExecutorService executorServiceProducer = Executors.newFixedThreadPool(15);

        Set<String> produced = ConcurrentHashMap.newKeySet();

        for (String url : DB.urls) {
            final String u = url;
            if (produced.add(u)) {
                executorServiceProducer.submit(() -> {
                    try {
                        crawler.produce(u);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }

        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    while (true) {
                        crawler.consume();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executorServiceProducer.shutdown();
        executorService.shutdown();

        //        Crawler crawler = new Crawler();
//
//        Thread thread3 = new Thread(new Crawl(crawler ,"https://www.theodinproject.com/"), "Thread-1");
//        Thread thread1 = new Thread(new Crawl(crawler ,"https://leetcode.com/"), "Thread-2");
//        Thread thread2 = new Thread(new Crawl(crawler ,"https://courses.telusko.com/"), "Thread-3");
//
//        thread1.start();
//        thread2.start();
//        thread3.start();
//
//        try {
//            thread1.join();
//            thread2.join();
//            thread3.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}

class Crawl implements Runnable{

    private final Crawler crawler;
    private final String url;

    public Crawl(Crawler crawler, String url) {
        this.crawler = crawler;
        this.url = url;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " is crawling in url: " + url);
        crawler.crawl(url);
    }
}
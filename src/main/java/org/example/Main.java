package org.example;

import org.example.Entity.Crawler;

public class Main {

    public static void main(String[] args) {

        Crawler crawler = new Crawler();

        Thread producer = new Thread(() -> {
            try {
                for (String url : DB.urls ){
                    crawler.produce(url);
                }
                System.out.println("Producing completed!");
                Thread.currentThread().interrupt();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");


        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    crawler.consume();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer 1");

        Thread consumer2 = new Thread(() -> {
            try {
                while (true) {
                    crawler.consume();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer 2");

        producer.start();
        consumer.start();
        consumer2.start();
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
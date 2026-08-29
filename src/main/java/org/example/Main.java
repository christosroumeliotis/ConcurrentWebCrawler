package org.example;

import org.example.Entity.Crawler;

public class Main {
    public static void main(String[] args) {

        Thread thread3 = new Thread(new Crawl("https://www.theodinproject.com/"), "Thread-1");
        Thread thread1 = new Thread(new Crawl("https://leetcode.com/"), "Thread-2");
        Thread thread2 = new Thread(new Crawl("https://courses.telusko.com/"), "Thread-3");

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class Crawl implements Runnable{

    private final Crawler crawler = new Crawler();
    private final String url;

    public Crawl(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " is crawling in url: " + url);
        crawler.crawl(url);
    }
}
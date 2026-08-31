package org.example;

import org.example.Entity.Crawler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LatchBarrierDemo {
    public static void main(String[] args) {
        Crawler crawler = new Crawler();

        CountDownLatch latch = new CountDownLatch(DB.urls.size());
        CountDownLatch latchConsumer = new CountDownLatch(DB.urls.size());
        Set<String> produced = ConcurrentHashMap.newKeySet();

        List<String> urls = new CopyOnWriteArrayList<>(DB.urls);
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                while (!urls.isEmpty()) {
                    String u = urls.removeLast();
                    if (produced.add(u)) {
                        try {
                            crawler.produce(u);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            //latch.countDown();
                        }
                    }
                }
            }).start();
        }

//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("All producers finished " + urls);

        AtomicInteger consumedUrls = new AtomicInteger(0);
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    while (true) {
                        try {
                            crawler.consume();
                            consumedUrls.incrementAndGet();
                            System.out.println(consumedUrls + ", " + latchConsumer.getCount());
                        } finally {
                            //latchConsumer.countDown();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

//        try {
//            latchConsumer.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        System.out.println("All concumers finished");
    }
}

class ConsumerProducer implements Runnable {

    private final Crawler crawler;
    private final String url;

    public ConsumerProducer(Crawler crawler, String url) {
        this.crawler = crawler;
        this.url = url;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " is crawling in url: " + url);
        crawler.crawl(url);
    }
}


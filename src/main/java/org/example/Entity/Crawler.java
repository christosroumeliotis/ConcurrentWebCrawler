package org.example.Entity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Crawler {

    private final Queue<String> queue = new LinkedList<>();
    private static final int QUEUE_CAPACITY = 5;

    private AtomicInteger globalUrlsProduced = new AtomicInteger(0);


    private final Set<String> linksFound = new HashSet<>();
    private final Set<String> linksVisited = new HashSet<>();

    public void crawl (String url) {
        downloadPage(url);
        Set<String> linksFoundInThread = extractLinks(url);
        visitLinks(linksFoundInThread, url);
    }

    private void visitLinks(Set<String> linksFoundInThread, String url) {
        Set<String> linksVisitedInThread = new HashSet<>();
        for (String link : linksFoundInThread) {
            URI uri;
            try {
                uri = new URI(link);
                if (Desktop.isDesktopSupported()) {
                    //Desktop.getDesktop().browse(uri);
                    linksVisitedInThread.add(link);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        linksVisited.addAll(linksVisitedInThread);
        System.out.println(Thread.currentThread().getName() + " visited links: " + linksVisitedInThread.size() + ", total links visited: " + linksVisited.size());

        Path path = Path.of("pages_loaded/" + URI.create(url).getHost() + ".html");
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> extractLinks(String url) {
        Set<String> linksFoundInThread;
        File file = new File("pages_loaded/" + URI.create(url).getHost() + ".html");
        Document doc;
        try {
            doc = Jsoup.parse(file, "UTF-8", url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        linksFoundInThread = new HashSet<>();
        for (Element link : doc.select("a[href]")) {
            String href = link.attr("href");
            if (href.isEmpty() || href.startsWith("#") || href.startsWith("mailto:") || href.startsWith("javascript:")) {
                continue;
            }

            String fullUrl = URI.create(url).resolve(href).toString();
            linksFoundInThread.add(fullUrl);
        }
        linksFound.addAll(linksFoundInThread);
        System.out.println(Thread.currentThread().getName() + " found links: " + linksFoundInThread.size() + ", total links found: " + linksFound.size());

        return linksFoundInThread;
    }

    private void downloadPage(String url) {
        try(HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(Thread.currentThread().getName() + " Status code returned: " + response.statusCode());

            if (response.statusCode() == 200) {
                Path directory = Path.of("pages_loaded");
                Files.createDirectories(directory);
                Files.writeString(
                        Path.of( "pages_loaded/" + request.uri().getHost() + ".html"),
                        response.body()
                );
            } else {
                throw new RuntimeException(Thread.currentThread().getName() + " Failed to download or save the page!");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void produce(String url) throws InterruptedException {
        synchronized (queue){
            while (queue.size() == QUEUE_CAPACITY) {
                queue.wait();
            }
            queue.add(url);
            globalUrlsProduced.addAndGet(1);
            queue.notifyAll();
            System.out.println(Thread.currentThread().getName() + " produced: " + globalUrlsProduced + " URLs");

        }
    }

    public void consume() throws InterruptedException {
        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }
            String url = queue.remove();
            queue.notifyAll();
            System.out.println(Thread.currentThread().getName() + " consumed: " + url);
            try {
                crawl(url);
            } catch (Exception e) {
                System.err.println("Failed for url: " + url);
            }

        }
    }
}

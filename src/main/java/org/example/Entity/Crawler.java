package org.example.Entity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import java.util.Set;

public class Crawler {

    private final Set<String> linksFound = new HashSet<>();
    private final Set<String> linksVisited = new HashSet<>();

    public void crawl (String url) {
        downloadPage(url);
        extractLinks(url);
        visitLinks();
    }

    private void visitLinks() {
        for (String link : linksFound) {
            URI uri;
            try {
                uri = new URI(link);
                if (Desktop.isDesktopSupported()) {
                    //Desktop.getDesktop().browse(uri);
                    linksVisited.add(link);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(Thread.currentThread().getName() + " Visited links: " + linksVisited.size());
    }

    private void extractLinks(String url) {

        File file = new File("page.html");
        Document doc;
        try {
            doc = Jsoup.parse(file, "UTF-8", url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Element link : doc.select("a[href]")) {
            String href = link.attr("href");
            if (href.isEmpty() || href.startsWith("#") || href.startsWith("mailto:") || href.startsWith("javascript:")) {
                continue;
            }

            String fullUrl = URI.create(url).resolve(href).toString();
            linksFound.add(fullUrl);
        }
        System.out.println(Thread.currentThread().getName() + " Found links: " + linksFound.size());
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
                System.out.println(Thread.currentThread().getName() + " Page downloaded successfully!");
                Files.writeString(
                        Path.of(Thread.currentThread().getName() + "page.html"),
                        response.body()
                );
                System.out.println(Thread.currentThread().getName() + " Page saved successfully!");
            } else {
                throw new RuntimeException(Thread.currentThread().getName() + " Failed to download or save the page!");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}

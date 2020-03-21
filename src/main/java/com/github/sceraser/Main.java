package com.github.sceraser;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Main {
    private static List<String> linkPool = new ArrayList<>();
    private static HashSet processedLinks = new HashSet();

    private static final String properUa = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36\n";
    private static final String indexLink = "http://sina.cn";


    public static void main(String[] args) throws IOException {
        linkPool.add(indexLink);

        while (!linkPool.isEmpty()) {
            String link = linkPool.remove(linkPool.size() - 1);

            //判断是否处理过
            if (isProcessed(link)) {
                continue;
            }
            processedLinks.add(link);
            if (link.startsWith("//")) {
                link = "https:" + link;
            }


            if (isInterestedLink(link)) {
                CloseableHttpClient httpclient = HttpClients.createDefault();

                System.out.println(link);
                HttpGet httpGet = new HttpGet(link);
                httpGet.setHeader("User-Agent", properUa);
                try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                    HttpEntity entity1 = response1.getEntity();
                    String html = EntityUtils.toString(entity1);
                    Document document = Jsoup.parse(html);
                    ArrayList<Element> links = document.select("a");

                    //将所有链接放入链接池
                    document.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);

                    //假如是个新闻页面，存储它

                    storeIntoDatabaseIfItIsNewsPage(document);

                }
            }

        }
    }

    private static void storeIntoDatabaseIfItIsNewsPage(Document document) {
        ArrayList<Element> articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                System.out.println((articleTag.child(0).text()));
            }
        }
    }


    private static boolean isProcessed(String link) {
        return processedLinks.contains(link);
    }

    private static boolean isInterestedLink(String link) {
        return (isNewsPage(link) || isIndexPage(link)) && isProperLink(link);
    }

    private static boolean isProperLink(String link) {
        return !link.contains("keyword.d.html");
    }

    private static boolean isIndexPage(String link) {
        return link.equals("http://sina.cn");
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

}

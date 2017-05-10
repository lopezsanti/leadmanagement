package com.pm.har.page.impl;

import com.pm.har.page.PageFetcher;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApachePageFetcher implements PageFetcher, Closeable {

    CloseableHttpClient httpClient;
    private CookieStore cookieStore;

    public ApachePageFetcher() {
        cookieStore = new BasicCookieStore();
        httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
    }

    @Override
    public String fetch(URL url) {
        try {
            HttpUriRequest request = new HttpGet(url.toString());
            request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            request.addHeader("Accept-Encoding", "gzip, deflate, sdch");
            request.addHeader("Referer", url.toString());
            request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36");


            try (CloseableHttpResponse response = httpClient.execute(request)) {
                boolean isSuccess = response.getStatusLine().getStatusCode() == 200;
                if (!isSuccess) {
                    if (response.getStatusLine().getStatusCode() == 302) { // todo guard against infinite loop
                        String redirectTraget = response.getFirstHeader("location").getValue();
                        return fetch(new URL(redirectTraget));
                    }
                    return null;
                } else {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    response.getEntity().writeTo(buffer);
                    String page = buffer.toString("UTF-8");
                    return page;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String post(URL url, Map<String, String> params) {
        try {
            HttpPost request = new HttpPost(url.toURI());
            request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            request.addHeader("Accept-Encoding", "gzip, deflate, sdch");
            request.addHeader("Referer", url.toString());
            request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36");
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");


            List<NameValuePair> formparams = new ArrayList<>();
            for (Map.Entry<String, String> paramEntry : params.entrySet()) {
                formparams.add(new BasicNameValuePair(paramEntry.getKey(), paramEntry.getValue()));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
            request.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                boolean isSuccess = response.getStatusLine().getStatusCode() == 200;
                if (!isSuccess) {
                    if (response.getStatusLine().getStatusCode() == 302) {
                        String redirectTraget = response.getFirstHeader("location").getValue();
                        return fetch(new URL(redirectTraget));
                    }
                    return null;
                } else {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    response.getEntity().writeTo(buffer);
                    String page = buffer.toString("UTF-8");
                    return page;
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}

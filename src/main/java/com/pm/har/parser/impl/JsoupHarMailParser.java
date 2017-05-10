package com.pm.har.parser.impl;

import com.pm.har.parser.HarMailParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class JsoupHarMailParser implements HarMailParser {
    @Override
    public URL getViewUrl(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("a[href^=http://search.har.com/_leadtrack]:contains(View)").first())
                .map(e -> e.attr("href"))
                .map(h -> {
                    try {
                        return new URL(h);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                })
                .orElse(null);
    }
}

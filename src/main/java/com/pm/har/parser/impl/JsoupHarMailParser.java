package com.pm.har.parser.impl;

import com.pm.har.parser.HarMailParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class JsoupHarMailParser implements HarMailParser {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

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

    @Override
    public Date getMailDate(String page) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MM/dd/yy KK:mm a");
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("td:contains(Sent)"))
                .map(Elements::last)
                .map(Element::siblingElements)
                .map(Elements::last)
                .map(Element::text).map(source -> {
                    try {
                        return dateFormat.parse(source);
                    } catch (ParseException e) {
                        logger.warn("Invalid date: {}. Error: {]", source, e.getMessage());
                        return null;
                    }
                })
                .orElse(null);
    }
}

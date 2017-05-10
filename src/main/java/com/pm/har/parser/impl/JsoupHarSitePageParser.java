package com.pm.har.parser.impl;

import com.pm.har.parser.HarSitePageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class JsoupHarSitePageParser implements HarSitePageParser {
    @Override
    public String getFrom(String page) {
        Document d = Jsoup.parse(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(From)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1).text())
                .orElse(null);
    }

    @Override
    public String getComments(String page) {
        Document d = Jsoup.parse(page);
        return Optional
                .ofNullable(d.select("span:containsOwn(Comments:)").first())
                .map(element -> element.parent().text())
                .map(t -> t.replaceFirst("^Comments: *", ""))
                .orElse(null)
                ;
    }

    @Override
    public String getPhone(String page) {
        Document d = Jsoup.parse(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(Phone)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1).text())
                .orElse(null);
    }

//    @Override
//    public String getAddress(String page) {
//        Document d = Jsoup.parse(page);
//        return Optional
//                .ofNullable(d.select("td:containsOwn(Lead Source), td:containsOwn(Leadsource)").first())
//                .map(Element::parent)
//                .filter(e -> e.children().size() > 1)
//                .map(e -> e.child(1).select("a").first())
//                .map(Element::text)
//                .orElse(null);
//    }

    @Override
    public URL getDetailsLink(String page) {
        Document d = Jsoup.parse(page);
        URL url = Optional
                .ofNullable(d.select("td:containsOwn(Lead Source), td:containsOwn(Leadsource)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1).select("a").first())
                .map(e -> e.attr("href"))
                .map(h -> {
                    try {
                        return new URL(h);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                })
                .orElse(null);
        if (url == null) {
            url = Optional
                    .ofNullable(d.select("a:containsOwn(Click here to view property detail)").first())
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
        return url;
    }
}
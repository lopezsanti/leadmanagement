package com.pm.har.parser.impl;

import com.pm.har.parser.HarSitePageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsoupHarSitePageParser implements HarSitePageParser {

    @Override
    public String getFrom(String page) {
        Document d = Jsoup.parse(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(From)"))
                .map(Elements::last)
                .map(Element::parent)
                .map(e -> e.child(1))
                .map(e -> e.select("span"))
                .map(Elements::last)
                .map(Element::text)
                .map(s -> s.replaceAll("[\\[\\] ]*", ""))
                .orElse(null);
    }

    @Override
    public String getFromName(String page) {
        Document d = Jsoup.parse(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(From)"))
                .map(Elements::last)
                .map(Element::parent)
                .map(e -> e.child(1))
                .map(Element::text)
                .map(s -> s.replaceAll("\\[.*\\]", ""))
                .map(s -> s.replaceAll(" {2,}", " "))
                .map(String::trim)
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
        Pattern phoneFormatter = Pattern.compile("(.*)(...)(...)(....)");
        Document d = Jsoup.parse(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(Phone)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1).text())
                .map(t -> t.replaceAll("[\\(\\)\\- ]*", ""))
                .map(t -> {
                    Matcher matcher = phoneFormatter.matcher(t);
                    if (matcher.find()) {
                        return String.format("%s(%s) %s-%s", matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
                    } else {
                        return null;
                    }

                })
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

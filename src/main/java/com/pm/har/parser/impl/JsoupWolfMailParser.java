package com.pm.har.parser.impl;

import com.pm.har.parser.WolfMailParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsoupWolfMailParser implements WolfMailParser {
    @Override
    public String getFrom(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(Email)").first())
                .map(Element::nextElementSibling)
                .map(Element::text)
                .orElse(null)
                ;
    }

    @Override
    public String getFromName(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(Name)").first())
                .map(Element::nextElementSibling)
                .map(Element::text)
                .orElse(null)
                ;
    }

    @Override
    public String getComments(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        String comment = Optional
                .ofNullable(d.select("div.body:containsOwn(is inquiring about listing)").first())
                .map(Element::ownText)
                .orElse(null)
                ;
        if (comment != null) {
            String mls = getMLS(page);
            comment = comment.replace("#", "#" + mls);
        }
        return comment;
    }

    @Override
    public String getPhone(String page) {
        Pattern phoneFormatter = Pattern.compile("(.*)(...)(...)(....)");
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(Phone)").first())
                .map(Element::nextElementSibling)
                .map(Element::text)
                .map(t -> t.replaceAll("[\\(\\)\\- ]*", ""))
                .map(t -> {
                    Matcher matcher = phoneFormatter.matcher(t);
                    if (matcher.find()) {
                        return String.format("%s(%s) %s-%s", matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
                    } else {
                        return null;
                    }

                })
                .orElse(null)
                ;
    }

    @Override
    public String getAddress(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(Listing Address)").first())
                .map(Element::nextElementSibling)
                .map(Element::text)
                .orElse(null)
                ;
    }

    @Override
    public URL getDetailsLink(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(Listing Number)").first())
                .map(Element::nextElementSibling)
                .map(Element::children)
                .map(Elements::first)
                .map(e -> e.attr("href"))
                .map(l -> {
                    try {
                        return new URL(l);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                })
                .orElse(null)
                ;
    }

    @Override
    public String getZipCode(String page) {
        return Optional
                .ofNullable(getAddress(page))
                .map(a -> a.substring(a.length() - 5))
                .orElse(null)
                ;
    }

    @Override
    public String getListPrice(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(Listing Price)").first())
                .map(Element::nextElementSibling)
                .map(Element::text)
                .orElse(null)
                ;
    }

    @Override
    public String getMLS(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("td:containsOwn(Listing Number)").first())
                .map(Element::nextElementSibling)
                .map(Element::children)
                .map(Elements::first)
                .map(Element::text)
                .orElse(null)
                ;
    }
}

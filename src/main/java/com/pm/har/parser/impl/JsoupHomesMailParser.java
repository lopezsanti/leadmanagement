package com.pm.har.parser.impl;

import com.pm.har.parser.HomesMailParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsoupHomesMailParser implements HomesMailParser {
    @Override
    public String getFrom(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("div:contains(Contact Details)"))
                .map(Elements::first)
                .map(e -> e.select("strong:contains(Email) ~ a"))
                .map(Elements::text)
                .orElse(null);
    }

    @Override
    public String getComments(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        String data = Optional
                .ofNullable(d.select("div:contains(Contact Details)"))
                .map(Elements::first)
                .map(e -> e.select("p:has(strong:contains(Notes))"))
                .map(Elements::text)
                .orElse(null);
        if (data == null) {
            return null;
        }
        Pattern commentsPattern = Pattern.compile("Notes:(.*)");
        Matcher matcher = commentsPattern.matcher(data);
        if (matcher.find()) {
            return matcher.group(1)
                    .replaceAll("\\xA0", " ") // &nbsp;
                    .trim();
        }
        return null;
    }

    @Override
    public String getPhone(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("div:contains(Contact Details)"))
                .map(Elements::first)
                .map(e -> e.select("p:has(strong:contains(Phone)) > a[href^=tel:]"))
                .map(Elements::text)
                .orElse(null);
    }

    @Override
    public String getAddress(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        String data = Optional
                .ofNullable(d.select("div:contains(Contact Details)"))
                .map(Elements::first)
                .map(e -> e.select("p:has(strong:contains(Listing Address:))"))
                .map(Elements::text)
                .orElse(null);
        if (data == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("Listing Address:(.*)Listing City");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            return matcher.group(1)
                    .replaceAll("\\xA0", " ") // &nbsp;
                    .trim();
        }
        return null;
    }

    @Override
    public URL getDetailsLink(String page) {
        return null;
    }

    @Override
    public String getZipCode(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        String data = Optional
                .ofNullable(d.select("div:contains(Contact Details)"))
                .map(Elements::first)
                .map(e -> e.select("p:has(strong:contains(Listing Zip:))"))
                .map(Elements::text)
                .orElse(null);
        if (data == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("Listing Zip:(.*)Listing Price");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            return matcher.group(1)
                    .replaceAll("\\xA0", " ") // &nbsp;
                    .trim();
        }
        return null;

    }

    @Override
    public String getListPrice(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        String data = Optional
                .ofNullable(d.select("div:contains(Contact Details)"))
                .map(Elements::first)
                .map(e -> e.select("p:has(strong:contains(Listing Price:))"))
                .map(Elements::text)
                .orElse(null);
        if (data == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("Listing Price:(.*)Listing Agent");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            return matcher.group(1)
                    .replaceAll("\\xA0", " ") // &nbsp;
                    .trim();
        }
        return null;

    }

    @Override
    public String getMLS(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        String data = Optional
                .ofNullable(d.select("div:contains(Contact Details)"))
                .map(Elements::first)
                .map(e -> e.select("p:has(strong:contains(Listing MLS Number:))"))
                .map(Elements::text)
                .orElse(null);
        if (data == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("Listing MLS Number:(.*)Listing Address");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            return matcher.group(1)
                    .replaceAll("\\xA0", " ") // &nbsp;
                    .trim();
        }
        return null;
    }
}

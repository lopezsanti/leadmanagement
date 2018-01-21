package com.pm.har.parser.impl;

import com.pm.har.parser.HomesMailParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsoupHomesMailParser implements HomesMailParser {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

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
        Pattern phoneFormatter = Pattern.compile("(.*)(...)(...)(....)");
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("div:contains(Contact Details)"))
                .map(Elements::first)
                .map(e -> e.select("p:has(strong:contains(Phone)) > a[href^=tel:]"))
                .map(Elements::text)
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
                    .replaceAll("\\xA0", "") // &nbsp;
                    .replaceAll(",", "")
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

    @Override
    public Date getMailDate(String page) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("'Time: 'MM/dd/yy KK:mm a", Locale.US);
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("p:has(strong:contains(Time:))"))
                .map(Elements::last)
                .map(Element::text)
                .map(s -> s.replaceAll("\\xA0", " "))
                .map(s -> s.replaceAll(" {2,}", " "))
                .map(source -> {
                    try {
                        return dateFormat.parse(source);
                    } catch (ParseException e) {
                        logger.warn("Invalid date: {}. Error: {]", source, e.getMessage());
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public String getFromName(String page) {
        Document d = Jsoup.parseBodyFragment(page);
        return Optional
                .ofNullable(d.select("p:has(strong:contains(Full Name:))"))
                .map(Elements::last)
                .map(e -> {e.child(0).remove(); return e;})
                .map(Element::text)
                .map(s -> s.replaceAll("\\xA0", " "))
                .map(s -> s.replaceAll(" {2,}", " "))
                .map(String::trim)
                .orElse(null);
    }
}

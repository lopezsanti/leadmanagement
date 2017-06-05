package com.pm.har.parser.impl;

import com.pm.har.parser.DetailsPageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;

public class JsoupDetailsPageParser implements DetailsPageParser {
    @Override
    public String getZipCode(String page) {
        Document d = Jsoup.parse(page);
        return Optional
                .ofNullable(d.select("div.dc_label:containsOwn(Zip Code)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1).select("a").first())
                .map(Element::text)
                .orElse(null);
    }

    @Override
    public String getListPrice(String page) {
        Document d = Jsoup.parse(page);
        return Optional
                .ofNullable(d.select("div.dc_label:containsOwn(Listing Price),div.dc_label:containsOwn(Sold Price Range)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1))
                .map(p -> {
                    p.select("a,div").remove();
                    return p;
                })
                .map(Element::text)
                .map(t -> t.replaceAll(" \\(.*\\)", ""))
                .map(t -> t.replaceAll(",", ""))
                .orElse(null);
    }

    @Override
    public String getMLS(String page) {
        Document d = Jsoup.parse(page);
        return Optional
                .ofNullable(d.select("div.dc_label:containsOwn(MLS#)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1))
                .map(Element::text)
                .map(t -> t.replaceAll(" \\(HAR\\)", ""))
                .orElse(null);
    }

    @Override
    public String getAddress(String page) {
        Document d = Jsoup.parse(page);
        String address = Optional
                .ofNullable(d.select("div.dc_label:containsOwn(Address:)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1))
                .map(Element::text)
                .orElse(null);
        String city = Optional
                .ofNullable(d.select("div.dc_label:containsOwn(City:)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1))
                .map(Element::text)
                .orElse(null);
        String state = Optional
                .ofNullable(d.select("div.dc_label:containsOwn(State:)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1))
                .map(Element::text)
                .orElse(null);
        String zip = Optional
                .ofNullable(d.select("div.dc_label:containsOwn(Zip Code:)").first())
                .map(Element::parent)
                .filter(e -> e.children().size() > 1)
                .map(e -> e.child(1))
                .map(Element::text)
                .orElse(null);
        if (address != null) {
            return address + ", " + city + ", " + state + " " + zip;
        }
        return null;
    }
}

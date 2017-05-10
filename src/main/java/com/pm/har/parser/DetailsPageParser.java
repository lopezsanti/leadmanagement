package com.pm.har.parser;

public interface DetailsPageParser {
    String getZipCode(String page);

    String getListPrice(String page);

    String getMLS(String page);

    String getAddress(String page);

}

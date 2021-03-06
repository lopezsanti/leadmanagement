package com.pm.har.parser;

import java.net.URL;

public interface MoveMailParser {
    String getFrom(String page);

    String getFromName(String page);

    String getComments(String page);

    String getPhone(String page);

    String getAddress(String page);

    URL getDetailsLink(String page);

    String getZipCode(String page);

    String getListPrice(String page);

    String getMLS(String page);

}

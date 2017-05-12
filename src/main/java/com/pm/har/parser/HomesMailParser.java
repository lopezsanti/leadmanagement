package com.pm.har.parser;

import java.net.URL;
import java.util.Date;

public interface HomesMailParser {
    String getFrom(String page);

    String getComments(String page);

    String getPhone(String page);

    String getAddress(String page);

    URL getDetailsLink(String page);

    String getZipCode(String page);

    String getListPrice(String page);

    String getMLS(String page);

    Date getMailDate(String page);

    String getFromName(String page);
}

package com.pm.har.parser;

import java.net.URL;

public interface HarSitePageParser {
    String getFrom(String page);

    String getFromName(String page);

    String getComments(String page);

    String getPhone(String page);

    URL getDetailsLink(String page);
}

package com.pm.har.parser;

import java.net.URL;
import java.util.Date;

public interface HarMailParser {
    URL getViewUrl(String page);

    Date getMailDate(String page);
}

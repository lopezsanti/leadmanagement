package com.pm.har.page;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.Map;

public interface PageFetcher {
    @Nullable
    String fetch(URL url);

    String post(URL url, Map<String, String> params);
}

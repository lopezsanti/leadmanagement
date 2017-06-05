package com.pm.har.parser;

import javax.annotation.Nullable;

public interface MailSourceDetector {
    @Nullable
    Source detectSource(String content);

    enum Source {
        HAR,
        HOMES,
        MOVES,
        CHRON,
        WOLF_NET
    }
}

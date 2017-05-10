package com.pm.har.parser.impl;

import com.pm.har.parser.MailSourceDetector;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

@Service
public class MailSourceDetectorImpl implements MailSourceDetector {
    private Pattern homes = Pattern.compile("homes.com");
    private Pattern har = Pattern.compile("har.com");
    private Pattern move = Pattern.compile("move.com");

    @Nullable
    @Override
    public Source detectSource(String content) {
        if (homes.matcher(content).find()) {
            return Source.HOMES;
        } else if (har.matcher(content).find()) {
            return Source.HAR;
        } else if (move.matcher(content).find()) {
            return Source.MOVES;
        }
        return null;
    }
}

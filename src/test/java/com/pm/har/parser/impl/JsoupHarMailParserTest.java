package com.pm.har.parser.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsoupHarMailParserTest {

    private static String mailText;

    @BeforeClass
    public static void setUp() throws Exception {
        java.net.URL url = JsoupHarMailParserTest.class.getResource("/mail_example.html");
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        mailText = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }

    @Test
    public void getViewUrl() throws Exception {
        URL url = new JsoupHarMailParser().getViewUrl(mailText);
        assertEquals("search.har.com", url.getHost());
    }

    @Test
    public void getViewUrl_empty() throws Exception {
        URL url = new JsoupHarMailParser().getViewUrl("");
        assertNull(url);
    }

    @Test
    public void mailDate() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MM/dd/yy KK:mm a", Locale.US);
        Date date = simpleDateFormat.parse("Wed 05/25/16 11:46 AM");
        Date mailDate = new JsoupHarMailParser().getMailDate(mailText);
        assertEquals(date, mailDate);
    }
}
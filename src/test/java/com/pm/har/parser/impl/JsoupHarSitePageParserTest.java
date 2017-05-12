package com.pm.har.parser.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class JsoupHarSitePageParserTest {
    private static String pageText;

    @BeforeClass
    public static void setUp() throws Exception {
        java.net.URL url = JsoupHarMailParserTest.class.getResource("/search_page_example.html");
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        pageText = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }

    @Test
    public void getFrom() throws Exception {
        String from = new JsoupHarSitePageParser().getFrom(pageText);
        assertEquals("slayton.helen@gmail.com", from);
    }

    @Test
    public void getFromName() throws Exception {
        String from = new JsoupHarSitePageParser().getFromName(pageText);
        assertEquals("Helen Slayton", from);
    }

    @Test
    public void getComments() throws Exception {
        String comments = new JsoupHarSitePageParser().getComments(pageText);
        assertNotNull(comments);
    }

    @Test
    public void getPhone() throws Exception {
        String phone = new JsoupHarSitePageParser().getPhone(pageText);
        assertEquals("(713) 319-7100", phone);

    }

    @Test
    public void testDetailsLink() throws Exception {
        URL detailsLink = new JsoupHarSitePageParser().getDetailsLink(pageText);
        assertEquals("http://www.har.com/6450-w-linpar-ct/sale_63685137", detailsLink.toString());
    }

}
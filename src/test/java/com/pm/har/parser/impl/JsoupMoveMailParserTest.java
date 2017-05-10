package com.pm.har.parser.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class JsoupMoveMailParserTest {
    private static String mailText;

    @BeforeClass
    public static void setUp() throws Exception {
        java.net.URL url = JsoupHarMailParserTest.class.getResource("/mail_example_move.html");
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        mailText = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }

    @Test
    public void from() throws Exception {
        JsoupMoveMailParser parser = new JsoupMoveMailParser();
        String from = parser.getFrom(mailText);
        assertEquals("bruce bosshard [bosshardeng@aol.com]", from);
    }

    @Test
    public void comment() throws Exception {
        JsoupMoveMailParser parser = new JsoupMoveMailParser();
        String comments = parser.getComments(mailText);
        assertEquals("I would like more information regarding the property at 22109 Iron Knoll Dr, Kingwood", comments);
    }

    @Test
    public void address() throws Exception {
        JsoupMoveMailParser parser = new JsoupMoveMailParser();
        String address = parser.getAddress(mailText);
        assertEquals("22109 Iron Knoll Dr , Kingwood, Texas 77339", address);
    }

    @Test
    public void phone() throws Exception {
        JsoupMoveMailParser parser = new JsoupMoveMailParser();
        String phone = parser.getPhone(mailText);
        assertEquals("(928)897-1194", phone);
    }

    @Test
    public void zipCode() throws Exception {
        JsoupMoveMailParser parser = new JsoupMoveMailParser();
        String zipCode = parser.getZipCode(mailText);
        assertEquals("77339", zipCode);
    }

    @Test
    public void price() throws Exception {
        JsoupMoveMailParser parser = new JsoupMoveMailParser();
        String listPrice = parser.getListPrice(mailText);
        assertEquals("$2,250", listPrice);
    }

    @Test
    public void mls() throws Exception {
        JsoupMoveMailParser parser = new JsoupMoveMailParser();
        String mls = parser.getMLS(mailText);
        assertEquals("22249049", mls);
    }

    @Test
    public void link() throws Exception {
        JsoupMoveMailParser parser = new JsoupMoveMailParser();
        URL link = parser.getDetailsLink(mailText);
        assertEquals("http://www.realtor.com/realestateandhomes-detail/M8989381448", link.toString());
    }
}
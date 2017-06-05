package com.pm.har.parser.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class JsoupWolfMailParserTest {
    private static String mailText;
    private static JsoupWolfMailParser parser;

    @BeforeClass
    public static void setUp() throws Exception {
        URL url = JsoupHarMailParserTest.class.getResource("/mail_example_wolfnet.html");
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        mailText = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
        parser = new JsoupWolfMailParser();
    }

    @Test
    public void from() throws Exception {
        String from = parser.getFrom(mailText);
        assertEquals("Hugo832hdz@gmail.com", from);
    }

    @Test
    public void fromName() throws Exception {
        String from = parser.getFromName(mailText);
        assertEquals("Hugo Hernandez", from);
    }

    @Test
    public void comment() throws Exception {
        String comments = parser.getComments(mailText);
        assertEquals("Hugo Hernandez is inquiring about listing #50231115 located at 6525 Tadlock.", comments);
    }

    @Test
    public void address() throws Exception {
        String address = parser.getAddress(mailText);
        assertEquals("6525 Tadlock Houston, TX 77085", address);
    }

    @Test
    public void phone() throws Exception {
        String phone = parser.getPhone(mailText);
        assertEquals("(281) 460-0308", phone);
    }

    @Test
    public void zipCode() throws Exception {
        String zipCode = parser.getZipCode(mailText);
        assertEquals("77085", zipCode);
    }

    @Test
    public void price() throws Exception {
        String listPrice = parser.getListPrice(mailText);
        assertEquals("$210,000", listPrice);
    }

    @Test
    public void mls() throws Exception {
        String mls = parser.getMLS(mailText);
        assertEquals("50231115", mls);
    }

    @Test
    public void link() throws Exception {
        URL link = parser.getDetailsLink(mailText);
        assertEquals("http://houston.creg1.com/listing/50231115", link.toString());
    }
}
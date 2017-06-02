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
        RegexpMoveMailParser parser = new RegexpMoveMailParser();
        String from = parser.getFrom(mailText);
        assertEquals("hayleyroberts47@gmail.com", from);
    }

    @Test
    public void fromName() throws Exception {
        RegexpMoveMailParser parser = new RegexpMoveMailParser();
        String from = parser.getFromName(mailText);
        assertEquals("Hayley Roberts", from);
    }

    @Test
    public void comment() throws Exception {
        RegexpMoveMailParser parser = new RegexpMoveMailParser();
        String comments = parser.getComments(mailText);
        assertEquals("I would like more information regarding the property at 760 Memorial Mews St Apt D, Houston", comments);
    }

    @Test
    public void address() throws Exception {
        RegexpMoveMailParser parser = new RegexpMoveMailParser();
        String address = parser.getAddress(mailText);
        assertEquals("760 Memorial Mews St Apt D, Houston, Texas 77079", address);
    }

    @Test
    public void phone() throws Exception {
        RegexpMoveMailParser parser = new RegexpMoveMailParser();
        String phone = parser.getPhone(mailText);
        assertEquals("(325) 277-7404", phone);
    }

    @Test
    public void zipCode() throws Exception {
        RegexpMoveMailParser parser = new RegexpMoveMailParser();
        String zipCode = parser.getZipCode(mailText);
        assertEquals("77079", zipCode);
    }

    @Test
    public void price() throws Exception {
        RegexpMoveMailParser parser = new RegexpMoveMailParser();
        String listPrice = parser.getListPrice(mailText);
        assertEquals("$1,025", listPrice);
    }

    @Test
    public void mls() throws Exception {
        RegexpMoveMailParser parser = new RegexpMoveMailParser();
        String mls = parser.getMLS(mailText);
        assertEquals("45352345", mls);
    }

    @Test
    public void link() throws Exception {
        RegexpMoveMailParser parser = new RegexpMoveMailParser();
        URL link = parser.getDetailsLink(mailText);
        assertEquals("http://www.realtor.com/realestateandhomes-detail/M8871937478", link.toString());
    }
}
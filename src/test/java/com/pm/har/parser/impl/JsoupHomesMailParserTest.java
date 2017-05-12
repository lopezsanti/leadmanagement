package com.pm.har.parser.impl;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class JsoupHomesMailParserTest {
    private static String mailText;
    private JsoupHomesMailParser parser;

    @BeforeClass
    public static void setUpStatic() throws Exception {
        java.net.URL url = JsoupHarMailParserTest.class.getResource("/mail_example_homes.html");
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        mailText = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }

    @Before
    public void setUp() throws Exception {
        parser = new JsoupHomesMailParser();
    }

    @Test
    public void from() throws Exception {
        String from = parser.getFrom(mailText);
        assertEquals("lorreynaharris@yahoo.com", from);
    }

    @Test
    public void comments() throws Exception {
        String comments = parser.getComments(mailText);
        assertEquals("I'd like more information about the listing that I found at Homes.com at 13108 Winecup Mallow Trail.", comments);
    }

    @Test
    public void phone() throws Exception {
        String phone = parser.getPhone(mailText);
        assertEquals("(512) 803-1688", phone);
    }

    @Test
    public void address() throws Exception {
        String address = parser.getAddress(mailText);
        assertEquals("13108 Winecup Mallow Trail", address);
    }

    @Test
    public void zip() throws Exception {
        String zipCode = parser.getZipCode(mailText);
        assertEquals("78621", zipCode);
    }

    @Test
    public void price() throws Exception {
        String listPrice = parser.getListPrice(mailText);
        assertEquals("$169,000.00", listPrice);
    }

    @Test
    public void mls() throws Exception {
        String mls = parser.getMLS(mailText);
        assertEquals("90092754", mls);
    }

    @Test
    public void mailDate() throws Exception {
        Date date = new SimpleDateFormat("MM/dd/yy KK:mm a").parse("4/11/2017 3:39 PM");
        Date mailDate = parser.getMailDate(mailText);
        assertEquals(date, mailDate);
    }

    @Test
    public void fromName() throws Exception {
        String fromName = parser.getFromName(mailText);
        assertEquals("lorreyna harris", fromName);
    }
}
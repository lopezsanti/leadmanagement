package com.pm.har.parser.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsoupDetailsPageParserTest {
    private static String pageText;

    @BeforeClass
    public static void setUp() throws Exception {
        java.net.URL url = JsoupHarMailParserTest.class.getResource("/details_page_on_sale_example.html");
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        pageText = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }

    @Test
    public void getZipCode() throws Exception {
        String zipCode = new JsoupDetailsPageParser().getZipCode(pageText);
        assertEquals("77040", zipCode);
    }

    @Test
    public void getListPrice_sold() throws Exception {
        java.net.URL url = JsoupHarMailParserTest.class.getResource("/details_page_sold_example.html");
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        pageText = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");

        String listPrice = new JsoupDetailsPageParser().getListPrice(pageText);
        assertNull(listPrice);
    }

    @Test
    public void getListPrice() throws Exception {
        String listPrice = new JsoupDetailsPageParser().getListPrice(pageText);
        assertEquals("$ 208,500 ($88.42/sqft.)", listPrice);
    }

    @Test
    public void getMLS() throws Exception {
        String mls = new JsoupDetailsPageParser().getMLS(pageText);
        assertEquals("82127448 (HAR)", mls);
    }

    @Test
    public void getAddress() throws Exception {
        String address = new JsoupDetailsPageParser().getAddress(pageText);
        assertEquals("6539 Wilshire Fern, Houston, TX 77040", address);

    }

}
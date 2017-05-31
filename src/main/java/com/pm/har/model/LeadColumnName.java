package com.pm.har.model;

import java.util.Arrays;

public enum LeadColumnName {
    mailId(true, "mailId"),
    date("date"),
    time("Received Time"),
    comments("Comments"),
    phone("Phone Number"),
    name_from("Name"),
    mail_from("Email Address"),
    zipCode("Zip Code"),
    mls("MLS #"),
    listPrice("Property Price"),
    address("Property Address"),
    source("Lead Source");

    private boolean isPrimary = false;
    private String columnHeader;

    LeadColumnName(String columnHeader) {
        this.columnHeader = columnHeader;
    }

    LeadColumnName(boolean isPrimary, String columnHeader) {
        this.isPrimary = isPrimary;
        this.columnHeader = columnHeader;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public String getColumnHeader() {
        return columnHeader;
    }

    public static LeadColumnName getByColumnHeader(String header) {
        return Arrays.stream(values())
                .filter(v -> v.getColumnHeader().equals(header))
                .findAny()
                .orElse(null);
    }
}

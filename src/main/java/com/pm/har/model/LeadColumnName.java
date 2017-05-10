package com.pm.har.model;

public enum LeadColumnName {
    mailId(true),
    comments,
    phone,
    from,
    zipCode,
    mls,
    listPrice,
    address,
    source;

    private boolean isPrimary = false;

    LeadColumnName() {
    }

    LeadColumnName(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public boolean isPrimary() {
        return isPrimary;
    }
}

package com.pm.har.model;

import java.util.HashMap;
import java.util.Map;

public class LeadDto {
    private Map<LeadColumnName, String> row = new HashMap<>();

    public LeadDto put(LeadColumnName name, String value) {
        row.put(name, value);
        return this;
    }

    public String get(LeadColumnName name) {
        return row.get(name);
    }

    @Override
    public String toString() {
        return "LeadDto{" + row + '}';
    }
}

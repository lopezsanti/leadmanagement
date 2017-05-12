package com.pm.har.parser.impl;

import com.pm.har.parser.MoveMailParser;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsoupMoveMailParser implements MoveMailParser {

    private final Pattern infoFragmentPattern = Pattern.compile("(?ms)This is an automated inquiry sent by a REALTOR(.*)For questions and support");
    private final Pattern firstNameInfoPattern = Pattern.compile("First Name: (.*)<br>");
    private final Pattern lastNameInfoPattern = Pattern.compile("Last Name: (.*)<br>");
    private final Pattern fromInfoPattern = Pattern.compile("Email Address: (.*)<br>");
    private final Pattern commentInfoPattern = Pattern.compile("(?ms)Comment:(?:\\r?\\n)*<br>(?:\\r?\\n)*(.*)This consumer inquired");
    private final Pattern phoneInfoPattern = Pattern.compile("Phone Number: (.*)<br>");
    private final Pattern addressInfoPattern = Pattern.compile("(?ms)Property Address:<br>(?:</span>)?(.*)MLSID #");
    private final Pattern zipAddressPattern = Pattern.compile("(?ms)(\\d+)$");
    private final Pattern priceInfoPattern = Pattern.compile("(?m)Basic Property Attributes:<br>(?:\\r?\\n)*(.*)<br>");
    private final Pattern mlsInfoPattern = Pattern.compile("MLSID # (.*)<br>");
    private final Pattern detailsLinkInfoPattern = Pattern.compile("View this listing on REALTOR.com.: <a href=\"(.*)\" id=");

    @Override
    public String getFrom(String page) {
        return getData(page, this.fromInfoPattern);
    }

    @Override
    public String getFromName(String page) {
        String firstName = getData(page, this.firstNameInfoPattern);
        String lastName = getData(page, this.lastNameInfoPattern);
        return String.format("%s %s", firstName, lastName);
    }

    @Override
    public String getComments(String page) {
        String data = getData(page, this.commentInfoPattern);
        if (data == null) {
            return null;
        }
        data = data
                .replaceAll("<br>", " ")
                .replaceAll("<span[^>]*>", " ")
                .replaceAll("(?m)^\\s*(\r)?\n\\s*$", "")
                .replaceAll("(\r)?\n\\s*$", "")
                .replaceAll("(\r)?\n", ", ")
                .replaceAll(" {2,}", " ")
                .trim();
        return data;
    }

    @Override
    public String getPhone(String page) {
        Pattern phoneFormatter = Pattern.compile("(.*)(...)(...)(....)");
        return Optional.ofNullable(getData(page, phoneInfoPattern))
                .map(t -> t.replaceAll("[\\(\\)\\- ]*", ""))
                .map(t -> {
                    Matcher matcher = phoneFormatter.matcher(t);
                    if (matcher.find()) {
                        return String.format("%s(%s) %s-%s", matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
                    } else {
                        return null;
                    }

                })
                .orElse(null);
    }

    @Override
    public String getAddress(String page) {
        String data = getData(page, addressInfoPattern);
        if (data == null) {
            return null;
        }
        data = data
                .replaceAll("<br>", " ")
                .replaceAll("<span[^>]*>", " ")
                .replaceAll("(?m)^\\s*(\r)?\n\\s*$", "")
                .replaceAll("(\r)?\n\\s*$", "")
                .replaceAll("(\r)?\n", ", ")
                .replaceAll(" {2,}", " ")
                .trim();
        return data;
    }

    @Override
    public URL getDetailsLink(String page) {
        String data = getData(page, detailsLinkInfoPattern);
        return Optional.ofNullable(data)
                .map(l -> {
                    try {
                        return new URL(l);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public String getZipCode(String page) {
        String address = getAddress(page);
        if (address == null) {
            return null;
        }
        Matcher matcher = zipAddressPattern.matcher(address);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public String getListPrice(String page) {
        return getData(page, priceInfoPattern);
    }

    @Override
    public String getMLS(String page) {
        return getData(page, mlsInfoPattern);
    }

    @Nullable
    public String getData(String page, Pattern pattern) {
        String info = getInfoFragment(page);
        if (info == null) {
            return null;
        }

        Matcher fieldMatcher = pattern.matcher(info);
        if (!fieldMatcher.find()) {
            return null;
        }

        return fieldMatcher.group(1).trim();
    }

    @Nullable
    public String getInfoFragment(String page) {
        Matcher matcher = infoFragmentPattern.matcher(page);
        if (!matcher.find()) {
            return null;
        }
        String info = matcher.group(1);
        if (info == null) {
            return null;
        }
        return info;
    }

}

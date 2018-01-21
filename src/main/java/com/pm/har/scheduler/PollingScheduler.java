package com.pm.har.scheduler;

import com.google.api.client.util.IOUtils;
import com.pm.har.mail.MailReader;
import com.pm.har.model.LeadColumnName;
import com.pm.har.model.LeadDto;
import com.pm.har.page.PageFetcher;
import com.pm.har.page.impl.ApachePageFetcher;
import com.pm.har.parser.*;
import com.pm.har.parser.impl.*;
import com.pm.har.smartsheet.SmartSheetService;
import com.smartsheet.api.SmartsheetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PollingScheduler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Logger mailLogger = LoggerFactory.getLogger("mail");

    private HarMailParser mailParser = new JsoupHarMailParser();
    private HarSitePageParser searchPageParser = new JsoupHarSitePageParser();
    private DetailsPageParser detailsPageParser = new JsoupDetailsPageParser();
    private PageFetcher fetcher = new ApachePageFetcher();

    private HomesMailParser homesMailParser = new JsoupHomesMailParser();
    private MoveMailParser moveMailParser = new RegexpMoveMailParser();
    private WolfMailParser wolfMailParser = new JsoupWolfMailParser();

    @Autowired
    private MailReader mailReader;

    @Autowired
    private SmartSheetService smartSheetService;

    @Autowired
    private MailSourceDetector mailSourceDetector;

    @Scheduled(fixedRateString = "${app.scheduler.interval.seconds}000",
            initialDelay = 5000
    )
    public void poll() throws IOException, MessagingException, SmartsheetException {
        logger.info("poll for new mails started");

        List<String> mails = mailReader.listIncomingMails();
        logger.debug("{} new mails to process", mails.size());

        for (String mailId : mails) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            MimeMessage message = mailReader.getMimeMessage(mailId);
            String content = mailReader.getHtmlContent(message);
            if (content.isEmpty()) {
                content = mailReader.getTextContent(message);
            }
            MailSourceDetector.Source source = mailSourceDetector.detectSource(content);
            if (source == null) {
                logger.warn("Mail source not detected: mailId={} from={}", mailId, message.getFrom());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(102400);
                IOUtils.copy(message.getInputStream(), outputStream);
                outputStream.close();
                mailLogger.info("from {}, content={}",
                        Arrays.stream(message.getFrom())
                        .map(Address::toString)
                        .collect(Collectors.joining("; ")),
                        Base64.getEncoder().encodeToString(outputStream.toByteArray())
                );
                continue;
            }
            LeadDto leadDto = null;
            switch (source) {
                case CHRON:
                case HAR:
                    leadDto = processHarMail(content, mailId, source);
                    break;
                case HOMES:
                    leadDto = processHomesMail(content, mailId);
                    break;
                case MOVES:
                    leadDto = processMovesMail(content, mailId, message.getSentDate());
                    break;
                case WOLF_NET:
                    leadDto = processWolfMail(content, mailId, message.getSentDate());
                    break;
            }
            if (leadDto == null) {
                continue;
            }
            logger.info("mail id={} lead={}", mailId, leadDto.toString());

            smartSheetService.saveLead(leadDto);

            mailReader.markMailProcessed(mailId);
            logger.info("mail id={} marked as processed", mailId);
        }
        logger.info("poll for new mails finished");
    }

    private LeadDto processMovesMail(String content, String mailId, Date sentDate) {
        Optional<Date> date = Optional.ofNullable(sentDate);
        String mailDate = date.map(d -> getDateFromat().format(d)).orElse(null);
        String mailTime = date.map(d -> getTimeFromat().format(d)).orElse(null);
        return new LeadDto()
                .put(LeadColumnName.mailId, getMailLink(mailId))
                .put(LeadColumnName.address, moveMailParser.getAddress(content))
                .put(LeadColumnName.comments, moveMailParser.getComments(content))
                .put(LeadColumnName.mail_from, moveMailParser.getFrom(content))
                .put(LeadColumnName.phone, moveMailParser.getPhone(content))
                .put(LeadColumnName.listPrice, moveMailParser.getListPrice(content))
                .put(LeadColumnName.mls, moveMailParser.getMLS(content))
                .put(LeadColumnName.zipCode, moveMailParser.getZipCode(content))
                .put(LeadColumnName.source, "move.com")
                .put(LeadColumnName.date, mailDate)
                .put(LeadColumnName.time, mailTime)
                .put(LeadColumnName.name_from, moveMailParser.getFromName(content))
                ;
    }

    private LeadDto processWolfMail(String content, String mailId, Date sentDate) {
        Optional<Date> date = Optional.ofNullable(sentDate);
        String mailDate = date.map(d -> getDateFromat().format(d)).orElse(null);
        String mailTime = date.map(d -> getTimeFromat().format(d)).orElse(null);
        return new LeadDto()
                .put(LeadColumnName.mailId, getMailLink(mailId))
                .put(LeadColumnName.address, wolfMailParser.getAddress(content))
                .put(LeadColumnName.comments, wolfMailParser.getComments(content))
                .put(LeadColumnName.mail_from, wolfMailParser.getFrom(content))
                .put(LeadColumnName.phone, wolfMailParser.getPhone(content))
                .put(LeadColumnName.listPrice, wolfMailParser.getListPrice(content))
                .put(LeadColumnName.mls, wolfMailParser.getMLS(content))
                .put(LeadColumnName.zipCode, wolfMailParser.getZipCode(content))
                .put(LeadColumnName.source, "WolfNet")
                .put(LeadColumnName.date, mailDate)
                .put(LeadColumnName.time, mailTime)
                .put(LeadColumnName.name_from, wolfMailParser.getFromName(content))
                ;
    }

    private LeadDto processHomesMail(String content, String mailId) {
        Optional<Date> date = Optional.ofNullable(homesMailParser.getMailDate(content));
        String mailDate = date.map(d -> getDateFromat().format(d)).orElse(null);
        String mailTime = date.map(d -> getTimeFromat().format(d)).orElse(null);
        return new LeadDto()
                .put(LeadColumnName.mailId, getMailLink(mailId))
                .put(LeadColumnName.address, homesMailParser.getAddress(content))
                .put(LeadColumnName.comments, homesMailParser.getComments(content))
                .put(LeadColumnName.mail_from, homesMailParser.getFrom(content))
                .put(LeadColumnName.phone, homesMailParser.getPhone(content))
                .put(LeadColumnName.listPrice, homesMailParser.getListPrice(content))
                .put(LeadColumnName.mls, homesMailParser.getMLS(content))
                .put(LeadColumnName.zipCode, homesMailParser.getZipCode(content))
                .put(LeadColumnName.source, "homes.com")
                .put(LeadColumnName.date, mailDate)
                .put(LeadColumnName.time, mailTime)
                .put(LeadColumnName.name_from, homesMailParser.getFromName(content))
                ;
    }

    private LeadDto processHarMail(String content, String mailId, MailSourceDetector.Source source) {
        URL viewUrl = mailParser.getViewUrl(content);
        if (viewUrl == null) {
            logger.info("Invalid message id={} skipped", mailId);
            return null;
        }
        String searchPage = fetcher.fetch(viewUrl);
        if (searchPage == null) {
            logger.info("Can't fetch search page by url {}", viewUrl);
            return null;
        }

        String from = searchPageParser.getFrom(searchPage);
        String phone = searchPageParser.getPhone(searchPage);
        String comments = searchPageParser.getComments(searchPage);
        URL detailsUrl = searchPageParser.getDetailsLink(searchPage);

        if (detailsUrl == null) {
            logger.info("Can't find details page link, message id={}", mailId);
            return null;
        }

        String detailsPage = fetcher.fetch(detailsUrl);
        if (detailsPage == null) {
            logger.info("Can't fetch details page by url {}", detailsUrl);
            return null;
        }

        String address = detailsPageParser.getAddress(detailsPage);
        String listPrice = detailsPageParser.getListPrice(detailsPage);
        String mls = detailsPageParser.getMLS(detailsPage);
        String zipCode = detailsPageParser.getZipCode(detailsPage);
        Optional<Date> date = Optional.ofNullable(mailParser.getMailDate(content));
        String mailDate = date.map(d -> getDateFromat().format(d)).orElse(null);
        String mailTime = date.map(d -> getTimeFromat().format(d)).orElse(null);

        return new LeadDto()
                .put(LeadColumnName.mailId, getMailLink(mailId))
                .put(LeadColumnName.address, address)
                .put(LeadColumnName.comments, comments)
                .put(LeadColumnName.mail_from, from)
                .put(LeadColumnName.phone, phone)
                .put(LeadColumnName.listPrice, listPrice)
                .put(LeadColumnName.mls, mls)
                .put(LeadColumnName.zipCode, zipCode)
                .put(LeadColumnName.source, source == MailSourceDetector.Source.HAR ? "har.com" : "chron.com")
                .put(LeadColumnName.date, mailDate)
                .put(LeadColumnName.time, mailTime)
                .put(LeadColumnName.name_from, searchPageParser.getFromName(content))
                ;

    }

    private String getMailLink(String mailId) {
        return "https://mail.google.com/mail/u/0/#all/" + mailId;
    }

    private DateFormat getDateFromat() {
        return new SimpleDateFormat("EEE MM/dd/yyyy", Locale.US);
    }
    private DateFormat getTimeFromat() {
        return new SimpleDateFormat("KK:mm a", Locale.US);
    }
}

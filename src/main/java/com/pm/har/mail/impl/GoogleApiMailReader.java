package com.pm.har.mail.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import com.pm.har.mail.MailReader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class GoogleApiMailReader implements MailReader {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("googleAccessTokenProvider")
    protected AccessTokenProvider accessTokenProvider;
    @Autowired
    @Qualifier("offlineOperations")
    private OAuth2RestOperations restTemplate;
    @Autowired
    @Qualifier("googleResource")
    private OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails;


    @Value("${app.sourceLabel.name}")
    private String sourceLabel;
    @Value("${app.targetLabel.name}")
    private String targetLabel;

    private String sourceLabelId;
    private String targetLabelId;


    @Override
    public List<String> listIncomingMails() throws IOException {
        Gmail gmail = getGmailService();
        if (gmail == null) {
            logger.warn("Mail processing aborted due to configuration error");
            return Collections.emptyList();
        }
        List<String> messageIds = new ArrayList<>();
        String nextPageToken = null;
        do {
            ListMessagesResponse listMessagesResponse = gmail.users().messages().list("me")
                    .setLabelIds(Collections.singletonList(sourceLabelId))
                    .setPageToken(nextPageToken)
                    .execute();
            List<Message> messages = listMessagesResponse.getMessages();
            nextPageToken = listMessagesResponse.getNextPageToken();

            if (messages != null) {
                messageIds.addAll(messages.stream()
                        .map(Message::getId)
                        .collect(Collectors.toList()));
            }
        } while (nextPageToken != null);
        return messageIds;
    }

    @Override
    public String getHtmlContent(MimeMessage message) throws IOException, MessagingException {
        List<String> htmls = findHtmlParts(message);
        return StringUtils.collectionToDelimitedString(htmls, " ");
    }

    @Override
    public void markMailProcessed(String mailId) throws IOException {
        Gmail gmail = getGmailService();
        ModifyMessageRequest request = new ModifyMessageRequest();
        request.setAddLabelIds(Collections.singletonList(targetLabelId));
        request.setRemoveLabelIds(Collections.singletonList(sourceLabelId));
        Message modifyResponse = gmail.users().messages()
                .modify("me", mailId, request)
                .execute();
    }


    public Gmail getGmailService() throws IOException {
        logger.info("getGmailService. Source label: '{}', target label: '{}'", sourceLabel, targetLabel);
        OAuth2AccessToken accessToken = restTemplate.getAccessToken();
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken.getValue());
        credential.setExpirationTimeMilliseconds(accessToken.getExpiration().getTime());

        Gmail gmail = new Gmail.Builder(
                new ApacheHttpTransport(),
                new JacksonFactory(),
                credential)
                .setApplicationName("HAR Scrapper")
                .build();

        if (sourceLabelId == null || targetLabelId == null) {
            List<Label> labels = gmail.users().labels()
                    .list("me").execute()
                    .getLabels();

            sourceLabelId = labels.stream()
                    .filter(l -> l.getName().equals(sourceLabel))
                    .map(Label::getId).findAny().orElse(null);
            if (sourceLabelId == null) {
                logger.error("Configured source label '{}' not found. Available labels are '{}'",
                        sourceLabel, StringUtils.collectionToCommaDelimitedString(labels));
                return null;
            }

            targetLabelId = labels.stream()
                    .filter(l -> l.getName().equals(targetLabel))
                    .map(Label::getId).findAny().orElse(null);
            if (targetLabelId == null) {
                logger.error("Configured target label '{}' not found. Available labels are '{}'",
                        targetLabel, StringUtils.collectionToCommaDelimitedString(labels));
                return null;
            }
        }

        return gmail;
    }

    public List<String> findHtmlParts(Part message) throws MessagingException, IOException {
        ArrayList<String> htmls = new ArrayList<>();

        if (message.getContentType().startsWith("text/html")) {
            Object content = message.getContent();
            if (content instanceof String) {
                htmls.add((String) content);
            }
        } else {
            Object content = message.getContent();
            if (content instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) content;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    htmls.addAll(findHtmlParts(part));
                }
            }
        }
        return htmls;
    }

    private List<String> findTextParts(Part message) throws MessagingException, IOException {
        ArrayList<String> textBody = new ArrayList<>();

        if (!message.getContentType().startsWith("text/html")) {
            Object content = message.getContent();
            if (content instanceof String) {
                textBody.add((String) content);
            }
        } else {
            Object content = message.getContent();
            if (content instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) content;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    textBody.addAll(findTextParts(part));
                }
            }
        }
        return textBody;
    }

    @Override
    public MimeMessage getMimeMessage(String messageId)
            throws IOException, MessagingException {
        Gmail service = getGmailService();
        Message message = service.users().messages().get("me", messageId).setFormat("raw").execute();
//        String from = message.getPayload().getHeaders().stream()
//                .filter(h -> h.getName().equals("From"))
//                .findAny()
//                .map(MessagePartHeader::getValue)
//                .orElse(null);

        byte[] emailBytes = Base64.decodeBase64(message.getRaw());

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

        return email;
    }

    @Override
    public String getTextContent(MimeMessage message) throws IOException, MessagingException {
        List<String> text = findTextParts(message);
        return StringUtils.collectionToDelimitedString(text, " ");
    }


}

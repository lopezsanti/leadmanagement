package com.pm.har.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

public interface MailReader {

    List<String> listIncomingMails() throws IOException;

    String getHtmlContent(MimeMessage message) throws IOException, MessagingException;

    void markMailProcessed(String mailId) throws IOException;

    MimeMessage getMimeMessage(String messageId)
            throws IOException, MessagingException;
}

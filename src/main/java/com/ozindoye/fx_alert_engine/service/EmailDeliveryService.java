package com.ozindoye.fx_alert_engine.service;

import com.ozindoye.fx_alert_engine.model.AlertSubscription;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class EmailDeliveryService {

    private final SendGrid sendGrid;

    public EmailDeliveryService(@Value("${sendgrid.api.key}") String apiKey) {
        this.sendGrid = new SendGrid(apiKey);
    }

    public void sendAlert(AlertSubscription subscription,
                          BigDecimal rate,
                          String pairLabel) {
        Email from = new Email("ozindoye@gmail.com");
        Email to = new Email(subscription.getUserEmail());
        String subject = "FX Alert: " + pairLabel + " has crossed your threshold";

        String body = "Hello,\n\n"
                + "Your alert has been triggered.\n\n"
                + "Pair: " + pairLabel + "\n"
                + "Current Rate: " + rate + "\n"
                + "Your Threshold: " + subscription.getThresholdValue() + "\n"
                + "Condition: " + subscription.getThresholdType() + "\n\n"
                + "This is an automated alert from FX Rate Alert Engine.";

        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            System.out.println("Email sent to: " + subscription.getUserEmail()
                    + " — status: " + response.getStatusCode());

        } catch (IOException e) {
            System.out.println("Email delivery failed for: "
                    + subscription.getUserEmail()
                    + " — error: " + e.getMessage());
        }
    }
}

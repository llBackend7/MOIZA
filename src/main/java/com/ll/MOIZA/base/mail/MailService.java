package com.ll.MOIZA.base.mail;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MailService {

    private final SendGrid sg;

    @Value("${custom.mail.sender}")
    private String mailSender;

    public void sendMailTo(Member to, String content) {
        Mail mail = buildMail(to.getEmail(), content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("메일을 보내는 도중 문제가 발생했습니다.");
        }
    }

    private Mail buildMail(String userEmail, String mailContents) {
        Email from = new Email(mailSender);
        String subject = "[MOIZA] 모이자에서 발송한 메일입니다.";
        Email to = new Email(userEmail);
        Content content = new Content("text/html", mailContents);
        Mail mail = new Mail(from, subject, to, content);
        return mail;
    }
}
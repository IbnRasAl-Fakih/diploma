package com.diploma.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender sender;

    public EmailService(JavaMailSender sender) {
        this.sender = sender;
    }

    public void sendConfirmationEmail(String to, String code) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Код подтверждения");
            helper.setText(
                "<p>Здравствуйте!</p>" +
                "<p>Ваш код подтверждения: <b>" + code + "</b></p>" +
                "<p>Код действителен 15 минут.</p>",
                true
            );

            sender.send(message);
            System.out.println("📨 Email отправлен на " + to);
        } catch (Exception e) {
            System.err.println("❌ Ошибка при отправке email: " + e.getMessage());
        }
    }
}

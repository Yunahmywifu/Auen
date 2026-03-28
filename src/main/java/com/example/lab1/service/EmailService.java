package com.example.lab1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendConfirmationCode(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(to);
        msg.setSubject("AUEN — Подтверждение аккаунта");
        msg.setText(
            "Добро пожаловать в AUEN Music Library!\n\n" +
            "Ваш код подтверждения:\n\n" +
            "  " + code + "\n\n" +
            "Код действителен 10 минут.\n" +
            "Если вы не регистрировались — просто проигнорируйте это письмо."
        );
        mailSender.send(msg);
    }
}


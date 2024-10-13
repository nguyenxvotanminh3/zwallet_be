package com.nguyenminh.microservices.zwallet.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final HttpServletRequest request;
    // Send email
    public void sendEmail(String to, String token) {
        String baseUrl = request.getRequestURL().toString()
                .replace(request.getRequestURI(), "");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("worknguyenvotanminh@gmail.com");
        message.setTo(to);
        message.setSubject("Reset Your Password");
//        message.setText("Click the link to reset your password: " +
//                baseUrl + "/auth/reset-password?token=" + token);
        message.setText("This is your token: " + token + "\n" +
                "Click the link below to change your password:\n" +
                "https://thezwallet.netlify.app/Zwallet/pages/changethepass");

        mailSender.send(message);
    }
}

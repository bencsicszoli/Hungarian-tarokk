package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.exceptionhandling.customexception.FeedbackDeliveryException;
import com.codecool.tarokkgame.exceptionhandling.customexception.NotAllowedOperationException;
import com.codecool.tarokkgame.model.dto.restdto.FeedbackDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class FeedbackService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${app.feedback.to:}")
    private String to;

    public FeedbackService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public Map<String, String> sendFeedback(FeedbackDTO request) {
        if (request.message() == null || request.message().isBlank()) {
            throw new NotAllowedOperationException("Feedback message must not be empty");
        }

        String username = currentUsername();

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(to);
        email.setSubject("Tarokk feedback from " + username);
        email.setText(buildBody(username, request));

        try {
            mailSender.send(email);
        } catch (MailException e) {
            throw new FeedbackDeliveryException("Could not send feedback email", e);
        }

        return Map.of("message", "Thanks for your feedback!");
    }

    private String buildBody(String username, FeedbackDTO request) {
        StringBuilder body = new StringBuilder();
        body.append("From player: ").append(username).append("\n");
        if (request.userAgent() != null && !request.userAgent().isBlank()) {
            body.append("Browser / device: ").append(request.userAgent()).append("\n");
        }
        body.append("\n").append(request.message().trim()).append("\n");
        return body.toString();
    }

    private String currentUsername() {
        Object principal = Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (principal instanceof User user) {
            return user.getUsername();
        }
        return String.valueOf(principal);
    }
}

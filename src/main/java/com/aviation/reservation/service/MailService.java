package com.aviation.reservation.service;

import com.aviation.reservation.dto.ReservationDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async
    public void sendReservationConfirm(String toEmail, ReservationDto.Response reservation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("[Project Aviation] 예약 확인 - " + reservation.getFlightNumber());

            Context context = new Context();
            context.setVariable("reservation", reservation);
            String html = templateEngine.process("reservation-confirm", context);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("예약 확인 메일 발송 완료: {} → {}", reservation.getFlightNumber(), toEmail);
        } catch (MessagingException e) {
            log.error("예약 확인 메일 발송 실패 [{}]: {}", toEmail, e.getMessage());
        }
    }
}

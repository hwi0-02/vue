package com.example.backend.admin.service;

import com.example.backend.admin.dto.CustomerNotificationRequest;
import com.example.backend.admin.dto.CustomerNotificationResponse;
import com.example.backend.payment.domain.Payment;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPaymentNotificationService {

    private final JavaMailSender mailSender;

    public CustomerNotificationResponse sendCustomerNotification(Payment payment,
                                                                  String recipient,
                                                                  CustomerNotificationRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), true);
            helper.setSentDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
            helper.setFrom("noreply@hotel-admin.com");
            mailSender.send(message);
            return CustomerNotificationResponse.builder()
                    .recipient(recipient)
                    .subject(request.getSubject())
                    .sentAt(LocalDateTime.now())
                    .success(true)
                    .messageId(message.getMessageID())
                    .build();
        } catch (Exception ex) {
            log.error("고객 알림 이메일 발송 실패 - paymentId={}", payment.getId(), ex);
            return CustomerNotificationResponse.builder()
                    .recipient(recipient)
                    .subject(request.getSubject())
                    .sentAt(LocalDateTime.now())
                    .success(false)
                    .messageId(null)
                    .build();
        }
    }

    public void sendPartialRefundNotification(Payment payment,
                                               int refundAmount,
                                               int fee,
                                               int remainingAmount) {
        if (payment.getUser() == null || payment.getUser().getEmail() == null) {
            return;
        }
        String subject = "[EGODA] 부분 환불이 처리되었습니다.";
        String body = "<p>안녕하세요, " + payment.getUser().getName() + " 고객님.</p>"
                + "<p>요청하신 결제에 대해 부분 환불이 완료되었습니다.</p>"
                + "<ul>"
                + "<li>환불 금액: " + formatCurrency(refundAmount) + "</li>"
                + "<li>공제 수수료: " + formatCurrency(fee) + "</li>"
                + "<li>잔여 환불 가능액: " + formatCurrency(remainingAmount) + "</li>"
                + "</ul>"
                + "<p>궁금하신 사항은 고객센터로 문의해주세요.</p>";
        CustomerNotificationRequest request = CustomerNotificationRequest.builder()
                .subject(subject)
                .body(body)
                .build();
        sendCustomerNotification(payment, payment.getUser().getEmail(), request);
    }

    private String formatCurrency(int amount) {
        return String.format("%,d원", amount);
    }
}

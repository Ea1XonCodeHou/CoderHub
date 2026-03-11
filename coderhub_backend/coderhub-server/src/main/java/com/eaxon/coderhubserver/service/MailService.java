package com.eaxon.coderhubserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 邮件发送服务
 */
@Service
@Slf4j
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送密码重置验证码邮件
     * @param toEmail 收件人邮箱
     * @param code 6位验证码
     */
    public void sendResetPasswordCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("【CoderHub】密码重置验证码");
        message.setText("【CoderHub】官方提醒：尊敬的用户，您的密码重置验证码为：" + code
                + "，有效期5分钟。如非本人操作，请忽略此邮件。");
        mailSender.send(message);
        log.info("验证码邮件已发送至: {}", toEmail);
    }
}

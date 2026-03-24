package com.yyds.hrcscommon.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j  // 添加日志记录
@PropertySource("classpath:application.yml")
public class MailClient {

    @Autowired
    private JavaMailSender mailSender;  // 建议用 JavaMailSender（功能更全）

    @Value("${spring.mail.username}")
    private String from;  //修正拼写

    private static final String SUBJECT = "你的一次性代码";

    public void sendMail(String to, String code) {
        //清除首尾引号和空格
        to = to.replaceAll("^[\"']|[\"']$", "").trim();

        log.info("准备发送邮件: from={}, to={}, code={}", from, to, code);
        // 1. 前置验证
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("邮箱格式无效: " + to);
        }

        // 2. 日志记录（关键调试信息）
        log.info("准备发送邮件: from={}, to={}, code={}", from, to, code);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(SUBJECT);
            message.setText(createText(to, code));

            mailSender.send(message);
            log.info("邮件发送成功 to={}", to);

        } catch (MailSendException e) {
            log.error("邮件发送失败 to={}, error={}", to, e.getMessage());
            // 3. 捕获特定异常
            if (e.getMessage().contains("Invalid Addresses")) {
                throw new RuntimeException("邮箱地址不存在: " + to, e);
            }
            throw new RuntimeException("邮件发送失败，请稍后重试", e);
        }
    }

    /**
     * 发送自定义邮件（用于工资通知等）
     */
    public void sendCustomMail(String to, String subject, String content) {
        to = to.replaceAll("^[\\\"']|[\\\"']$", "").trim();
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("邮箱格式无效: " + to);
        }
        String realSubject = (subject == null || subject.trim().isEmpty()) ? "通知" : subject;
        String realContent = content == null ? "" : content;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(realSubject);
            message.setText(realContent);
            mailSender.send(message);
            log.info("邮件发送成功 to={}, subject={}", to, realSubject);
        } catch (MailSendException e) {
            log.error("邮件发送失败 to={}, error={}", to, e.getMessage());
            if (e.getMessage().contains("Invalid Addresses")) {
                throw new RuntimeException("邮箱地址不存在: " + to, e);
            }
            throw new RuntimeException("邮件发送失败，请稍后重试", e);
        }
    }

    /**
     * 邮箱格式验证
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // 去除可能的引号或空格
        String cleanEmail = email.replaceAll("^[\"']|[\"']$", "").trim();

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return cleanEmail.matches(regex);
    }

    private String createText(String to, String code) {
        return String.format(
                "%s,\n\n您好，\n" +
                        "感谢您使用我们的服务。\n" +
                        "您的验证码是：\n" +
                        "%s\n\n" +
                        "请勿将此验证码透露给任何人。\n\n" +
                        "如果您并未申请验证码，请忽略此邮件。\n\n谢谢!",
                to, code
        );
    }
}

package com.yyds.hrcscommon.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.yml")
public class MailClient {
    @Autowired
    private MailSender mailSender;
    @Value("${spring.mail.username}")
    private String form;
    private final static String subject = "你的一次性代码";

    public void sendMail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(form);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(createText(to, code));
        mailSender.send(message);
    }

    private String createText(String to, String code) {
        // 构建一个友好的邮件内容
        return to + ",\n\n" + // 添加收件人问候
                "您好，\n" +
                "感谢您使用我们的服务。\n" +
                "您的验证码是：\n" +
                code + "\n\n" +
                "请勿将此验证码透露给任何人或第三方，否则可能会导致您的账号安全问题。\n\n" +
                "如果您并未申请验证码，请忽略此邮件。\n\n" +
                "谢谢!";
    }
}

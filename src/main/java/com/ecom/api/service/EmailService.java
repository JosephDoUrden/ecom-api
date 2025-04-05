package com.ecom.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  /**
   * Send a password reset email to the user
   *
   * @param to        recipient email address
   * @param name      recipient name
   * @param resetLink link to reset password
   */
  public void sendPasswordResetEmail(String to, String name, String resetLink) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject("Password Reset Request");

      String htmlContent = "<html>" +
          "<body>" +
          "<h2>Password Reset</h2>" +
          "<p>Hello " + name + ",</p>" +
          "<p>You have requested to reset your password. Click the link below to set a new password:</p>" +
          "<p><a href='" + resetLink + "'>Reset Password</a></p>" +
          "<p>This link will expire in 30 minutes.</p>" +
          "<p>If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>"
          +
          "<p>Thanks,<br/>The E-Commerce Team</p>" +
          "</body>" +
          "</html>";

      helper.setText(htmlContent, true);
      mailSender.send(message);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send password reset email", e);
    }
  }
}

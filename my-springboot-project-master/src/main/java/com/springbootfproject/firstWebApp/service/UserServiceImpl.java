package com.springbootfproject.firstWebApp.service;

import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springbootfproject.firstWebApp.dto.UserDto;
import com.springbootfproject.firstWebApp.repository.UserRepository;
import com.springbootfproject.firstWebApp.todomodel.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class UserServiceImpl implements UserService {

	@Value("${application.base-url}")
	private String baseUrl;

	@Value("${application.reset-password-path}")
	private String resetPasswordPath;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private JavaMailSender mailSender;

	private UserRepository userRepository;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public User findByUsername(String username) {
		logger.debug("Finding user by username: {}", username);
		return userRepository.findByUsername(username);
	}

	@Override
	public User save2(UserDto userDto) {
		logger.debug("Saving user: {}", userDto.getUsername());
		User user = new User(userDto.getUsername(), passwordEncoder.encode(userDto.getPassword()),
				userDto.getFullname(), userDto.getEmail(), userDto.getSecurityQuestion(), userDto.getSecurityAnswer(),
				userDto.getResetToken());
		return userRepository.save(user);
	}

	@Override
	public User save(UserDto userDto) {
		logger.debug("Saving user: {}", userDto.getUsername());
		User user = new User(userDto.getUsername(), passwordEncoder.encode(userDto.getPassword()),
				userDto.getFullname(), userDto.getEmail(), userDto.getSecurityQuestion(), userDto.getSecurityAnswer(),
				userDto.getResetToken());
		// Otp validation
		int otp =  (int) (Math.random() * 9000) + 1000;
		logger.debug("The OTP: {}", otp);
		SimpleMailMessage message = new SimpleMailMessage();
		if (isEmailValid(user.getEmail())) {
			message.setTo(user.getEmail());
		} else {
			logger.error("Invalid email address: " + user.getEmail());
			
		}
		message.setSubject("OTP VALIDATION");
		message.setText("Hello \n\n" + "Your Login OTP :" + otp + ".Please Verify. \n\n" + "Regards \n" + "Your Food Check List Team");
		try {
			mailSender.send(message);
			logger.debug("OTP is generated and email sent to user: {}", userDto.getUsername());	
			
		} catch (MailAuthenticationException ex) {
			logger.error("Mail authentication failed", ex);
		} catch (MailException ex) {
			logger.error("Failed to send email", ex);
		}

		return userRepository.save(user);
	}

	@Override
	public boolean isSecurityQuestionAnswerValid(String username, String securityAnswer, String securityQuestion) {
	    logger.debug("Validating security question answer for user: {}", username);
	    User user = userRepository.findByUsername(username);
	    if (user != null && user.getSecurityQuestion().equals(securityQuestion)
	            && user.getSecurityAnswer().equals(securityAnswer)) {
	        String resetToken = UUID.randomUUID().toString();
	        user.setResetToken(resetToken);
	        userRepository.save(user);
	        String resetUrl = baseUrl + resetPasswordPath + "?token=" + resetToken;

	        String htmlContent = "<html>"
	                            + "<head><style>"
	                            + "body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; color: #333; }"
	                            + ".container { width: 80%; margin: auto; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); position: relative; }"
	                            + ".container::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: url('https://www.transparenttextures.com/patterns/scream.png'); opacity: 0.2; z-index: -1; }"
	                            + "h1 { color: #4CAF50; font-size: 24px; }"
	                            + ".reminder-tag { background-color: #ff5722; color: #fff; font-size: 14px; padding: 5px 10px; border-radius: 3px; display: inline-block; margin-bottom: 20px; font-weight: bold; }"
	                            + "p { font-size: 16px; line-height: 1.5; margin: 10px 0; font-weight: bold; }"
	                            + "a { color: #ff9800; background-color: #ff9800; padding: 10px 20px; text-decoration: none; font-weight: bold; border-radius: 5px; display: inline-block; font-size: 18px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2); transition: box-shadow 0.3s ease-in-out; }"
	                            + "a:hover { background-color: #ffb74d; box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3); }"
	                            + ".highlight { background-color: #eaf3fc; padding: 10px; border-radius: 5px; }"
	                            + ".info-box { border: 1px solid #eaf3fc; padding: 10px; border-radius: 5px; background-color: #f9f9f9; }"
	                            + ".info-box p { margin: 5px 0; }"
	                            + ".info-box strong { color: #4CAF50; }"
	                            + ".highlight p { font-style: italic; color: #ff9800; }"
	                            + "footer { font-size: 14px; color: #777; margin-top: 20px; font-weight: bold; text-align: left; }"
	                            + "</style></head>"
	                            + "<body>"
	                            + "<div class='container'>"
	                            + "<h1>Password Reset Request</h1>"
	                            + "<p>Hi <strong>" + user.getFullname() + "</strong>,</p>"
	                            + "<p>We received a request to reset your password. Click the link below to reset your password:</p>"
	                            + "<p><a href=\"" + resetUrl + "\">Reset Password</a></p>"
	                            + "<div class='info-box'>"
	                            + "<p>If you did not request a password reset, please ignore this email.</p>"
	                            + "<p class='highlight'><em><strong>Note:</strong> This is an automated message. Please do not reply to this email.</em></p>"
	                            + "</div>"
	                            + "<footer>"
	                            + "<p>Thanks and regards,</p>"
	                            + "<p>Your Food Check List Team</p>"
	                            + "<p>Have a nice day!</p>"
	                            + "</footer>"
	                            + "</div>"
	                            + "</body></html>";

	        MimeMessage mimeMessage = mailSender.createMimeMessage();
	        try {
	            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
	            if (isEmailValid(user.getEmail())) {
	                helper.setTo(user.getEmail());
	            } else {
	                logger.error("Invalid email address: " + user.getEmail());
	                return false;
	            }
	            helper.setSubject("Password Reset Request");
	            helper.setText(htmlContent, true);
	            mailSender.send(mimeMessage);
	            logger.debug("Password reset token generated and email sent to user: {}", username);
	            return true;
	        } catch (MessagingException ex) {
	            logger.error("Failed to send email", ex);
	        }
	    }
	    logger.debug("Security question or answer invalid for user: {}", username);
	    return false;
	}

	public boolean isEmailValid(String email) {
	    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
	    Pattern pat = Pattern.compile(emailRegex);
	    if (email == null)
	        return false;
	    return pat.matcher(email).matches();
	}


	@Override
	public boolean isResetTokenValid(String token) {
		logger.debug("Validating reset token: {}", token);
		User user = userRepository.findByResetToken(token);
		boolean isValid = user != null && token.equals(user.getResetToken());
		logger.debug("Is token valid: {}", isValid);
		return isValid;
	}

	@Override
	public void updatePassword(String token, String newPassword) {
		logger.debug("Updating password for token: {}", token);
		User user = userRepository.findByResetToken(token);
		if (user != null) {
			user.setPassword(passwordEncoder.encode(newPassword));
			user.setResetToken(null);
			userRepository.save(user);
			logger.debug("Password updated and reset token cleared for user: {}", user.getUsername());
		} else {
			logger.debug("User not found for token: {}", token);
		}
	}

	@Override
	public boolean isValidSecurityQuestionAnswer(String question, String answer) {
		logger.debug("Validating security question and answer");
		return question != null && !question.trim().isEmpty() && answer != null && !answer.trim().isEmpty();
	}

	@Override
	public User save(User user) {
		logger.debug("Saving user: {}", user.getUsername());
		return userRepository.save(user);
	}
}

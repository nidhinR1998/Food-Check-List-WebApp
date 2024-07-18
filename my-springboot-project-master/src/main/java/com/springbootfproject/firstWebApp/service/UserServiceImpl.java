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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springbootfproject.firstWebApp.dto.UserDto;
import com.springbootfproject.firstWebApp.repository.UserRepository;
import com.springbootfproject.firstWebApp.todomodel.User;

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
				userDto.getFullname(),userDto.getEmail(), userDto.getSecurityQuestion(), userDto.getSecurityAnswer(),
				userDto.getResetToken());
		return userRepository.save(user);
	}

	@Override
	public User save(UserDto userDto) {
		logger.debug("Saving user: {}", userDto.getUsername());
		User user = new User(userDto.getUsername(), passwordEncoder.encode(userDto.getPassword()),
				userDto.getFullname(),userDto.getEmail(), userDto.getSecurityQuestion(), userDto.getSecurityAnswer(),
				userDto.getResetToken());
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
	        SimpleMailMessage message = new SimpleMailMessage();
	        if (isEmailValid(user.getEmail())) {
	            message.setTo(user.getEmail());
	        } else {
	            logger.error("Invalid email address: " + user.getEmail());
	            return false;
	        }
	        message.setTo(user.getEmail());
	        message.setSubject("Password Reset Request");
	        message.setText("Click the link to reset your password: " + resetUrl);
	        try {
	            mailSender.send(message);
	            logger.debug("Password reset token generated and email sent to user: {}", username);
	            return true;
	        } catch (MailAuthenticationException ex) {
	            logger.error("Mail authentication failed", ex);
	        } catch (MailException ex) {
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
	        user.setResetToken(null); // Clear the reset token after successful password reset
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

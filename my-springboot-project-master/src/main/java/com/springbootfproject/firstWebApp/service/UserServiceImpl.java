package com.springbootfproject.firstWebApp.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springbootfproject.firstWebApp.Util.EncryptionUtil;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TwilioService twilioService;

    private final EncryptionUtil encryptionUtil;

    private final UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public UserServiceImpl(UserRepository userRepository, EncryptionUtil encryptionUtil) {
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
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
				userDto.getFullname(), userDto.getPhoneNumber(), userDto.getEmail(), userDto.getResetToken(),
				userDto.getOtpCode());
		return userRepository.save(user);
	}

	@Override
    public User save(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFullname(userDto.getFullname());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setEmail(userDto.getEmail());
        user.setResetToken(userDto.getResetToken());
        user.setOtpCode(userDto.getOtpCode());
        return userRepository.save(user);
    }
	
	@Override
	public void updateTokenByEmail(String email, String token) {
		User user = userRepository.findByEmail(email);
		if (user != null) {
			user.setResetToken(token);
			userRepository.save(user);
		}
	}

	@Override
	public void updateTokenByPhoneNumber(String phoneNumber, String token) {
		User user = userRepository.findByPhoneNumber(phoneNumber);
		if (user != null) {
			user.setResetToken(token);
			userRepository.save(user);
		}
	}

	@Override
	public boolean getResetPasswordOTP(String username, String email) {
		logger.debug("Validating security question answer for user: {}", username);
		User user = userRepository.findByUsername(username);
		// && user.getSecurityAnswer().equals(securityAnswer))
		if (user != null && user.getEmail().equals(email)) {
			String resetToken = UUID.randomUUID().toString();
			int otp = (int) (Math.random() * 9000) + 1000;
			logger.debug("Generated OTP for email verification: {}", otp);
			user.setResetToken(resetToken);
			user.setOtpCode(otp);
			userRepository.save(user);
			String htmlContent = "<html>" + "<head><style>"
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
					+ ".info-box p { margin: 5px 0; }" + ".info-box strong { color: #4CAF50; }"
					+ ".highlight p { font-style: italic; color: #ff9800; }"
					+ "footer { font-size: 14px; color: #777; margin-top: 20px; font-weight: bold; text-align: left; }"
					+ "</style></head>" + "<body>" + "<div class='container'>" + "<h1>Password Reset Request</h1>"
					+ "<p>Hi <strong>" + user.getFullname() + "</strong>,</p>"
					+ "<p>We received a request to reset your password. Use the OTP below to reset your password:</p>"
					+ "<p class='reminder-tag'>Your OTP: <strong>" + otp + "</strong></p>" + "<div class='info-box'>"
					+ "<p>If you did not request a password reset, please ignore this email.</p>"
					+ "<p class='highlight'><em><strong>Note:</strong> This is an automated message. Please do not reply to this email.</em></p>"
					+ "</div>" + "<footer>" + "<p>Thanks and regards,</p>" + "<p>Your Food Check List Team</p>"
					+ "<p>Have a nice day!</p>" + "</footer>" + "</div>" + "</body></html>";

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
				logger.debug("Password reset code generated and email sent to user: {}", username);
				return true;
			} catch (MessagingException ex) {
				logger.error("Failed to send email", ex);
			}
		}
		logger.debug("Email invalid for user by given one by user: {}", username);
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
	public void updatePassword(String email, String newPassword) {
		logger.debug("Updating password for token: {}", email);
		User user = userRepository.findByEmail(email);
		if (user != null) {
			user.setPassword(passwordEncoder.encode(newPassword));
			user.setResetToken(null);
			user.setOtpCode(0);
			userRepository.save(user);
			logger.debug("Password updated and reset token cleared for user: {}", user.getUsername());
		} else {
			logger.debug("User not found for token: {}", email);
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

	@Override
	public boolean verifyCode(String email, String code) {
		if ((email == null || email.isBlank()) && (code != null && !code.isEmpty())) {
			try {
				int otpCode = Integer.parseInt(code);
				User user = userRepository.findByOtpCode(otpCode);

				if (user != null && user.getOtpCode() == otpCode) {
					user.setOtpCode(0);
					user.setResetToken(null);
					userRepository.save(user);
					logger.debug("After verification, token and code removed for user: {}", user.getFullname());
					return true;
				} else {
					logger.debug("Invalid OTP format: {}", code);
					return false;
				}
			} catch (NumberFormatException e) {
				logger.error("Invalid OTP format: {}", code, e);
				return false;
			}
		} else {
			User user = userRepository.findByEmail(email);

			if (user == null || code == null) {
				return false;
			}

			try {
				int otpCode = Integer.parseInt(code);

				if (user.getOtpCode() == otpCode) {
					user.setOtpCode(0);
					user.setResetToken(null);
					userRepository.save(user);
					logger.debug("After verification, token and code removed for user: {}", user.getUsername());
					return true;
				} else {
					logger.debug("Invalid OTP format: {}", code);
					return false;
				}
			} catch (NumberFormatException e) {
				logger.error("Invalid OTP format: {}", code, e);
				return false;
			}
		}
	}

	@Override
	public Map<String, Object> handleUserRegistration(UserDto userDto) {
		Map<String, Object> response = new HashMap<>();
		boolean isSmsOtpSent = false;
		boolean isEmailOtpSent = false;

		try {
			String token = generateToken();
			int otp = (int) (Math.random() * 9000) + 1000;
			userDto.setOtpCode(otp);
			userDto.setResetToken(token);
			User Dbuser = new User();
			Dbuser.setUsername(userDto.getUsername());
			Dbuser.setPassword(passwordEncoder.encode(userDto.getPassword()));
			Dbuser.setFullname(userDto.getFullname());
			Dbuser.setPhoneNumber(userDto.getPhoneNumber());
			Dbuser.setEmail(userDto.getEmail());
			Dbuser.setResetToken(userDto.getResetToken());
			Dbuser.setOtpCode(userDto.getOtpCode());
			userRepository.save(Dbuser);

			logger.debug("Sending SMS to phone number: {}", userDto.getPhoneNumber());
			
			try {
				twilioService.sendSms(userDto.getPhoneNumber());
				isSmsOtpSent = true;
			} catch (Exception e) {
				logger.error("Failed to send SMS OTP: {}", e.getMessage());
			}

			logger.debug("Generated OTP for email verification: {}", otp);
			String htmlContent = generateEmailContent(userDto.getFullname(), otp);
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			helper.setTo(userDto.getEmail());
			helper.setSubject("Email Verification");
			helper.setText(htmlContent, true);
			logger.debug("Sending email to: {}", userDto.getEmail());
			mailSender.send(mimeMessage);
			isEmailOtpSent = true;

			
			try {
				String encryptedEmail = encryptionUtil.encrypt(userDto.getEmail());
				String encryptedPhoneNumber = encryptionUtil.encrypt(userDto.getPhoneNumber());
				String encryptedToken = encryptionUtil.encrypt(token);

				response.put("email", encryptedEmail);
				response.put("phoneNumber", encryptedPhoneNumber);
				response.put("token", encryptedToken);
			} catch (Exception e) {
				logger.error("Error encrypting data: {}", e.getMessage(), e);
				response.put("errorMessage", "An error occurred during registration. Please try again.");
				return response;
			}

			// Set messages based on OTP sending status
			if (isSmsOtpSent && isEmailOtpSent) {
				response.put("otpMessage", "Both OTP to email and phone have been sent.");
			} else if (isSmsOtpSent) {
				response.put("otpMessage", "OTP to phone has been sent.");
			} else if (isEmailOtpSent) {
				response.put("otpMessage", "OTP to email has been sent.");
			} else {
				response.put("errorMessage", "Failed to send OTP. Please try again.");
			}

		} catch (Exception e) {
			logger.error("Error during registration process: {}", e.getMessage(), e);
			response.put("errorMessage", "An error occurred during registration. Please try again.");
		}

		return response;
	}

	private String generateToken() {
		
		String token = UUID.randomUUID().toString();
		logger.debug("Generated Token: {}", token);
		return token;
	}

	private String generateEmailContent(String fullName, int otp) {
		return "<html><head><style>"
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
				+ ".info-box p { margin: 5px 0; }" + ".info-box strong { color: #4CAF50; }"
				+ ".highlight p { font-style: italic; color: #ff9800; }"
				+ "footer { font-size: 14px; color: #777; margin-top: 20px; font-weight: bold; text-align: left; }"
				+ "</style></head><body>" + "<div class='container'>" + "<h1>Email Verification Request</h1>"
				+ "<p>Hi <strong>" + fullName + "</strong>,</p>"
				+ "<p>We received your request for Email verification. Use the OTP below to complete your Registration:</p>"
				+ "<p class='reminder-tag'>Your OTP: <strong>" + otp + "</strong></p>" + "<div class='info-box'>"
				+ "<p class='highlight'><em><strong>Note:</strong> This is an automated message. Please do not reply to this email.</em></p>"
				+ "</div>"
				+ "<footer><p>Thanks and regards,</p><p>Your Food Check List Team</p><p>Have a nice day!</p></footer>"
				+ "</div></body></html>";
	}

}

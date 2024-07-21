package com.springbootfproject.firstWebApp.controller;

import java.security.Principal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springbootfproject.firstWebApp.dto.UserDto;
import com.springbootfproject.firstWebApp.repository.UserRepository;
import com.springbootfproject.firstWebApp.service.TodoService;
import com.springbootfproject.firstWebApp.service.TwilioService;
import com.springbootfproject.firstWebApp.service.UserService;
import com.springbootfproject.firstWebApp.todomodel.User;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/userDetails")
@SessionAttributes("username")
public class UserController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private TodoService service;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private TwilioService twilioService;

	private UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String home(Model model, Principal principal) {
		String username = getLoggedinUsername();
		User user = userService.findByUsername(username);
		logger.debug("HomePage Hit by User: {}", user.getFullname());
		model.addAttribute("username", user.getFullname());
		return "home";
	}

	private String getLoggedinUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		logger.debug("Retrieved Logged in Username: {}", username);
		String fullname = service.getFullName(username);
		logger.debug("Full Name retrieved for Username {}: {}", username, fullname);
		return username;
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(Model model, UserDto userDto) {
		logger.debug("LoginPage Hit by User: {}", userDto.getUsername());
		model.addAttribute("user", userDto);
		return "login";
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
			logger.debug("User logged out: {}", authentication.getName());
		}
		return "redirect:/login?logout";
	}

	@GetMapping("/register")
	public String register(Model model, UserDto userDto) {
		logger.debug("RegisterPage Hit");
		model.addAttribute("user", userDto);
		return "register";
	}

	@PostMapping("/register")
	public String registerSave(@ModelAttribute("user") UserDto userDto, Model model,
			RedirectAttributes redirectAttributes) {
		logger.debug("Register Save Hit for User: {}", userDto.getUsername());

		User existingUser = userService.findByUsername(userDto.getUsername());
		if (existingUser != null) {
			logger.debug("User already exists: {}", userDto.getUsername());
			model.addAttribute("userExists", true);
			return "register";
		}

		try {
			// Phone verification
			// logger.debug("Sending SMS to phone number: {}", userDto.getPhoneNumber());
			// twilioService.sendSms(userDto.getPhoneNumber());

			// Email verification
			String token = generateToken();
			int otp = (int) (Math.random() * 9000) + 1000;
			logger.debug("Generated OTP for email verification: {}", otp);

			// temporary token
			userDto.setOtpCode(otp);
			userDto.setResetToken(token);
			userService.save(userDto);
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
					+ "</style></head>" + "<body>" + "<div class='container'>" + "<h1>Email Verification Request</h1>"
					+ "<p>Hi <strong>" + userDto.getFullname() + "</strong>,</p>"
					+ "<p>We received your request for Email verification . Use the OTP below to complete your Registration:</p>"
					+ "<p class='reminder-tag'>Your OTP: <strong>" + otp + "</strong></p>" + "<div class='info-box'>"
					+ "<p class='highlight'><em><strong>Note:</strong> This is an automated message. Please do not reply to this email.</em></p>"
					+ "</div>" + "<footer>" + "<p>Thanks and regards,</p>" + "<p>Your Food Check List Team</p>"
					+ "<p>Have a nice day!</p>" + "</footer>" + "</div>" + "</body></html>";

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			helper.setTo(userDto.getEmail());
			helper.setSubject("Email Verification");
			helper.setText(htmlContent, true);
			logger.debug("Sending email to: {}", userDto.getEmail());
			mailSender.send(mimeMessage);

			redirectAttributes.addAttribute("email", userDto.getEmail());
			redirectAttributes.addAttribute("phoneNumber", userDto.getPhoneNumber());
			return "redirect:/userDetails/verify";
			/*
			 * model.addAttribute("email", userDto.getEmail());
			 * model.addAttribute("phoneNumber", userDto.getPhoneNumber()); return "verify";
			 */

		} catch (Exception e) {
			logger.error("Error during registration process: {}", e.getMessage(), e);
			model.addAttribute("errorMessage", "An error occurred during registration. Please try again.");
			return "register";
		}
	}

	@GetMapping("/verify")
	public String verify(@RequestParam String email, @RequestParam String phoneNumber, Model model) {
		logger.debug("VerifyPage Hit for Email: {} and PhoneNumber: {}", email, phoneNumber);
		model.addAttribute("email", email);
		model.addAttribute("phoneNumber", phoneNumber);
		return "verify";
	}

	@PostMapping("/verifyCode")
	public String verifySmsCode(@RequestParam String phoneNumber, @RequestParam String email, @RequestParam String code,
			Model model) {
		logger.debug("Verifying code for email: {}", email);
		// boolean isValid = twilioService.verifySms(phoneNumber, code);
		boolean isValid = userService.verifyCode(email, code);
		if (isValid) {
			logger.debug("Eamil verification successful for Email: {}", email);

			return "login";
		} else {
			logger.debug("Invalid code for Email: {}", email);
			model.addAttribute("error", "Invalid OTP");
			return "verify";
		}
	}

	private String generateToken() {
		// Generate a secure token for OTP
		String token = UUID.randomUUID().toString();
		logger.debug("Generated Token: {}", token);
		return token;
	}

	@RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
	public String forgotPassword(Model model) {
		logger.debug("ForgotPassword Page Hit");
		model.addAttribute("userDto", new UserDto());
		return "forgotPassword";
	}

	@RequestMapping(value = "/forgotPassword2", method = RequestMethod.POST)
	public String processForgotPasswordVerify1(@RequestParam("username") String username, @RequestParam String email,
			Model model, RedirectAttributes redirectAttributes) {
		logger.debug("Email: {}", email);
		logger.debug("Process Forgot Password for User: {}", username);
		User user = userService.findByUsername(username);

		if (user != null) {
			boolean isValid = userService.getResetPasswordOTP(username, email);
			if (isValid) {
				logger.debug("Email varification done for User: {}", username);
				model.addAttribute("email", email);
				return "verifyPassword";
			} else {
				logger.debug("Invalid Email for User: {}", username);
				model.addAttribute("error", "Invalid Email by User.");
				return "forgotPassword";
			}
		} else {
			logger.debug("User not found: {}", username);
			model.addAttribute("error", "User not found.");
			return "forgotPassword";
		}
	}

	@RequestMapping(value = "/resetPassword1", method = RequestMethod.POST)
	public String resetPassword(@RequestParam String email, @RequestParam String code, Model model,
			RedirectAttributes redirectAttributes) {
		logger.debug("ResetPassword Page Hit with Token: {}", email);
		boolean isValid = userService.verifyCode(email, code);
		if (isValid) {
			logger.debug("Valid reset token: {}", email);
			model.addAttribute("email", email);
			return "resetPassword";
		} else {
			logger.debug("Invalid or expired reset token: {}", email);
			model.addAttribute("error", "Invalid or expired reset token.");
			return "login";
		}
	}

	@RequestMapping(value = "/resetPassword2", method = RequestMethod.POST)
	public String processResetPassword(@RequestParam("email") String email,
			@RequestParam("newPassword") String newPassword, Model model) {
		logger.debug("Process Reset Password with Token: {}", email);

		logger.debug("Token is valid: {}", email);
		userService.updatePassword(email, newPassword);
		logger.debug("Password has been reset successfully for Token: {}", email);
		model.addAttribute("message", "Password has been reset successfully.");
		return "login";
	}
}

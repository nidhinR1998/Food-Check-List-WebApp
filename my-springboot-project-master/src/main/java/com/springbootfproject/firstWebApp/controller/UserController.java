package com.springbootfproject.firstWebApp.controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
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

import com.springbootfproject.firstWebApp.Util.EncryptionUtil;
import com.springbootfproject.firstWebApp.dto.UserDto;
import com.springbootfproject.firstWebApp.repository.UserRepository;
import com.springbootfproject.firstWebApp.service.TodoService;
import com.springbootfproject.firstWebApp.service.TwilioService;
import com.springbootfproject.firstWebApp.service.UserService;
import com.springbootfproject.firstWebApp.todomodel.User;

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
	private TwilioService twilioService;
	@Autowired
	private final EncryptionUtil encryptionUtil;

	private UserService userService;

	public UserController(UserService userService, EncryptionUtil encryptionUtil) {
		this.encryptionUtil = null;
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
	public String registerSave(@ModelAttribute("user") UserDto userDto, Model model, RedirectAttributes redirectAttributes) {
	    logger.debug("Register Save Hit for User: {}", userDto.getUsername());

	    User existingUser = userService.findByUsername(userDto.getUsername());
	    if (existingUser != null) {
	        logger.debug("User already exists: {}", userDto.getUsername());
	        model.addAttribute("userExists", true);
	        return "register";
	    }

	    Map<String, Object> response = userService.handleUserRegistration(userDto);

	    if (response.containsKey("errorMessage")) {
	        model.addAttribute("errorMessage", response.get("errorMessage"));
	        return "register";
	    }

	    redirectAttributes.addAttribute("email", response.get("email"));
	    redirectAttributes.addAttribute("phoneNumber", response.get("phoneNumber"));
	    redirectAttributes.addAttribute("token", response.get("token"));
	    redirectAttributes.addFlashAttribute("otpMessage", response.get("otpMessage"));

	    return "redirect:/userDetails/verify";
	}


	@GetMapping("/verify")
	public String verify(@RequestParam String email, @RequestParam String phoneNumber,@RequestParam String token, Model model) {
		logger.debug("VerifyPage Hit for Email: {} and PhoneNumber: {}", email, phoneNumber);

		model.addAttribute("email", email);
		model.addAttribute("phoneNumber", phoneNumber);
		model.addAttribute("token", token);
		return "verify";
	}

	@PostMapping("/verifyCode")
    public String verifySmsCode(@RequestParam String phoneNumber, @RequestParam String email, @RequestParam String code, @RequestParam String token,
                                Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Verifying code for email: {}", email);

        boolean isValid = false;

        try {
            
            String decryptedEmail = encryptionUtil.decrypt(email);
            String decryptedPhoneNumber = encryptionUtil.decrypt(phoneNumber);
            String decryptedToken = encryptionUtil.decrypt(token);

            if (code.length() == 6) {
                logger.debug("Verifying with Twilio service for phone number: {}", decryptedPhoneNumber);
                isValid = twilioService.verifySms(decryptedPhoneNumber, code);
            } else if (code.length() == 4) {
                logger.debug("Verifying with User service for email: {}", decryptedEmail);
                isValid = userService.verifyCode(decryptedEmail, code);
            } else {
                logger.debug("Invalid code length for Email: {}", decryptedEmail);
                redirectAttributes.addFlashAttribute("error", "Invalid OTP length");

                // Encrypting data
                String encryptedEmail = encryptionUtil.encrypt(decryptedEmail);
                String encryptedPhoneNumber = encryptionUtil.encrypt(decryptedPhoneNumber);
                String encryptedToken = encryptionUtil.encrypt(decryptedToken);

                redirectAttributes.addAttribute("email", encryptedEmail);
                redirectAttributes.addAttribute("phoneNumber", encryptedPhoneNumber);
                redirectAttributes.addAttribute("token", encryptedToken);
                return "redirect:/userDetails/verify";
            }
        } catch (Exception e) {
            logger.error("Error during code verification for email: {}", email, e);
            redirectAttributes.addFlashAttribute("error", "An error occurred during verification. Please try again.");

            // Encrypting data
            try {
                String encryptedEmail = encryptionUtil.encrypt(email);
                String encryptedPhoneNumber = encryptionUtil.encrypt(phoneNumber);
                String encryptedToken = encryptionUtil.encrypt(token);

                redirectAttributes.addAttribute("email", encryptedEmail);
                redirectAttributes.addAttribute("phoneNumber", encryptedPhoneNumber);
                redirectAttributes.addAttribute("token", encryptedToken);
            } catch (Exception ex) {
                logger.error("Error encrypting data: {}", ex.getMessage(), ex);
            }
            return "redirect:/userDetails/verify";
        }

	    if (isValid) {
	        logger.debug("Email/PhoneNo verification successful for user: {}", email);
	        redirectAttributes.addFlashAttribute("success", "Email/PhoneNo verification successful");
	        return "redirect:/login";
	    } else {
	        logger.debug("Invalid code for Email: {}", email);
	        
	        try {
                String encryptedEmail = encryptionUtil.encrypt(email);
                String encryptedPhoneNumber = encryptionUtil.encrypt(phoneNumber);
                String encryptedToken = encryptionUtil.encrypt(token);

                redirectAttributes.addAttribute("email", encryptedEmail);
                redirectAttributes.addAttribute("phoneNumber", encryptedPhoneNumber);
                redirectAttributes.addAttribute("token", encryptedToken);
            } catch (Exception ex) {
                logger.error("Error encrypting data: {}", ex.getMessage(), ex);
            }
	        return "redirect:/userDetails/verify";
	    }
	}


	private String generateToken() {
		
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

package com.springbootfproject.firstWebApp.controller;

import java.security.Principal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
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
import com.springbootfproject.firstWebApp.service.TodoService;
import com.springbootfproject.firstWebApp.service.UserService;
import com.springbootfproject.firstWebApp.todomodel.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/userDetails")
@SessionAttributes("username")
public class UserController {
	@Autowired
	private JavaMailSender mailSender;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private TodoService service;

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

		if (!userService.isValidSecurityQuestionAnswer(userDto.getSecurityQuestion(), userDto.getSecurityAnswer())) {
			logger.debug("Invalid Security Question or Answer for User: {}", userDto.getUsername());
			model.addAttribute("securityQuestionError", true);
			return "register";
		}

		userService.save(userDto);
		logger.debug("User registered successfully: {}", userDto.getUsername());

		redirectAttributes.addAttribute("success", "true");
		return "redirect:/register";
	}

	@RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
	public String forgotPassword(Model model) {
		logger.debug("ForgotPassword Page Hit");
		model.addAttribute("userDto", new UserDto());
		return "forgotPassword";
	}

//	@RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
	public String processForgotPassword(@RequestParam("username") String username, Model model) {
		logger.debug("Process Forgot Password for User: {}", username);
		User user = userService.findByUsername(username);
		if (user != null) {
			logger.debug("User found: {}", username);
			model.addAttribute("username", username);
			model.addAttribute("securityQuestion", user.getSecurityQuestion());
			logger.debug("User Security Question: {}", user.getSecurityQuestion());
			return "forgotPassword";
		} else {
			logger.debug("User not found: {}", username);
			model.addAttribute("error", "Invalid username.");
			return "forgotPassword";
		}
	}
	
	@RequestMapping(value = "/forgotPassword2", method = RequestMethod.POST)
	public String processForgotPasswordVerify1(@RequestParam("username") String username,
	                                           @RequestParam("securityAnswer") String securityAnswer,
	                                           @RequestParam("securityQuestion") String securityQuestion, Model model) {

	    logger.debug("Process Forgot Password for User: {}", username);
	    User user = userService.findByUsername(username);

	    if (user != null) {
	        boolean isValid = userService.isSecurityQuestionAnswerValid(username, securityAnswer, securityQuestion);
	        if (isValid) {
	            return "forgotPassword";
	        } else {
	            logger.debug("Invalid security answer for User: {}", username);
	            model.addAttribute("error", "Invalid security answer.");
	            return "forgotPassword";
	        }
	    } else {
	        logger.debug("User not found: {}", username);
	        model.addAttribute("error", "User not found.");
	        return "forgotPassword";
	    }
	}


//	@RequestMapping(value = "/forgotPasswordVerify", method = RequestMethod.POST)
	public String processForgotPasswordVerify(@RequestParam("username") String username,
			@RequestParam("securityAnswer") String securityAnswer, Model model) {
		logger.debug("Process Forgot Password Verify for User: {}", username);
		User user = userService.findByUsername(username);
		if (!userService.isSecurityQuestionAnswerValid(username, username, securityAnswer)) {
			return "resetPassword";

		} else {
			logger.debug("Invalid security answer for User: {}", username);
			model.addAttribute("error", "Invalid security answer.");
			return "forgotPassword";
		}
	}

	@RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
	public String resetPassword(@RequestParam("token") String token, Model model) {
	    logger.debug("ResetPassword Page Hit with Token: {}", token);
	    if (userService.isResetTokenValid(token)) {
	        model.addAttribute("token", token);
	        return "resetPassword";
	    } else {
	        model.addAttribute("error", "Invalid or expired reset token.");
	        return "login";
	    }
	}

	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	public String processResetPassword(@RequestParam("token") String token,
	                                   @RequestParam("newPassword") String newPassword, Model model) {
	    logger.debug("Process Reset Password with Token: {}", token);
	    boolean isTokenValid = userService.isResetTokenValid(token);

	    if (isTokenValid) {
	        logger.debug("Token is valid: {}", token);
	        userService.updatePassword(token, newPassword);
	        model.addAttribute("message", "Password has been reset successfully.");
	        return "login";
	    } else {
	        logger.debug("Invalid or expired reset token: {}", token);
	        model.addAttribute("error", "Invalid or expired reset token.");
	        return "resetPassword";
	    }
	}
}

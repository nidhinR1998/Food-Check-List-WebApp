package com.springbootfproject.firstWebApp.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

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
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private TodoService service;
	
	private UserService userService;
	public UserController(UserService userService) {
	
		this.userService = userService;
	}

	@RequestMapping(value="/home", method = RequestMethod.GET)
	public String home(Model model, Principal principal) {
	    model.addAttribute("username", getLoggedinUsername());
	    return "home";
	}

	private String getLoggedinUsername() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName();
	    String fullname = service.getFullName(username);
	    return username;
	}

	@RequestMapping(value="/login", method = RequestMethod.GET)
	public String login(Model model, UserDto userDto) {
		model.addAttribute("user", userDto);
		return "login";
	}
	
	@RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/login?logout";
    }
	
	
	@GetMapping("/register")
	public String register(Model model, UserDto userDto) {
		
		model.addAttribute("user", userDto);
		return "register";
	}
	
	@PostMapping("/register")
	public String registerSave(@ModelAttribute("user") UserDto userDto, Model model) {
		User user = userService.findByUsername(userDto.getUsername());
		if (user != null) {
			model.addAttribute("userexist", user);
			return "register";
			
		}
		userService.save(userDto);
		return "redirect:/register?success";
	}

}

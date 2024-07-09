package com.springbootfproject.firstWebApp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

//@Controller
@RequestMapping("/welcome")
@SessionAttributes("name")
public class WelcomeController {
	/*
	 * private loginAuthenticationService authenticateService;
	 * 
	 * public logginController(loginAuthenticationService authenticateService) {
	 * super(); this.authenticateService = authenticateService; }
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	@RequestMapping(value = "/", method = RequestMethod.GET)
	private String gotoWelcomepage(ModelMap model) {
      model.put("name", getLoggedinUsername());
		return "welcome";
	}
	
	private String getLoggedinUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	/* USER FOR TESTING
	 * @RequestMapping(value = "login", method = RequestMethod.POST) private String
	 * toWelcome(@RequestParam String name, @RequestParam String password, ModelMap
	 * model) {
	 * 
	 * if (authenticateService.authenticate(name, password)) { model.put("name",
	 * name); model.put("password", password);
	 * logger.info("Login Succes with username: {}", name); return "welcome";
	 * 
	 * }
	 * 
	 * model.put("errorMessage", "Invalid Credential! Please Login again!");
	 * logger.info("Login failed with username:{}",name); return "login"; }
	 */

}

package com.springbootfproject.firstWebApp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.springbootfproject.firstWebApp.Util.ConstantsUtil;
import com.springbootfproject.firstWebApp.repository.UserRepository;
import com.springbootfproject.firstWebApp.todomodel.User;

@Service
public class ScheduledTaskService {
	@Value("${application.base-url}")
	private String baseUrl;
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
     
 //  @Scheduled(cron = "0 */2 * * * ?") // This runs every 2 minutes

   @Scheduled(cron = "0 0 17 * * ?") // Runs every day at 5 PM
   public void sendDailyFoodUpdateReminder() {
	    List<User> users = userRepository.findAll(); 
	    String appLink = baseUrl;
	    String subject = ConstantsUtil.REMINDER_SUB;
	   // String subject = "Invitation: Register for Food Check List Application";

	    for (User user : users) {
	        String email = user.getEmail();
	        String fullName = user.getFullname();
	        String htmlContent = ConstantsUtil.DAILY_FOOD_UPDATE_REMINDER_CONTENT
	                .replace("{fullName}", fullName)
	                .replace("{appLink}", appLink);
	        	        
	        emailService.sendEmail(email, subject, htmlContent);
	        
			
	       
	    }
	}
}
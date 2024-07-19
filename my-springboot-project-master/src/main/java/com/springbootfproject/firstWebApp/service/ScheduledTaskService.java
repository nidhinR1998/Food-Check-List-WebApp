package com.springbootfproject.firstWebApp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
	    String subject = "Reminder: Update Your Food Track";
	   // String subject = "Invitation: Register for Food Check List Application";

	    for (User user : users) {
	        String email = user.getEmail();
	        String fullName = user.getFullname();
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
                    + "<h1>Hi <strong>" + fullName + "</strong>,</h1>"
                    + "<span class='reminder-tag'>Reminder</span>"
                    + "<p>Please update your food track for today on the following link:</p>"
                    + "<p><a href=\"" + appLink + "\">Food Check List</a></p>"
                    + "<div class='info-box'>"
                    + "<p>If you have already updated your food track, please ignore this email.</p>"
                    + "<p class='highlight'><em><strong>Note:</strong> This is an automated message. Please do not reply to this email.</em></p>"                    
                    + "</div>"
                    + "<footer>"
                    + "<p>Thanks and regards,</p>"
                    + "<p>Your Food Check List Team</p>"
                    + "<p>Have a nice day!</p>"
                    + "</footer>"
                    + "</div>"
                    + "</body></html>";
	        
	        
	        emailService.sendEmail(email, subject, htmlContent);
	        
			/* For New registration Mail */
	        
			/*
			 * String htmlContent ="<html>" + "<head><style>" +
			 * "body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; color: #333; }"
			 * +
			 * ".container { width: 80%; margin: auto; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }"
			 * + "h1 { color: #4CAF50; font-size: 24px; }" +
			 * "p { font-size: 16px; line-height: 1.5; margin: 10px 0; font-weight: bold; }"
			 * +
			 * "a { color: #fff; background-color: #5bc0de; padding: 10px 20px; text-decoration: none; font-weight: bold; border-radius: 5px; display: inline-block; font-size: 18px; }"
			 * + "a:hover { background-color: #31b0d5; }" +
			 * ".highlight { background-color: #eaf3fc; padding: 10px; border-radius: 5px; }"
			 * +
			 * "footer { font-size: 14px; color: #777; margin-top: 20px; font-weight: bold; }"
			 * + "</style></head>" + "<body>" + "<div class='container'>" +
			 * "<h1>Hi <strong>" + fullName + "</strong>,</h1>" +
			 * "<p>We are excited to invite you to register for our <strong>Food Check List</strong> application.</p>"
			 * +
			 * "<p>You can add your daily food details along with the cost using the following link:</p>"
			 * + "<p><a href=\"" + appLink + "\">Food Check List</a></p>" +
			 * "<p class='highlight'><em>Please note that you will receive a reminder email every day at 9 PM to fill in your daily food details.</em></p>"
			 * +
			 * "<p><em><strong>Note:</strong> This is an automated message. Please do not reply to this email.</em></p>"
			 * + "<footer>" + "<p>Thanks and regards,</p>" +
			 * "<p>Your Food Check List Team</p>" + "<p>Have a nice day!</p>" + "</footer>"
			 * + "</div>" + "</body></html>";
			 */
	       
	    }
	}
}
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
     
//   @Scheduled(cron = "0 */2 * * * ?") // This runs every 2 minutes

   @Scheduled(cron = "0 0 21 * * ?") // Runs every day at 9 PM
   public void sendDailyFoodUpdateReminder() {
	    List<User> users = userRepository.findAll(); 
	    String appLink = baseUrl;
	    String subject = "Reminder: Update Your Food Track";

	    for (User user : users) {
	        String email = user.getEmail();
	        String fullName = user.getFullname();
	        String htmlContent = "<html>"
	                            + "<head><style>"
	                            + "body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; color: #333; }"
	                            + ".container { width: 80%; margin: auto; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }"
	                            + "h1 { color: #4CAF50; font-size: 24px; }"
	                            + "p { font-size: 16px; line-height: 1.5; margin: 10px 0; }"
	                            + "a { color: #4CAF50; text-decoration: none; font-weight: bold; }"
	                            + "a:hover { text-decoration: underline; }"
	                            + "footer { font-size: 14px; color: #777; margin-top: 20px; }"
	                            + "</style></head>"
	                            + "<body>"
	                            + "<div class='container'>"
	                            + "<h1>Hi <strong>" + fullName + "</strong>,</h1>"
	                            + "<p>Please update your food track for today on the following link:</p>"
	                            + "<p><a href=\"" + appLink + "\">Food Check List</a></p>"
	                            + "<p><em>If you have already updated your food track, please ignore this email.</em></p>"
	                            + "<p><em><strong>Note:</strong> This is an automated message. Please do not reply to this email.</em></p>"
	                            + "<footer>"
	                            + "<p>Thanks and regards,</p>"
	                            + "<p>Your Food Check List Team</p>"
	                            + "<p>Have a nice day!</p>"
	                            + "</footer>"
	                            + "</div>"
	                            + "</body></html>";

	        emailService.sendEmail(email, subject, htmlContent);
	    }
	}
}
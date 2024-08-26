package com.springbootfproject.firstWebApp.dummy;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DummyScheduledTasks {

	private final RestTemplate restTemplate = new RestTemplate();
	private static final String URL = "https://foodchecklistapplication.onrender.com/actuator/health"; 
																																																			
	@Scheduled(fixedRate = 120000)
	public void keepAppAlive() {
		try {
			restTemplate.getForObject(URL, String.class);
			System.out.println("Hi request sent to: " + URL);
		} catch (Exception e) {
			System.err.println("Error sending keep-alive request: " + e.getMessage());
		}
	}
}

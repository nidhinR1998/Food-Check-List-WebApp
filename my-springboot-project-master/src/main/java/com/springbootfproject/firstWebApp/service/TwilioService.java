package com.springbootfproject.firstWebApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.springbootfproject.firstWebApp.repository.UserRepository;
import com.springbootfproject.firstWebApp.todomodel.User;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

@Service
public class TwilioService {

	private static final Logger logger = LoggerFactory.getLogger(TwilioService.class);

	@Autowired
	private UserRepository userRepository;

	@Value("${twilio.account.sid}")
	private String accountSid;

	@Value("${twilio.auth.token}")
	private String authToken;

	@Value("${twilio.verify.service.sid}")
	private String verifyServiceSid;

	public void sendSms(String phoneNumber) {
		Twilio.init(accountSid, authToken);

		if (!phoneNumber.startsWith("+91")) {
			phoneNumber = "+91" + phoneNumber;
		}

		try {
			Verification verification = Verification.creator(verifyServiceSid, phoneNumber, "sms").create();
			logger.debug("SMS sent to phone number: {}", phoneNumber);
		} catch (ApiException e) {
			logger.error("Error sending SMS to phone number: {}", phoneNumber, e);
			throw e;
		}
	}

	public boolean verifySms(String phoneNumber, String code) {
		User user = userRepository.findByPhoneNumber(phoneNumber);
		Twilio.init(accountSid, authToken);

		try {
			if (!phoneNumber.startsWith("+91")) {
				phoneNumber = "+91" + phoneNumber;
			}

			VerificationCheck verificationCheck = VerificationCheck.creator(verifyServiceSid, code).setTo(phoneNumber)
					.create();
			boolean isValid = "approved".equals(verificationCheck.getStatus());			
			if (isValid) {
				user.setOtpCode(0);
				user.setResetToken(null);
				userRepository.save(user);
				logger.debug("After verification, token and code removed for user: {}", user.getUsername());
			}
			logger.debug("Verification status for phone number {}: {}", phoneNumber, verificationCheck.getStatus());
			return isValid;
		} catch (ApiException e) {
			logger.error("Error verifying code for phone number: {}", phoneNumber, e);
			throw e;
		}
	}
}

package com.springbootfproject.firstWebApp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

@Service
public class TwilioService {

	@Value("${twilio.account.sid}")
	private String accountSid;

	@Value("${twilio.auth.token}")
	private String authToken;

	@Value("${twilio.verify.service.sid}")
	private String verifyServiceSid;

	public void sendSms(String phoneNumber) {
		Twilio.init(accountSid, authToken);

		Verification verification = Verification.creator(verifyServiceSid, phoneNumber, "sms").create();
	}

	public boolean verifySms(String phoneNumber, String code) {
		Twilio.init(accountSid, authToken);

		VerificationCheck verificationCheck = VerificationCheck.creator(verifyServiceSid, code).setTo(phoneNumber)
				.create();

		return "approved".equals(verificationCheck.getStatus());
	}
}

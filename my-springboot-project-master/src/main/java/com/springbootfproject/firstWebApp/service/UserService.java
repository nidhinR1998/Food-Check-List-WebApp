package com.springbootfproject.firstWebApp.service;

import java.util.Map;

import com.springbootfproject.firstWebApp.dto.UserDto;
import com.springbootfproject.firstWebApp.todomodel.User;

public interface UserService {
	
	User findByUsername(String username);
	User save2 (UserDto userDto);
	User save (UserDto userDto);
	User save(User user);
	void updateTokenByEmail(String email, String token);
    void updateTokenByPhoneNumber(String phoneNumber, String token);
	boolean getResetPasswordOTP(String username,String email);
	boolean isResetTokenValid(String token);
	void updatePassword(String email,String newPassword);	
	boolean isValidSecurityQuestionAnswer(String securityQuestion, String securityAnswer);
	boolean verifyCode(String email, String code);
	Map<String, Object> handleUserRegistration(UserDto userDto);

}
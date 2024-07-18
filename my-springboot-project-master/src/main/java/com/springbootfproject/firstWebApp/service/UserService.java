package com.springbootfproject.firstWebApp.service;

import com.springbootfproject.firstWebApp.dto.UserDto;
import com.springbootfproject.firstWebApp.todomodel.User;

public interface UserService {
	
	User findByUsername(String username);
	User save2 (UserDto userDto);
	User save (UserDto userDto);
	User save(User user);
	//boolean isValidSecurityQuestionAnswer(String question, String answer);
	boolean isSecurityQuestionAnswerValid(String username,String securityAnswer, String securityQuestion);
	boolean isResetTokenValid(String token);
	void updatePassword(String token, String newPassword);
	//void save(User user);
	boolean isValidSecurityQuestionAnswer(String securityQuestion, String securityAnswer);

}
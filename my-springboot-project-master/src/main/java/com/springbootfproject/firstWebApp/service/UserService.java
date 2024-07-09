package com.springbootfproject.firstWebApp.service;

import com.springbootfproject.firstWebApp.dto.UserDto;
import com.springbootfproject.firstWebApp.todomodel.User;

public interface UserService {
	
	User findByUsername(String username);
	User save (UserDto userDto);

}
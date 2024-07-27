package com.springbootfproject.firstWebApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springbootfproject.firstWebApp.todomodel.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	User findByUsername (String username);
	User findByResetToken(String resetToken);
	User findByEmail(String email);
	User findByPhoneNumber(String phoneNumber);
	User findByOtpCode(int otpCode);

}
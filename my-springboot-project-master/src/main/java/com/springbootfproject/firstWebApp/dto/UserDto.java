package com.springbootfproject.firstWebApp.dto;

public class UserDto {
    private String fullname;
    private String username;
    private String password;
    private String email;
    private String securityQuestion;
    private String securityAnswer;
    private String resetToken;// Add this field with getter and setter

    // Constructors, getters, setters
    public UserDto() {
    }

    public UserDto(String fullname, String username, String password,String email, String securityQuestion, String securityAnswer, String resetToken) {
        this.fullname = fullname;
        this.username = username;
        this.password = password;
        this.email = email;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.resetToken = resetToken;
    }

    // Getters and setters
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

	public String getResetToken() {
		return resetToken;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
    
}

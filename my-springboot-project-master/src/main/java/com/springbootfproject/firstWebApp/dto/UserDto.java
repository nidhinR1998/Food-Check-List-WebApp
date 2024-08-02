package com.springbootfproject.firstWebApp.dto;

public class UserDto {
    private String fullname;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String resetToken;// Token for resetPassword
    private int otpCode;

    // Constructors, getters, setters
    public UserDto() {
    }
    
    public UserDto(String fullname, String username, String password,String phoneNumber,String email, String resetToken, int otpCode) {
        this.fullname = fullname;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.resetToken = resetToken;
        this.otpCode = otpCode;
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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public int getOtpCode() {
		return otpCode;
	}

	public void setOtpCode(int otpCode) {
		this.otpCode = otpCode;
	}
	
    
}

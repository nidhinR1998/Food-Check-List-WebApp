package com.springbootfproject.firstWebApp.dto;

public class PaymentRequest {
    private double amount;
    private String username;
    

    // Getters and Setters
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
    
}

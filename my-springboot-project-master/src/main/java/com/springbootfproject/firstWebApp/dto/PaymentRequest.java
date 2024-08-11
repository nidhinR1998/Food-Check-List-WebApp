package com.springbootfproject.firstWebApp.dto;

public class PaymentRequest {
    private double amount;
    private String username;
    private double updateAdvance;
    

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

	public double getUpdateAdvance() {
		return updateAdvance;
	}

	public void setUpdateAdvance(double updateAdvance) {
		this.updateAdvance = updateAdvance;
	}

	
    
	
}

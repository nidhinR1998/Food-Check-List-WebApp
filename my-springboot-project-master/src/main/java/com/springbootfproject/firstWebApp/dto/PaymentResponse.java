package com.springbootfproject.firstWebApp.dto;

public class PaymentResponse {
    private String upiUrl;
    private String qrCodeImage;
    private String transactionId;  // Add this field
    private String message; // Add this field to handle error messages

    public String getUpiUrl() {
        return upiUrl;
    }

    public void setUpiUrl(String upiUrl) {
        this.upiUrl = upiUrl;
    }

    public String getQrCodeImage() {
        return qrCodeImage;
    }

    public void setQrCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

	
}

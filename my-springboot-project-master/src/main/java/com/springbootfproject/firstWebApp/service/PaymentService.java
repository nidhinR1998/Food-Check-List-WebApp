package com.springbootfproject.firstWebApp.service;

import com.springbootfproject.firstWebApp.dto.PaymentRequest;
import com.springbootfproject.firstWebApp.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse generatePaymentRequest(PaymentRequest paymentRequest);
    void updateTransactionStatus(String transactionId,String upiTxnId, String status);
	String getManualConfirmation(String username, double amount);
}

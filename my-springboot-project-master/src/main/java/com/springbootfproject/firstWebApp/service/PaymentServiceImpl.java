package com.springbootfproject.firstWebApp.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.springbootfproject.firstWebApp.Util.ConstantsUtil;
import com.springbootfproject.firstWebApp.Util.PaymentException;
import com.springbootfproject.firstWebApp.dto.PaymentRequest;
import com.springbootfproject.firstWebApp.dto.PaymentResponse;
import com.springbootfproject.firstWebApp.repository.AdvanceRepository;
import com.springbootfproject.firstWebApp.repository.TodoRepository;
import com.springbootfproject.firstWebApp.repository.TransactionRepository;
import com.springbootfproject.firstWebApp.todomodel.Todo;
import com.springbootfproject.firstWebApp.todomodel.Transaction;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TodoRepository todoRepository;
    
    @Autowired
    private AdvanceRepository advRepo;
    

	/*
	 * @Override public PaymentResponse generatePaymentRequest(PaymentRequest
	 * paymentRequest) { PaymentResponse paymentResponse = new PaymentResponse();
	 * try { // Check if there are any pending transactions for the user
	 * List<Transaction> pendingTransactions =
	 * transactionRepository.findByUsernameAndStatus(paymentRequest.getUsername(),
	 * "PENDING"); if (!pendingTransactions.isEmpty()) { throw new
	 * PaymentException("User has pending transactions. Please complete them before proceeding."
	 * ); }
	 * 
	 * // Generate a unique 9-digit transaction ID String uniqueTid =
	 * generateUniqueTransactionId(); String redirectionUrl =
	 * "https://food-check-list.up.railway.app/api/payment/status";
	 * logger.info("Generated redirection URL: {}", redirectionUrl);
	 * 
	 * // Determine the month value String monthValue =
	 * determineMonth(paymentRequest.getUsername());
	 * 
	 * // Set up UPI payment parameters String num = "FOOD-BILL-FOR-THE-MONTH: " +
	 * monthValue; String upiLink = "upi://pay?pa=7356324654@axisb&pn=Sanitha&am=" +
	 * paymentRequest.getAmount() + "&tn=" + num+ "&url=" + redirectionUrl; //
	 * String upiLink = "upi://pay?pa=nidhinrajesh1998-2@okicici&pn=NidhinR&tid=" +
	 * uniqueTid +"&am=" + paymentRequest.getAmount() + "&tn=" + num + "&url=" +
	 * redirectionUrl;
	 * 
	 * // Generate QR code image String qrCodeImage = generateQRCodeImage(upiLink);
	 * 
	 * paymentResponse.setUpiUrl(upiLink);
	 * paymentResponse.setQrCodeImage(qrCodeImage);
	 * logger.info("Generated UPI URL: {}", upiLink);
	 * logger.info("Generated QR Code Image: {}", qrCodeImage);
	 * 
	 * // Save transaction details Transaction transaction = new Transaction();
	 * transaction.setTransactionId(uniqueTid);
	 * transaction.setUsername(paymentRequest.getUsername());
	 * transaction.setAmount(paymentRequest.getAmount());
	 * transaction.setUpiUrl(upiLink); transaction.setStatus("PENDING");
	 * transactionRepository.save(transaction);
	 * 
	 * // Log the saved transaction logger.info("Transaction saved: {}",
	 * transaction); } catch (PaymentException e) {
	 * logger.error("Error generating UPI URL", e); throw e; } catch (Exception e) {
	 * logger.error("Error generating UPI URL", e); throw new
	 * PaymentException("Error generating UPI URL", e); }
	 * 
	 * return paymentResponse; }
	 */
    
    @Override
    public PaymentResponse generatePaymentRequest(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse = new PaymentResponse();
        try {
            // Check if there are any pending transactions for the user
            List<Transaction> pendingTransactions = transactionRepository.findByUsernameAndStatus(paymentRequest.getUsername(), ConstantsUtil.PENDING_STATUS);
            if (!pendingTransactions.isEmpty()) {
                throw new PaymentException("User has pending transactions. Please complete them before proceeding.");
            }

            // Generate a unique 9-digit transaction ID
            String uniqueTid = generateUniqueTransactionId();
            String redirectionUrl = ConstantsUtil.REDIRECTION_URL;
            logger.info("Generated redirection URL: {}", redirectionUrl);

            // Determine the month value
            String monthValue = determineMonth(paymentRequest.getUsername());

            // Set up UPI payment parameters
            String num = ConstantsUtil.UPI_NUM + monthValue;
            double amount = paymentRequest.getAmount();
            String payee = ConstantsUtil.PAYEE;
            String payeeName = ConstantsUtil.PAYEE_NAME;

            String googlePayLink = ConstantsUtil.GOOGLE_PAY_SCHEME + payee + "&pn=" + payeeName + "&am=" + amount + "&tn=" + num + "&url=" + redirectionUrl;
            String phonePeLink = ConstantsUtil.PHONE_PE_SCHEME + payee + "&pn=" + payeeName + "&am=" + amount + "&tn=" + num + "&url=" + redirectionUrl;

            // Generate QR code image
            String qrCodeImage = generateQRCodeImage(googlePayLink); // Use Google Pay link for QR code

            paymentResponse.setGooglePayUrl(googlePayLink);
            paymentResponse.setPhonePeUrl(phonePeLink);
            paymentResponse.setQrCodeImage(qrCodeImage);
            logger.info("Generated Google Pay URL: {}", googlePayLink);
            logger.info("Generated PhonePe URL: {}", phonePeLink);
            logger.info("Generated QR Code Image: {}", qrCodeImage);

            // Save transaction details
            Transaction transaction = new Transaction();
            transaction.setTransactionId(uniqueTid);
            transaction.setUsername(paymentRequest.getUsername());
            transaction.setAmount(paymentRequest.getAmount());
            transaction.setUpiUrl(googlePayLink); 
            transaction.setStatus(ConstantsUtil.PENDING_STATUS);
            transactionRepository.save(transaction);
            
            // Log the saved transaction
            logger.info("Transaction saved: {}", transaction);
        } catch (PaymentException e) {
            logger.error(ConstantsUtil.ERROR_GENERATING_UPI_URL, e);
            throw e;
        } catch (Exception e) {
            logger.error(ConstantsUtil.ERROR_GENERATING_UPI_URL, e);
            throw new PaymentException(ConstantsUtil.ERROR_GENERATING_UPI_URL, e);
        }

        return paymentResponse;
    }


    private String generateUniqueTransactionId() {
        Random random = new Random();
        int uniqueTid;
        do {
            uniqueTid = 100000000 + random.nextInt(900000000); // Generate a 9-digit number
        } while (transactionRepository.existsByTransactionId(String.valueOf(uniqueTid)));
        return String.valueOf(uniqueTid);
    }

	private String generateQRCodeImage(String upiUrl) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(upiUrl, BarcodeFormat.QR_CODE, 200, 200);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        return Base64.getEncoder().encodeToString(pngData);
    }

    private String determineMonth(String username) {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.getDayOfMonth() >= 1 && currentDate.getDayOfMonth() <= 5) {
            LocalDate startDate = currentDate.minusDays(30);
            List<Todo> todos = todoRepository.findByUsername(username)
                                             .stream()
                                             .filter(todo -> !todo.getTargetDate().isBefore(startDate))
                                             .collect(Collectors.toList());

            if (!todos.isEmpty()) {
                List<Integer> months = todos.stream()
                                            .map(todo -> todo.getTargetDate().getMonthValue())
                                            .collect(Collectors.toList());
                
                int mostCommonMonth = months.stream()
                                            .reduce((a, b) -> 
                                                months.stream().filter(v -> v == a).count() > 
                                                months.stream().filter(v -> v == b).count() ? a : b)
                                            .orElse(currentDate.getMonthValue());
                
                return LocalDate.of(currentDate.getYear(), mostCommonMonth, 1).getMonth().name();
            } else {
                return currentDate.minusMonths(1).getMonth().name();
            }
        } else {
            return currentDate.getMonth().name();
        }
    }

    @Override
    public void updateTransactionStatus(String transactionId, String upiTxnId, String status, String updateAdvance, String username) {
        // Retrieve the transaction using the generated transactionId
        Transaction transaction = transactionRepository.findByTransactionId(transactionId);
        if (transaction != null) {
            logger.debug("Current transaction status: {}, New status: {}", transaction.getStatus(), status);

            // Save the upiTxnId if not already saved
            if (transaction.getUpiTxnId() == null) {
                transaction.setUpiTxnId(upiTxnId);
            }

            // Update the status only if it's different
            if (!transaction.getStatus().equalsIgnoreCase(status)) {
                transaction.setStatus(status);
                transactionRepository.save(transaction);
                logger.info("Transaction status updated to: {}", status);

                // Retrieve the updated transaction to ensure changes are persisted
                Transaction updatedTransaction = transactionRepository.findByTransactionId(transactionId);
                logger.debug("Updated transaction status: {}", updatedTransaction.getStatus());

                // Check if the new status is 'success' and the previous status was 'PENDING'
                if (updatedTransaction.getStatus().equals(ConstantsUtil.SUCCESS_STATUS)) {
                    logger.debug("All the Food Entries will be deleted for the username: {}", transaction.getUsername());

                    // Perform the deletion and updating the Remaining Advance
                    long deletedRecords = todoRepository.deleteByUsername(transaction.getUsername());
                    double updateAdvanceDouble = Double.parseDouble(updateAdvance);
                    int updateAdvanceInt = (int) updateAdvanceDouble;
           
                    advRepo.updateTotalAdvanceAmount(updateAdvanceInt, username);
                    
                    logger.debug("{} records deleted for the username: {}", deletedRecords, transaction.getUsername());
                } else {
                    logger.debug("No deletion needed. Transaction status: {}", updatedTransaction.getStatus());
                }
            } else {
                logger.info("Transaction status is already: {}", status);
            }
        } else {
            throw new PaymentException("Transaction not found");
        }
    }

	@Override
	public String getManualConfirmation(String username, double amount, double updateAdvance) {
		
		Transaction pendingTransactions = transactionRepository.getByUsernameAndStatus(username, ConstantsUtil.PENDING_STATUS);
		
		String transactionId = pendingTransactions.getTransactionId();
		return transactionId;
	}

}

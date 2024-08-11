package com.springbootfproject.firstWebApp.controller;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springbootfproject.firstWebApp.Util.PaymentException;
import com.springbootfproject.firstWebApp.dto.PaymentRequest;
import com.springbootfproject.firstWebApp.dto.PaymentResponse;
import com.springbootfproject.firstWebApp.repository.AdvanceRepository;
import com.springbootfproject.firstWebApp.repository.TodoRepository;
import com.springbootfproject.firstWebApp.service.PaymentService;
import com.springbootfproject.firstWebApp.service.TodoService;
import com.springbootfproject.firstWebApp.todomodel.Advance;
import com.springbootfproject.firstWebApp.todomodel.Todo;

@Controller
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final TodoRepository todoRepository;
    private AdvanceRepository advMoney;
    private TodoService todoService;

    @Autowired
    public PaymentController(PaymentService paymentService, TodoRepository todoRepository, AdvanceRepository advMoney, TodoService todoService) {
        this.paymentService = paymentService;
        this.todoRepository = todoRepository;
        this.advMoney = advMoney;
        this.todoService = todoService;
    }

    @RequestMapping(value = "/generatePaymentPage", method = RequestMethod.POST)
    public String generatePaymentPage(@RequestParam("totalAdvanceAmount") int totalAdvanceAmount,
                                      @RequestParam("payableAmount") int payableAmount,
                                      Model model, Principal principal) {
        logger.debug("GeneratePaymentPage Hit");
        String username = principal.getName();
        logger.debug("Username: {}", username);
        
        model.addAttribute("totalAdvanceAmount", totalAdvanceAmount);
        model.addAttribute("payableAmount", payableAmount);
        model.addAttribute("username", username);
        
        return "paymentPage";
    }
    
    
	/*
	 * @RequestMapping(value = "/generatePaymentPage", method = RequestMethod.GET)
	 * public String generatePaymentPage(Model model, Principal principal) {
	 * logger.debug("GeneratePaymentPage Hit"); String username =
	 * principal.getName(); logger.debug("Username: {}", username); // Fetch todos
	 * and calculate the consumed amount List<Todo> todos =
	 * todoRepository.findByUsername(username); int consumedAmount =
	 * todos.stream().mapToInt(Todo::getAmount).sum();
	 * 
	 * // Fetch advances and calculate the total advance amount List<Advance>
	 * advances = advMoney.findByUsername(username); int totalAdvanceAmount =
	 * advances.stream().mapToInt(Advance::getAmount).sum();
	 * 
	 * int payableAmount = todoService.getPayAmount(totalAdvanceAmount,
	 * consumedAmount, username); // int totalAmount =
	 * todos.stream().mapToInt(Todo::getAmount).sum();
	 * model.addAttribute("payableAmount", payableAmount);
	 * model.addAttribute("username", username); return "paymentPage"; }
	 */
    

    @RequestMapping(value = "/generatePaymentRequest", method = RequestMethod.POST)
    public ResponseEntity<PaymentResponse> generatePaymentRequest(@RequestBody PaymentRequest paymentRequest) throws UnsupportedEncodingException {
        logger.info("Received payment request for amount: " + paymentRequest.getAmount());

        try {
            PaymentResponse paymentResponse = paymentService.generatePaymentRequest(paymentRequest);
            return ResponseEntity.ok(paymentResponse);
        } catch (PaymentException e) {
            logger.error("Error processing payment request", e);
            // Create a response with the error message
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<String> handlePaymentException(PaymentException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, String>> paymentStatus(
        @RequestParam(value = "transactionId", required = false) String transactionId,
        @RequestParam(value = "upiTxnId", required = false) String upiTxnId,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "updateAdvance", required = false) String updateAdvance,
        @RequestParam(value = "username", required = false) String username,
        @RequestParam(value = "message", required = false) String message) {
        
        Map<String, String> response = new HashMap<>();
        logger.debug("Payment status endpoint hit with transactionId: {}, upiTxnId: {}, status: {}, updateAdvance{}, username{}, message: {}", transactionId, upiTxnId, status, updateAdvance, username, message);
        
        if (transactionId != null && upiTxnId != null) {
            try {
                paymentService.updateTransactionStatus(transactionId, upiTxnId, status, updateAdvance, username );
                response.put("status", "success");
                response.put("message", "Payment status updated successfully");
            } catch (PaymentException e) {
                logger.error("Error updating payment status", e);
                response.put("status", "error");
                response.put("message", e.getMessage());
            }
        } else if (message != null) {
            response.put("status", status);
            response.put("message", message);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid request parameters");
        }
        
        return ResponseEntity.ok(response);
    }

    
    @RequestMapping(value = "/manualConfirmation", method = RequestMethod.POST)
    public String manualConfirmation(@RequestBody PaymentRequest paymentRequest, Model model) {
        String username = paymentRequest.getUsername();
        double amount = paymentRequest.getAmount();
        double updateAdvance = paymentRequest.getUpdateAdvance();
        
        // Process the manual confirmation (e.g., save to database, generate transaction ID, etc.)
        String transactionId = paymentService.getManualConfirmation(paymentRequest.getUsername(), paymentRequest.getAmount(), paymentRequest.getUpdateAdvance()); // Replace with actual transaction ID generation logic
        
        // Add attributes to the model
        model.addAttribute("username", username);
        model.addAttribute("amount", amount);
        model.addAttribute("updateAdvance", updateAdvance);
        model.addAttribute("transactionId", transactionId);
        
        // Return the manualConfirmation view
        return "manualConfirmation";
    }

}

package com.springbootfproject.firstWebApp.controller;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springbootfproject.firstWebApp.Util.PaymentException;
import com.springbootfproject.firstWebApp.dto.PaymentRequest;
import com.springbootfproject.firstWebApp.dto.PaymentResponse;
import com.springbootfproject.firstWebApp.repository.TodoRepository;
import com.springbootfproject.firstWebApp.service.PaymentService;
import com.springbootfproject.firstWebApp.todomodel.Todo;

@Controller
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final TodoRepository todoRepository;

    @Autowired
    public PaymentController(PaymentService paymentService, TodoRepository todoRepository) {
        this.paymentService = paymentService;
        this.todoRepository = todoRepository;
    }

    @RequestMapping(value = "/generatePaymentPage", method = RequestMethod.GET)
    public String generatePaymentPage(Model model, Principal principal) {
        logger.debug("GeneratePaymentPage Hit");
        String username = principal.getName();
        logger.debug("Username: {}", username);
        List<Todo> todos = todoRepository.findByUsername(username);
        int totalAmount = todos.stream().mapToInt(Todo::getAmount).sum();
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("username", username);
        return "paymentPage";
    }

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

    @RequestMapping(value="/status", method = RequestMethod.GET)
    public String paymentStatus(@RequestParam(value = "transactionId", required = false) String transactionId,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "message", required = false) String message,
                                Model model) {
        logger.debug("Payment status endpoint hit with transactionId: {}, status: {}, message: {}", transactionId, status, message);
        if (transactionId != null) {
            try {
                paymentService.updateTransactionStatus(transactionId, status);
                model.addAttribute("status", "success");
                model.addAttribute("message", "Payment status updated successfully");
            } catch (PaymentException e) {
                logger.error("Error updating payment status", e);
                model.addAttribute("status", "error");
                model.addAttribute("message", e.getMessage());
            }
        } else if (message != null) {
            model.addAttribute("status", status);
            model.addAttribute("message", message);
        }
        return "status";
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<String> handlePaymentException1(PaymentException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}

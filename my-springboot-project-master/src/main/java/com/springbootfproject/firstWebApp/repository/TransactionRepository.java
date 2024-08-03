package com.springbootfproject.firstWebApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springbootfproject.firstWebApp.todomodel.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
   
    
    List<Transaction> findByUsernameAndStatus(String username, String status);    
    Transaction findByTransactionId(String transactionId);
    Transaction findByUpiTxnId(String upiTxnId);
    boolean existsByTransactionId(String transactionId);
    boolean existsByUpiTxnId(String upiTxnId);
	Transaction findByUsername(String username);
	Transaction getByUsernameAndStatus(String username, String status);
}
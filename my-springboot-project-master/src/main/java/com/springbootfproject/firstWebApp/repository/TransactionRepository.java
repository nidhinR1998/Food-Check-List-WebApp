package com.springbootfproject.firstWebApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springbootfproject.firstWebApp.todomodel.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findByTransactionId(String transactionId);

    List<Transaction> findByUsernameAndStatus(String username, String status);

    boolean existsByTransactionId(String transactionId);
}
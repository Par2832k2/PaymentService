package com.project.paymentgateway.payment_gateway_service.repository;

import com.project.paymentgateway.payment_gateway_service.dao.Transaction;
import com.project.paymentgateway.payment_gateway_service.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllBySender(User sender);
}

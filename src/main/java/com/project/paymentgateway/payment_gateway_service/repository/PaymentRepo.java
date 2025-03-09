package com.project.paymentgateway.payment_gateway_service.repository;

import com.project.paymentgateway.payment_gateway_service.dao.Payment;
import com.project.paymentgateway.payment_gateway_service.dao.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {
    Payment findByTransaction(Transaction transaction);
}

package com.project.paymentgateway.payment_gateway_service.dao;

import com.project.paymentgateway.payment_gateway_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    @OneToOne
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;
    @Column(name="otp")
    private Integer otp;
    @Column(name="payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // INITIATED, AUTHORIZED, CAPTURED, FAILED
    @Column(name="payment_time")
    private LocalDateTime processedAt;
    @Column(name="failed_transactions_count")
    private int failedTransactionsCount;
}

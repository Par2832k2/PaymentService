package com.project.paymentgateway.payment_gateway_service.dao;

import com.project.paymentgateway.payment_gateway_service.enums.PaymentMethod;
import com.project.paymentgateway.payment_gateway_service.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
    @Column(name="amount", nullable = false)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private TransactionStatus status; // PENDING, SUCCESS, FAILED
    @Enumerated(EnumType.STRING)
    @Column(name="payment_method", nullable = false)
    private PaymentMethod paymentMethod; // CARD, UPI, BANK_TRANSFER
    @Column(name="transaction_time")
    private LocalDateTime transactionTime;
}

package com.project.paymentgateway.payment_gateway_service.dto;

import com.project.paymentgateway.payment_gateway_service.dao.User;
import com.project.paymentgateway.payment_gateway_service.enums.PaymentMethod;
import com.project.paymentgateway.payment_gateway_service.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    @NonNull
    private Long transactionId;
    @NonNull
    private String receiver;
    @NonNull
    private BigDecimal amount;
    @NonNull
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // CARD, UPI, BANK_TRANSFER
    @NonNull
    private LocalDateTime transactionTime;
}

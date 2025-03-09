package com.project.paymentgateway.payment_gateway_service.dto;

import com.project.paymentgateway.payment_gateway_service.enums.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmPaymentResponse {
    private Long transactionId;
    private BigDecimal amountTransferred;
    private BigDecimal availableBalance;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private LocalDateTime processedAt;
}

package com.project.paymentgateway.payment_gateway_service.dto;

import com.project.paymentgateway.payment_gateway_service.enums.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitiatePaymentResponse {
    private Long transactionId;
    private String otp;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}

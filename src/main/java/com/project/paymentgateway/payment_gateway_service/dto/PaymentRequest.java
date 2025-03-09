package com.project.paymentgateway.payment_gateway_service.dto;

import com.project.paymentgateway.payment_gateway_service.dao.Payment;
import com.project.paymentgateway.payment_gateway_service.enums.PaymentMethod;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    @NonNull
    private Long userId;
    @NonNull
    private String paymentReceiver;
    @NonNull
    private BigDecimal amount;
    @NonNull
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
}

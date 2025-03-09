package com.project.paymentgateway.payment_gateway_service.dto;

import com.project.paymentgateway.payment_gateway_service.enums.kycDocumentType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycDto {
    @NonNull
    private Long userId;
    @NonNull
    @Enumerated(EnumType.STRING)
    private kycDocumentType documentType;

}

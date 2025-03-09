package com.project.paymentgateway.payment_gateway_service.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.paymentgateway.payment_gateway_service.enums.kycDocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KYCInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kycId;
    @OneToOne()
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private kycDocumentType documentType;
    @Column(nullable = false, unique = true)
    private String documentId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private com.project.paymentgateway.payment_gateway_service.enums.kycStatus kycStatus;
    @Column
    private LocalDateTime createdAt;
    @Column
    private LocalDateTime updatedAt;
}


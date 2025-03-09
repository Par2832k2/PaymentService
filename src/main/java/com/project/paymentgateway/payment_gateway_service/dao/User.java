package com.project.paymentgateway.payment_gateway_service.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.paymentgateway.payment_gateway_service.enums.Roles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(name="username")
    private String userName;
    @Column(name="fullname")
    private String fullName;
    @Column(name="contact_number")
    private String contactNumber;
    @Column(name="email_id")
    private String emailId;
    @Column(name="password")
    private String password;
    @Column(name="role")
    @Enumerated(EnumType.STRING)
    private Roles role;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private KYCInfo kycInfo;
    @Column(name="last_successful_login")
    private LocalDateTime lastSuccessfulLogin;
    @Column(name="failed_login_count")
    private int failedLoginCount;

    // Helper method to break the relationship
    public void removeKYCInfo() {
        if (this.kycInfo != null) {
            this.kycInfo.setUser(null);
            this.kycInfo = null;
        }
    }
}


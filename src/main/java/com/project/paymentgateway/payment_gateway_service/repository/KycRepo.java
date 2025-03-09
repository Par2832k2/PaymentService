package com.project.paymentgateway.payment_gateway_service.repository;

import com.project.paymentgateway.payment_gateway_service.dao.KYCInfo;
import com.project.paymentgateway.payment_gateway_service.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycRepo extends JpaRepository<KYCInfo, Long> {
    KYCInfo findByDocumentId(String documentId);
    KYCInfo findByUser(User user);
}

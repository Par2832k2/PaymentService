package com.project.paymentgateway.payment_gateway_service.repository;

import com.project.paymentgateway.payment_gateway_service.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    User findByUserName(String userName);
    User findByUserId(Long userID);
}

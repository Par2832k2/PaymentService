package com.project.paymentgateway.payment_gateway_service.controller;

import com.project.paymentgateway.payment_gateway_service.ExceptionHandler.ObjectNotFoundException;
import com.project.paymentgateway.payment_gateway_service.ExceptionHandler.ValidationException;
import com.project.paymentgateway.payment_gateway_service.dao.KYCInfo;
import com.project.paymentgateway.payment_gateway_service.dao.User;
import com.project.paymentgateway.payment_gateway_service.dao.Wallet;
import com.project.paymentgateway.payment_gateway_service.dto.KycDto;
import com.project.paymentgateway.payment_gateway_service.service.PaymentService;
import com.project.paymentgateway.payment_gateway_service.service.TokenService;
import com.project.paymentgateway.payment_gateway_service.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final PaymentService paymentService;

    public UserController(UserService userService, TokenService tokenService, PaymentService paymentService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public User registerUser(@RequestBody User user){
        return userService.createUser(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllUsers")
    public List<User> retrieveAllUsers(HttpServletRequest request) throws JwtException {
        tokenService.validateToken(request);
        return userService.retrieveAllUsers();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping("/updateUser")
    public User updateUser(@RequestBody User user, HttpServletRequest request) throws ValidationException, ObjectNotFoundException, AuthorizationServiceException {
        return userService.updateUser(user, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/removeUser/{userId}")
    public void removeUser(@PathVariable Long userId) throws ObjectNotFoundException {
        userService.deleteUser(userId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/kycDetails")
    public KYCInfo registerKYC(@RequestBody KycDto kycDto) throws ObjectNotFoundException {
        return userService.registerKyc(kycDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/setupWallet/{userId}/{money}")
    public Wallet setupWallet(@PathVariable Long userId, @PathVariable BigDecimal money) throws ValidationException {
        return userService.setupWallet(userId, money);
    }
}

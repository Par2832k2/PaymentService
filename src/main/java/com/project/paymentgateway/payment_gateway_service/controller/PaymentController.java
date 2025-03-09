package com.project.paymentgateway.payment_gateway_service.controller;

import com.project.paymentgateway.payment_gateway_service.ExceptionHandler.ObjectNotFoundException;
import com.project.paymentgateway.payment_gateway_service.ExceptionHandler.PaymentProcessingException;
import com.project.paymentgateway.payment_gateway_service.ExceptionHandler.ValidationException;
import com.project.paymentgateway.payment_gateway_service.dto.ConfirmPaymentResponse;
import com.project.paymentgateway.payment_gateway_service.dto.InitiatePaymentResponse;
import com.project.paymentgateway.payment_gateway_service.dto.PaymentRequest;
import com.project.paymentgateway.payment_gateway_service.dto.TransactionDto;
import com.project.paymentgateway.payment_gateway_service.service.PaymentService;
import com.project.paymentgateway.payment_gateway_service.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final TokenService tokenService;

    public PaymentController(PaymentService paymentService, TokenService tokenService) {
        this.paymentService = paymentService;
        this.tokenService = tokenService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/initiatePayment")
    public InitiatePaymentResponse initiatePayment(HttpServletRequest httpRequest, @RequestBody PaymentRequest paymentRequest) throws AuthorizationServiceException, PaymentProcessingException, ObjectNotFoundException {
        return paymentService.initiatePayment(httpRequest, paymentRequest);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/confirmPayment/{transactionId}/{otp}")
    public ConfirmPaymentResponse confirmPayment(HttpServletRequest httpRequest, @PathVariable Long transactionId, @PathVariable Integer otp) throws PaymentProcessingException {
        return paymentService.confirmPayment(httpRequest, transactionId, otp);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/checkBalance/{userId}")
    public BigDecimal checkBalance(@PathVariable Long userId) throws ObjectNotFoundException {
        return paymentService.checkBalance(userId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/getAllTransactions/{userId}")
    public List<TransactionDto> retrieveAllTransactions(@PathVariable Long userId) throws ObjectNotFoundException {
        return paymentService.retrieveAllTransactions(userId);
    }
}

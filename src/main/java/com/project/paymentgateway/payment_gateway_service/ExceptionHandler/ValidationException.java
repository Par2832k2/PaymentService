package com.project.paymentgateway.payment_gateway_service.ExceptionHandler;

public class ValidationException extends Exception{
    public ValidationException(String message) {
        super(message);
    }
}

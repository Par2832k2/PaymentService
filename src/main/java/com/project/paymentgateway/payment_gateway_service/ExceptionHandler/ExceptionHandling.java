package com.project.paymentgateway.payment_gateway_service.ExceptionHandler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandling {
    @ExceptionHandler(ValidationException.class)
    public String validationException(Exception e){
        return e.getMessage();
    }
    @ExceptionHandler(ObjectNotFoundException.class)
    public String objectNotFoundException(Exception e){ return e.getMessage();}
    @ExceptionHandler(PaymentProcessingException.class)
    public String paymentProcessingException(Exception e){ return e.getMessage();}
}

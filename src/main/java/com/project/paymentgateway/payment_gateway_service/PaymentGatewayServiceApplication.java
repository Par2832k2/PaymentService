package com.project.paymentgateway.payment_gateway_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class PaymentGatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewayServiceApplication.class, args);
	}

}

package com.project.paymentgateway.payment_gateway_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String userName;
    private String fullName;
    private String contactNumber;
    private String emailId;
}

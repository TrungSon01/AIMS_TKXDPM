package com.example.AIMS_TKXDPM.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentRequest {
    @NotBlank(message = "Payment code is required")
    private String paymentCode;
}

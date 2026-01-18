package com.example.AIMS_TKXDPM.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Integer id;
    private String paymentCode;
    private Integer orderId;
    private BigDecimal amount;
    private String description;
    private String status;
    private String qrCodeUrl;
    private String qrCodeData; // Raw QR code data for frontend
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String paymentMethod;
}

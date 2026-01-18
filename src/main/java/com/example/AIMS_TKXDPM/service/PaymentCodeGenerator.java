package com.example.AIMS_TKXDPM.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for generating unique payment codes
 */
@Component
public class PaymentCodeGenerator {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    public String generatePaymentCode() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAY-" + timestamp + "-" + uuid;
    }
}

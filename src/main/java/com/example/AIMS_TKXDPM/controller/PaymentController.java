package com.example.AIMS_TKXDPM.controller;

import com.example.AIMS_TKXDPM.dto.ConfirmPaymentRequest;
import com.example.AIMS_TKXDPM.dto.PaymentRequest;
import com.example.AIMS_TKXDPM.dto.PaymentResponse;
import com.example.AIMS_TKXDPM.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request) {
        PaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentCode}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentCode) {
        return paymentService.getPaymentByCode(paymentCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

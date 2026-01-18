package com.example.AIMS_TKXDPM.service;

import com.example.AIMS_TKXDPM.dto.ConfirmPaymentRequest;
import com.example.AIMS_TKXDPM.dto.PaymentRequest;
import com.example.AIMS_TKXDPM.dto.PaymentResponse;
import com.example.AIMS_TKXDPM.entity.Order;
import com.example.AIMS_TKXDPM.entity.Payment;
import com.example.AIMS_TKXDPM.repository.OrderRepository;
import com.example.AIMS_TKXDPM.repository.PaymentRepository;
import com.example.AIMS_TKXDPM.service.qr.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for payment operations
 * Follows Single Responsibility Principle
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentCodeGenerator paymentCodeGenerator;
    private final QrCodeGenerator qrCodeGenerator;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Find order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + request.getOrderId()));

        // Generate payment code
        String paymentCode = paymentCodeGenerator.generatePaymentCode();

        // Create payment entity
        Payment payment = new Payment();
        payment.setPaymentCode(paymentCode);
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setDescription(request.getDescription());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentMethod("VIETQR");
        payment.setExpiresAt(LocalDateTime.now().plusHours(24)); // Expires in 24 hours

        // Generate QR code data
        String qrCodeData = qrCodeGenerator.generateQrCodeData(
                null, // accountNo - can be configured later
                null, // accountName - can be configured later
                request.getAmount().toString(),
                request.getDescription() != null ? request.getDescription() : "Payment for order #" + order.getId()
        );

        // Generate QR code image and convert to base64
        try {
            var qrImage = qrCodeGenerator.generateQrCodeImage(qrCodeData, 300, 300);
            String qrCodeBase64 = imageToBase64(qrImage);
            payment.setQrCodeUrl("data:image/png;base64," + qrCodeBase64);
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code image", e);
        }

        // Save payment
        payment = paymentRepository.save(payment);

        // Convert to response
        return toResponse(payment, qrCodeData);
    }

    @Transactional
    public PaymentResponse confirmPayment(ConfirmPaymentRequest request) {
        Payment payment = paymentRepository.findByPaymentCode(request.getPaymentCode())
                .orElseThrow(() -> new RuntimeException("Payment not found with code: " + request.getPaymentCode()));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is already " + payment.getStatus());
        }

        if (payment.getExpiresAt() != null && payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            payment.setStatus(Payment.PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment has expired");
        }

        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        return toResponse(payment, null);
    }

    public Optional<PaymentResponse> getPaymentByCode(String paymentCode) {
        return paymentRepository.findByPaymentCode(paymentCode)
                .map(payment -> toResponse(payment, null));
    }

    private PaymentResponse toResponse(Payment payment, String qrCodeData) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentCode(payment.getPaymentCode());
        response.setOrderId(payment.getOrder().getId());
        response.setAmount(payment.getAmount());
        response.setDescription(payment.getDescription());
        response.setStatus(payment.getStatus().name());
        response.setQrCodeUrl(payment.getQrCodeUrl());
        response.setQrCodeData(qrCodeData);
        response.setCreatedAt(payment.getCreatedAt());
        response.setExpiresAt(payment.getExpiresAt());
        response.setPaymentMethod(payment.getPaymentMethod());
        return response;
    }

    private String imageToBase64(java.awt.image.BufferedImage image) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error converting image to base64", e);
        }
    }
}

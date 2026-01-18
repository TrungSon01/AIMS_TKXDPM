package com.example.AIMS_TKXDPM.service.qr;

import java.awt.image.BufferedImage;

/**
 * Strategy Pattern: Interface for QR code generation
 */
public interface QrCodeGenerator {
    BufferedImage generateQrCodeImage(String data, int width, int height);
    String generateQrCodeData(String accountNo, String accountName, String amount, String description);
}

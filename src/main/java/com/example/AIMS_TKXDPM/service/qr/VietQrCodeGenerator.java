package com.example.AIMS_TKXDPM.service.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Concrete implementation of QR code generator for VietQR format
 */
@Component
public class VietQrCodeGenerator implements QrCodeGenerator {

    private static final String DEFAULT_ACCOUNT_NO = "1234567890";
    private static final String DEFAULT_ACCOUNT_NAME = "AIMS STORE";
    private static final String DEFAULT_ACQ_ID = "970415";

    @Override
    public BufferedImage generateQrCodeImage(String data, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage qrImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            qrImage.createGraphics();

            Graphics2D graphics = (Graphics2D) qrImage.getGraphics();
            try {
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, width, height);
                graphics.setColor(Color.BLACK);

                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (bitMatrix.get(i, j)) {
                            graphics.fillRect(i, j, 1, 1);
                        }
                    }
                }
            } finally {
                graphics.dispose(); // Release graphics resources
            }

            return qrImage;
        } catch (WriterException e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }

    @Override
    public String generateQrCodeData(String accountNo, String accountName, String amount, String description) {
        // VietQR format: JSON string
        // Format: {"accountNo":"...","accountName":"...","acqId":"...","amount":...,"addInfo":"...","template":"compact"}
        String formattedAmount = new BigDecimal(amount).intValue() + "";
        
        return String.format(
            "{\"accountNo\":\"%s\",\"accountName\":\"%s\",\"acqId\":\"%s\",\"amount\":%s,\"addInfo\":\"%s\",\"template\":\"compact\"}",
            accountNo != null ? accountNo : DEFAULT_ACCOUNT_NO,
            accountName != null ? accountName : DEFAULT_ACCOUNT_NAME,
            DEFAULT_ACQ_ID,
            formattedAmount,
            description != null ? description.replace("\"", "\\\"") : ""
        );
    }
}

package com.oexil.studentreg.utils;

import org.springframework.stereotype.Service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QRCodeUtil {

    public static byte[] generateQRCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);

            // Create a BufferedImage with transparency
            BufferedImage qrImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = qrImage.createGraphics();

            // Set transparent background
            graphics.setColor(new Color(255, 255, 255, 0)); // Fully transparent
            graphics.fillRect(0, 0, 200, 200);

            // Set foreground color (QR code pixels)
            graphics.setColor(Color.BLACK);

            // Draw QR code pixels
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            graphics.dispose();

            // Convert BufferedImage to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

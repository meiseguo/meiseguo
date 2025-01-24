package com.meiseguo.api.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Hashtable;

/**
 * QRCodeUtil 生成二维码工具类
 */
public class QRCodeUtil {

    private static final String CHARSET = "utf-8";
    private static final String FORMAT_NAME = "JPG";
    // 二维码尺寸
    private static final int QRCODE_SIZE = 132;
    // LOGO宽度
    private static final int WIDTH = 832;
    // LOGO高度
    private static final int HEIGHT = 466;

    private static BufferedImage createImage(String content, String imgPath) throws Exception {
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        if (imgPath == null || "".equals(imgPath)) {
            return image;
        }
        // 插入图片
        BufferedImage result = QRCodeUtil.insertImage(image, imgPath);
        return result;
    }

    private static BufferedImage insertImage(BufferedImage source, String imgPath) throws Exception {
        File file = new File(imgPath);
        if (!file.exists()) {
            System.err.println("" + imgPath + "   该文件不存在！");
            return source;
        }
        Image image = ImageIO.read(new File(imgPath));
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(image, 0, 0, null); // 绘制缩小后的图
        int x = 108;
        int y = 180;
        g.drawImage(source, x, y, null);
        g.dispose();
        return tag;
    }


    public static void encode(String content, String imgPath, OutputStream output )
            throws Exception {
        BufferedImage image = QRCodeUtil.createImage(content, imgPath);
        ImageIO.write(image, FORMAT_NAME, output);
    }

    public static void encode(String content, OutputStream output) throws Exception {
        QRCodeUtil.encode(content, null, output);
    }

    public static String base64(String content, String imgPath) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            BufferedImage image = QRCodeUtil.createImage(content, imgPath);
            ImageIO.write(image, FORMAT_NAME, os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (Exception e){
            return null;
        }
    }
}
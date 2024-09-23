package org.start2do.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MockDataUtil {

    private final static int borderThickness = 1; // 边框厚度
    private final static int stripeSpacing = 10; // 条纹间距

    public BufferedImage mockImageData(String fontName,Color color, int width, int height, int index, String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取Graphics2D对象
        Graphics2D g2d = image.createGraphics();

        // 设置背景颜色为白色
        g2d.setColor(color);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

        // 设置文本颜色为黑色
        g2d.setColor(Color.BLACK);

        // 创建要显示的文本
        String text_ = "Width: " + width + " Height: " + height;
        if (index > 0) {
            text_ += " Index:" + index;
        }

        // 计算合适的字体大小
        int maxFontSize = (int) (Math.min(width, height) * 0.08); // 初始字体大小为图像较小边界的8%
        int fontSize = maxFontSize;

        // 计算文本的宽度和高度
        Font font = new Font(fontName, Font.BOLD, fontSize);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text_);
        String calcText = text_;
        if (StringUtils.isNotEmpty(text)) {
            if (calcText.length() < text.length()) {
                calcText = text;
            }
        }
        // 动态调整字体大小，直到文本宽度小于图像宽度的90%
        while (textWidth > 0.9 * width && fontSize > 1) {
            fontSize--;
            font = new Font("宋体", Font.BOLD, fontSize);
            g2d.setFont(font);
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(calcText);
        }

        // 计算文本在图像中心的位置
        int textHeight = fm.getHeight();
        int x = (image.getWidth() - textWidth) / 2;
        int y = ((image.getHeight() - textHeight) / 2 + fm.getAscent()) - (fontSize / 2);

        // 在图像中心绘制文本
        g2d.drawString(text_, x, y);

        if (StringUtils.isNotEmpty(text)) {
            int x1 = (image.getWidth() - fm.stringWidth(text)) / 2;
            int y1 = y + textHeight + (fontSize / 2);
            g2d.drawString(text, x1, y1);
        }

        // 绘制边框
        g2d.setColor(Color.lightGray);
        g2d.fillRect(0, 0, width, borderThickness); // 上边框
        g2d.fillRect(0, height - borderThickness, width, borderThickness); // 下边框
        g2d.fillRect(0, 0, borderThickness, height); // 左边框
        g2d.fillRect(width - borderThickness, 0, borderThickness, height); // 右边框

        // 绘制斜向条纹
        g2d.setColor(Color.lightGray);
        for (int p = -height; p < width; p += stripeSpacing) {
            g2d.drawLine(p, 0, p + height, height);
        }

        // 释放Graphics2D对象
        g2d.dispose();

        return image;
    }
}

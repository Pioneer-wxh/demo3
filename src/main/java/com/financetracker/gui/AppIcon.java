package com.financetracker.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * 提供应用程序图标的工具类
 */
public class AppIcon {
    
    /**
     * 生成应用程序图标
     * @param size 图标尺寸
     * @return 图标ImageIcon对象
     */
    public static ImageIcon createAppIcon(int size) {
        // 创建一个空的图像，指定尺寸
        Image image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 计算一些基础尺寸
        int padding = size / 10;
        int innerSize = size - (padding * 2);
        
        // 定义渐变色
        Color primaryColor = new Color(41, 128, 185); // 蓝色
        Color secondaryColor = new Color(52, 152, 219); // 浅蓝色
        
        // 绘制圆角矩形作为背景
        g2d.setPaint(new GradientPaint(0, 0, secondaryColor, size, size, primaryColor));
        g2d.fill(new RoundRectangle2D.Float(padding, padding, innerSize, innerSize, size/5, size/5));
        
        // 绘制¥符号（人民币标志）
        g2d.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, size / 2);
        g2d.setFont(font);
        
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth("¥");
        int textHeight = metrics.getHeight();
        
        // 居中绘制符号
        g2d.drawString("¥", size/2 - textWidth/2, size/2 + textHeight/4);
        
        // 释放图形上下文
        g2d.dispose();
        
        return new ImageIcon(image);
    }
} 
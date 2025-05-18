package com.financetracker.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class SplashScreenManager {

    private static final Logger LOGGER = Logger.getLogger(SplashScreenManager.class.getName());
    private JProgressBar loadingProgressBar;
    private JLabel progressIconLabel;
    private JFrame splashFrame;
    private static final int ICON_SIZE = 48; // 与 Main 中一致

    public SplashScreenManager() {
        // 构造函数可以留空，或者进行一些早期初始化（如果需要）
    }

    public void show() {
        splashFrame = createSplashScreen();
        splashFrame.setVisible(true);
    }

    private JFrame createSplashScreen() {
        JFrame frame = new JFrame("启动中...");
        frame.setUndecorated(true);

        ImageIcon appIcon = AppIcon.createAppIcon(ICON_SIZE);
        frame.setIconImage(appIcon.getImage());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(41, 128, 185));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("个人财务跟踪器", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        JLabel iconLabel = new JLabel(appIcon);
        titlePanel.add(iconLabel, BorderLayout.WEST);

        panel.add(titlePanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel progressPanel = new JPanel(new BorderLayout(10, 0));
        progressPanel.setBackground(Color.WHITE);

        loadingProgressBar = new JProgressBar(0, 100);
        loadingProgressBar.setStringPainted(true);
        loadingProgressBar.setString("加载应用程序...");
        loadingProgressBar.setValue(0);
        progressPanel.add(loadingProgressBar, BorderLayout.CENTER);

        progressIconLabel = new JLabel();
        progressIconLabel.setIcon(new ImageIcon(createProgressIndicator(24, 0))); // 使用内部方法
        progressPanel.add(progressIconLabel, BorderLayout.EAST);

        contentPanel.add(progressPanel, BorderLayout.CENTER);

        JLabel versionLabel = new JLabel("版本 1.0", JLabel.CENTER);
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        contentPanel.add(versionLabel, BorderLayout.SOUTH);

        panel.add(contentPanel, BorderLayout.CENTER);

        frame.add(panel);
        frame.pack();
        frame.setSize(450, 250);
        frame.setLocationRelativeTo(null);
        
        return frame;
    }

    public void simulateLoading() {
        if (splashFrame == null || loadingProgressBar == null || progressIconLabel == null) {
            LOGGER.warning("启动画面未初始化，无法模拟加载。");
            return;
        }
        try {
            for (int i = 0; i <= 100; i += 10) {
                Thread.sleep(100); 
                final int progress = i;
                SwingUtilities.invokeLater(() -> {
                    loadingProgressBar.setValue(progress);
                    progressIconLabel.setIcon(new ImageIcon(createProgressIndicator(24, progress))); // 使用内部方法
                    
                    switch (progress) {
                        case 10:
                            loadingProgressBar.setString("初始化服务...");
                            break;
                        case 30:
                            loadingProgressBar.setString("加载设置...");
                            break;
                        case 50:
                            loadingProgressBar.setString("加载交易数据...");
                            break;
                        case 70:
                            loadingProgressBar.setString("初始化UI组件...");
                            break;
                        case 90:
                            loadingProgressBar.setString("完成加载...");
                            break;
                    }
                });
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "加载过程中断: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 创建一个圆形进度指示器图像.
     * 此方法从 AppIcon 类移动过来，因为它与启动画面的进度显示紧密相关。
     * @param size 图像大小
     * @param progress 进度百分比（0-100）
     * @return 进度指示器图像
     */
    private static Image createProgressIndicator(int size, int progress) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(new Color(200, 200, 200, 50));
        g2d.fillOval(0, 0, size, size);
        
        g2d.setColor(new Color(41, 128, 185));
        g2d.setStroke(new BasicStroke(size / 10f));
        int arcSize = size - (size / 10);
        int offset = size / 20;
        g2d.drawArc(offset, offset, arcSize, arcSize, 90, (int)(-3.6 * progress));
        
        g2d.dispose();
        
        return image;
    }

    public void dispose() {
        if (splashFrame != null) {
            splashFrame.dispose();
            LOGGER.info("启动画面已关闭。");
        }
    }
} 
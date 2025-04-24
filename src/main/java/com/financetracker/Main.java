package com.financetracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.financetracker.gui.AppIcon;
import com.financetracker.gui.MainFrame;

/**
 * Main entry point for launching the Finance Tracker application using the MainFrame structure.
 */
public class Main { // Renamed from AppLauncher
    
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static JProgressBar loadingProgressBar; // 存储进度条的静态引用
    private static JLabel progressIconLabel; // 存储进度图标的标签
    private static final int ICON_SIZE = 48; // 图标尺寸

    public static void main(String[] args) {
        LOGGER.info("Finance Tracker application starting...");
        
        // 创建并显示启动画面
        JFrame splashFrame = createSplashScreen();
        
        // 确保GUI更新在事件分发线程上完成
        SwingUtilities.invokeLater(() -> {
            try {
                LOGGER.info("设置应用程序外观和感觉...");
                
                // 尝试使用跨平台现代主题 - Nimbus
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                    LOGGER.info("已应用Nimbus主题");
                } catch (Exception e) {
                    // 如果Nimbus不可用，使用系统外观
                    LOGGER.log(Level.WARNING, "无法设置Nimbus主题，使用系统外观: " + e.getMessage());
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                
                // 模拟加载过程
                simulateLoading(splashFrame);
                
                // 创建并显示主应用程序窗口
                LOGGER.info("初始化主应用程序窗口...");
                MainFrame mainFrame = new MainFrame();
                
                // 设置应用程序图标
                ImageIcon appIcon = AppIcon.createAppIcon(ICON_SIZE);
                mainFrame.setIconImage(appIcon.getImage());
                
                // 关闭启动画面
                splashFrame.dispose();
                
                // 显示主窗口
                mainFrame.setVisible(true);
                LOGGER.info("应用程序成功启动");
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "启动应用程序时出错: " + e.getMessage(), e);
                JOptionPane.showMessageDialog(null, 
                    "启动应用程序时出错: " + e.getMessage(), 
                    "启动错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * 创建启动画面窗口
     * @return 启动画面JFrame
     */
    private static JFrame createSplashScreen() {
        JFrame frame = new JFrame("启动中...");
        frame.setUndecorated(true); // 无边框
        
        // 创建应用程序图标并设置
        ImageIcon appIcon = AppIcon.createAppIcon(ICON_SIZE);
        frame.setIconImage(appIcon.getImage());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(41, 128, 185)); // 使用应用程序主色调
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 创建标题标签
        JLabel titleLabel = new JLabel("个人财务跟踪器", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        // 创建图标标签
        JLabel iconLabel = new JLabel(appIcon);
        titlePanel.add(iconLabel, BorderLayout.WEST);
        
        // 添加标题面板
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // 创建内容面板
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建进度条面板
        JPanel progressPanel = new JPanel(new BorderLayout(10, 0));
        progressPanel.setBackground(Color.WHITE);
        
        // 创建进度条
        loadingProgressBar = new JProgressBar(0, 100);
        loadingProgressBar.setStringPainted(true);
        loadingProgressBar.setString("加载应用程序...");
        loadingProgressBar.setValue(0);
        progressPanel.add(loadingProgressBar, BorderLayout.CENTER);
        
        // 创建进度图标
        progressIconLabel = new JLabel();
        progressIconLabel.setIcon(new ImageIcon(AppIcon.createProgressIndicator(24, 0)));
        progressPanel.add(progressIconLabel, BorderLayout.EAST);
        
        contentPanel.add(progressPanel, BorderLayout.CENTER);
        
        // 设置版本信息标签
        JLabel versionLabel = new JLabel("版本 1.0", JLabel.CENTER);
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        contentPanel.add(versionLabel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        frame.add(panel);
        frame.pack();
        frame.setSize(450, 250);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        return frame;
    }
    
    /**
     * 模拟应用程序加载过程
     * @param splashFrame 启动画面框架
     */
    private static void simulateLoading(JFrame splashFrame) {
        try {
            for (int i = 0; i <= 100; i += 10) {
                Thread.sleep(100); // 模拟加载时间
                final int progress = i;
                SwingUtilities.invokeLater(() -> {
                    loadingProgressBar.setValue(progress);
                    
                    // 更新进度图标
                    progressIconLabel.setIcon(new ImageIcon(AppIcon.createProgressIndicator(24, progress)));
                    
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
}

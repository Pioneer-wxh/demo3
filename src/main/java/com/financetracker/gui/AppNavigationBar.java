package com.financetracker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AppNavigationBar extends JPanel {

    // Theme Colors (Consider moving to a central ThemeManager or passing them)
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color PRIMARY_DARK_COLOR = new Color(31, 97, 141);
    private static final Color ACCENT_COLOR = new Color(26, 188, 156);

    private JPanel buttonsPanel;
    private List<JButton> navButtons = new ArrayList<>();
    private ActionListener panelSwitcherListener;

    public AppNavigationBar(ActionListener panelSwitcherListener) {
        this.panelSwitcherListener = panelSwitcherListener;
        setLayout(new BorderLayout());
        setBackground(PRIMARY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Added padding for title

        createTitlePanel();
        createButtonsPanel();

        // Add initial navigation buttons
        addNavigationButton("首页", "home", "返回应用主页");
        addNavigationButton("交易记录", "transactions", "管理您的交易记录");
        addNavigationButton("分析", "analysis", "查看AI辅助分析");
        addNavigationButton("设置", "settings", "配置应用程序设置");
    }

    private void createTitlePanel() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // Added gap
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel(AppIcon.createAppIcon(24));
        titlePanel.add(iconLabel);

        JLabel titleLabel = new JLabel("个人财务跟踪器");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titlePanel.add(titleLabel);

        add(titlePanel, BorderLayout.WEST);
    }

    private void createButtonsPanel() {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Centered buttons
        buttonsPanel.setOpaque(false);
        add(buttonsPanel, BorderLayout.CENTER);
    }

    private void addNavigationButton(String text, String command, String tooltip) {
        JButton button = new JButton(text);
        button.setActionCommand(command); // Use action command for the listener
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!"active".equals(button.getClientProperty("state"))) {
                    button.setBackground(PRIMARY_DARK_COLOR);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!"active".equals(button.getClientProperty("state"))) {
                    button.setBackground(PRIMARY_COLOR);
                }
            }
        });

        button.addActionListener(e -> {
            if (panelSwitcherListener != null) {
                panelSwitcherListener.actionPerformed(e); // Forward the event
            }
            updateNavigationButtons(e.getActionCommand());
            // The MainFrame will be responsible for updateStatusText
        });
        
        navButtons.add(button);
        buttonsPanel.add(button);
    }

    public void updateNavigationButtons(String activePanelCommand) {
        for (JButton button : navButtons) {
            if (button.getActionCommand().equals(activePanelCommand)) {
                button.setBackground(ACCENT_COLOR);
                button.putClientProperty("state", "active");
            } else {
                button.setBackground(PRIMARY_COLOR);
                button.putClientProperty("state", "inactive");
            }
        }
    }
    
    // Method to allow MainFrame or a ThemeManager to apply theme colors
    public void applyThemeColors(Color primary, Color primaryDark, Color accent) {
        setBackground(primary);
        for (JButton button : navButtons) {
            if ("active".equals(button.getClientProperty("state"))) {
                button.setBackground(accent);
            } else {
                button.setBackground(primary);
            }
            // Potentially update foreground, etc.
        }
        // Re-evaluate title color if needed
    }
} 
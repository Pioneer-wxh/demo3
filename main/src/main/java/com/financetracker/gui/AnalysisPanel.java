package com.financetracker.gui;

import com.financetracker.model.Transaction;
import com.financetracker.service.TransactionService;
import com.financetracker.ai.AiAssistantService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Panel for AI-assisted analysis.
 */
public class AnalysisPanel extends JPanel {
    
    private MainFrame mainFrame;
    private TransactionService transactionService;
    private AiAssistantService aiAssistantService;
    
    private JPanel currentMonthPanel;
    private JPanel aiAssistantPanel;
    private JPanel budgetPanel;
    
    private JTextArea summaryTextArea;
    private JTextArea categoryBreakdownTextArea;
    private JTextArea aiResponseTextArea;
    private JTextField aiQueryField;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> monthComboBox;
    
    /
}

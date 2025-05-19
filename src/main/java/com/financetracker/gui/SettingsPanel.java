package com.financetracker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.financetracker.model.SavingGoal;
import com.financetracker.model.Settings;
import com.financetracker.model.SpecialDate;
import com.financetracker.service.BudgetAdjustmentService;
import com.financetracker.service.FinancialCycleService;
import com.financetracker.service.SettingsService;
import com.financetracker.service.SpecialDateService;

// Enum to distinguish category types
enum CategoryType {
    EXPENSE, INCOME
}

/**
 * Panel for managing application settings.
 */
public class SettingsPanel extends JPanel {

    private SettingsService settingsService;
    private SpecialDateService specialDateService;
    private BudgetAdjustmentService budgetAdjustmentService;
    private FinancialCycleService financialCycleService;
    private ActionListener panelNavigationListener;
    private Runnable globalCategoryRefreshCallback;
    private Runnable analysisRefreshCallback;
    private Runnable homePanelRefreshCallback;

    private JTabbedPane tabbedPane;

    private JPanel specialDatesPanel;
    private JPanel savingsGoalsPanel;
    private JPanel categoryManagementPanel;
    private JPanel monthStartDayPanel;
    private JPanel monthEndClosingPanel;
    private JPanel generalSettingsPanelHolder;

    private JTable specialDatesTable;
    private DefaultTableModel specialDatesTableModel;
    private JTextField specialDateNameField;
    private JSpinner specialDateSpinner;
    private JTextField specialDateDescriptionField;
    private JComboBox<String> specialDateCategoryComboBox;
    private JTextField specialDateImpactField;

    private JTextField savingsAmountField;
    private JSpinner savingsDateSpinner;

    private JTextField newExpenseCategoryField;
    private JTextField newIncomeCategoryField;
    private JPanel expenseCategoriesDisplayPanel;
    private JPanel incomeCategoriesDisplayPanel;

    private JSpinner monthStartDaySpinner;

    private JTable savingGoalsTable;
    private DefaultTableModel savingGoalsTableModel;
    private JTextField sgNameField;
    private JTextField sgDescriptionField;
    private JSpinner sgTargetAmountSpinner;
    private JSpinner sgMonthlyContributionSpinner;
    private JSpinner sgStartDateSpinner;
    private JSpinner sgTargetDateSpinner;
    private JCheckBox sgIsActiveCheckBox;
    private SavingGoal currentEditingSavingGoal = null;
    private JButton addGoalButton;
    private JButton saveGoalButton;

    /**
     * Constructor for SettingsPanel.
     * 
     * @param settingsService               Service for settings management.
     * @param specialDateService            Service for special date management.
     * @param budgetAdjustmentService       Service for budget adjustment
     *                                      management.
     * @param financialCycleService         Service for financial cycle operations.
     * @param panelNavigationListener       Listener for panel navigation.
     * @param globalCategoryRefreshCallback Callback to refresh category lists
     *                                      globally.
     * @param analysisRefreshCallback       Callback to refresh analysis panel.
     * @param homePanelRefreshCallback      Callback to refresh home panel.
     */
    public SettingsPanel(SettingsService settingsService,
            SpecialDateService specialDateService,
            BudgetAdjustmentService budgetAdjustmentService,
            FinancialCycleService financialCycleService,
            ActionListener panelNavigationListener,
            Runnable globalCategoryRefreshCallback,
            Runnable analysisRefreshCallback,
            Runnable homePanelRefreshCallback) {
        this.settingsService = settingsService;
        this.specialDateService = specialDateService;
        this.budgetAdjustmentService = budgetAdjustmentService;
        this.financialCycleService = financialCycleService;
        this.panelNavigationListener = panelNavigationListener;
        this.globalCategoryRefreshCallback = globalCategoryRefreshCallback;
        this.analysisRefreshCallback = analysisRefreshCallback;
        this.homePanelRefreshCallback = homePanelRefreshCallback;
        initComponents();
        loadSettingsData();
    }

    /**
     * Initializes the panel components.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        JButton homeButton = new JButton("HOME");
        homeButton.setActionCommand("home");
        homeButton.addActionListener(this.panelNavigationListener);
        headerPanel.add(homeButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 创建面板
        specialDatesPanel = createSpecialDatesPanel();
        savingsGoalsPanel = createSavingsGoalsPanel();

        // 创建通用设置面板
        generalSettingsPanelHolder = new JPanel();
        generalSettingsPanelHolder.setLayout(new BoxLayout(generalSettingsPanelHolder, BoxLayout.Y_AXIS));
        generalSettingsPanelHolder.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加子面板到通用设置面板
        categoryManagementPanel = createCategoryManagementPanel();
        monthStartDayPanel = createMonthStartDayPanel();
        monthEndClosingPanel = createMonthEndClosingPanel();

        generalSettingsPanelHolder.add(categoryManagementPanel);
        generalSettingsPanelHolder.add(Box.createVerticalStrut(15));
        generalSettingsPanelHolder.add(monthStartDayPanel);
        generalSettingsPanelHolder.add(Box.createVerticalStrut(15));
        generalSettingsPanelHolder.add(monthEndClosingPanel);
        generalSettingsPanelHolder.add(Box.createVerticalGlue());

        // 直接将面板添加到标签页，不使用额外的容器
        tabbedPane.addTab("Special Dates", new JScrollPane(specialDatesPanel));
        tabbedPane.addTab("Saving Goals", new JScrollPane(savingsGoalsPanel));
        tabbedPane.addTab("General Settings", new JScrollPane(generalSettingsPanelHolder));

        // 添加特殊日期表格的选择监听器
        if (specialDatesTable != null) {
            specialDatesTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    fillSpecialDateFormFromSelection();
                }
            });
        }

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton resetButton = new JButton("Reset to Default");
        resetButton.addActionListener(e -> resetToDefault());
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveChanges());
        bottomButtonPanel.add(resetButton);
        bottomButtonPanel.add(saveButton);
        add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    /**
     * 根据表格选择填充特殊日期表单
     */
    private void fillSpecialDateFormFromSelection() {
        try {
            int selectedRow = specialDatesTable.getSelectedRow();
            if (selectedRow == -1) {
                // 没有选择行时清空表单
                return;
            }

            // 获取选择行的数据
            String name = (String) specialDatesTableModel.getValueAt(selectedRow, 1);
            String dateStr = (String) specialDatesTableModel.getValueAt(selectedRow, 2);
            String description = (String) specialDatesTableModel.getValueAt(selectedRow, 3);
            String category = (String) specialDatesTableModel.getValueAt(selectedRow, 4);
            Double impact = null;
            Object impactObj = specialDatesTableModel.getValueAt(selectedRow, 5);
            if (impactObj != null) {
                if (impactObj instanceof Double) {
                    impact = (Double) impactObj;
                } else if (impactObj instanceof String) {
                    impact = Double.parseDouble((String) impactObj);
                }
            }

            // 解析日期
            LocalDate date = null;
            try {
                date = LocalDate.parse(dateStr);
                Date utilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                specialDateSpinner.setValue(utilDate);
            } catch (Exception ex) {
                System.err.println("无法解析日期: " + dateStr);
            }

            // 填充表单
            specialDateNameField.setText(name);
            specialDateDescriptionField.setText(description);
            if (category != null) {
                specialDateCategoryComboBox.setSelectedItem(category);
            }
            if (impact != null) {
                specialDateImpactField.setText(String.valueOf(impact));
            }

        } catch (Exception ex) {
            System.err.println("填充特殊日期表单时出错: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * 删除选中的特殊日期
     */
    private void deleteSpecialDate() {
        try {
            int selectedRow = specialDatesTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的特殊日期", "未选择", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 确保ID和名称不为空
            Object idObj = specialDatesTableModel.getValueAt(selectedRow, 0);
            Object nameObj = specialDatesTableModel.getValueAt(selectedRow, 1);

            if (idObj == null) {
                JOptionPane.showMessageDialog(this, "无法获取特殊日期ID", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String idToDelete = idObj.toString();
            String nameToDelete = nameObj != null ? nameObj.toString() : "未命名";

            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除这个特殊日期吗: " + nameToDelete + "?",
                    "确认删除", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // 获取设置并执行删除
            Settings settings = settingsService.getSettings();
            if (settings == null) {
                JOptionPane.showMessageDialog(this, "无法获取设置对象", "系统错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean removed = settings.removeSpecialDate(idToDelete);

            if (removed) {
                boolean saved = settingsService.saveSettings();
                if (!saved) {
                    JOptionPane.showMessageDialog(this, "无法保存更改", "保存错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                loadSpecialDates();
                clearSpecialDateForm();
                updateSpecialDateCategoryComboBox();

                if (globalCategoryRefreshCallback != null) {
                    globalCategoryRefreshCallback.run();
                }
                if (analysisRefreshCallback != null) {
                    analysisRefreshCallback.run();
                }

                JOptionPane.showMessageDialog(this, "特殊日期已成功删除!", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "无法删除ID为: " + idToDelete + " 的特殊日期", "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除特殊日期时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * 创建特殊日期面板
     * 
     * @return 特殊日期面板
     */
    private JPanel createSpecialDatesPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // ===== 表格部分 =====
        JPanel tablePanel = new JPanel(new BorderLayout());

        // 说明文字
        JLabel explanationLabel = new JLabel(
                "<html>特殊日期允许您标记特定日期并设置其对预算的影响。<br>正数表示预算增加，负数表示预算减少。<br>您可以设置日期是一次性的，还是每月或每年重复的。</html>");
        explanationLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        tablePanel.add(explanationLabel, BorderLayout.NORTH);

        // 设置表格模型
        String[] columns = { "ID", "名称", "日期", "描述", "影响分类", "金额影响", "预算影响示例", "重复类型" };
        specialDatesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 创建表格
        specialDatesTable = new JTable(specialDatesTableModel);
        specialDatesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 隐藏ID列
        specialDatesTable.getColumnModel().getColumn(0).setMinWidth(0);
        specialDatesTable.getColumnModel().getColumn(0).setMaxWidth(0);
        specialDatesTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // 添加表格到滚动面板
        JScrollPane scrollPane = new JScrollPane(specialDatesTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // 添加表格面板到主面板
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // ===== 表单部分 =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("名称:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        specialDateNameField = new JTextField(20);
        formPanel.add(specialDateNameField, gbc);

        // 日期
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("日期:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        Date now = new Date();
        SpinnerDateModel specialDateModel = new SpinnerDateModel(now, null, null, Calendar.DAY_OF_MONTH);
        specialDateSpinner = new JSpinner(specialDateModel);
        JSpinner.DateEditor specialDateEditor = new JSpinner.DateEditor(specialDateSpinner, "yyyy-MM-dd");
        specialDateSpinner.setEditor(specialDateEditor);
        formPanel.add(specialDateSpinner, gbc);

        // 描述
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("描述:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        specialDateDescriptionField = new JTextField(20);
        formPanel.add(specialDateDescriptionField, gbc);

        // 影响分类
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("影响分类:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        specialDateCategoryComboBox = new JComboBox<>();
        updateSpecialDateCategoryComboBox();
        formPanel.add(specialDateCategoryComboBox, gbc);

        // 金额影响
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("金额影响:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        specialDateImpactField = new JTextField(10);
        formPanel.add(specialDateImpactField, gbc);

        // 重复类型
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("重复类型:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        JComboBox<String> recurrenceTypeComboBox = new JComboBox<>(new String[] { "不重复", "每月重复", "每年重复" });
        formPanel.add(recurrenceTypeComboBox, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> {
            String recurrenceType = (String) recurrenceTypeComboBox.getSelectedItem();
            addSpecialDateWithRecurrence(recurrenceType);
        });

        JButton editButton = new JButton("编辑");
        editButton.addActionListener(e -> {
            String recurrenceType = (String) recurrenceTypeComboBox.getSelectedItem();
            editSpecialDateWithRecurrence(recurrenceType);
        });

        JButton deleteButton = new JButton("删除");
        deleteButton.addActionListener(e -> deleteSpecialDate());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // 添加按钮面板到表单
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // 添加表单面板到主面板
        mainPanel.add(formPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * 添加特殊日期（包含重复设置）
     * 
     * @param recurrenceTypeStr 重复类型的字符串表示
     */
    private void addSpecialDateWithRecurrence(String recurrenceTypeStr) {
        try {
            // 验证所需字段
            String name = specialDateNameField.getText();
            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入特殊日期名称", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date date = (Date) specialDateSpinner.getValue();
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            String description = specialDateDescriptionField.getText();

            if (specialDateCategoryComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "请选择影响分类", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String category = (String) specialDateCategoryComboBox.getSelectedItem();

            // 解析金额
            if (specialDateImpactField.getText() == null || specialDateImpactField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入金额影响值", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double amountIncrease = Double.parseDouble(specialDateImpactField.getText());

            // 转换重复类型
            boolean isRecurring = !recurrenceTypeStr.equals("不重复");
            SpecialDate.RecurrenceType recurrenceType = SpecialDate.RecurrenceType.NONE;
            if (isRecurring) {
                if (recurrenceTypeStr.equals("每月重复")) {
                    recurrenceType = SpecialDate.RecurrenceType.MONTHLY;
                } else if (recurrenceTypeStr.equals("每年重复")) {
                    recurrenceType = SpecialDate.RecurrenceType.ANNUALLY;
                }
            }

            int dayOfMonth = localDate.getDayOfMonth();
            int monthOfYear = localDate.getMonthValue();

            // 创建特殊日期
            SpecialDate specialDate = new SpecialDate(
                    null, name, description, localDate,
                    category, amountIncrease, isRecurring,
                    recurrenceType, dayOfMonth, monthOfYear);

            // 保存到设置中
            Settings settings = settingsService.getSettings();
            if (settings != null) {
                settings.addSpecialDate(specialDate);
                boolean saved = settingsService.saveSettings();
                if (!saved) {
                    JOptionPane.showMessageDialog(this, "保存特殊日期失败", "保存错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(this, "无法获取设置对象", "系统错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 刷新表格
            loadSpecialDates();
            clearSpecialDateForm();
            updateSpecialDateCategoryComboBox();
            if (globalCategoryRefreshCallback != null) {
                globalCategoryRefreshCallback.run();
            }
            if (analysisRefreshCallback != null) {
                analysisRefreshCallback.run();
            }

            JOptionPane.showMessageDialog(this, "特殊日期已添加", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "金额必须是有效的数字", "输入错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "添加特殊日期时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * 编辑特殊日期（包含重复设置）
     * 
     * @param recurrenceTypeStr 重复类型的字符串表示
     */
    private void editSpecialDateWithRecurrence(String recurrenceTypeStr) {
        int selectedRow = specialDatesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的特殊日期", "未选择", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 确保有ID
            if (specialDatesTableModel.getValueAt(selectedRow, 0) == null) {
                JOptionPane.showMessageDialog(this, "所选特殊日期无有效ID", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String id = (String) specialDatesTableModel.getValueAt(selectedRow, 0);

            // 验证所需字段
            String name = specialDateNameField.getText();
            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入特殊日期名称", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date date = (Date) specialDateSpinner.getValue();
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            String description = specialDateDescriptionField.getText();

            if (specialDateCategoryComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "请选择影响分类", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String category = (String) specialDateCategoryComboBox.getSelectedItem();

            // 解析金额
            if (specialDateImpactField.getText() == null || specialDateImpactField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入金额影响值", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double amountIncrease = Double.parseDouble(specialDateImpactField.getText());

            // 转换重复类型
            boolean isRecurring = !recurrenceTypeStr.equals("不重复");
            SpecialDate.RecurrenceType recurrenceType = SpecialDate.RecurrenceType.NONE;
            if (isRecurring) {
                if (recurrenceTypeStr.equals("每月重复")) {
                    recurrenceType = SpecialDate.RecurrenceType.MONTHLY;
                } else if (recurrenceTypeStr.equals("每年重复")) {
                    recurrenceType = SpecialDate.RecurrenceType.ANNUALLY;
                }
            }

            int dayOfMonth = localDate.getDayOfMonth();
            int monthOfYear = localDate.getMonthValue();

            // 创建特殊日期
            SpecialDate specialDate = new SpecialDate(
                    id, name, description, localDate,
                    category, amountIncrease, isRecurring,
                    recurrenceType, dayOfMonth, monthOfYear);

            // 保存到设置中
            Settings settings = settingsService.getSettings();
            if (settings != null) {
                settings.updateSpecialDate(specialDate);
                boolean saved = settingsService.saveSettings();
                if (!saved) {
                    JOptionPane.showMessageDialog(this, "保存特殊日期失败", "保存错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(this, "无法获取设置对象", "系统错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 刷新表格
            loadSpecialDates();
            clearSpecialDateForm();
            updateSpecialDateCategoryComboBox();
            if (globalCategoryRefreshCallback != null) {
                globalCategoryRefreshCallback.run();
            }
            if (analysisRefreshCallback != null) {
                analysisRefreshCallback.run();
            }

            JOptionPane.showMessageDialog(this, "特殊日期已更新", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "金额必须是有效的数字", "输入错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "更新特殊日期时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Clears the special date form.
     */
    private void clearSpecialDateForm() {
        specialDateNameField.setText("");
        specialDateSpinner.setValue(new Date());
        specialDateDescriptionField.setText("");
        if (specialDateCategoryComboBox.getItemCount() > 0) {
            specialDateCategoryComboBox.setSelectedIndex(0);
        }
        specialDateImpactField.setText("");
    }

    /**
     * 加载特殊日期到表格中
     */
    public void loadSpecialDates() {
        try {
            // 检查必要组件是否初始化
            if (specialDatesTableModel == null) {
                System.err.println("表格模型未初始化");
                return;
            }
            if (specialDateService == null) {
                System.err.println("特殊日期服务未初始化");
                return;
            }
            if (settingsService == null) {
                System.err.println("设置服务未初始化");
                return;
            }

            // 清除表格数据
            specialDatesTableModel.setRowCount(0);

            // 获取设置对象
            Settings settings = settingsService.getSettings();
            if (settings == null) {
                System.err.println("无法获取设置对象");
                return;
            }
            if (settings.getSpecialDates() == null) {
                System.err.println("特殊日期列表为空");
                return;
            }

            // 获取特殊日期列表并按日期排序
            List<SpecialDate> specialDates = new ArrayList<>(settings.getSpecialDates());
            // 按日期排序
            specialDates.sort((date1, date2) -> {
                if (date1 == null || date1.getDate() == null)
                    return 1;
                if (date2 == null || date2.getDate() == null)
                    return -1;
                return date1.getDate().compareTo(date2.getDate());
            });

            // 填充表格数据
            double baseBudget = (settings != null) ? settings.getMonthlyBudget() : 0.0;

            for (SpecialDate specialDate : specialDates) {
                if (specialDate == null)
                    continue;

                // 计算预算影响
                double amountIncreaseValue = specialDate.getAmountIncrease();
                double adjustedBudget = baseBudget + amountIncreaseValue;
                String budgetEffect = String.format("%.2f + %.2f = %.2f",
                        baseBudget, amountIncreaseValue, adjustedBudget);

                // 生成重复类型文本
                String recurrenceTypeText = "一次性";
                if (specialDate.isRecurring()) {
                    switch (specialDate.getRecurrenceType()) {
                        case MONTHLY:
                            recurrenceTypeText = "每月" + specialDate.getDayOfMonth() + "日";
                            break;
                        case ANNUALLY:
                            recurrenceTypeText = "每年" + specialDate.getMonthOfYear() + "月" +
                                    specialDate.getDayOfMonth() + "日";
                            break;
                        default:
                            recurrenceTypeText = "一次性";
                    }
                }

                // 添加行到表格
                Object[] rowData = new Object[] {
                        specialDate.getId(),
                        specialDate.getName(),
                        specialDate.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        specialDate.getDescription(),
                        specialDate.getAffectedCategory(),
                        specialDate.getAmountIncrease(),
                        budgetEffect,
                        recurrenceTypeText
                };

                specialDatesTableModel.addRow(rowData);
            }

            // 刷新表格UI
            if (specialDatesTable != null) {
                specialDatesTable.repaint();
            }

        } catch (Exception e) {
            System.err.println("加载特殊日期时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the savings goals panel.
     * 
     * @return The savings goals panel
     */
    private JPanel createSavingsGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] goalColumns = { "Name", "Target Amount", "Current Amount", "Monthly Contribution", "Start Date",
                "Target Date", "Active" };
        savingGoalsTableModel = new DefaultTableModel(goalColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        savingGoalsTable = new JTable(savingGoalsTableModel);
        savingGoalsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        savingGoalsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = savingGoalsTable.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        showSavingGoalDetails(row);
                    }
                }
            }
        });
        JScrollPane tableScrollPane = new JScrollPane(savingGoalsTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editSelectedGoalButton = new JButton("Edit Selected");
        editSelectedGoalButton.addActionListener(e -> editSelectedSavingGoal());
        JButton deleteSelectedGoalButton = new JButton("Delete Selected");
        deleteSelectedGoalButton.addActionListener(e -> deleteSelectedSavingGoal());
        tableActionsPanel.add(editSelectedGoalButton);
        tableActionsPanel.add(deleteSelectedGoalButton);
        bottomPanel.add(tableActionsPanel, BorderLayout.NORTH);

        JPanel formOuterPanel = new JPanel(new BorderLayout(5, 5));
        formOuterPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel formFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints formFieldsGbc = new GridBagConstraints();
        formFieldsGbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsGbc.insets = new Insets(5, 5, 5, 5);
        formFieldsGbc.weightx = 1.0;
        formFieldsGbc.gridx = 0;
        formFieldsGbc.anchor = GridBagConstraints.WEST;

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(5, 5, 5, 5);
        fieldConstraints.weightx = 1.0;
        fieldConstraints.gridx = 1;

        int y = 0;

        formFieldsGbc.gridy = y;
        formFieldsPanel.add(new JLabel("Goal Name:"), formFieldsGbc);
        fieldConstraints.gridy = y++;
        sgNameField = new JTextField();
        formFieldsPanel.add(sgNameField, fieldConstraints);

        formFieldsGbc.gridy = y;
        formFieldsPanel.add(new JLabel("Description (Optional):"), formFieldsGbc);
        fieldConstraints.gridy = y++;
        sgDescriptionField = new JTextField();
        formFieldsPanel.add(sgDescriptionField, fieldConstraints);

        formFieldsGbc.gridy = y;
        formFieldsPanel.add(new JLabel("Target Amount:"), formFieldsGbc);
        sgTargetAmountSpinner = new JSpinner(new SpinnerNumberModel(1000.0, 0.0, Double.MAX_VALUE, 100.0));
        fieldConstraints.gridy = y++;
        formFieldsPanel.add(sgTargetAmountSpinner, fieldConstraints);

        formFieldsGbc.gridy = y;
        formFieldsPanel.add(new JLabel("Monthly Contribution:"), formFieldsGbc);
        sgMonthlyContributionSpinner = new JSpinner(new SpinnerNumberModel(50.0, 0.0, Double.MAX_VALUE, 10.0));
        fieldConstraints.gridy = y++;
        formFieldsPanel.add(sgMonthlyContributionSpinner, fieldConstraints);

        formFieldsGbc.gridy = y;
        formFieldsPanel.add(new JLabel("Start Date:"), formFieldsGbc);
        sgStartDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        sgStartDateSpinner.setEditor(new JSpinner.DateEditor(sgStartDateSpinner, "yyyy-MM-dd"));
        fieldConstraints.gridy = y++;
        formFieldsPanel.add(sgStartDateSpinner, fieldConstraints);

        formFieldsGbc.gridy = y;
        formFieldsPanel.add(new JLabel("Target Date (Optional):"), formFieldsGbc);
        sgTargetDateSpinner = new JSpinner(new SpinnerDateModel(
                Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()), null, null,
                Calendar.DAY_OF_MONTH));
        sgTargetDateSpinner.setEditor(new JSpinner.DateEditor(sgTargetDateSpinner, "yyyy-MM-dd"));
        fieldConstraints.gridy = y++;
        formFieldsPanel.add(sgTargetDateSpinner, fieldConstraints);

        formFieldsGbc.gridy = y;
        formFieldsPanel.add(new JLabel("Active Goal:"), formFieldsGbc);
        sgIsActiveCheckBox = new JCheckBox();
        sgIsActiveCheckBox.setSelected(true);
        fieldConstraints.gridy = y++;
        formFieldsPanel.add(sgIsActiveCheckBox, fieldConstraints);

        JScrollPane formFieldsScrollPane = new JScrollPane(formFieldsPanel);
        formFieldsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formOuterPanel.add(formFieldsScrollPane, BorderLayout.CENTER);

        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.addGoalButton = new JButton("Add New Goal");
        this.addGoalButton.addActionListener(e -> addSavingGoal());
        this.saveGoalButton = new JButton("Save Edited Goal");
        this.saveGoalButton.addActionListener(e -> saveEditedSavingGoal());
        this.saveGoalButton.setEnabled(false);
        JButton clearFormButton = new JButton("Clear Form / Cancel Edit");
        clearFormButton.addActionListener(e -> clearSavingGoalForm());

        formButtonPanel.add(addGoalButton);
        formButtonPanel.add(saveGoalButton);
        formButtonPanel.add(clearFormButton);
        formOuterPanel.add(formButtonPanel, BorderLayout.SOUTH);

        bottomPanel.add(formOuterPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        loadSavingGoals();
        return panel;
    }

    /**
     * Sets a savings goal.
     */
    private void setSavingsGoal() {
        JOptionPane.showMessageDialog(this, "Savings Goal functionality not yet fully implemented in Settings Panel.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Adds a new category to the settings based on the type.
     * 
     * @param type The type of category to add (EXPENSE or INCOME).
     */
    private void addCategory(CategoryType type) {
        String categoryName;
        List<String> categories;
        Settings settings = settingsService.getSettings();

        if (type == CategoryType.EXPENSE) {
            categoryName = newExpenseCategoryField.getText().trim();
            categories = settings.getExpenseCategories();
        } else {
            categoryName = newIncomeCategoryField.getText().trim();
            categories = settings.getIncomeCategories();
        }

        if (categoryName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a category name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (categories.contains(categoryName)) {
            JOptionPane.showMessageDialog(this, "Category already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (type == CategoryType.EXPENSE) {
            settings.addExpenseCategory(categoryName);
            newExpenseCategoryField.setText("");
        } else {
            settings.addIncomeCategory(categoryName);
            newIncomeCategoryField.setText("");
        }

        refreshCategoryDisplay();
        if (globalCategoryRefreshCallback != null)
            globalCategoryRefreshCallback.run();

        JOptionPane.showMessageDialog(this, "Category added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Deletes a category of the specified type.
     * 
     * @param categoryToDelete The name of the category to delete.
     * @param type             The type of category (EXPENSE or INCOME).
     */
    private void deleteCategory(String categoryToDelete, CategoryType type) {
        if (categoryToDelete == null || categoryToDelete.trim().isEmpty())
            return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the category: " + categoryToDelete + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Settings settings = settingsService.getSettings();
            boolean removed;

            if (type == CategoryType.EXPENSE) {
                if (settings.getExpenseCategories().size() <= 1) {
                    JOptionPane.showMessageDialog(this, "At least one expense category must be kept.", "Cannot Delete",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                removed = settings.removeExpenseCategory(categoryToDelete);
            } else {
                if (settings.getIncomeCategories().size() <= 1) {
                    JOptionPane.showMessageDialog(this, "At least one income category must be kept.", "Cannot Delete",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                removed = settings.removeIncomeCategory(categoryToDelete);
            }

            if (removed) {
                settingsService.saveSettings();
                refreshCategoryDisplay();
                if (globalCategoryRefreshCallback != null)
                    globalCategoryRefreshCallback.run();
                JOptionPane.showMessageDialog(this, "Category deleted successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Category not found or could not be deleted.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Refreshes the display of both expense and income categories.
     */
    public void refreshCategoryDisplay() {
        if (expenseCategoriesDisplayPanel == null || incomeCategoriesDisplayPanel == null || settingsService == null
                || settingsService.getSettings() == null) {
            return;
        }
        Settings settings = settingsService.getSettings();
        populateCategoryPanel(expenseCategoriesDisplayPanel, settings.getExpenseCategories(), CategoryType.EXPENSE);
        populateCategoryPanel(incomeCategoriesDisplayPanel, settings.getIncomeCategories(), CategoryType.INCOME);
        updateSpecialDateCategoryComboBox();
    }

    /**
     * Helper method to populate a given panel with category items.
     * 
     * @param displayPanel The JPanel to populate.
     * @param categories   The list of category strings.
     * @param type         The type of category (for the delete action).
     */
    private void populateCategoryPanel(JPanel displayPanel, List<String> categories, CategoryType type) {
        displayPanel.removeAll();
        if (categories != null) {
            for (String category : categories) {
                JPanel categoryItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

                JLabel categoryLabel = new JLabel(category);
                categoryItemPanel.add(categoryLabel);

                JButton deleteButton = new JButton("×");
                deleteButton.setFont(new Font(deleteButton.getFont().getName(), Font.BOLD, 12));
                deleteButton.setMargin(new Insets(0, 2, 0, 2));
                deleteButton.setToolTipText("Delete this category");
                deleteButton.addActionListener(e -> deleteCategory(category, type));
                categoryItemPanel.add(deleteButton);

                displayPanel.add(categoryItemPanel);
            }
        }
        displayPanel.revalidate();
        displayPanel.repaint();
    }

    /**
     * Resets settings to default values.
     */
    private void resetToDefault() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reset all settings to default values?", "Confirm Reset",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        settingsService.resetToDefault();

        loadSettingsData();

        JOptionPane.showMessageDialog(this, "Settings reset to default values.", "Success",
                JOptionPane.INFORMATION_MESSAGE);

        if (globalCategoryRefreshCallback != null)
            globalCategoryRefreshCallback.run();
        if (analysisRefreshCallback != null)
            analysisRefreshCallback.run();
        if (homePanelRefreshCallback != null)
            homePanelRefreshCallback.run();
    }

    /**
     * Saves all changes made in the settings panel.
     */
    private void saveChanges() {
        saveFinancialMonthSettings();

        settingsService.saveSettings();

        if (globalCategoryRefreshCallback != null)
            globalCategoryRefreshCallback.run();
        if (analysisRefreshCallback != null)
            analysisRefreshCallback.run();
        if (homePanelRefreshCallback != null)
            homePanelRefreshCallback.run();

        JOptionPane.showMessageDialog(this,
                "Settings changes (like Month Start Day) saved!\nMost other settings (categories, special dates, goals) are saved upon modification.",
                "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 保存财务月设置
     */
    private void saveFinancialMonthSettings() {
        try {
            int monthStartDay = (int) monthStartDaySpinner.getValue();
            Settings settings = settingsService.getSettings();
            settings.setMonthStartDay(monthStartDay);

            boolean saved = settingsService.saveSettings();

            if (saved) {
                JOptionPane.showMessageDialog(this,
                        "Financial month settings have been updated. New month start day: " + monthStartDay + ".",
                        "Settings Update", JOptionPane.INFORMATION_MESSAGE);
                if (analysisRefreshCallback != null)
                    analysisRefreshCallback.run();
                if (homePanelRefreshCallback != null)
                    homePanelRefreshCallback.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save financial month settings.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving financial month settings: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * 刷新类别列表
     */
    public void refreshCategoryList() {
        refreshCategoryDisplay();
    }

    public void loadSavingGoals() {
        if (savingGoalsTableModel == null || settingsService == null)
            return;
        savingGoalsTableModel.setRowCount(0);
        Settings settings = settingsService.getSettings();
        if (settings != null && settings.getSavingGoals() != null) {
            for (SavingGoal goal : settings.getSavingGoals()) {
                savingGoalsTableModel.addRow(new Object[] {
                        goal.getName(),
                        goal.getTargetAmount(),
                        goal.getCurrentAmount(),
                        goal.getMonthlyContribution(),
                        goal.getStartDate() != null ? goal.getStartDate().format(DateTimeFormatter.ISO_DATE) : "",
                        goal.getTargetDate() != null ? goal.getTargetDate().format(DateTimeFormatter.ISO_DATE) : "N/A",
                        goal.isActive() ? "Yes" : "No",
                        String.format("%.2f%%", goal.getProgressPercentage())
                });
            }
        }
    }

    private SavingGoal prepareSavingGoalFromInputs(SavingGoal existingGoal) {
        String name = sgNameField.getText();
        String description = sgDescriptionField.getText();

        double targetAmount = ((Number) sgTargetAmountSpinner.getValue()).doubleValue();
        double monthlyContributionRaw = ((Number) sgMonthlyContributionSpinner.getValue()).doubleValue();

        Date startDateRaw = (Date) sgStartDateSpinner.getValue();
        Date targetDateRaw = (Date) sgTargetDateSpinner.getValue();
        boolean isActive = sgIsActiveCheckBox.isSelected();

        if (name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Saving goal name cannot be empty.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (targetAmount <= 0) {
            JOptionPane.showMessageDialog(this, "Target amount must be greater than zero.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (startDateRaw == null) {
            JOptionPane.showMessageDialog(this, "Start date cannot be empty.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        LocalDate startDate = startDateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate targetDate = null;

        if (targetDateRaw != null) {
            LocalDate tempTargetDate = targetDateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (tempTargetDate.isAfter(startDate)) {
                targetDate = tempTargetDate;
            }
        }

        double finalMonthlyContribution = 0;
        LocalDate finalTargetDate = null;

        LocalDate initialTargetDateDefault = LocalDate.now().plusYears(1);
        boolean targetDateIsEffectivelyInitialDefault = false;
        if (targetDateRaw != null) {
            LocalDate currentTargetDateInSpinner = targetDateRaw.toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (currentTargetDateInSpinner.isEqual(initialTargetDateDefault)) {
                targetDateIsEffectivelyInitialDefault = true;
            }
        }

        boolean monthlyContributionEffectivelyProvided = monthlyContributionRaw > 0.001;
        boolean userHasExplicitlySetTargetDate = (targetDate != null && !targetDateIsEffectivelyInitialDefault);

        if (userHasExplicitlySetTargetDate) {
            finalTargetDate = targetDate;
            long numberOfMonths = ChronoUnit.MONTHS.between(startDate, finalTargetDate);

            if (numberOfMonths == 0 && startDate.isBefore(finalTargetDate)) {
                numberOfMonths = 1;
            }
            if (numberOfMonths <= 0) {
                JOptionPane.showMessageDialog(this,
                        "The period between start and target date must result in at least one contribution month.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (targetAmount <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Target amount must be greater than zero to calculate monthly contribution.", "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
            finalMonthlyContribution = targetAmount / numberOfMonths;

            sgMonthlyContributionSpinner.setValue(finalMonthlyContribution);
            if (finalTargetDate != null) {
                sgTargetDateSpinner
                        .setValue(Date.from(finalTargetDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            if (monthlyContributionEffectivelyProvided) {
                double tolerance = 0.01;
                if (Math.abs(finalMonthlyContribution - monthlyContributionRaw) > tolerance) {
                }
            }

        } else if (monthlyContributionEffectivelyProvided && !userHasExplicitlySetTargetDate) {
            finalMonthlyContribution = monthlyContributionRaw;
            if (finalMonthlyContribution <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Monthly contribution must be a positive value to calculate target date.", "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (targetAmount <= 0) {
                JOptionPane.showMessageDialog(this, "Target amount must be greater than zero to calculate target date.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            long numberOfMonths = (long) Math.ceil(targetAmount / finalMonthlyContribution);
            if (numberOfMonths <= 0)
                numberOfMonths = 1;
            finalTargetDate = startDate.plusMonths(numberOfMonths);

            sgTargetDateSpinner.setValue(Date.from(finalTargetDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            sgMonthlyContributionSpinner.setValue(finalMonthlyContribution);

        } else if (monthlyContributionEffectivelyProvided && userHasExplicitlySetTargetDate) {
            if (finalTargetDate == null)
                finalTargetDate = targetDate;
            if (finalMonthlyContribution == 0 && monthlyContributionRaw > 0.001)
                finalMonthlyContribution = monthlyContributionRaw;

            if (finalTargetDate == null || finalMonthlyContribution <= 0.001) {
                JOptionPane.showMessageDialog(this,
                        "Both target date and monthly contribution need to be effectively set to check for discrepancies or save.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            long monthsBetween = ChronoUnit.MONTHS.between(startDate, finalTargetDate);
            if (monthsBetween <= 0) {
                JOptionPane.showMessageDialog(this, "Target date must be after start date for a valid calculation.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (targetAmount <= 0) {
                JOptionPane.showMessageDialog(this, "Target amount must be greater than zero.", "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
            double expectedContribution = targetAmount / monthsBetween;
            double tolerance = 0.01;

            if (Math.abs(expectedContribution - finalMonthlyContribution) > tolerance) {
                int response = JOptionPane.showConfirmDialog(this,
                        String.format(
                                "Warning: Based on your inputs, the values may be inconsistent.\nTarget Amount: %.2f\nStart Date: %s\nTarget Date: %s (implies approx. %.2f monthly)\nMonthly Contribution: %.2f (implies reaching target in approx. %d months)\n\nDo you want to proceed with your entered/currently displayed values (Monthly: %.2f, Target Date: %s)?",
                                targetAmount, startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                finalTargetDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                expectedContribution,
                                finalMonthlyContribution,
                                (long) Math.ceil(targetAmount / finalMonthlyContribution),
                                finalMonthlyContribution,
                                finalTargetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                        "Potential Discrepancy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    return null;
                }
            }
            sgMonthlyContributionSpinner.setValue(finalMonthlyContribution);
            sgTargetDateSpinner.setValue(Date.from(finalTargetDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        } else {
            JOptionPane.showMessageDialog(this,
                    "Please provide either a positive Monthly Contribution or an explicit Target Date (after start date).",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        String id = (existingGoal != null) ? existingGoal.getId() : null;
        double currentAmount = (existingGoal != null) ? existingGoal.getCurrentAmount() : 0.0;
        String associatedAccount = (existingGoal != null) ? existingGoal.getAssociatedAccount() : null;

        if (finalMonthlyContribution < 0)
            finalMonthlyContribution = 0;

        return new SavingGoal(id, name, description, targetAmount, currentAmount, finalMonthlyContribution, startDate,
                finalTargetDate, isActive, associatedAccount);
    }

    private void addSavingGoal() {
        SavingGoal newGoal = prepareSavingGoalFromInputs(null);
        if (newGoal != null) {
            Settings settings = settingsService.getSettings();
            settings.addSavingGoal(newGoal);
            settingsService.saveSettings();
            loadSavingGoals();
            clearSavingGoalForm();
            if (analysisRefreshCallback != null)
                analysisRefreshCallback.run();
            if (homePanelRefreshCallback != null)
                homePanelRefreshCallback.run();
            JOptionPane.showMessageDialog(this, "New saving goal added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearSavingGoalForm() {
        sgNameField.setText("");
        sgDescriptionField.setText("");
        sgTargetAmountSpinner.setValue(1000.0);
        sgMonthlyContributionSpinner.setValue(50.0);
        sgStartDateSpinner.setValue(new Date());
        sgTargetDateSpinner
                .setValue(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        sgIsActiveCheckBox.setSelected(true);

        currentEditingSavingGoal = null;
        if (addGoalButton != null)
            addGoalButton.setEnabled(true);
        if (saveGoalButton != null)
            saveGoalButton.setEnabled(false);
        sgNameField.setEditable(true);
    }

    private void editSelectedSavingGoal() {
        int selectedRow = savingGoalsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a saving goal to edit.", "Selection Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String goalName = (String) savingGoalsTableModel.getValueAt(selectedRow, 0);
        Settings settings = settingsService.getSettings();
        SavingGoal goalToEdit = null;

        for (SavingGoal goal : settings.getSavingGoals()) {
            if (goal.getName().equals(goalName)) {
                goalToEdit = goal;
                break;
            }
        }

        if (goalToEdit != null) {
            currentEditingSavingGoal = goalToEdit;

            sgNameField.setText(goalToEdit.getName());
            sgDescriptionField.setText(goalToEdit.getDescription());
            sgTargetAmountSpinner.setValue(goalToEdit.getTargetAmount());
            sgMonthlyContributionSpinner.setValue(goalToEdit.getMonthlyContribution());
            sgStartDateSpinner
                    .setValue(Date.from(goalToEdit.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            if (goalToEdit.getTargetDate() != null) {
                sgTargetDateSpinner.setValue(
                        Date.from(goalToEdit.getTargetDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } else {
                sgTargetDateSpinner.setValue(
                        Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            sgIsActiveCheckBox.setSelected(goalToEdit.isActive());

            sgNameField.setEditable(false);
            if (addGoalButton != null)
                addGoalButton.setEnabled(false);
            if (saveGoalButton != null)
                saveGoalButton.setEnabled(true);

        } else {
            JOptionPane.showMessageDialog(this, "Could not find the selected saving goal for editing.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveEditedSavingGoal() {
        if (currentEditingSavingGoal == null) {
            JOptionPane.showMessageDialog(this, "No goal selected for editing or error in selection.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        SavingGoal updatedGoal = prepareSavingGoalFromInputs(currentEditingSavingGoal);
        if (updatedGoal != null) {
            Settings settings = settingsService.getSettings();
            settings.updateSavingGoal(updatedGoal);
            settingsService.saveSettings();
            loadSavingGoals();
            clearSavingGoalForm();
            if (analysisRefreshCallback != null)
                analysisRefreshCallback.run();
            if (homePanelRefreshCallback != null)
                homePanelRefreshCallback.run();
            JOptionPane.showMessageDialog(this, "Saving goal updated successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedSavingGoal() {
        int selectedRow = savingGoalsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a saving goal to delete.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String goalNameFromTable = (String) savingGoalsTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the saving goal: " + goalNameFromTable + "?", "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Settings settings = settingsService.getSettings();
        boolean removed = settings.getSavingGoals().removeIf(goal -> goal.getName().equals(goalNameFromTable)
                && goal.getId().equals(currentEditingSavingGoal != null ? currentEditingSavingGoal.getId() : ""));
        if (!removed) {
            removed = settings.getSavingGoals().removeIf(goal -> goal.getName().equals(goalNameFromTable));
        }

        if (removed) {
            settingsService.saveSettings();
            loadSavingGoals();
            clearSavingGoalForm();
            if (analysisRefreshCallback != null)
                analysisRefreshCallback.run();
            if (homePanelRefreshCallback != null)
                homePanelRefreshCallback.run();
            JOptionPane.showMessageDialog(this, "Saving goal deleted successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not find or delete the saving goal: " + goalNameFromTable,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createCategoryManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel expensePanel = new JPanel(new BorderLayout(5, 5));
        expensePanel.setBorder(BorderFactory.createTitledBorder("Expense Categories"));

        expenseCategoriesDisplayPanel = new JPanel();
        expenseCategoriesDisplayPanel.setLayout(new BoxLayout(expenseCategoriesDisplayPanel, BoxLayout.Y_AXIS));
        JScrollPane expenseScrollPane = new JScrollPane(expenseCategoriesDisplayPanel);

        JPanel addExpensePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newExpenseCategoryField = new JTextField(15);
        JButton addExpenseButton = new JButton("Add Expense Category");
        addExpenseButton.addActionListener(e -> addCategory(CategoryType.EXPENSE));
        addExpensePanel.add(new JLabel("New Expense Category:"));
        addExpensePanel.add(newExpenseCategoryField);
        addExpensePanel.add(addExpenseButton);

        expensePanel.add(expenseScrollPane, BorderLayout.CENTER);
        expensePanel.add(addExpensePanel, BorderLayout.SOUTH);
        panel.add(expensePanel);

        JPanel incomePanel = new JPanel(new BorderLayout(5, 5));
        incomePanel.setBorder(BorderFactory.createTitledBorder("Income Categories"));

        incomeCategoriesDisplayPanel = new JPanel();
        incomeCategoriesDisplayPanel.setLayout(new BoxLayout(incomeCategoriesDisplayPanel, BoxLayout.Y_AXIS));
        JScrollPane incomeScrollPane = new JScrollPane(incomeCategoriesDisplayPanel);

        JPanel addIncomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newIncomeCategoryField = new JTextField(15);
        JButton addIncomeButton = new JButton("Add Income Category");
        addIncomeButton.addActionListener(e -> addCategory(CategoryType.INCOME));
        addIncomePanel.add(new JLabel("New Income Category:"));
        addIncomePanel.add(newIncomeCategoryField);
        addIncomePanel.add(addIncomeButton);

        incomePanel.add(incomeScrollPane, BorderLayout.CENTER);
        incomePanel.add(addIncomePanel, BorderLayout.SOUTH);
        panel.add(Box.createVerticalStrut(10));
        panel.add(incomePanel);

        refreshCategoryDisplay();
        return panel;
    }

    private JPanel createMonthStartDayPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Financial Month Start Day"));

        panel.add(new JLabel("Select the day your financial month starts:"));
        monthStartDaySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 28, 1));
        panel.add(monthStartDaySpinner);

        return panel;
    }

    private JPanel createMonthEndClosingPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton performClosingButton = new JButton("Perform Month-End Closing");
        performClosingButton.setToolTipText(
                "Calculates surplus of previous financial month(s) and adds to Overall Account Balance.");
        performClosingButton.addActionListener(e -> performMonthEndClosingAction());

        panel.add(performClosingButton);
        return panel;
    }

    private void performMonthEndClosingAction() {
        if (settingsService == null || financialCycleService == null) {
            JOptionPane.showMessageDialog(this, "Required services not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to perform month-end closing?\n" +
                        "This will process recurring transactions, auto-savings, etc., based on your current settings.\n"
                        +
                        "This action cannot be undone easily.",
                "Confirm Month-End Closing",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            boolean result = financialCycleService.performMonthEndClosing();
            if (result) {
                JOptionPane.showMessageDialog(this, "Month-end closing process completed successfully!",
                        "Month-End Closing Success", JOptionPane.INFORMATION_MESSAGE);
                if (homePanelRefreshCallback != null)
                    homePanelRefreshCallback.run();
                if (analysisRefreshCallback != null)
                    analysisRefreshCallback.run();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Month-end closing process failed or was partially completed. Check logs for details.",
                        "Month-End Closing Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 更新特殊日期分类下拉框的选项
     */
    private void updateSpecialDateCategoryComboBox() {
        try {
            // 检查组件是否初始化
            if (specialDateCategoryComboBox == null) {
                System.err.println("特殊日期分类下拉框未初始化");
                return;
            }

            // 检查服务是否可用
            if (settingsService == null) {
                System.err.println("设置服务未初始化");
                return;
            }

            Settings settings = settingsService.getSettings();
            if (settings == null) {
                System.err.println("无法获取设置对象");
                return;
            }

            // 保存当前选中项
            String previouslySelected = null;
            if (specialDateCategoryComboBox.getSelectedItem() != null) {
                previouslySelected = specialDateCategoryComboBox.getSelectedItem().toString();
            }

            // 清空并重新填充选项
            specialDateCategoryComboBox.removeAllItems();

            List<String> expenseCategories = settings.getExpenseCategories();
            if (expenseCategories != null && !expenseCategories.isEmpty()) {
                for (String category : expenseCategories) {
                    if (category != null && !category.trim().isEmpty()) {
                        specialDateCategoryComboBox.addItem(category);
                    }
                }
            }

            // 恢复之前的选择或默认选择第一项
            if (previouslySelected != null) {
                specialDateCategoryComboBox.setSelectedItem(previouslySelected);
            } else if (specialDateCategoryComboBox.getItemCount() > 0) {
                specialDateCategoryComboBox.setSelectedIndex(0);
            }

        } catch (Exception e) {
            System.err.println("更新特殊日期分类下拉框时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSavingGoalDetails(int rowIndex) {
        if (settingsService == null || settingsService.getSettings() == null
                || settingsService.getSettings().getSavingGoals() == null) {
            System.err.println("Settings or saving goals not available for details view.");
            return;
        }
        List<SavingGoal> goals = settingsService.getSettings().getSavingGoals();
        SavingGoal goalToShow = null;
        if (rowIndex >= 0 && rowIndex < savingGoalsTableModel.getRowCount()) {
            String goalNameFromTable = (String) savingGoalsTableModel.getValueAt(rowIndex, 0);
            for (SavingGoal g : goals) {
                if (g.getName().equals(goalNameFromTable)) {
                    goalToShow = g;
                    break;
                }
            }
        } else {
            System.err.println("Row index out of bounds for saving goal details based on table model.");
            return;
        }

        if (goalToShow == null) {
            JOptionPane.showMessageDialog(this, "Could not retrieve details for the selected saving goal.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("<html><body>");
        details.append("<h1>Saving Goal Details</h1>");
        details.append("<p><b>Name:</b> ").append(goalToShow.getName()).append("</p>");
        details.append("<p><b>Description:</b> ")
                .append(goalToShow.getDescription() != null ? goalToShow.getDescription() : "N/A").append("</p>");
        details.append(String.format("<p><b>Target Amount:</b> %.2f</p>", goalToShow.getTargetAmount()));
        details.append(String.format("<p><b>Current Amount:</b> %.2f (%.2f%%)</p>", goalToShow.getCurrentAmount(),
                goalToShow.getProgressPercentage()));
        details.append(String.format("<p><b>Monthly Contribution:</b> %.2f</p>", goalToShow.getMonthlyContribution()));
        details.append("<p><b>Start Date:</b> ")
                .append(goalToShow.getStartDate() != null
                        ? goalToShow.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        : "N/A")
                .append("</p>");
        details.append("<p><b>Target Date:</b> ")
                .append(goalToShow.getTargetDate() != null
                        ? goalToShow.getTargetDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        : "N/A")
                .append("</p>");
        details.append("<p><b>Active:</b> ").append(goalToShow.isActive() ? "Yes" : "No").append("</p>");
        details.append("</body></html>");

        JOptionPane.showMessageDialog(this, details.toString(), "Saving Goal: " + goalToShow.getName(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean isThemeable() {
        return true;
    }

    /**
     * 应用主题设置到整个设置面板
     * 
     * @param settings 设置对象
     */
    public void applyTheme(Settings settings) {
        boolean isDark = settings.isDarkModeEnabled();
        Color backgroundColor = isDark ? new Color(50, 50, 55) : UIManager.getColor("Panel.background");
        Color foregroundColor = isDark ? Color.LIGHT_GRAY : UIManager.getColor("Label.foreground");
        Color textFieldBg = isDark ? new Color(60, 60, 60) : UIManager.getColor("TextField.background");
        Color tableHeaderBg = isDark ? new Color(70, 70, 70) : new Color(220, 220, 220);
        Color tableHeaderFg = isDark ? Color.WHITE : Color.BLACK;
        Color tableBg = isDark ? new Color(60, 63, 65) : Color.WHITE;
        Color tableFg = isDark ? Color.LIGHT_GRAY : Color.BLACK;

        // 设置主面板背景
        this.setBackground(backgroundColor);

        // 处理标签页和滚动面板
        if (tabbedPane != null) {
            tabbedPane.setBackground(backgroundColor);
            tabbedPane.setForeground(foregroundColor);

            // 处理所有标签页
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Component comp = tabbedPane.getComponentAt(i);
                // 处理滚动面板
                if (comp instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) comp;
                    scrollPane.setBackground(backgroundColor);
                    scrollPane.getViewport().setBackground(backgroundColor);

                    // 获取滚动面板中的内容并应用主题
                    Component view = scrollPane.getViewport().getView();
                    if (view instanceof JPanel) {
                        themeGenericPanel((JPanel) view, settings);
                    }
                } else if (comp instanceof JPanel) {
                    themeGenericPanel((JPanel) comp, settings);
                }
            }
        }

        // 处理表格
        if (specialDatesTable != null) {
            specialDatesTable.setBackground(tableBg);
            specialDatesTable.setForeground(tableFg);
            specialDatesTable.getTableHeader().setBackground(tableHeaderBg);
            specialDatesTable.getTableHeader().setForeground(tableHeaderFg);
            if (specialDatesTable.getParent() instanceof JViewport) {
                ((JViewport) specialDatesTable.getParent()).setBackground(tableBg);
            }
        }

        if (savingGoalsTable != null) {
            savingGoalsTable.setBackground(tableBg);
            savingGoalsTable.setForeground(tableFg);
            savingGoalsTable.getTableHeader().setBackground(tableHeaderBg);
            savingGoalsTable.getTableHeader().setForeground(tableHeaderFg);
            if (savingGoalsTable.getParent() instanceof JViewport) {
                ((JViewport) savingGoalsTable.getParent()).setBackground(tableBg);
            }
        }

        // 处理其他主面板中的组件
        for (Component c : getComponents()) {
            if (c instanceof JPanel) {
                themeGenericPanel((JPanel) c, settings);
            }
        }

        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    private void themeGenericPanel(JPanel panel, Settings settings) {
        if (panel == null)
            return;

        boolean isDark = settings.isDarkModeEnabled();
        Color backgroundColor = isDark ? new Color(50, 50, 55) : UIManager.getColor("Panel.background");
        Color foregroundColor = isDark ? Color.LIGHT_GRAY : UIManager.getColor("Label.foreground");
        Color textFieldBg = isDark ? new Color(60, 60, 60) : UIManager.getColor("TextField.background");
        Color buttonBg = isDark ? new Color(80, 80, 80) : UIManager.getColor("Button.background");

        panel.setBackground(backgroundColor);
        panel.setForeground(foregroundColor);

        for (Component comp : panel.getComponents()) {
            try {
                comp.setForeground(foregroundColor);
                if (comp instanceof JLabel || comp instanceof JCheckBox) {
                    comp.setBackground(backgroundColor);
                } else if (comp instanceof JTextField || comp instanceof JSpinner) {
                    comp.setBackground(textFieldBg);
                } else if (comp instanceof JButton) {
                    comp.setBackground(buttonBg);
                } else if (comp instanceof JComboBox) {
                    comp.setBackground(textFieldBg);
                } else if (comp instanceof JPanel) {
                    themeGenericPanel((JPanel) comp, settings);
                } else if (comp instanceof JScrollPane) {
                    JScrollPane scroll = (JScrollPane) comp;
                    scroll.setBackground(backgroundColor);
                    scroll.getViewport().setBackground(backgroundColor);

                    Component viewportView = scroll.getViewport().getView();
                    if (viewportView != null) {
                        if (viewportView instanceof JPanel) {
                            themeGenericPanel((JPanel) viewportView, settings);
                        } else {
                            viewportView.setBackground(isDark ? new Color(55, 55, 60) : backgroundColor);
                            viewportView.setForeground(foregroundColor);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("应用主题到组件失败: " + comp.getClass().getName() + " - " + e.getMessage());
            }
        }
    }

    public void loadSettingsData() {
        if (settingsService == null)
            return;
        Settings settings = settingsService.getSettings();
        if (settings == null)
            return;

        if (monthStartDaySpinner != null) {
            monthStartDaySpinner.setValue(settings.getMonthStartDay());
        }

        refreshCategoryDisplay();

        loadSpecialDates();

        loadSavingGoals();

        updateSpecialDateCategoryComboBox();

        System.out.println("SettingsPanel: All settings data loaded into UI.");
    }
}

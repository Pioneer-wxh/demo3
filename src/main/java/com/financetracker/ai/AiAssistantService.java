package com.financetracker.ai;

import com.financetracker.model.Transaction;
import com.financetracker.service.TransactionService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for AI-assisted analysis.
 */
public class AiAssistantService {
    
    /**
     * Gets a response to a user query.
     * 
     * @param query The user query
     * @param transactionService The transaction service
     * @return The AI response
     */
    public String getResponse(String query, TransactionService transactionService) {
        // Normalize query
        String normalizedQuery = query.toLowerCase().trim();
        
        // Get transactions
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        List<Transaction> currentMonthTransactions = transactionService.getTransactionsForCurrentMonth();
        
        // Check for specific query patterns
        if (normalizedQuery.contains("save") || normalizedQuery.contains("saving")) {
            return getSavingAdvice(allTransactions, currentMonthTransactions, transactionService);
        } else if (normalizedQuery.contains("spend") || normalizedQuery.contains("spending") || normalizedQuery.contains("habit")) {
            return getSpendingHabitsAnalysis(allTransactions, transactionService);
        } else if (normalizedQuery.contains("budget") || normalizedQuery.contains("next month")) {
            return getBudgetAdvice(allTransactions, transactionService);
        } else if (normalizedQuery.contains("income") || normalizedQuery.contains("earn")) {
            return getIncomeAnalysis(allTransactions, transactionService);
        } else if (normalizedQuery.contains("expense") || normalizedQuery.contains("cost")) {
            return getExpenseAnalysis(allTransactions, transactionService);
        } else {
            return getGeneralAdvice(allTransactions, transactionService);
        }
    }
    
    /**
     * Gets saving advice.
     * 
     * @param allTransactions All transactions
     * @param currentMonthTransactions Current month transactions
     * @param transactionService The transaction service
     * @return The saving advice
     */
    private String getSavingAdvice(List<Transaction> allTransactions, List<Transaction> currentMonthTransactions, TransactionService transactionService) {
        StringBuilder response = new StringBuilder();
        response.append("Here are some tips to help you save more money:\n\n");
        
        // Calculate current month spending
        double currentMonthExpense = transactionService.getTotalExpense(currentMonthTransactions);
        double currentMonthIncome = transactionService.getTotalIncome(currentMonthTransactions);
        double currentMonthSavings = currentMonthIncome - currentMonthExpense;
        double savingsRate = currentMonthIncome > 0 ? (currentMonthSavings / currentMonthIncome) * 100 : 0;
        
        response.append(String.format("Your current month savings rate is %.1f%% (%.2f out of %.2f income).\n\n", 
                savingsRate, currentMonthSavings, currentMonthIncome));
        
        // Identify top expense categories
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction transaction : currentMonthTransactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
            }
        }
        
        // Sort categories by expense amount
        List<Map.Entry<String, Double>> sortedCategories = categoryExpenses.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        if (!sortedCategories.isEmpty()) {
            response.append("Your top expense categories this month:\n");
            for (int i = 0; i < Math.min(3, sortedCategories.size()); i++) {
                Map.Entry<String, Double> entry = sortedCategories.get(i);
                response.append(String.format("%d. %s: %.2f (%.1f%% of expenses)\n", 
                        i + 1, entry.getKey(), entry.getValue(), (entry.getValue() / currentMonthExpense) * 100));
            }
            response.append("\n");
            
            // Provide category-specific advice
            if (!sortedCategories.isEmpty()) {
                String topCategory = sortedCategories.get(0).getKey();
                response.append("Tips for reducing ").append(topCategory).append(" expenses:\n");
                
                if (topCategory.equalsIgnoreCase("Food")) {
                    response.append("- Cook at home more often instead of eating out\n");
                    response.append("- Plan your meals and make a shopping list to avoid impulse purchases\n");
                    response.append("- Buy groceries in bulk when on sale\n");
                } else if (topCategory.equalsIgnoreCase("Entertainment")) {
                    response.append("- Look for free or low-cost entertainment options\n");
                    response.append("- Use streaming services instead of cable TV\n");
                    response.append("- Take advantage of discounts and promotions\n");
                } else if (topCategory.equalsIgnoreCase("Shopping")) {
                    response.append("- Wait 24 hours before making non-essential purchases\n");
                    response.append("- Look for sales and use coupons\n");
                    response.append("- Consider buying second-hand items\n");
                } else if (topCategory.equalsIgnoreCase("Transportation")) {
                    response.append("- Use public transportation when possible\n");
                    response.append("- Carpool with colleagues or friends\n");
                    response.append("- Maintain your vehicle to prevent costly repairs\n");
                } else {
                    response.append("- Review your spending in this category and identify non-essential expenses\n");
                    response.append("- Look for more affordable alternatives\n");
                    response.append("- Set a budget for this category and stick to it\n");
                }
                response.append("\n");
            }
        }
        
        // General saving tips
        response.append("General saving tips:\n");
        response.append("- Set up automatic transfers to a savings account\n");
        response.append("- Follow the 50/30/20 rule: 50% for needs, 30% for wants, 20% for savings\n");
        response.append("- Track your expenses regularly to identify areas for improvement\n");
        response.append("- Consider using cash for discretionary spending to make it more tangible\n");
        response.append("- Review and cancel unused subscriptions\n");
        
        return response.toString();
    }
    
    /**
     * Gets spending habits analysis.
     * 
     * @param transactions The transactions to analyze
     * @param transactionService The transaction service
     * @return The spending habits analysis
     */
    private String getSpendingHabitsAnalysis(List<Transaction> transactions, TransactionService transactionService) {
        StringBuilder response = new StringBuilder();
        response.append("Here's an analysis of your spending habits:\n\n");
        
        // Calculate total income and expenses
        double totalIncome = transactionService.getTotalIncome(transactions);
        double totalExpense = transactionService.getTotalExpense(transactions);
        double savingsRate = totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome) * 100 : 0;
        
        response.append(String.format("Overall savings rate: %.1f%%\n", savingsRate));
        response.append(String.format("Total income: %.2f\n", totalIncome));
        response.append(String.format("Total expenses: %.2f\n\n", totalExpense));
        
        // Identify top expense categories
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction transaction : transactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
            }
        }
        
        // Sort categories by expense amount
        List<Map.Entry<String, Double>> sortedCategories = categoryExpenses.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        if (!sortedCategories.isEmpty()) {
            response.append("Your top expense categories:\n");
            for (int i = 0; i < Math.min(5, sortedCategories.size()); i++) {
                Map.Entry<String, Double> entry = sortedCategories.get(i);
                response.append(String.format("%d. %s: %.2f (%.1f%% of expenses)\n", 
                        i + 1, entry.getKey(), entry.getValue(), (entry.getValue() / totalExpense) * 100));
            }
            response.append("\n");
        }
        
        // Analyze spending patterns by day of week
        Map<String, Double> dayOfWeekExpenses = new HashMap<>();
        Map<String, Integer> dayOfWeekCounts = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            if (transaction.isExpense()) {
                String dayOfWeek = transaction.getDate().getDayOfWeek().toString();
                double amount = transaction.getAmount();
                dayOfWeekExpenses.put(dayOfWeek, dayOfWeekExpenses.getOrDefault(dayOfWeek, 0.0) + amount);
                dayOfWeekCounts.put(dayOfWeek, dayOfWeekCounts.getOrDefault(dayOfWeek, 0) + 1);
            }
        }
        
        if (!dayOfWeekExpenses.isEmpty()) {
            response.append("Spending patterns by day of week:\n");
            for (Map.Entry<String, Double> entry : dayOfWeekExpenses.entrySet()) {
                String dayOfWeek = entry.getKey();
                double totalAmount = entry.getValue();
                int count = dayOfWeekCounts.get(dayOfWeek);
                double averageAmount = count > 0 ? totalAmount / count : 0;
                
                response.append(String.format("%s: %.2f total, %.2f average per transaction\n", 
                        dayOfWeek, totalAmount, averageAmount));
            }
            response.append("\n");
        }
        
        // Provide insights and recommendations
        response.append("Insights and recommendations:\n");
        
        if (savingsRate < 10) {
            response.append("- Your savings rate is low. Consider increasing your income or reducing expenses.\n");
        } else if (savingsRate < 20) {
            response.append("- Your savings rate is moderate. Aim for at least 20% to build a strong financial foundation.\n");
        } else {
            response.append("- Your savings rate is good. Keep up the good work!\n");
        }
        
        if (!sortedCategories.isEmpty()) {
            String topCategory = sortedCategories.get(0).getKey();
            double topCategoryPercentage = (sortedCategories.get(0).getValue() / totalExpense) * 100;
            
            if (topCategoryPercentage > 40) {
                response.append(String.format("- Your %s expenses are very high (%.1f%% of total). Consider ways to reduce this.\n", 
                        topCategory, topCategoryPercentage));
            } else if (topCategoryPercentage > 30) {
                response.append(String.format("- Your %s expenses are significant (%.1f%% of total). Look for ways to optimize this category.\n", 
                        topCategory, topCategoryPercentage));
            }
        }
        
        response.append("- Track your expenses regularly to maintain awareness of your spending habits.\n");
        response.append("- Set specific financial goals to stay motivated.\n");
        response.append("- Consider using budgeting tools or apps to help manage your finances.\n");
        
        return response.toString();
    }
    
    /**
     * Gets budget advice.
     * 
     * @param transactions The transactions to analyze
     * @param transactionService The transaction service
     * @return The budget advice
     */
    private String getBudgetAdvice(List<Transaction> transactions, TransactionService transactionService) {
        StringBuilder response = new StringBuilder();
        response.append("Here's a suggested budget for next month based on your spending history:\n\n");
        
        // Calculate average monthly income and expenses
        LocalDate today = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(6);
        
        List<Transaction> recentTransactions = transactions.stream()
                .filter(t -> !t.getDate().isBefore(sixMonthsAgo))
                .collect(Collectors.toList());
        
        double totalIncome = transactionService.getTotalIncome(recentTransactions);
        double totalExpense = transactionService.getTotalExpense(recentTransactions);
        
        double monthlyIncome = totalIncome / 6;
        double monthlyExpense = totalExpense / 6;
        
        response.append(String.format("Projected Monthly Income: %.2f\n", monthlyIncome));
        response.append(String.format("Projected Monthly Expenses: %.2f\n", monthlyExpense));
        response.append(String.format("Projected Monthly Savings: %.2f\n\n", monthlyIncome - monthlyExpense));
        
        // Calculate category distribution
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction transaction : recentTransactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
            }
        }
        
        // Calculate category percentages
        Map<String, Double> categoryPercentages = new HashMap<>();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            categoryPercentages.put(entry.getKey(), entry.getValue() / totalExpense);
        }
        
        // Generate budget allocation
        response.append("Suggested Budget Allocation:\n");
        
        for (Map.Entry<String, Double> entry : categoryPercentages.entrySet()) {
            double budgetAmount = monthlyIncome * entry.getValue();
            response.append(String.format("%s: %.2f (%.1f%% of income)\n", 
                    entry.getKey(), budgetAmount, entry.getValue() * 100));
        }
        
        response.append("\n");
        
        // Provide budgeting tips
        response.append("Budgeting Tips:\n");
        response.append("- Use the 50/30/20 rule as a guideline: 50% for needs, 30% for wants, 20% for savings\n");
        response.append("- Track your expenses regularly to ensure you're staying within budget\n");
        response.append("- Adjust your budget as needed based on changing circumstances\n");
        response.append("- Consider using envelope budgeting or separate accounts for different categories\n");
        response.append("- Build an emergency fund of 3-6 months of expenses\n");
        response.append("- Review and update your budget at the beginning of each month\n");
        
        return response.toString();
    }
    
    /**
     * Gets income analysis.
     * 
     * @param transactions The transactions to analyze
     * @param transactionService The transaction service
     * @return The income analysis
     */
    private String getIncomeAnalysis(List<Transaction> transactions, TransactionService transactionService) {
        StringBuilder response = new StringBuilder();
        response.append("Here's an analysis of your income:\n\n");
        
        // Filter income transactions
        List<Transaction> incomeTransactions = transactions.stream()
                .filter(t -> !t.isExpense())
                .collect(Collectors.toList());
        
        // Calculate total income
        double totalIncome = transactionService.getTotalIncome(transactions);
        
        response.append(String.format("Total Income: %.2f\n", totalIncome));
        response.append(String.format("Number of Income Transactions: %d\n", incomeTransactions.size()));
        
        if (!incomeTransactions.isEmpty()) {
            double averageIncome = totalIncome / incomeTransactions.size();
            response.append(String.format("Average Income per Transaction: %.2f\n\n", averageIncome));
        }
        
        // Analyze income by month
        Map<String, Double> monthlyIncome = new HashMap<>();
        
        for (Transaction transaction : incomeTransactions) {
            String month = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            double amount = transaction.getAmount();
            monthlyIncome.put(month, monthlyIncome.getOrDefault(month, 0.0) + amount);
        }
        
        if (!monthlyIncome.isEmpty()) {
            response.append("Income by Month:\n");
            
            List<Map.Entry<String, Double>> sortedMonthlyIncome = monthlyIncome.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());
            
            for (Map.Entry<String, Double> entry : sortedMonthlyIncome) {
                response.append(String.format("%s: %.2f\n", entry.getKey(), entry.getValue()));
            }
            
            response.append("\n");
        }
        
        // Provide income insights and recommendations
        response.append("Income Insights and Recommendations:\n");
        
        if (incomeTransactions.isEmpty()) {
            response.append("- No income transactions found. Make sure to record all sources of income.\n");
        } else {
            // Check income consistency
            if (monthlyIncome.size() >= 2) {
                double minIncome = Double.MAX_VALUE;
                double maxIncome = Double.MIN_VALUE;
                
                for (double income : monthlyIncome.values()) {
                    minIncome = Math.min(minIncome, income);
                    maxIncome = Math.max(maxIncome, income);
                }
                
                double incomeVariation = (maxIncome - minIncome) / maxIncome;
                
                if (incomeVariation > 0.3) {
                    response.append("- Your income varies significantly from month to month. Consider building a larger emergency fund.\n");
                } else {
                    response.append("- Your income is relatively stable. This is good for financial planning.\n");
                }
            }
            
            // Check income sources
            Map<String, Double> incomeBySource = new HashMap<>();
            
            for (Transaction transaction : incomeTransactions) {
                String source = transaction.getDescription();
                double amount = transaction.getAmount();
                incomeBySource.put(source, incomeBySource.getOrDefault(source, 0.0) + amount);
            }
            
            if (incomeBySource.size() == 1) {
                response.append("- You have a single source of income. Consider developing multiple income streams for financial security.\n");
            } else {
                response.append(String.format("- You have %d different income sources. This diversification is good for financial stability.\n", 
                        incomeBySource.size()));
            }
        }
        
        response.append("- Consider ways to increase your income, such as asking for a raise, developing new skills, or starting a side business.\n");
        response.append("- Make sure to save and invest a portion of your income for long-term financial goals.\n");
        response.append("- Review your tax situation to ensure you're taking advantage of all available deductions and credits.\n");
        
        return response.toString();
    }
    
    /**
     * Gets expense analysis.
     * 
     * @param transactions The transactions to analyze
     * @param transactionService The transaction service
     * @return The expense analysis
     */
    private String getExpenseAnalysis(List<Transaction> transactions, TransactionService transactionService) {
        StringBuilder response = new StringBuilder();
        response.append("Here's an analysis of your expenses:\n\n");
        
        // Filter expense transactions
        List<Transaction> expenseTransactions = transactions.stream()
                .filter(Transaction::isExpense)
                .collect(Collectors.toList());
        
        // Calculate total expenses
        double totalExpense = transactionService.getTotalExpense(transactions);
        
        response.append(String.format("Total Expenses: %.2f\n", totalExpense));
        response.append(String.format("Number of Expense Transactions: %d\n", expenseTransactions.size()));
        
        if (!expenseTransactions.isEmpty()) {
            double averageExpense = totalExpense / expenseTransactions.size();
            response.append(String.format("Average Expense per Transaction: %.2f\n\n", averageExpense));
        }
        
        // Analyze expenses by category
        Map<String, Double> categoryExpenses = new HashMap<>();
        
        for (Transaction transaction : expenseTransactions) {
            String category = transaction.getCategory();
            double amount = transaction.getAmount();
            categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
        }
        
        if (!categoryExpenses.isEmpty()) {
            response.append("Expenses by Category:\n");
            
            List<Map.Entry<String, Double>> sortedCategoryExpenses = categoryExpenses.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());
            
            for (Map.Entry<String, Double> entry : sortedCategoryExpenses) {
                response.append(String.format("%s: %.2f (%.1f%%)\n", 
                        entry.getKey(), entry.getValue(), (entry.getValue() / totalExpense) * 100));
            }
            
            response.append("\n");
        }
        
        // Analyze expenses by month
        Map<String, Double> monthlyExpenses = new HashMap<>();
        
        for (Transaction transaction : expenseTransactions) {
            String month = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            double amount = transaction.getAmount();
            monthlyExpenses.put(month, monthlyExpenses.getOrDefault(month, 0.0) + amount);
        }
        
        if (!monthlyExpenses.isEmpty()) {
            response.append("Expenses by Month:\n");
            
            List<Map.Entry<String, Double>> sortedMonthlyExpenses = monthlyExpenses.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());
            
            for (Map.Entry<String, Double> entry : sortedMonthlyExpenses) {
                response.append(String.format("%s: %.2f\n", entry.getKey(), entry.getValue()));
            }
            
            response.append("\n");
        }
        
        // Provide expense insights and recommendations
        response.append("Expense Insights and Recommendations:\n");
        
        if (!categoryExpenses.isEmpty()) {
            // Identify top expense category
            Map.Entry<String, Double> topCategory = categoryExpenses.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            
            if (topCategory != null) {
                double topCategoryPercentage = (topCategory.getValue() / totalExpense) * 100;
                
                response.append(String.format("- Your highest expense category is %s (%.1f%% of total expenses).\n", 
                        topCategory.getKey(), topCategoryPercentage));
                
                if (topCategoryPercentage > 40) {
                    response.append("  This category dominates your expenses. Look for ways to reduce spending in this area.\n");
                }
            }
        }
        
        if (!monthlyExpenses.isEmpty() && monthlyExpenses.size() >= 2) {
            // Check for expense trends
            List<Map.Entry<String, Double>> sortedMonthlyExpenses = monthlyExpenses.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());
            
            if (sortedMonthlyExpenses.size() >= 2) {
                double firstMonthExpense = sortedMonthlyExpenses.get(0).getValue();
                double lastMonthExpense = sortedMonthlyExpenses.get(sortedMonthlyExpenses.size() - 1).getValue();
                
                double expenseChange = (lastMonthExpense - firstMonthExpense) / firstMonthExpense * 100;
                
                if (expenseChange > 10) {
                    response.append(String.format("- Your expenses have increased by %.1f%% from %s to %s. Review your spending habits.\n", 
                            expenseChange, sortedMonthlyExpenses.get(0).getKey(), sortedMonthlyExpenses.get(sortedMonthlyExpenses.size() - 1).getKey()));
                } else if (expenseChange < -10) {
                    response.append(String.format("- Your expenses have decreased by %.1f%% from %s to %s. Good job!\n", 
                            -expenseChange, sortedMonthlyExpenses.get(0).getKey(), sortedMonthlyExpenses.get(sortedMonthlyExpenses.size() - 1).getKey()));
                } else {
                    response.append("- Your expenses have been relatively stable over time.\n");
                }
            }
        }
        
        response.append("- Review your recurring expenses and subscriptions to identify potential savings.\n");
        response.append("- Consider using cash for discretionary spending to make it more tangible.\n");
        response.append("- Set specific spending limits for each category and track your progress.\n");
        response.append("- Look for ways to reduce expenses in your top spending categories.\n");
        
        return response.toString();
    }
    
    /**
     * Gets general advice.
     * 
     * @param transactions The transactions to analyze
     * @param transactionService The transaction service
     * @return The general advice
     */
    private String getGeneralAdvice(List<Transaction> transactions, TransactionService transactionService) {
        StringBuilder response = new StringBuilder();
        response.append("Here's a general financial analysis and advice based on your transaction history:\n\n");
        
        // Calculate total income and expenses
        double totalIncome = transactionService.getTotalIncome(transactions);
        double totalExpense = transactionService.getTotalExpense(transactions);
        double netAmount = totalIncome - totalExpense;
        double savingsRate = totalIncome > 0 ? (netAmount / totalIncome) * 100 : 0;
        
        response.append(String.format("Total Income: %.2f\n", totalIncome));
        response.append(String.format("Total Expenses: %.2f\n", totalExpense));
        response.append(String.format("Net Amount: %.2f\n", netAmount));
        response.append(String.format("Savings Rate: %.1f%%\n\n", savingsRate));
        
        // Provide financial health assessment
        response.append("Financial Health Assessment:\n");
        
        if (savingsRate < 0) {
            response.append("- You're spending more than you earn. This is not sustainable in the long term.\n");
            response.append("- Focus on increasing income or reducing expenses to achieve a positive savings rate.\n");
        } else if (savingsRate < 10) {
            response.append("- Your savings rate is low. Aim to save at least 10-15% of your income.\n");
            response.append("- Look for ways to reduce expenses or increase income to improve your savings rate.\n");
        } else if (savingsRate < 20) {
            response.append("- Your savings rate is moderate. This is a good start, but aim for 20% or more for long-term financial security.\n");
            response.append("- Continue to look for ways to optimize your spending and increase your income.\n");
        } else {
            response.append("- Your savings rate is good. Keep up the good work!\n");
            response.append("- Consider investing your savings for long-term growth.\n");
        }
        
        // Provide general financial advice
        response.append("\nGeneral Financial Advice:\n");
        response.append("- Build an emergency fund of 3-6 months of expenses.\n");
        response.append("- Pay off high-interest debt as quickly as possible.\n");
        response.append("- Save for retirement by contributing to retirement accounts.\n");
        response.append("- Invest in a diversified portfolio for long-term growth.\n");
        response.append("- Review your insurance coverage to ensure you're adequately protected.\n");
        response.append("- Set specific financial goals and track your progress.\n");
        response.append("- Regularly review and update your budget.\n");
        response.append("- Consider working with a financial advisor for personalized advice.\n");
        
        return response.toString();
    }
}

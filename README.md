# Personal Finance Tracker

A Java-based personal finance tracking application with AI-assisted analysis.

## Project Overview

This Java Swing application helps users track their personal finances, categorize expenses, and get AI-assisted insights and predictions. Key features include:

- Manual and automated data entry (CSV import)
- Expense categorization (manual correction supported)
- Spending insights, predictions, and budgeting suggestions (AI-assisted)
- Special date management for local financial context (e.g., Chinese New Year)
- Budget goal setting (partially implemented)

## Project Structure

The project follows a modular architecture with clear separation of concerns:

### Frontend (GUI) - Java Swing

Located in `src/main/java/com/financetracker/gui`:

- `MainFrame.java`: The main application window using `CardLayout` to switch panels.
- `HomePanel.java`: Home screen with main navigation buttons.
- `TransactionPanel.java`: Interface for adding, editing, deleting, and viewing transactions, including CSV import.
- `AnalysisPanel.java`: Tabbed interface for monthly summary, AI assistant queries, and next month's budget forecast.
- `SettingsPanel.java`: Interface for managing special dates, budget goals (UI only), categories, and month start day.
- `ImportCsvDialog.java`: Dialog for handling CSV file import details.

### Models

Located in `src/main/java/com/financetracker/model`:

- `Transaction.java`: Represents a single financial transaction (income or expense).
- `Category.java`: (Currently unused, categories are strings in `Settings` and `Transaction`). Represents a transaction category.
- `SpecialDate.java`: Represents a special date (e.g., holiday) that might affect spending patterns.
- `Budget.java`: (Currently unused). Represents a budget with category-specific limits.
- `Settings.java`: Represents application settings like default categories, month start day, etc.

### Services

Located in `src/main/java/com/financetracker/service`:

- `DataService.java`: Interface defining data persistence operations (load/save).
- `JsonDataService.java`: Implementation of `DataService` using Gson for JSON persistence. Handles `List<T>` and single `T`.
- `CsvDataService.java`: (Currently unused placeholder). Intended for CSV data operations.
- `TransactionService.java`: Business logic for managing transactions (CRUD, filtering, calculations). Uses `JsonDataService`.
- `SettingsService.java`: Business logic for managing application settings. Uses `JsonDataService`.
- `SpecialDateService.java`: Business logic for managing special dates. Uses `JsonDataService`.
- `LocalDateAdapter.java`: Gson `TypeAdapter` for serializing/deserializing `java.time.LocalDate` to JSON.

### AI

Located in `src/main/java/com/financetracker/ai`:

- `AiAssistantService.java`: Provides AI-assisted analysis and recommendations based on transaction data. (Current implementation might be basic).

### Main Application Class

- `AppLauncher.java`: Main entry point (`main` method) for the application. Initializes and shows the `MainFrame`.

## Data Storage

All application data is stored as JSON files in the `data` directory (created automatically if it doesn't exist):

- `data/transactions.json`: Stores the list of all `Transaction` objects.
- `data/settings.json`: Stores the single `Settings` object.
- `data/special_dates.json`: Stores the list of all `SpecialDate` objects.

## Team Member Assignments

### Team Member 1: Core Infrastructure and Data Management

**Responsibilities:**
- Ensure proper functioning of data persistence services
- Implement error handling and validation
- Optimize data loading and saving operations
- Implement data backup and recovery features

**Files to focus on:**
- `DataService.java`
- `JsonDataService.java`
- `CsvDataService.java`
- `LocalDateAdapter.java`

### Team Member 2: Transaction Management

**Responsibilities:**
- Enhance transaction management functionality
- Implement advanced filtering and sorting
- Improve CSV import functionality
- Add data validation and error handling

**Files to focus on:**
- `Transaction.java`
- `TransactionService.java`
- `TransactionPanel.java`
- `ImportCsvDialog.java`

### Team Member 3: Analysis and Visualization

**Responsibilities:**
- Implement graphical charts (e.g., using JFreeChart) in `AnalysisPanel` to replace text-based visualizations.
- Enhance analysis algorithms in `AnalysisPanel` and potentially `TransactionService`.
- Add export functionality for reports (e.g., export monthly summary to CSV).

**Files to focus on:**
- `AnalysisPanel.java`
- `TransactionService.java`
- `pom.xml` (ensure JFreeChart is correctly used)

### Team Member 4: AI Assistant

**Responsibilities:**
- Enhance AI analysis capabilities
- Implement more sophisticated prediction algorithms
- Add personalized recommendations
- Improve natural language processing for queries

**Files to focus on:**
- `AiAssistantService.java`
- Add new AI-related classes as needed

### Team Member 5: Settings and Configuration

**Responsibilities:**
- Fully implement the "Budget Goal" saving functionality in `SettingsPanel` and `SettingsService`.
- Enhance theme customization in `MainFrame`.
- Add language localization support (internationalization).
- Improve special date management (e.g., recurring dates).

**Files to focus on:**
- `Settings.java` / `Budget.java` (if needed for goals)
- `SettingsService.java`
- `SettingsPanel.java`
- `SpecialDate.java`
- `SpecialDateService.java`
- `MainFrame.java`

### Team Member 6: UI/UX and Integration

**Responsibilities:**
- Improve overall UI/UX design
- Ensure consistent styling across the application
- Implement keyboard shortcuts
- Ensure proper integration between components

**Files to focus on:**
- `MainFrame.java`
- `HomePanel.java`
- All GUI components

## Setting Up and Running the Project

### Prerequisites

- Java Development Kit (JDK) 11 or later installed
- Apache Maven installed
- Visual Studio Code (Optional, but recommended) with Java Extension Pack installed

### Setup Instructions

1.  **Clone or download** this repository.
2.  **Open the project folder** in your terminal or IDE (like VSCode).
3.  **Build the project using Maven:** This will download dependencies and compile the code.
    ```bash
    mvn clean install
    ```
    This command cleans previous builds, compiles the code, runs tests (if any), and packages the application into an executable JAR file in the `target` directory (e.g., `target/finance-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar`).

### Running the Application

#### Option 1: Using Maven (Recommended)

After building the project (`mvn install`), you can run the application using Maven:

```bash
mvn exec:java -Dexec.mainClass="com.financetracker.AppLauncher"
```

#### Option 2: Running the Executable JAR

After building the project (`mvn install`), navigate to the `target` directory and run the generated JAR file:

```bash
cd target
java -jar finance-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar
```
*(Note: The exact JAR filename might vary slightly based on the version)*

#### Option 3: Using VSCode (If installed)

1.  Open the project folder in Visual Studio Code.
2.  Ensure the Java Extension Pack is installed.
3.  Open the `src/main/java/com/financetracker/AppLauncher.java` file.
4.  Click the "Run" button (play icon) that appears above the `main` method.

## Future Enhancements

See the `FUTURE_ENHANCEMENTS.md` file for planned improvements and feature additions.

## AI助手服务配置

为了使AI助手功能正常工作，您需要：

1. 注册OpenRouter账户并获取API密钥：https://openrouter.ai/
2. 在OpenRouter网站的设置页面中，确保在隐私设置下允许"moderate"和"no-store"策略
3. 打开`config/config.properties`文件
4. 设置以下配置项：
   - `openrouter.api.key`：您的API密钥
   - `openrouter.http.referer`：HTTP Referer（保持默认即可）
   - `connection.timeout`和`request.timeout`：根据网络情况调整

配置示例：
```
openrouter.api.key=sk-or-v1-your-api-key-here
openrouter.http.referer=https://financetracker.app
connection.timeout=60000
request.timeout=60000
```

错误排查：
- "HTTP connect timed out"：检查网络连接或增加超时设置
- "No endpoints found matching your data policy"：在OpenRouter网站设置中启用数据政策（moderate/no-store）
- "Invalid JWT form"或API密钥相关错误：确认API密钥是否正确

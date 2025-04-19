---

# FinanceTracker API 文档

## 1. 数据服务接口 (DataService)

### 接口定义
```java
public interface DataService<T>
```
通用数据持久化接口，支持多种存储格式（JSON/CSV）

#### 方法列表

| 方法签名 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| `boolean saveToFile(List<T> items, String filePath)` | 保存对象列表到文件 | `items`: 要保存的对象列表<br>`filePath`: 文件路径 | 成功返回true，失败false |
| `List<T> loadFromFile(String filePath)` | 从文件加载对象列表 | `filePath`: 文件路径 | 加载的对象列表 |
| `boolean saveItemToFile(T item, String filePath)` | 保存单个对象到文件 | `item`: 要保存的对象<br>`filePath`: 文件路径 | 成功返回true，失败false |
| `T loadItemFromFile(String filePath)` | 从文件加载单个对象 | `filePath`: 文件路径 | 加载的对象 |
| `boolean appendToFile(T item, String filePath)` | 追加对象到文件 | `item`: 要追加的对象<br>`filePath`: 文件路径 | 成功返回true，失败false |
| `boolean fileExists(String filePath)` | 检查文件是否存在 | `filePath`: 文件路径 | 存在返回true |
| `boolean createBackup(String filePath, String backupFilePath)` | 创建文件备份 | `filePath`: 原文件路径<br>`backupFilePath`: 备份路径 | 成功返回true |

---

## 2. JSON数据服务 (JsonDataService)

### 类定义
```java
public class JsonDataService<T> implements DataService<T>
```
使用Gson库实现的JSON数据服务

#### 构造方法

| 构造方法 | 描述 |
|---------|------|
| `JsonDataService(Class<T> type, TypeToken<List<T>> listTypeToken)` | 完整构造方法，支持列表操作 |
| `JsonDataService(Class<T> type)` | 简化构造方法，仅支持单对象操作 |

#### 关键特性
- 自动处理LocalDate类型的序列化/反序列化
- 自动创建不存在的目录
- 提供详细的错误日志

---

## 3. 设置服务 (SettingsService)

### 类定义
```java
public class SettingsService
```
管理应用程序设置的服务

#### 方法列表

| 方法签名 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| `Settings loadSettings()` | 加载设置 | 无 | 加载的Settings对象 |
| `boolean saveSettings(Settings settings)` | 保存设置 | `settings`: 要保存的设置对象 | 成功返回true |
| `Settings resetToDefault()` | 重置为默认设置 | 无 | 默认Settings对象 |
| `boolean createBackup()` | 创建设置备份 | 无 | 成功返回true |

---

## 4. 交易服务 (TransactionService)

### 类定义
```java
public class TransactionService
```
管理交易记录的核心服务

#### 方法列表

| 方法签名 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| `List<Transaction> getAllTransactions()` | 获取所有交易 | 无 | 按日期降序排列的交易列表 |
| `List<Transaction> getTransactionsForDateRange(LocalDate startDate, LocalDate endDate)` | 获取日期范围内的交易 | `startDate`: 开始日期<br>`endDate`: 结束日期 | 符合条件的交易列表 |
| `List<Transaction> getTransactionsForMonth(int year, int month)` | 获取某月的交易 | `year`: 年<br>`month`: 月(1-12) | 该月交易列表 |
| `List<Transaction> getTransactionsForCurrentMonth()` | 获取当月交易 | 无 | 当月交易列表 |
| `List<Transaction> getTransactionsForCategory(String category)` | 获取分类交易 | `category`: 分类名称 | 该分类交易列表 |
| `double getTotalAmount(List<Transaction> transactions, boolean isExpense)` | 计算总金额 | `transactions`: 交易列表<br>`isExpense`: 是否为支出 | 总金额 |
| `boolean addTransaction(Transaction transaction)` | 添加交易 | `transaction`: 要添加的交易 | 成功返回true |
| `boolean updateTransaction(Transaction transaction)` | 更新交易 | `transaction`: 要更新的交易 | 成功返回true |
| `boolean deleteTransaction(Transaction transaction)` | 删除交易 | `transaction`: 要删除的交易 | 成功返回true |

---

## 5. 特殊日期服务 (SpecialDateService)

### 类定义
```java
public class SpecialDateService
```
管理特殊日期（如节假日、纪念日）的服务

#### 方法列表

| 方法签名 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| `List<SpecialDate> getAllSpecialDates()` | 获取所有特殊日期 | 无 | 按日期排序的特殊日期列表 |
| `List<SpecialDate> getSpecialDatesForDateRange(LocalDate startDate, LocalDate endDate)` | 获取日期范围内的特殊日期 | `startDate`: 开始日期<br>`endDate`: 结束日期 | 符合条件的特殊日期列表 |
| `List<SpecialDate> getSpecialDatesForMonth(int year, int month)` | 获取某月的特殊日期 | `year`: 年<br>`month`: 月(1-12) | 该月特殊日期列表 |
| `boolean addSpecialDate(SpecialDate specialDate)` | 添加特殊日期 | `specialDate`: 要添加的日期 | 成功返回true |
| `boolean updateSpecialDate(SpecialDate specialDate)` | 更新特殊日期 | `specialDate`: 要更新的日期 | 成功返回true |
| `boolean deleteSpecialDate(SpecialDate specialDate)` | 删除特殊日期 | `specialDate`: 要删除的日期 | 成功返回true |

以下是文档的剩余部分，包含数据模型和工具类的详细说明：

---

## 6. 数据模型 (Data Models)

### 6.1 Settings (应用设置)

#### 类定义
```java
public class Settings
```
存储应用程序的所有配置设置

#### 字段说明

| 字段 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| monthStartDay | int | 1 | 财务月开始日(1-31) |
| defaultCurrency | String | "CNY" | 默认货币代码 |
| dateFormat | String | "yyyy-MM-dd" | 日期显示格式 |
| darkModeEnabled | boolean | false | 是否启用暗黑模式 |
| defaultCategories | List\<String\> | 12个预设分类 | 默认交易分类列表 |
| dataStoragePath | String | "data/" | 数据存储路径 |
| autoBackupEnabled | boolean | true | 是否启用自动备份 |
| backupFrequencyDays | int | 7 | 备份频率(天) |
| aiAssistanceEnabled | boolean | true | 是否启用AI辅助 |

#### 关键方法
```java
public boolean isInCurrentFinancialMonth(LocalDate date)
```
检查给定日期是否在当前财务月内

---

### 6.2 Budget (预算)

#### 类定义
```java
public class Budget
```
表示带有分类预算的财务预算

#### 字段说明

| 字段 | 类型 | 描述 |
|------|------|------|
| id | String | 唯一标识符(UUID) |
| name | String | 预算名称 |
| startDate | LocalDate | 预算开始日期 |
| endDate | LocalDate | 预算结束日期 |
| totalBudget | double | 总预算金额 |
| categoryBudgets | Map\<String, Double\> | 分类预算映射 |
| notes | String | 备注信息 |

#### 关键方法
```java
public void setCategoryBudget(String categoryId, double amount)
```
设置特定分类的预算金额

```java
public double getCategoryBudget(String categoryId)
```
获取特定分类的预算金额

---

### 6.3 Category (分类)

#### 类定义
```java
public class Category
```
表示交易分类

#### 字段说明

| 字段 | 类型 | 描述 |
|------|------|------|
| id | String | 唯一标识符(UUID) |
| name | String | 分类名称 |
| description | String | 分类描述 |
| isExpenseCategory | boolean | 是否为支出分类 |

---

### 6.4 SpecialDate (特殊日期)

#### 类定义
```java
public class SpecialDate
```
表示特殊日期（如节假日）

#### 字段说明
| 字段 | 类型 | 描述 |
|------|------|------|
| id | String | 唯一标识符(UUID) |
| date | LocalDate | 日期 |
| name | String | 日期名称 |
| description | String | 日期描述 |
| isRecurring | boolean | 是否每年重复 |

---

## 7. 工具类 (Utility Classes)

### 7.1 LocalDateAdapter

#### 类定义
```java
public class LocalDateAdapter extends TypeAdapter<LocalDate>
```
Gson类型适配器，用于LocalDate与JSON的转换

#### 方法说明

| 方法 | 描述 |
|------|------|
| `write(JsonWriter out, LocalDate value)` | 将LocalDate写入JSON(格式: YYYY-MM-DD) |
| `read(JsonReader in)` | 从JSON读取LocalDate，处理格式错误 |

---

### 7.2 CsvDataService

#### 类定义
```java
public class CsvDataService<T> implements DataService<T>
```
CSV格式数据服务（基础实现）

#### 接口说明
```java
public interface CsvConverter<T>
```
CSV转换器接口，需要实现：
- `String[] toCsvRecord(T item)` 对象→CSV行
- `T fromCsvRecord(String[] record)` CSV行→对象
- `String[] getHeader()` 获取CSV表头

---

## 8. 文件存储结构

```
data/
├── settings.json          # 应用设置
├── transactions.json      # 交易记录
├── special_dates.json     # 特殊日期
└── *.backup               # 自动备份文件
```

## 9. 异常处理

- 所有服务方法都会捕获并打印IOException
- 关键操作失败时会返回false或空集合
- 日期解析错误会记录到标准错误输出

## 10. 使用示例

```java
// 初始化服务
TransactionService transactionService = new TransactionService();
SettingsService settingsService = new SettingsService();

// 添加交易
Transaction newTransaction = new Transaction();
newTransaction.setAmount(100.0);
newTransaction.setCategory("Food");
transactionService.addTransaction(newTransaction);

// 获取当月交易
List<Transaction> monthlyTransactions = 
    transactionService.getTransactionsForCurrentMonth();

// 修改设置
Settings settings = settingsService.loadSettings();
settings.setDarkModeEnabled(true);
settingsService.saveSettings(settings);
```


## 11. AI辅助服务 (AiAssistantService)

### 类定义
```java
public class AiAssistantService
```
提供基于交易数据的AI财务分析服务

#### 核心方法

| 方法签名 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| `String getResponse(String query, TransactionService transactionService)` | 处理用户查询并返回AI分析结果 | `query`: 用户问题<br>`transactionService`: 交易服务实例 | 格式化分析结果字符串 |
| `String getSavingAdvice(...)` | 生成储蓄建议 | 交易数据和交易服务 | 储蓄建议字符串 |
| `String getSpendingHabitsAnalysis(...)` | 分析消费习惯 | 交易数据和交易服务 | 消费分析报告 |
| `String getBudgetAdvice(...)` | 生成预算建议 | 交易数据和交易服务 | 预算建议报告 |
| `String getIncomeAnalysis(...)` | 分析收入情况 | 交易数据和交易服务 | 收入分析报告 |
| `String getExpenseAnalysis(...)` | 分析支出情况 | 交易数据和交易服务 | 支出分析报告 |

---

## 12. 图形界面组件

### 12.1 MainFrame (主窗口)

#### 类定义
```java
public class MainFrame extends JFrame
```
应用主窗口，管理所有面板切换

#### 关键方法
| 方法 | 描述 |
|------|------|
| `showPanel(String panelName)` | 切换显示指定面板 |
| `applyTheme()` | 根据设置应用主题 |
| `getSettings()` | 获取当前设置 |

---

### 12.2 HomePanel (主页)

#### 类定义
```java
public class HomePanel extends JPanel
```
应用主页，提供导航功能

#### 功能
- 显示主菜单按钮
- 跳转到各功能模块

---

### 12.3 TransactionPanel (交易管理)

#### 类定义
```java
public class TransactionPanel extends JPanel
```
交易记录管理界面

#### 核心方法
| 方法 | 描述 |
|------|------|
| `addTransaction()` | 添加新交易 |
| `deleteSelectedTransaction()` | 删除选中交易 |
| `importCsv()` | 导入CSV交易记录 |

---

### 12.4 AnalysisPanel (分析面板)

#### 类定义
```java
public class AnalysisPanel extends JPanel
```
财务数据分析界面

#### 核心功能
- 当前月收支分析
- AI助手问答系统
- 下月预算预测
- 支持按类别/时间筛选

---

### 12.5 SettingsPanel (设置面板)

#### 类定义
```java
public class SettingsPanel extends JPanel
```
应用设置管理界面

#### 配置项
- 特殊日期管理
- 预算目标设置
- 分类管理
- 财务月开始日设置

---

## 13. 数据模型

### 13.1 Transaction (交易)

#### 字段说明
| 字段 | 类型 | 描述 |
|------|------|------|
| id | String | 唯一标识符 |
| date | LocalDate | 交易日期 |
| amount | double | 金额 |
| category | String | 分类 |
| isExpense | boolean | 是否为支出 |

---

### 13.2 SpecialDate (特殊日期)

#### 字段说明
| 字段 | 类型 | 描述 |
|------|------|------|
| date | LocalDate | 日期 |
| expectedImpact | double | 预期消费影响(%) |
| affectedCategories | String | 受影响分类 |

---

## 14. 工具类

### 14.1 ImportCsvDialog

#### 类定义
```java
public class ImportCsvDialog extends JDialog
```
CSV导入对话框

#### 功能
- 映射CSV列到交易字段
- 支持日期格式设置
- 处理导入结果反馈

---

## 15. 文件存储结构

```
data/
├── transactions.json       # 交易记录
├── settings.json           # 应用设置
├── special_dates.json      # 特殊日期
└── backups/                # 自动备份
```

## 16. 异常处理策略

- 所有服务方法捕获并记录IOException
- 关键操作提供用户友好的错误提示
- 数据损坏时自动恢复默认值

## 17. 典型使用流程

1. **添加交易**:
```java
transactionPanel.addTransaction();
```

2. **获取分析报告**:
```java
String advice = aiAssistant.getResponse("How can I save more?", transactionService);
```

3. **修改设置**:
```java
settings.setMonthStartDay(15);
settingsService.saveSettings(settings);
```

---

完整API文档包含：
1. 所有服务接口说明
2. 数据模型定义
3. UI组件功能
4. 关键业务流程
5. 错误处理机制

需要任何部分的详细示例或特定使用场景说明，请随时告知！


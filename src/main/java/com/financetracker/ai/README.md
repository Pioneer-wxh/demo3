# AI助手模块

本模块实现了与AI助手的交互功能，利用DeepSeek大模型API进行智能对话。

## 功能特点

- 使用OpenAI兼容格式的API调用
- 支持流式传输显示消息
- 自动重试机制，提高连接稳定性
- 提供财务分析、预算建议等AI辅助功能

## 设置步骤

1. 首先，您需要获取DeepSeek平台的API密钥
2. 复制`src/main/resources/config.properties.template`到同目录下并重命名为`config.properties`
3. 在`config.properties`文件中设置您的API密钥：
   ```properties
   deepseek.api.key=您的API密钥
   ```
4. 可选：如果需要，您也可以修改模型名称和API URL

## 环境变量设置

您也可以通过环境变量设置API密钥：

- Windows:
  ```cmd
  set DEEPSEEK_API_KEY=您的API密钥
  ```

- Linux/macOS:
  ```bash
  export DEEPSEEK_API_KEY=您的API密钥
  ```

## 使用方法

```java
// 创建AI助手服务
AiAssistantService aiAssistant = new AiAssistantService();

// 非流式调用
String response = aiAssistant.getResponse("我本月的支出情况如何？", transactionService);

// 流式调用
aiAssistant.getResponseStream("给我一些节省开支的建议", transactionService, message -> {
    // 逐字显示消息
    System.out.print(message);
});
``` 
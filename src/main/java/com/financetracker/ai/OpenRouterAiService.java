package com.financetracker.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用OpenRouter API连接DeepSeek模型的服务类
 */
public class OpenRouterAiService {
    
    private static final String DEFAULT_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek/deepseek-chat-v3-0324:free";
    
    private final String apiUrl;
    private final String model;
    private final String apiKey;
    private final HttpClient httpClient;
    
    /**
     * 构造函数 - 从配置文件加载设置
     */
    public OpenRouterAiService() {
        // 从配置文件获取API密钥和设置
        this.apiKey = ConfigLoader.getProperty("openrouter.api.key", System.getenv("OPENROUTER_API_KEY"));
        this.apiUrl = ConfigLoader.getProperty("openrouter.api.url", DEFAULT_API_URL).trim();
        this.model = ConfigLoader.getProperty("openrouter.model", DEFAULT_MODEL).trim();
        
        if (this.apiKey == null || this.apiKey.isEmpty() || "your_api_key_here".equals(this.apiKey)) {
            System.err.println("警告: OpenRouter API密钥未设置。请在config.properties中配置openrouter.api.key或设置OPENROUTER_API_KEY环境变量。");
        }
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    /**
     * 与AI对话（非流式）
     * 
     * @param prompt 用户提示
     * @return AI回复内容
     */
    public String chat(String prompt) {
        try {
            if (apiKey == null || apiKey.isEmpty() || "your_api_key_here".equals(apiKey)) {
                return "错误: API密钥未设置。请在配置文件中设置openrouter.api.key或设置OPENROUTER_API_KEY环境变量。";
            }
            
            String requestBodyJson = createRequestBodyJson(prompt, false);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "https://financetracker.app") // 你的应用URL
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return "抱歉，AI服务暂时不可用，请稍后再试。错误码: " + response.statusCode();
            }
            
            // 使用正则表达式提取内容
            String content = extractContentFromResponse(response.body());
            if (content != null) {
                return content;
            }
            
            return "抱歉，无法解析AI服务响应。";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，AI服务出现错误：" + e.getMessage();
        }
    }
    
    /**
     * 从API响应中提取内容
     */
    private String extractContentFromResponse(String responseBody) {
        // 尝试使用正则表达式提取内容
        Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            String content = matcher.group(1);
            // 处理转义字符
            return content.replace("\\n", "\n")
                          .replace("\\\"", "\"")
                          .replace("\\\\", "\\");
        }
        return null;
    }
    
    /**
     * 与AI对话（流式传输）
     * 
     * @param prompt 用户提示
     * @param messageConsumer 消息处理回调
     */
    public void chatStream(String prompt, Consumer<String> messageConsumer) {
        try {
            if (apiKey == null || apiKey.isEmpty() || "your_api_key_here".equals(apiKey)) {
                messageConsumer.accept("错误: API密钥未设置。请在配置文件中设置openrouter.api.key或设置OPENROUTER_API_KEY环境变量。");
                return;
            }
            
            String requestBodyJson = createRequestBodyJson(prompt, true);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "https://financetracker.app") // 你的应用URL
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();
            
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenApply(HttpResponse::body)
                    .thenAccept(lines -> {
                        lines.forEach(line -> {
                            if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                                String data = line.substring(6).trim();
                                
                                // 使用正则表达式提取内容
                                try {
                                    Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"([^\"]*)\"");
                                    Matcher matcher = pattern.matcher(data);
                                    if (matcher.find()) {
                                        String content = matcher.group(1);
                                        // 处理转义字符
                                        content = content.replace("\\n", "\n")
                                                       .replace("\\\"", "\"")
                                                       .replace("\\\\", "\\");
                                        messageConsumer.accept(content);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    });
        } catch (Exception e) {
            e.printStackTrace();
            messageConsumer.accept("抱歉，AI服务出现错误：" + e.getMessage());
        }
    }
    
    /**
     * 生成月度分析报告
     * 
     * @param data 财务数据
     * @return 分析报告
     */
    public String generateMonthlyAnalysisReport(String data) {
        String prompt = "你是一位专业的财务分析师。根据以下财务数据，生成一份详细的月度分析报告，包括收支情况分析、消费趋势分析和财务健康状况评估：\n\n" 
                + data;
        return chat(prompt);
    }
    
    /**
     * 生成预算建议
     * 
     * @param data 财务数据
     * @return 预算建议
     */
    public String generateBudgetSuggestions(String data) {
        String prompt = "你是一位专业的财务顾问。根据以下财务数据，为下个月提供详细的预算建议，包括各类别的建议预算额度和可能的节省方案：\n\n" 
                + data;
        return chat(prompt);
    }
    
    /**
     * 创建请求体JSON字符串
     * 
     * @param prompt 用户提示
     * @param stream 是否使用流式传输
     * @return JSON字符串
     */
    private String createRequestBodyJson(String prompt, boolean stream) {
        // 创建消息数组
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一位专业的财务顾问，名为财务智能助手。你擅长分析财务数据，提供个人理财建议，并以友好、专业的态度回答用户关于财务的各种问题。");
        messages.add(systemMessage);
        
        // 用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        
        // 使用SimpleJsonHelper创建请求体
        return SimpleJsonHelper.createJsonObject(
            "model", model,
            "messages", messages,
            "temperature", 0.7,
            "max_tokens", 2000,
            "stream", stream
        );
    }
} 
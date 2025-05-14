package com.financetracker.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用DeepSeek模型API的服务类
 */
public class DeepSeekAiService {
    
    private static final String DEFAULT_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek-chat";
    
    private final String apiUrl;
    private final String model;
    private final String apiKey;
    private final HttpClient httpClient;
    
    /**
     * 构造函数 - 从配置文件加载设置
     */
    public DeepSeekAiService() {
        // 从配置文件获取API密钥和设置
        this.apiKey = ConfigLoader.getProperty("deepseek.api.key", System.getenv("DEEPSEEK_API_KEY"));
        this.apiUrl = ConfigLoader.getProperty("deepseek.api.url", DEFAULT_API_URL).trim();
        this.model = ConfigLoader.getProperty("deepseek.model", DEFAULT_MODEL).trim();
        
        if (this.apiKey == null || this.apiKey.isEmpty() || "your_api_key_here".equals(this.apiKey)) {
            System.err.println("警告: DeepSeek API密钥未设置。请在config.properties中配置deepseek.api.key或设置DEEPSEEK_API_KEY环境变量。");
        }
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))  // 增加连接超时时间到60秒
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
                return "错误: API密钥未设置。请在配置文件中设置deepseek.api.key或设置DEEPSEEK_API_KEY环境变量。";
            }
            
            String requestBodyJson = createRequestBodyJson(prompt, false);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(60))  // 设置单个请求的超时时间为60秒
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();
            
            // 添加重试机制
            int maxRetries = 3;
            for (int retry = 0; retry < maxRetries; retry++) {
                try {
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() != 200) {
                        if (retry < maxRetries - 1) {
                            System.out.println("API请求失败，状态码: " + response.statusCode() + "，正在尝试重试...");
                            Thread.sleep(1000 * (retry + 1)); // 指数退避
                            continue;
                        }
                        return "抱歉，AI服务暂时不可用，请稍后再试。错误码: " + response.statusCode();
                    }
                    
                    // 解析OpenAI兼容格式响应
                    return parseResponseContent(response.body());
                    
                } catch (IOException e) {
                    if (retry < maxRetries - 1) {
                        System.out.println("网络错误: " + e.getMessage() + "，正在尝试重试...");
                        Thread.sleep(1000 * (retry + 1)); // 指数退避
                    } else {
                        throw e; // 重新抛出异常
                    }
                }
            }
            
            return "抱歉，多次尝试后仍无法连接到AI服务，请检查网络连接或稍后再试。";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，AI服务出现错误：" + e.getMessage();
        }
    }
    
    /**
     * 解析API响应内容
     */
    private String parseResponseContent(String responseBody) {
        try {
            // 使用正则表达式提取content内容
            Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"([^\"]*?)\"");
            Matcher matcher = pattern.matcher(responseBody);
            
            if (matcher.find()) {
                String content = matcher.group(1);
                // 处理转义字符
                return content.replace("\\n", "\n")
                              .replace("\\\"", "\"")
                              .replace("\\\\", "\\");
            }
            
            // 如果正则表达式未匹配，尝试使用SimpleJsonHelper解析
            Map<String, Object> jsonResponse = SimpleJsonHelper.parseJson(responseBody);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonResponse.get("choices");
            
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                
                if (message != null) {
                    return (String) message.get("content");
                }
            }
            
            return "无法解析AI响应: " + responseBody;
        } catch (Exception e) {
            e.printStackTrace();
            return "解析AI响应时出错: " + e.getMessage();
        }
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
                messageConsumer.accept("错误: API密钥未设置。请在配置文件中设置deepseek.api.key或设置DEEPSEEK_API_KEY环境变量。");
                return;
            }
            
            String requestBodyJson = createRequestBodyJson(prompt, true);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(60))  // 设置单个请求的超时时间为60秒
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();
            
            // 添加重试机制
            sendStreamRequestWithRetry(request, messageConsumer, 3);
        } catch (Exception e) {
            e.printStackTrace();
            messageConsumer.accept("抱歉，AI服务出现错误：" + e.getMessage());
        }
    }
    
    /**
     * 发送流式请求并实现重试机制
     * 
     * @param request HTTP请求
     * @param messageConsumer 消息处理回调
     * @param maxRetries 最大重试次数
     */
    private void sendStreamRequestWithRetry(HttpRequest request, Consumer<String> messageConsumer, int maxRetries) {
        AtomicInteger retryCount = new AtomicInteger(0);
        AtomicBoolean hasReceived = new AtomicBoolean(false);
        
        sendStreamRequest(request, messageConsumer, (error) -> {
            if (retryCount.get() < maxRetries && !hasReceived.get()) {
                int currentRetry = retryCount.incrementAndGet();
                try {
                    System.out.println("流式API请求失败，正在尝试第 " + currentRetry + " 次重试...");
                    Thread.sleep(1000 * currentRetry); // 指数退避
                    sendStreamRequest(request, messageConsumer, (retryError) -> {
                        if (currentRetry >= maxRetries) {
                            messageConsumer.accept("抱歉，多次尝试后仍无法连接到AI服务，请检查网络连接或稍后再试。");
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    messageConsumer.accept("操作被中断。");
                }
            } else if (!hasReceived.get()) {
                messageConsumer.accept("抱歉，多次尝试后仍无法连接到AI服务，请检查网络连接或稍后再试。");
            }
        });
    }
    
    /**
     * 发送单次流式请求
     * 
     * @param request HTTP请求
     * @param messageConsumer 消息处理回调
     * @param errorHandler 错误处理回调
     */
    private void sendStreamRequest(HttpRequest request, Consumer<String> messageConsumer, Consumer<Throwable> errorHandler) {
        AtomicBoolean hasReceived = new AtomicBoolean(false);
        StringBuilder contentBuffer = new StringBuilder();
        
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenApply(HttpResponse::body)
                .thenAccept(lines -> {
                    lines.forEach(line -> {
                        if (!line.isEmpty()) {
                            hasReceived.set(true);
                            
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                
                                if ("[DONE]".equals(data)) {
                                    return; // 流结束标记
                                }
                                
                                try {
                                    // 解析返回的JSON数据
                                    Map<String, Object> jsonData = SimpleJsonHelper.parseJson(data);
                                    List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonData.get("choices");
                                    
                                    if (choices != null && !choices.isEmpty()) {
                                        Map<String, Object> choice = choices.get(0);
                                        Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                        
                                        if (delta != null && delta.containsKey("content")) {
                                            String content = (String) delta.get("content");
                                            if (content != null) {
                                                messageConsumer.accept(content);
                                                contentBuffer.append(content);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    System.err.println("解析流数据错误: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                })
                .exceptionally(throwable -> {
                    if (!hasReceived.get()) {
                        errorHandler.accept(throwable);
                    } else {
                        // 如果已经接收到一些数据但中途失败，向用户显示错误消息
                        messageConsumer.accept("\n[连接中断: " + throwable.getMessage() + "]");
                    }
                    return null;
                });
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
        String prompt = "作为一位财务顾问，请根据以下财务数据为用户制定下月的预算计划。包括各类别支出的合理预算分配，以及可能的节约建议：\n\n" 
                + data;
        return chat(prompt);
    }
    
    /**
     * 创建请求体JSON
     * 
     * @param prompt 用户提示
     * @param stream 是否使用流式传输
     * @return JSON字符串
     */
    private String createRequestBodyJson(String prompt, boolean stream) {
        // 构建符合OpenAI API格式的请求
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\": \"").append(model).append("\",");
        json.append("\"messages\": [{\"role\": \"user\", \"content\": \"").append(escapeJsonString(prompt)).append("\"}],");
        json.append("\"stream\": ").append(stream).append(",");
        json.append("\"temperature\": 0.7");
        json.append("}");
        return json.toString();
    }
    
    /**
     * 转义JSON字符串
     */
    private String escapeJsonString(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * 检查服务是否可用
     * 
     * @return 服务是否可用
     */
    public boolean isServiceAvailable() {
        return apiKey != null && !apiKey.isEmpty() && !"your_api_key_here".equals(apiKey);
    }
} 
package com.financetracker.ai;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 讯飞星火大模型服务
 * 使用WebSocket与讯飞星火API通信
 */
public class SparkAiService {
    private static final Logger logger = LoggerFactory.getLogger(SparkAiService.class);
    
    // 星火大模型配置参数 - 更新为截图中的值
    private static final String HOST_URL = "wss://spark-api.xf-yun.com/v3.5/chat";
    private static final String APP_ID = "eb20aeb6";
    private static final String API_SECRET = "ZDM5YTI3YzQwOTQwOWU4ODE3MDQwNTZm";
    private static final String API_KEY = "71ff38e94b9297355f42b63f7e8925c7";
    
    private final Gson gson = new Gson();
    
    /**
     * 向星火大模型发送请求并获取回复
     * 
     * @param message 用户消息
     * @return 星火大模型的回复
     */
    public String chat(String message) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            // 构建鉴权URL
            String authUrl = buildAuthUrl();
            logger.info("认证URL: {}", authUrl);
            
            // 创建WebSocket客户端
            WebSocketClient client = createWebSocketClient(authUrl, future, message);
            client.connect();
            
            // 等待响应
            return future.join();
        } catch (Exception e) {
            logger.error("星火大模型调用失败", e);
            return "抱歉，AI服务暂时不可用: " + e.getMessage();
        }
    }
    
    /**
     * 创建WebSocket客户端
     */
    private WebSocketClient createWebSocketClient(String url, CompletableFuture<String> future, String userMessage) {
        return new WebSocketClient(URI.create(url)) {
            StringBuilder responseBuilder = new StringBuilder();
            
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                logger.info("WebSocket连接已打开");
                send(buildRequestMessage(userMessage));
            }
            
            @Override
            public void onMessage(String message) {
                logger.info("收到消息: {}", message);
                try {
                    JsonObject response = gson.fromJson(message, JsonObject.class);
                    
                    // 检查是否有错误
                    if (response.has("header") && response.getAsJsonObject("header").has("code")) {
                        int code = response.getAsJsonObject("header").get("code").getAsInt();
                        if (code != 0) {
                            String errorMessage = "星火大模型返回错误码: " + code;
                            if (response.getAsJsonObject("header").has("message")) {
                                errorMessage += ", " + response.getAsJsonObject("header").get("message").getAsString();
                            }
                            future.completeExceptionally(new RuntimeException(errorMessage));
                            close();
                            return;
                        }
                    }
                    
                    // 提取回复内容
                    if (response.has("payload") && response.getAsJsonObject("payload").has("choices")) {
                        JsonObject choices = response.getAsJsonObject("payload").getAsJsonObject("choices");
                        if (choices.has("text")) {
                            JsonArray texts = choices.getAsJsonArray("text");
                            for (int i = 0; i < texts.size(); i++) {
                                JsonObject text = texts.get(i).getAsJsonObject();
                                if (text.has("content")) {
                                    responseBuilder.append(text.get("content").getAsString());
                                }
                            }
                        }
                    }
                    
                    // 检查是否是最后一条消息
                    if (response.has("header") && response.getAsJsonObject("header").has("status") && 
                        response.getAsJsonObject("header").get("status").getAsInt() == 2) {
                        future.complete(responseBuilder.toString());
                        close();
                    }
                } catch (Exception e) {
                    logger.error("解析响应失败", e);
                    future.completeExceptionally(e);
                    close();
                }
            }
            
            @Override
            public void onClose(int code, String reason, boolean remote) {
                logger.info("WebSocket连接已关闭, code: {}, reason: {}, remote: {}", code, reason, remote);
                if (!future.isDone()) {
                    if (responseBuilder.length() > 0) {
                        future.complete(responseBuilder.toString());
                    } else {
                        future.completeExceptionally(new RuntimeException("连接关闭，未收到有效响应"));
                    }
                }
            }
            
            @Override
            public void onError(Exception ex) {
                logger.error("WebSocket错误", ex);
                future.completeExceptionally(ex);
                close();
            }
        };
    }
    
    /**
     * 构建请求消息 - 更新为v3.5接口格式
     */
    private String buildRequestMessage(String userMessage) {
        JsonObject message = new JsonObject();
        
        // 构建header
        JsonObject header = new JsonObject();
        header.addProperty("app_id", APP_ID);
        header.addProperty("uid", "finance_tracker_user"); // 用户ID，可以是任意字符串
        message.add("header", header);
        
        // 构建parameter
        JsonObject parameter = new JsonObject();
        JsonObject chat = new JsonObject();
        chat.addProperty("domain", "generalv3.5"); // 使用v3.5模型
        chat.addProperty("temperature", 0.5); // 温度参数，控制创新性
        chat.addProperty("max_tokens", 2048); // 最大回复长度
        parameter.add("chat", chat);
        message.add("parameter", parameter);
        
        // 构建payload - 更新为v3.5接口格式
        JsonObject payload = new JsonObject();
        
        // 创建消息数组
        JsonArray textArray = new JsonArray();
        
        // 添加用户消息
        JsonObject userMessageObj = new JsonObject();
        userMessageObj.addProperty("role", "user");
        userMessageObj.addProperty("content", userMessage);
        textArray.add(userMessageObj);
        
        payload.add("message", new JsonObject());
        payload.getAsJsonObject("message").add("text", textArray);
        message.add("payload", payload);
        
        return gson.toJson(message);
    }
    
    /**
     * 构建带鉴权信息的URL - 更新为v3.5路径
     */
    private String buildAuthUrl() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        
        // 构建鉴权URL - 更新host和path
        String host = "spark-api.xf-yun.com";
        String path = "/v3.5/chat";
        
        String signatureOrigin = "host: " + host + "\n";
        signatureOrigin += "date: " + date + "\n";
        signatureOrigin += "GET " + path + " HTTP/1.1";
        
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(API_SECRET.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        
        byte[] hexDigits = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                API_KEY, "hmac-sha256", "host date request-line", sha);
        
        return HOST_URL + "?authorization=" + Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))
                + "&date=" + date + "&host=" + host;
    }
    
    /**
     * 分析交易并提供类别和交易类型建议
     * 
     * @param description 交易描述
     * @param amount 交易金额
     * @return 包含推荐类别和交易类型的Map
     */
    public Map<String, Object> analyzeTransaction(String description, double amount) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询
        String prompt = String.format(
                "以下是一条财务交易记录：\n描述: \"%s\"\n金额: %.2f\n\n这是收入还是支出？这笔交易应该属于哪个类别？请以JSON格式回复，格式为:{\"category\":\"具体类别名称\",\"isExpense\":true或false}",
                description, amount);
        
        String response = chat(prompt);
        
        // 尝试从回复中提取JSON
        try {
            // 默认值
            String category = "未分类";
            boolean isExpense = true;
            
            // 尝试提取JSON
            int startIdx = response.indexOf('{');
            int endIdx = response.lastIndexOf('}');
            
            if (startIdx >= 0 && endIdx > startIdx) {
                String jsonStr = response.substring(startIdx, endIdx + 1);
                JsonObject jsonResponse = gson.fromJson(jsonStr, JsonObject.class);
                
                if (jsonResponse.has("category")) {
                    category = jsonResponse.get("category").getAsString();
                }
                
                if (jsonResponse.has("isExpense")) {
                    isExpense = jsonResponse.get("isExpense").getAsBoolean();
                }
            }
            
            result.put("category", category);
            result.put("isExpense", isExpense);
        } catch (Exception e) {
            logger.error("解析AI响应失败", e);
            // 使用默认值
            result.put("category", "未分类");
            result.put("isExpense", amount < 0 ? true : (amount > 0 && !description.toLowerCase().contains("收入")));
        }
        
        return result;
    }
    
    /**
     * 生成当月分析报告
     * 
     * @param data 当月交易数据摘要
     * @return 分析报告
     */
    public String generateMonthlyAnalysisReport(String data) {
        String prompt = "请基于以下财务数据，生成本月财务状况的详细分析报告，包括总体情况、收支平衡、主要支出类别分析和节约建议：\n\n" + data;
        return chat(prompt);
    }
    
    /**
     * 生成下月预算建议
     * 
     * @param data 历史交易数据摘要
     * @return 预算建议
     */
    public String generateBudgetSuggestions(String data) {
        String prompt = "基于以下历史财务数据，请为下个月制定详细的预算计划，包括各类别支出预算建议和节约目标：\n\n" + data;
        return chat(prompt);
    }
} 
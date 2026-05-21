package com.projmodel.plugin.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class AIClient {
    private final AIConfig config;
    private final Gson gson;

    public AIClient() {
        this.config = new AIConfig();
        this.gson = new Gson();
    }

    public AIClient(AIConfig config) {
        this.config = config;
        this.gson = new Gson();
    }

    public String sendMessage(String systemPrompt, String userMessage) throws IOException {
        // Пробуем разные модели по порядку
        String[] models = {
                "google/gemini-2.0-flash-exp:free",     // Gemini 2.0 Flash (бесплатный)
                "google/gemini-flash-1.5:free",          // Gemini 1.5 Flash (бесплатный)
                "meta-llama/llama-3.2-3b-instruct:free", // Llama 3.2 3B (бесплатный)
                "mistralai/mistral-7b-instruct:free"      // Mistral 7B (бесплатный)
        };

        IOException lastException = null;

        // Пробуем каждую модель, пока какая-то не сработает
        for (String model : models) {
            try {
                return trySendMessage(model, systemPrompt, userMessage);
            } catch (IOException e) {
                lastException = e;
                // Если это не 404 (модель не найдена), а другая ошибка - выбрасываем сразу
                if (!e.getMessage().contains("404")) {
                    throw e;
                }
                // Иначе пробуем следующую модель
                System.out.println("[ProjModel] Модель " + model + " недоступна, пробуем следующую...");
            }
        }

        throw lastException != null ? lastException : new IOException("Все модели недоступны");
    }

    private String trySendMessage(String model, String systemPrompt, String userMessage) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.setRequestProperty("HTTP-Referer", "http://localhost:2990/jira");
            connection.setRequestProperty("X-Title", "ProjModel Jira Plugin");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);

            // Формируем тело запроса
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);

            JsonArray messages = new JsonArray();

            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content", systemPrompt);
            messages.add(systemMsg);

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            messages.add(userMsg);

            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.3);
            requestBody.addProperty("max_tokens", 2000);

            String jsonInputString = gson.toJson(requestBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                }
                throw new IOException("API Error " + responseCode + ": " + errorResponse.toString());
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            System.out.println("[ProjModel] Ответ от API: " + response.toString().substring(0, Math.min(200, response.length())));

            JsonObject responseJson = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray choices = responseJson.getAsJsonArray("choices");

            if (choices != null && choices.size() > 0) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");

                // Проверяем, есть ли content
                if (message.has("content") && !message.get("content").isJsonNull()) {
                    return message.get("content").getAsString();
                }

                // Если content null, проверяем reason
                if (message.has("reason")) {
                    return "Модель отказалась отвечать. Причина: " + message.get("reason").getAsString();
                }
            }

            return "Ошибка: неожиданный формат ответа от API";

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
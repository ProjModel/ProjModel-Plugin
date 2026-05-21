package com.projmodel.plugin.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AIClient {
    private final Gson gson;

    public AIClient() {
        this.gson = new Gson();
    }

    public AIClient(AIConfig config) {
        this.gson = new Gson();
    }

    public String sendMessage(String systemPrompt, String userMessage) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL("http://localhost:11434/api/generate");
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(120000);

            String fullPrompt;
            if (userMessage != null && !userMessage.isEmpty()) {
                fullPrompt = systemPrompt + "\n\n" + userMessage;
            } else {
                fullPrompt = systemPrompt;
            }

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "qwen2.5:3b");  // ← Новая модель
            requestBody.addProperty("prompt", fullPrompt);
            requestBody.addProperty("stream", false);

            JsonObject options = new JsonObject();
            options.addProperty("temperature", 0.3);
            options.addProperty("num_predict", 2000);
            requestBody.add("options", options);

            String jsonBody = gson.toJson(requestBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                throw new IOException("Ollama error: " + responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            JsonObject responseJson = JsonParser.parseString(response.toString()).getAsJsonObject();

            if (responseJson.has("response")) {
                String aiResponse = responseJson.get("response").getAsString();
                return aiResponse;
            }

            return "Пустой ответ от модели";

        } catch (java.net.ConnectException e) {
            throw new IOException("Ollama не запущена! Проверьте значок в трее.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
package com.example.webcinemabooking.controller;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.TimeUnit;

@RestController
public class ChatController {

    // API Key quyền lực của Boss Quan
    private final String API_KEY = "AIzaSyAqeNzAH0QXOPnGZCfIKtQp3YLlANbv2pI";

    // TÌM THẤY CHÂN ÁI: Đổi sang đúng model có trong danh sách của sếp (gemini-2.5-flash)
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    @GetMapping("/api/chat")
    public String chatWithGemini(@RequestParam String msg) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Tính cách AI mặn mòi cho rạp PELE
            String systemPrompt = "Bạn là PELE AI, trợ lý ảo rạp PELE Cinema. Gọi khách là Boss. Trả lời cực kỳ hài hước, mặn mòi, ngắn gọn dưới 2 câu.";

            // Đóng gói JSON
            JSONObject jsonBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject().put("text", systemPrompt + " Khách hỏi: " + msg);
            JSONArray parts = new JSONArray().put(part);
            contents.put(new JSONObject().put("parts", parts));
            jsonBody.put("contents", contents);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

            try (Response response = client.newCall(request).execute()) {
                String responseData = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    System.err.println("❌ LỖI GOOGLE: " + response.code() + " - " + responseData);
                    return "Ma lực AI đang bận (Lỗi " + response.code() + "), sếp thử lại nhé!";
                }

                JSONObject jsonResponse = new JSONObject(responseData);
                return jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }
        } catch (Exception e) {
            return "Kinh mạch AI bị tắc nghẽn: " + e.getMessage();
        }
    }
}
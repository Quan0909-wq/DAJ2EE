package com.example.demoj2ee.controller;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ChatController {

    private final String API_KEY = "AIzaSyAqeNzAH0QXOPnGZCfIKtQp3YLlANbv2pI";
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    @GetMapping("/api/chat")
    public String chatWithGemini(@RequestParam String msg) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            String systemPrompt = "Ban la PELE AI, tro ly ao rap PELE Cinema. Goi khach la Boss. Tra loi cuc ki hai huoc, man moi, ngan gon duoi 2 cau.";

            JSONObject jsonBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject().put("text", systemPrompt + " Khach hoi: " + msg);
            JSONArray parts = new JSONArray().put(part);
            contents.put(new JSONObject().put("parts", parts));
            jsonBody.put("contents", contents);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

            try (Response response = client.newCall(request).execute()) {
                String responseData = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    return "Ma luc AI dang ban (Loi " + response.code() + "), sep thu lai nhe!";
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
            return "Kinh mach AI bi tac nghen: " + e.getMessage();
        }
    }
}

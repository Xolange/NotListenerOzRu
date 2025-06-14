package com.example.notlistener; // Должен совпадать с вашим applicationId в build.gradle

import android.app.Notification;
import android.content.pm.ServiceInfo;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    private static final String CHANNEL_ID = "notification_forwarder_channel";
    private static final int FOREGROUND_ID = 1;

    private TelegramBotHelper telegramBotHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        telegramBotHelper = new TelegramBotHelper(getApplicationContext());
        createNotificationChannel();
        startForegroundService();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Forwarder",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Service for forwarding notifications to Telegram");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Создаем канал уведомлений
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Forwarder",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Service for forwarding notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

            // Создаем уведомление
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Notification Forwarder")
                    .setContentText("Forwarding notifications to Telegram")
                    .setSmallIcon(R.drawable.ic_notification)
                    .build();

            // Запускаем foreground service с указанием типа
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(FOREGROUND_ID, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(FOREGROUND_ID, notification);
            }
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Notification received from: " + sbn.getPackageName());


        // Игнорируем собственные уведомления
        if (sbn.getPackageName().equals(getPackageName())) {
            return;
        }

        try {
            String packageName = sbn.getPackageName();
            String title = "";
            String text = "";

            // Извлекаем данные из уведомления
            if (sbn.getNotification().extras != null) {
                title = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE, "");
                CharSequence textSeq = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT);
                text = textSeq != null ? textSeq.toString() : "";
            }

            // Формируем сообщение
            String message = "📱 Новое уведомление:\n" +
                    "Приложение: " + getAppName(packageName) + "\n" +
                    (title.isEmpty() ? "" : "Заголовок: " + title + "\n") +
                    (text.isEmpty() ? "" : "Текст: " + text + "\n") +
                    "Пакет: " + packageName;

            Log.d(TAG, "Forwarding notification: " + message);
            telegramBotHelper.sendNotification(message);
            Log.d(TAG, "Preparing to send: " + message);
            telegramBotHelper.sendNotification(message);

        } catch (Exception e) {
            Log.e(TAG, "Error processing notification", e);
        }
    }

    private String getAppName(String packageName) {
        try {
            return getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(packageName, 0)
            ).toString();
        } catch (Exception e) {
            return packageName;
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Не требуется для нашей задачи
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
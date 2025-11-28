package com.example.midterm_expriment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "todo_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        long noteId = intent.getLongExtra("note_id", -1);
        String title = intent.getStringExtra("note_title");

        if (noteId != -1) {
            showNotification(context, title, noteId);
        }
    }

    private void showNotification(Context context, String title, long noteId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 1. 创建通知通道 (Android O 及以上要求)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "待办提醒",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // 2. 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_notes) // 替换成你的图标
                .setContentTitle("待办提醒: " + title)
                .setContentText("是时候处理你的待办事项了！")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 3. 发送通知
        notificationManager.notify((int) noteId, builder.build());
    }
}
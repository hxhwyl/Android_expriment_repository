package com.example.midterm_expriment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public class NoteAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long noteId = intent.getLongExtra("note_id", -1);
        String noteTitle = intent.getStringExtra("note_title");

        if (noteId == -1) return;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "note_todo_channel";

        // Android 8.0+ 需要通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "待办提醒", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        // 点击通知跳转回编辑页面
        Intent editIntent = new Intent(context, NoteEditor.class);
        editIntent.setAction(Intent.ACTION_EDIT);
        editIntent.setData(ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, noteId));
        // 必须加上 Flags
        editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) noteId, editIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }

        builder.setContentTitle("待办事项提醒")
                .setContentText("需要完成: " + noteTitle)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL); // 震动和声音

        nm.notify((int) noteId, builder.build());
    }
}
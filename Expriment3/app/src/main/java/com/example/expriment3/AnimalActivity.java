package com.example.expriment3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalActivity extends AppCompatActivity {


    private static final String NOTIFICATION_CHANNEL_ID = "animal_channel";
    // 假设这是您的应用程序图标，替换为您的资源
    private static final int APP_ICON = R.mipmap.ic_launcher;
    private int[] iconIds = {
            R.drawable.lion,
            R.drawable.tiger,
            R.drawable.monkey,
            R.drawable.dog,
            R.drawable.cat,
            R.drawable.elephant
    };
    private String[] animalNames = {
            "Lion",
            "Tiger",
            "Monkey",
            "Dog",
            "Cat",
            "Elephant"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用 activity_animal.xml 布局
        setContentView(R.layout.activity_animal);

        ListView listView = findViewById(R.id.list_view_animals);

        // 确保通知通道在Android O及以上版本创建
        createNotificationChannel();

        // 1. 准备数据源
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < animalNames.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", animalNames[i]); // 键名: name
            map.put("icon", iconIds[i]);     // 键名: icon
            data.add(map);
        }

        // 2. SimpleAdapter 配置
        String[] from = {"name", "icon"}; // Map中的键名
        int[] to = {R.id.tv_animal_name, R.id.img_animal_icon}; // 列表项布局 (list_item_template.xml) 中的控件ID

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.list_item_template,
                from,
                to
        );

        listView.setAdapter(adapter);

        // 3. 设置列表项点击事件 (Toast & 通知)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // 设置当前点击项为选中状态
                view.setSelected(true);


                // 获取选中的数据
                Map<String, Object> selectedItem = (Map<String, Object>) parent.getItemAtPosition(position);
                String animalName = (String) selectedItem.get("name");

                // (3) 使用Toast显示选中的列表项信息
                Toast.makeText(AnimalActivity.this, animalName, Toast.LENGTH_SHORT).show();

                // (4) 发送通知
                sendNotification(animalName);
            }
        });
    }

    // (4) 发送通知的实现方法
    private void sendNotification(String title) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 点击通知后启动 AnimalActivity 的 Intent
        Intent intent = new Intent(this, AnimalActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                // 通知Icon为应用程序图标
                .setSmallIcon(APP_ICON)
                // 显示的Title为列表项内容
                .setContentTitle(title)
                // 通知内容自拟
                .setContentText("您刚刚选择了 " + title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // 设置点击操作
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // 点击后自动清除

        // 发送通知 (使用唯一ID)
        notificationManager.notify(title.hashCode(), builder.build());
    }

//    // (4) Android O (API 26) 及以上需要创建通知通道
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Animal Notifications";
            String description = "Channel for animal selection notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    name,
                    importance
            );
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
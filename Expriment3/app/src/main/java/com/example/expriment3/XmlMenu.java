package com.example.expriment3;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class XmlMenu extends AppCompatActivity {

    private TextView tvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_menu);
        tvTest = findViewById(R.id.tv_test);


        //绑定并设置 Toolbar 作为 ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // 关键：将 Toolbar 与 ActionBar 关联
        tvTest = findViewById(R.id.tv_test);
    }

    // 加载菜单布局
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }



    // 处理菜单点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId(); // 获取菜单项的id

        // 字体大小-小
        if (itemId == R.id.item_font_small) {
            tvTest.setTextSize(10);
            return true;
        }
        // 字体大小-中
        else if (itemId == R.id.item_font_medium) {
            tvTest.setTextSize(16);
            return true;
        }
        // 字体大小-大
        else if (itemId == R.id.item_font_large) {
            tvTest.setTextSize(20);
            return true;
        }
        // 普通菜单项
        else if (itemId == R.id.item_normal) {
            Toast.makeText(this, "点击了普通菜单项", Toast.LENGTH_SHORT).show();
            return true;
        }
        // 字体颜色-红色
        else if (itemId == R.id.item_color_red) {
            tvTest.setTextColor(Color.RED);
            return true;
        }
        // 字体颜色-黑色
        else if (itemId == R.id.item_color_black) {
            tvTest.setTextColor(Color.BLACK);
            return true;
        }
        // 默认情况（未匹配到任何菜单项）
        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
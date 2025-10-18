package com.example.expriment3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;

import android.widget.Toast;


public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 页面启动时直接显示自定义对话框（无需按钮点击）
        showLoginDialog();
    }

    // 自定义对话框逻辑
    private void showLoginDialog() {
        // 加载自定义布局
        View dialogView = getLayoutInflater().inflate(R.layout.activity_login, null);

        // 获取布局中的控件
        EditText etUsername = dialogView.findViewById(R.id.et_username);
        EditText etPassword = dialogView.findViewById(R.id.et_password);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSignIn = dialogView.findViewById(R.id.btn_sign_in);

        // 创建对话框构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("登录"); // 设置对话框标题
        builder.setView(dialogView); // 添加自定义布局
        builder.setCancelable(false); // 禁止点击外部关闭对话框（可选）

        // 创建对话框实例
        final AlertDialog dialog = builder.create();

        // 取消按钮逻辑
        /*
        todo:将取消和确定逻辑执行完毕后finish
         */
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });


        // 登录按钮逻辑
        btnSignIn.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "登录成功：" + username, Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // 登录成功后关闭对话框
                finish();
            }

        });

        // 显示对话框
        dialog.show();
    }
}
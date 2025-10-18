package com.example.expriment3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {


    Button bt1,bt2,bt3,bt4;
    Intent intent = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
    bt1 = findViewById(R.id.animal);
    bt2 = findViewById(R.id.login);
    bt3 = findViewById(R.id.menu);
    bt4 = findViewById(R.id.contextmenu);

    bt1.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            intent = new Intent(MainActivity.this, AnimalActivity.class);
            startActivity(intent);
        }
    });
    bt2.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        }
    });

    bt3.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            intent = new Intent(MainActivity.this, XmlMenu.class);
            startActivity(intent);
        }
    });

    bt4.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            intent = new Intent(MainActivity.this, ContextMenuActivity.class);
            startActivity(intent);
        }
    });
    }
}
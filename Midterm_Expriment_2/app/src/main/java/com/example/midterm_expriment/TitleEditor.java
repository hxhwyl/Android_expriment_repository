package com.example.midterm_expriment;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TitleEditor extends Activity {
    private Uri mUri;
    private EditText mText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title_editor);
        mUri = getIntent().getData();
        mText = findViewById(R.id.title);

        Button b = findViewById(R.id.ok);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mText.getText().length() == 0) return;
                ContentValues values = new ContentValues();
                values.put(NotePad.Notes.COLUMN_NAME_TITLE, mText.getText().toString());
                getContentResolver().update(mUri, values, null, null);
                finish();
            }
        });
    }
}
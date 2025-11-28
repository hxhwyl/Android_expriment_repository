package com.example.midterm_expriment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NoteEditor extends Activity {

    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
            NotePad.Notes.COLUMN_NAME_CATEGORY,
            NotePad.Notes.COLUMN_NAME_IS_TODO,
            NotePad.Notes.COLUMN_NAME_DUE_DATE
    };

    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private EditText mCategoryText;
    private CheckBox mTodoCheck;
    private TextView mTvDueDate;
    private String mNoteTitle = "新待办事项"; // 用于闹钟的标题，初始值

    private long mDueDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_editor);

        mText = findViewById(R.id.note);
        mCategoryText = findViewById(R.id.category_content);
        mTodoCheck = findViewById(R.id.todo_check);
        mTvDueDate = findViewById(R.id.tv_due_date);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_EDIT.equals(action)) {
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            mUri = getContentResolver().insert(intent.getData(), null);
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
        } else {
            finish();
            return;
        }

        // CheckBox 点击事件：选择时间
        mTodoCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTodoCheck.isChecked()) {
                    // 如果已经是待办，并且有时间，不再弹出选择器，除非用户点击 TextView
                    if (mDueDate == 0) {
                        showDateTimePicker();
                    }
                } else {
                    mDueDate = 0;
                    updateDueDateDisplay();
                }
            }
        });

        // 点击时间文本也可以重新选择时间
        mTvDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });
    }

    // 显示日期和时间选择器
    private void showDateTimePicker() {
        final Calendar c = Calendar.getInstance();
        if (mDueDate > 0) c.setTimeInMillis(mDueDate);

        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                new TimePickerDialog(NoteEditor.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0); // 清除毫秒

                        mDueDate = c.getTimeInMillis();

                        // 如果选择的时间比现在早，提示一下
                        if (mDueDate < System.currentTimeMillis()) {
                            Toast.makeText(NoteEditor.this, "注意：设置的时间已过期", Toast.LENGTH_SHORT).show();
                        }

                        mTodoCheck.setChecked(true);
                        updateDueDateDisplay();
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // 【已移除重复且不使用的 setReminder/cancelReminder 方法】

    private void updateDueDateDisplay() {
        if (mDueDate > 0 && mTodoCheck.isChecked()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm 提醒", Locale.getDefault());
            mTvDueDate.setText(sdf.format(mDueDate));
            mTvDueDate.setVisibility(View.VISIBLE);
        } else {
            mTvDueDate.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCursor = getContentResolver().query(mUri, PROJECTION, null, null, null);
        if (mCursor != null && mCursor.moveToFirst()) {
            mText.setText(mCursor.getString(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_NOTE)));
            mCategoryText.setText(mCursor.getString(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CATEGORY)));

            int isTodo = mCursor.getInt(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_IS_TODO));
            mTodoCheck.setChecked(isTodo == 1);

            mDueDate = mCursor.getLong(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_DUE_DATE));
            updateDueDateDisplay();

            // 获取标题并设置
            mNoteTitle = mCursor.getString(mCursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_TITLE));
            setTitle(mNoteTitle);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 确保在光标关闭前执行保存和闹钟设置
        if (mCursor != null) {
            saveNote();
            mCursor.close();
        }
    }

    private void saveNote() {
        if (mUri == null) return;

        String text = mText.getText().toString();
        String category = mCategoryText.getText().toString();
        boolean isTodo = mTodoCheck.isChecked();
        if (!isTodo) mDueDate = 0; // 如果取消待办，时间置空

        if (category == null || category.trim().isEmpty()) category = "Default";

        // 尝试从 Intent 中获取标题，如果光标不可用
        String titleForAlarm = mNoteTitle != null ? mNoteTitle : "（无标题）";

        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, category);
        values.put(NotePad.Notes.COLUMN_NAME_IS_TODO, isTodo ? 1 : 0);
        values.put(NotePad.Notes.COLUMN_NAME_DUE_DATE, mDueDate);
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());

        getContentResolver().update(mUri, values, null, null);

        // 处理闹钟
        setAlarm(isTodo, mDueDate, titleForAlarm);
    }

    // 【关键修正：统一接收器名称并简化逻辑】
    private void setAlarm(boolean isTodo, long time, String title) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        long noteId = Long.parseLong(mUri.getLastPathSegment());

        // Android 12+ 精确闹钟权限检查 (仅做提示，强行设置会崩溃，但权限已添加)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                // 如果用户禁用此权限，可以在这里提示用户，但我们已在清单文件中请求了权限。
                Toast.makeText(this, "警告：精确闹钟权限可能被禁用，提醒可能不准时。", Toast.LENGTH_LONG).show();
            }
        }

        // 【修正：使用正确的 ReminderReceiver.class】
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("note_id", noteId);
        intent.putExtra("note_title", title);

        // FLAG_IMMUTABLE 是必需的
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                (int) noteId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (isTodo && time > System.currentTimeMillis()) {
            // 使用 setExact 确保提醒准确
            am.setExact(AlarmManager.RTC_WAKEUP, time, pi);
        } else {
            // 如果不是待办或时间已过，取消闹钟
            am.cancel(pi);
        }
    }

    // ... (菜单代码保持不变) ...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            saveNote();
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            if (mUri != null) {
                // 删除时也取消闹钟
                setAlarm(false, 0, "");
                getContentResolver().delete(mUri, null, null);
            }
            mUri = null;
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_revert) {
            // 保持原有逻辑
            return true;
        } else if (item.getItemId() == R.id.menu_title) {
            Intent intent = new Intent(this, TitleEditor.class);
            intent.setData(mUri);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
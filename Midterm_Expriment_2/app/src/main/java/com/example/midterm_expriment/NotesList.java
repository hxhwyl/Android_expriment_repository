package com.example.midterm_expriment;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

//import androidx.loader.app.LoaderManager;
//import androidx.loader.content.CursorLoader;
//import androidx.loader.content.Loader;

import android.app.LoaderManager;       // 使用原生 LoaderManager
import android.content.CursorLoader;     // 使用原生 CursorLoader
import android.content.Loader;           // 使用原生 Loader

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_CREATE_DATE // 获取创建时间用于显示
    };

    private SimpleCursorAdapter mAdapter;
    private Spinner mSpinner;

    // [修正：添加 Loader ID 常量]
    private static final int NOTES_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置包含搜索按钮的自定义布局
        setContentView(R.layout.notes_list);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        // 设置搜索按钮监听
        Button searchBtn = findViewById(R.id.btn_open_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotesList.this, NoteSearch.class));
            }
        });

        getListView().setOnCreateContextMenuListener(this);

        // 1. 创建一个空的 Cursor 用于初始化 Adapter
        Cursor cursor = null; // 初始为 null
        String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_CREATE_DATE };
        int[] viewIDs = { android.R.id.text1, R.id.text2 };


        mAdapter = new SimpleCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor, // 初始使用 null Cursor
                dataColumns,
                viewIDs
        );


        // ViewBinder 用于将 Long 时间戳转换为可读的日期字符串
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CREATE_DATE)) {
                    long timestamp = cursor.getLong(columnIndex);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    ((TextView) view).setText(sdf.format(new Date(timestamp)));
                    return true;
                }
                return false;
            }
        });
        setListAdapter(mAdapter);
        // 2. 初始化 LoaderManager 来异步加载数据
        getLoaderManager().initLoader(NOTES_LOADER_ID, null, this);
        // [新增] 初始化分类下拉框
        initCategorySpinner();

    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // 在后台线程创建并执行查询
        String selection = null;
        String[] selectionArgs = null;

        // 获取当前 Spinner 选择的分类（如果需要）
        if (mSpinner != null && mSpinner.getAdapter() != null) {
            String category = mSpinner.getSelectedItem().toString();
            // 重复 updateNoteList 中的筛选逻辑
            if ("待办事项".equals(category)) {
                selection = NotePad.Notes.COLUMN_NAME_IS_TODO + "=?";
                selectionArgs = new String[] { "1" };
            } else if (!"全部笔记".equals(category)) {
                selection = NotePad.Notes.COLUMN_NAME_CATEGORY + "=?";
                selectionArgs = new String[] { category };
            }
        }

        return new CursorLoader(
                this,
                getIntent().getData(),
                PROJECTION,
                selection,
                selectionArgs,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // 加载完成后，将新的 Cursor 切换给 Adapter
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // 当 Loader 重置时，关闭 Adapter 上的 Cursor
        mAdapter.changeCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add) {
            startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            return true;
        } else if (item.getItemId() == R.id.menu_paste) {
            return true; // 简化处理
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu); // 假设你有名为 context_menu.xml 的文件
        menu.setHeaderTitle("操作笔记");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        if (item.getItemId() == R.id.context_open) {
            startActivity(new Intent(Intent.ACTION_EDIT, ContentUris.withAppendedId(getIntent().getData(), info.id)));
            return true;
        } else if (item.getItemId() == R.id.context_delete) {
            getContentResolver().delete(ContentUris.withAppendedId(getIntent().getData(), info.id), null, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        startActivity(new Intent(Intent.ACTION_EDIT, uri));
    }






    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到主页刷新下拉框，因为可能新增了分类
        initCategorySpinner();
    }

    private void initCategorySpinner() {
        mSpinner = findViewById(R.id.spinner_category);

        // 1. 获取所有唯一分类
        List<String> categories = new ArrayList<>();
        categories.add("全部笔记");  // 默认选项 1
        categories.add("待办事项");  // 默认选项 2 (你的待办区)

        // [关键修改：将 DISTINCT 作为前缀，让 Provider 识别并设置 qb.setDistinct(true)]
        Cursor catCursor = getContentResolver().query(
                NotePad.Notes.CONTENT_URI,
                new String[] { "DISTINCT " + NotePad.Notes.COLUMN_NAME_CATEGORY },
                null,
                null,
                NotePad.Notes.COLUMN_NAME_CATEGORY + " ASC");


        if (catCursor != null) {
            while (catCursor.moveToNext()) {
                String cat = catCursor.getString(0);
                if (cat != null && !cat.isEmpty()) {
                    categories.add(cat);
                }
            }
            catCursor.close();
        }

        // 2. 设置 Spinner Adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);

        // 3. 监听选择事件
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                updateNoteList(selected);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // --- 修改 updateNoteList 方法以触发重新加载 ---
    private void updateNoteList(String category) {
        // 无需手动执行 query 和 changeCursor
        // 只需要通知 Loader 重新加载数据

        // 将筛选条件存储到 Bundle 中
        Bundle bundle = new Bundle();
        if ("待办事项".equals(category)) {
            bundle.putString("selection", NotePad.Notes.COLUMN_NAME_IS_TODO + "=?");
            bundle.putStringArray("selectionArgs", new String[] { "1" });
        } else if (!"全部笔记".equals(category)) {
            bundle.putString("selection", NotePad.Notes.COLUMN_NAME_CATEGORY + "=?");
            bundle.putStringArray("selectionArgs", new String[] { category });
        }

        // 触发 Loader 重新加载数据，会调用 onCreateLoader
        getLoaderManager().restartLoader(NOTES_LOADER_ID, bundle, this);
    }







}
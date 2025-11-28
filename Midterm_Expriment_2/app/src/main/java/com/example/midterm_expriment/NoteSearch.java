package com.example.midterm_expriment;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteSearch extends Activity implements SearchView.OnQueryTextListener {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false); // 默认展开

        listView = findViewById(R.id.list_search_results);

        // 点击搜索结果跳转到编辑
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(NoteSearch.this, NoteEditor.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.setData(ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, id));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
        String[] selectionArgs = new String[] { "%" + newText + "%", "%" + newText + "%" };

        Cursor cursor = getContentResolver().query(
                NotePad.Notes.CONTENT_URI,
                new String[] { NotePad.Notes._ID, NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_CREATE_DATE },
                selection,
                selectionArgs,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );

        String[] from = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_CREATE_DATE };
        int[] to = { android.R.id.text1, R.id.text2 };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.noteslist_item, // 复用主界面的Item布局
                cursor,
                from,
                to
        );

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.text2) {
                    long timestamp = cursor.getLong(columnIndex);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    ((TextView) view).setText(sdf.format(new Date(timestamp)));
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(adapter);
        return true;
    }
}
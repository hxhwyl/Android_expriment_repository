package com.example.midterm_expriment;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

public class NotePadProvider extends ContentProvider {

    private static final String DATABASE_NAME = "note_pad.db";
    private static final int DATABASE_VERSION = 6; // 版本 3，包含新字段
    private static final String TABLE_NAME = "notes";

    private static HashMap<String, String> sNotesProjectionMap;

    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;

    private static final UriMatcher sUriMatcher;

    private DatabaseHelper mOpenHelper;

    // 静态代码块，初始化 UriMatcher 和 投影映射(Projection Map)
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes", NOTES);
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID);

        sNotesProjectionMap = new HashMap<>();
        sNotesProjectionMap.put(NotePad.Notes._ID, NotePad.Notes._ID);
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_TITLE);
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_NOTE, NotePad.Notes.COLUMN_NAME_NOTE);
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, NotePad.Notes.COLUMN_NAME_CREATE_DATE);
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);
        // 必须映射新增的字段，否则查询时会找不到列
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_CATEGORY, NotePad.Notes.COLUMN_NAME_CATEGORY);
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_IS_TODO, NotePad.Notes.COLUMN_NAME_IS_TODO);

        //新增
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_DUE_DATE, NotePad.Notes.COLUMN_NAME_DUE_DATE);
    }

    /**
     * 数据库助手类
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                    + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_CATEGORY + " TEXT DEFAULT 'Default',"
                    + NotePad.Notes.COLUMN_NAME_IS_TODO + " INTEGER DEFAULT 0,"
                    + NotePad.Notes.COLUMN_NAME_DUE_DATE + " INTEGER DEFAULT 0" // [新增]
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 简单粗暴的升级策略：删除旧表重建。
            // 实际生产中应该使用 ALTER TABLE 保留数据。
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        qb.setProjectionMap(sNotesProjectionMap);

        // [关键修改：允许使用 DISTINCT 关键字]
        if (projection != null && projection.length == 1 && projection[0].startsWith("DISTINCT ")) {
            // 如果 projection 看起来像 ["DISTINCT column_name"]，我们只取 column_name
            qb.setDistinct(true);
            // 修正 projection，去掉 DISTINCT 关键字，只保留列名
            projection[0] = projection[0].substring("DISTINCT ".length());
        }

        switch (sUriMatcher.match(uri)) {
            case NOTES:
                break;
            case NOTE_ID:
                qb.appendWhere(NotePad.Notes._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = NotePad.Notes.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // 设置通知 URI，当数据改变时通知监听者
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                return NotePad.Notes.CONTENT_TYPE;
            case NOTE_ID:
                return NotePad.Notes.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // 验证 URI
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = System.currentTimeMillis();

        // 确保创建日期存在
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE)) {
            values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, now);
        }

        // 确保修改日期存在
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)) {
            values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, now);
        }

        // 确保标题存在
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_TITLE)) {
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, ""); // 默认为空，或者 "无标题"
        }

        // 确保笔记内容存在
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_NOTE)) {
            values.put(NotePad.Notes.COLUMN_NAME_NOTE, "");
        }

        // [关键] 确保分类存在
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_CATEGORY)) {
            values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, "Default");
        }

        // [关键] 确保待办状态存在
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_IS_TODO)) {
            values.put(NotePad.Notes.COLUMN_NAME_IS_TODO, 0);
        }

        // [新增] 初始化提醒时间为 0
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_DUE_DATE)) {
            values.put(NotePad.Notes.COLUMN_NAME_DUE_DATE, 0);
        }


        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, NotePad.Notes.COLUMN_NAME_NOTE, values);

        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                count = db.delete(TABLE_NAME, where, whereArgs);
                break;

            case NOTE_ID:
                String noteId = uri.getPathSegments().get(1);
                count = db.delete(TABLE_NAME, NotePad.Notes._ID + "=" + noteId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                count = db.update(TABLE_NAME, values, where, whereArgs);
                break;

            case NOTE_ID:
                String noteId = uri.getPathSegments().get(1);
                count = db.update(TABLE_NAME, values, NotePad.Notes._ID + "=" + noteId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
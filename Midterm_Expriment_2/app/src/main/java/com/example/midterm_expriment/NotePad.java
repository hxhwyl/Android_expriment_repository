package com.example.midterm_expriment;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NotePad {
    public static final String AUTHORITY = "com.google.provider.NotePad";

    private NotePad() {}

    public static final class Notes implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notes");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        // 基本字段
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_NOTE = "note";
        public static final String COLUMN_NAME_CREATE_DATE = "created";
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";

        // 新增字段
        public static final String COLUMN_NAME_CATEGORY = "category"; // 分类
        public static final String COLUMN_NAME_IS_TODO = "is_todo";   // 待办 (0=笔记, 1=待办)
        // [新增] 提醒/到期时间 (存放 System.currentTimeMillis() 的 Long 值)
        public static final String COLUMN_NAME_DUE_DATE = "due_date";
    }
}
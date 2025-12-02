# 期中实验报告（notepad笔记本应用）

该笔记本应用基于 Android 原生 API 开发，采用 ContentProvider 实现数据持久化，支持笔记管理核心功能 + 拓展功能（时间戳、搜索、分类、待办提醒）。以下分功能拆解实现逻辑、核心原理及具体代码解释：


## 一、核心基础功能（新增 / 查看 / 编辑笔记）

## 实现效果

### 点击新增笔记
<img width="519" height="1154" alt="950e9222bfd53465497ff4ad1e23a98a" src="https://github.com/user-attachments/assets/042ff2bb-7d5c-4353-b82e-72db66adb03e" />

### 进入笔记编辑界面，可以修改标题与内容
<img width="523" height="1152" alt="6e4112e3dee33f540091e580f52403f8" src="https://github.com/user-attachments/assets/9c5a4326-a8d6-4450-ab34-73c743c45814" />

<img width="519" height="1157" alt="0645ffc95a6553a0351710d007f6fb86" src="https://github.com/user-attachments/assets/53133121-edf1-4dfe-a2ff-b2d18a345ace" />

<img width="526" height="1156" alt="506b463d7f2b9b964ee802df98bf8b42" src="https://github.com/user-attachments/assets/4f68e2d5-fcc8-4e0d-a365-9e5d21d5d79e" />





### 1. 功能概述
支持创建新笔记、查看笔记列表、编辑已有笔记，是所有拓展功能的基础。
### 2. 实现原理
数据存储：使用 SQLiteDatabase 作为本地数据库，通过 NotePadProvider（继承 ContentProvider）封装数据增删改查接口，解耦数据层与 UI 层。
数据模型：NotePad.Notes 类定义数据表字段（标题、内容、创建时间、修改时间等），遵循 BaseColumns 规范。
页面交互：NotesList（笔记列表页）展示数据，NoteEditor（编辑页）处理新增 / 编辑操作，通过 Intent 传递数据 URI。
### 3. 核心代码块及解释
#### （1）数据模型定义（NotePad.java）

```java
public final class NotePad {
    public static final String AUTHORITY = "com.google.provider.NotePad"; // ContentProvider 唯一标识
    private NotePad() {}

    public static final class Notes implements BaseColumns {
        // 数据URI：content://com.google.provider.NotePad/notes
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notes");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note"; // 多条数据类型
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note"; // 单条数据类型
        public static final String DEFAULT_SORT_ORDER = "modified DESC"; // 默认按修改时间降序
        // 核心字段
        public static final String COLUMN_NAME_TITLE = "title"; // 标题
        public static final String COLUMN_NAME_NOTE = "note"; // 内容
        public static final String COLUMN_NAME_CREATE_DATE = "created"; // 创建时间
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified"; // 修改时间
        // 拓展字段（分类/待办/提醒时间）
        public static final String COLUMN_NAME_CATEGORY = "category"; 
        public static final String COLUMN_NAME_IS_TODO = "is_todo";
        public static final String COLUMN_NAME_DUE_DATE = "due_date";
    }
}
```
解释：采用单例模式避免实例化，Notes 类继承 BaseColumns 自动获得 _ID 主键字段。定义 CONTENT_URI 作为 ContentProvider 的访问入口，遵循 Android 内容提供者规范。
#### （2）数据持久化实现（NotePadProvider.java - insert 方法）
```java
@Override
public Uri insert(Uri uri, ContentValues initialValues) {
    if (sUriMatcher.match(uri) != NOTES) {
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    ContentValues values = initialValues != null ? new ContentValues(initialValues) : new ContentValues();
    Long now = System.currentTimeMillis();

    // 补全默认字段（避免空值）
    if (!values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE)) {
        values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, now);
    }
    if (!values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)) {
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, now);
    }
    if (!values.containsKey(NotePad.Notes.COLUMN_NAME_TITLE)) {
        values.put(NotePad.Notes.COLUMN_NAME_TITLE, "");
    }
    if (!values.containsKey(NotePad.Notes.COLUMN_NAME_NOTE)) {
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, "");
    }

    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    long rowId = db.insert(TABLE_NAME, NotePad.Notes.COLUMN_NAME_NOTE, values); // 插入数据

    if (rowId > 0) {
        Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, rowId); // 拼接新数据URI
        getContext().getContentResolver().notifyChange(noteUri, null); // 通知数据变化
        return noteUri;
    }
    throw new SQLException("Failed to insert row into " + uri);
}
```
解释：插入数据前补全默认值（如创建时间、修改时间），确保数据完整性。插入成功后通过 ContentUris.withAppendedId 生成新数据的唯一 URI（格式：content://.../notes/[rowId]），并通知内容观察者数据变化。


#### （3）笔记编辑功能（NoteEditor.java - saveNote 方法）
```java
private void saveNote() {
    if (mUri == null) return;

    String text = mText.getText().toString();
    String category = mCategoryText.getText().toString();
    boolean isTodo = mTodoCheck.isChecked();
    if (!isTodo) mDueDate = 0; // 取消待办时清空提醒时间

    if (category == null || category.trim().isEmpty()) category = "Default";

    ContentValues values = new ContentValues();
    values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);
    values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, category);
    values.put(NotePad.Notes.COLUMN_NAME_IS_TODO, isTodo ? 1 : 0);
    values.put(NotePad.Notes.COLUMN_NAME_DUE_DATE, mDueDate);
    values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis()); // 更新修改时间

    getContentResolver().update(mUri, values, null, null); // 调用ContentProvider更新数据
    setAlarm(isTodo, mDueDate, mNoteTitle); // 联动待办提醒功能
}
```
解释：保存时收集编辑页输入的内容、分类、待办状态等信息，封装为 ContentValues。通过 getContentResolver().update 调用 ContentProvider 更新数据库，同时更新修改时间戳。联动 setAlarm 方法，实现 “待办状态变更→提醒闹钟同步”。



## 添加时间戳

<img width="521" height="1153" alt="3bcfde4668a50f67af816032eee23c56" src="https://github.com/user-attachments/assets/31bfc591-bc47-4454-a076-4a112419699a" />

## 笔记搜索

<img width="519" height="1156" alt="51faf32285730d648e992c76b658d6f9" src="https://github.com/user-attachments/assets/ff68dfa1-d4d6-472c-a6f7-9d1423973926" />

<img width="520" height="1155" alt="1a9f78a87a68509dc3ca75ab85c7f083" src="https://github.com/user-attachments/assets/d9cdb75f-e079-47d7-ab27-a31b3621cc20" />

<img width="519" height="1152" alt="66500eb38a8165bc8357c2493d2a0bc4" src="https://github.com/user-attachments/assets/9fc081e1-6543-48a6-a587-a8fe5891ccb6" />

<img width="520" height="1154" alt="ce890b307adde35c73eb96e2481a7c26" src="https://github.com/user-attachments/assets/f032927a-a594-4061-9c2a-c7c9bd78fd20" />

## 笔记分类

<img width="523" height="1158" alt="d504feb48de94c0e1f28cd3208bfb800" src="https://github.com/user-attachments/assets/6baa4de2-2e53-492c-a87e-61e8be9e380d" />

<img width="518" height="1155" alt="539f7f73442c981adff55c981e0443e3" src="https://github.com/user-attachments/assets/2d99725a-4358-47db-9d30-6d635578ce1e" />

<img width="521" height="1160" alt="f71cc03bf98add211b50b67352718960" src="https://github.com/user-attachments/assets/291583fd-7a99-47b8-b5ce-0cdf33060c7c" />

<img width="522" height="1156" alt="4fe41f69309134d9970a78378fb4d05f" src="https://github.com/user-attachments/assets/ca985930-4428-460c-83dc-b306828bb3b4" />

<img width="522" height="1155" alt="b62dbc3fab07eb46357990576e69a818" src="https://github.com/user-attachments/assets/0368b335-8841-4958-bc8d-04401396c4d9" />

<img width="518" height="1151" alt="6bbc92d55c10376a859ed2d75cfa0eaf" src="https://github.com/user-attachments/assets/743ae280-00f3-4f76-b3d2-34bf6da5fdd1" />

## 添加笔记待办

<img width="523" height="1156" alt="9cf70d81d5ab02da07007132dcb8c899" src="https://github.com/user-attachments/assets/6f3b7d67-0840-4b37-a334-f79f3a13d804" />

<img width="521" height="1156" alt="cf6af85193d31e9696bc4323bed2383b" src="https://github.com/user-attachments/assets/1a0a0352-b332-410c-9775-24a8e17be729" />

<img width="525" height="1156" alt="1a87ff6cb1ab9a8c34f5cddfd8dd15f6" src="https://github.com/user-attachments/assets/0ffb5d89-6709-4e74-a241-364733b99068" />

可以看到由于修改了笔记内容，所以笔记时间戳也一起改变，且待办事项的分类里多了刚刚添加的待办

<img width="520" height="1154" alt="d60af6100f2428a2286e49ec199c480d" src="https://github.com/user-attachments/assets/d53643cf-73ef-4f39-9897-abb5a91105d3" />

<img width="514" height="1152" alt="c51da9ca21640dc93a576ac8e2d9b17a" src="https://github.com/user-attachments/assets/8fbbcc68-1804-4464-af83-8322eaf492cb" />

<img width="521" height="1155" alt="563da9e301d2737e0537b997f3d80edf" src="https://github.com/user-attachments/assets/a07d2328-df0a-4560-bc45-d859e5443741" />

<img width="523" height="1153" alt="356beb822e13519e68ac9b5a081e1415" src="https://github.com/user-attachments/assets/a1f9945d-1e3b-48b1-b27c-c61e7803433a" />


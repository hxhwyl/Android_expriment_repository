# 期中实验报告（notepad笔记本应用）

该笔记本应用基于 Android 原生 API 开发，采用 ContentProvider 实现数据持久化，支持笔记管理核心功能 + 拓展功能（时间戳、搜索、分类、待办提醒）。以下分功能拆解实现逻辑、核心原理及具体代码解释：


## 一、核心基础功能（新增 / 查看 / 编辑笔记）

### 实现效果

### 点击新增笔记
<img width="519" height="1154" alt="950e9222bfd53465497ff4ad1e23a98a" src="https://github.com/user-attachments/assets/042ff2bb-7d5c-4353-b82e-72db66adb03e" />

### 进入笔记编辑界面，可以修改标题与内容
<img width="523" height="1152" alt="6e4112e3dee33f540091e580f52403f8" src="https://github.com/user-attachments/assets/9c5a4326-a8d6-4450-ab34-73c743c45814" />

### 修改后保存，取名为test1来方便后续测试
<img width="519" height="1157" alt="0645ffc95a6553a0351710d007f6fb86" src="https://github.com/user-attachments/assets/53133121-edf1-4dfe-a2ff-b2d18a345ace" />

### 再新增一个test3，方便后面的按内容搜索
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
解释：

采用单例模式避免实例化，Notes 类继承 BaseColumns 自动获得 _ID 主键字段。定义 CONTENT_URI 作为 ContentProvider 的访问入口，遵循 Android 内容提供者规范。

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
解释：

插入数据前补全默认值（如创建时间、修改时间），确保数据完整性。插入成功后通过 ContentUris.withAppendedId 生成新数据的唯一 URI（格式：content://.../notes/[rowId]），并通知内容观察者数据变化。

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
解释：

保存时收集编辑页输入的内容、分类、待办状态等信息，封装为 ContentValues。通过 getContentResolver().update 调用 ContentProvider 更新数据库，同时更新修改时间戳。联动 setAlarm 方法，实现 “待办状态变更→提醒闹钟同步”。



































---

## 二、重点拓展功能详解（时间戳 / 搜索 / 分类 / 待办）
## （一）时间戳功能（创建时间 + 修改时间）
### 实现效果
### 在前面创建好的test1，2，3，标题下有显示创建的时间戳，修改的后更新时间戳的效果会在后面修改状态时演示
<img width="521" height="1153" alt="3bcfde4668a50f67af816032eee23c56" src="https://github.com/user-attachments/assets/31bfc591-bc47-4454-a076-4a112419699a" />

### 1. 功能描述
自动记录笔记的「创建时间」和「修改时间」，在笔记列表页展示创建时间，支持按修改时间排序。
### 2. 实现原理
时间存储：以 Long 类型（System.currentTimeMillis() 毫秒值）存储在数据库，占用空间小、便于排序和格式化。

自动赋值：创建笔记时自动填充「创建时间」，编辑保存时自动更新「修改时间」。

格式展示：通过 SimpleDateFormat 将毫秒值转为可读字符串（如 2024-05-20 14:30:00）。

### 3. 核心代码块及解释
#### （1）数据库字段初始化（NotePadProvider.java）
```java
@Override
public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
            + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
            + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
            + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
            + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER," // 时间戳字段（INTEGER类型）
            + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER," // 时间戳字段
            + NotePad.Notes.COLUMN_NAME_CATEGORY + " TEXT DEFAULT 'Default',"
            + NotePad.Notes.COLUMN_NAME_IS_TODO + " INTEGER DEFAULT 0,"
            + NotePad.Notes.COLUMN_NAME_DUE_DATE + " INTEGER DEFAULT 0"
            + ");");
}
```
解释：

时间戳字段类型设为 INTEGER，存储 System.currentTimeMillis() 返回的毫秒值（如 1716200000000），比存储字符串更高效。

#### （2）时间戳自动赋值（NotePadProvider.java - insert/update 方法）
```java
// 插入时自动设置创建时间和初始修改时间
if (!values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE)) {
    values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, now);
}
if (!values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)) {
    values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, now);
}

// 更新时自动更新修改时间（NoteEditor.saveNote 中主动设置）
values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
```
解释：

插入笔记时：如果用户未指定创建时间，自动填充当前时间戳。

编辑笔记时：NoteEditor.saveNote 主动设置修改时间戳，确保每次编辑后时间同步更新。

#### （3）时间戳格式化展示（NotesList.java - ViewBinder）
``` java
mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        // 匹配创建时间字段
        if (columnIndex == cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CREATE_DATE)) {
            long timestamp = cursor.getLong(columnIndex); // 获取毫秒值
            // 格式化：转为 "2024-05-20 14:30:00" 字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            ((TextView) view).setText(sdf.format(new Date(timestamp)));
            return true; // 表示已手动处理该字段，无需Adapter默认处理
        }
        return false;
    }
});
```
解释：

使用 SimpleCursorAdapter.ViewBinder 自定义字段展示逻辑，将数据库中的 Long 型时间戳转为用户可读的日期字符串。

Locale.getDefault() 适配系统语言环境，确保日期格式符合用户习惯。


















































## （二）搜索功能

### 实现效果

### 点击左下角搜索按钮进入搜索界面，进入时没有携带数据所以默认不显示数据
<img width="519" height="1156" alt="51faf32285730d648e992c76b658d6f9" src="https://github.com/user-attachments/assets/ff68dfa1-d4d6-472c-a6f7-9d1423973926" />

### 搜索test，可以看到实时显示出三个test的内容，即支持模糊搜索
<img width="520" height="1155" alt="1a9f78a87a68509dc3ca75ab85c7f083" src="https://github.com/user-attachments/assets/d9cdb75f-e079-47d7-ab27-a31b3621cc20" />

### 搜索test1，可以看到显示出test1的内容，即支持精确搜索
<img width="519" height="1152" alt="66500eb38a8165bc8357c2493d2a0bc4" src="https://github.com/user-attachments/assets/9fc081e1-6543-48a6-a587-a8fe5891ccb6" />

### 搜索functionn，前面在test3中创建的特殊数据，可以看见显示出test3，即支持按内容搜索
<img width="520" height="1154" alt="ce890b307adde35c73eb96e2481a7c26" src="https://github.com/user-attachments/assets/f032927a-a594-4061-9c2a-c7c9bd78fd20" />

### 1. 功能描述
支持通过关键词搜索笔记标题和内容，实时展示搜索结果，点击结果可跳转编辑。

### 2. 实现原理

搜索触发：通过 SearchView 监听输入变化（onQueryTextChange），实时执行搜索。

数据库查询：使用 LIKE 模糊查询匹配标题（COLUMN_NAME_TITLE）和内容（COLUMN_NAME_NOTE）。

结果展示：用 SimpleCursorAdapter 绑定搜索结果到 ListView，复用笔记列表页的 Item 布局。

### 3. 核心代码块及解释

#### （1）搜索页面布局与初始化（NoteSearch.java - onCreate）

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.note_search);

    SearchView searchView = findViewById(R.id.search_view);
    searchView.setOnQueryTextListener(this); // 设置搜索监听
    searchView.setSubmitButtonEnabled(true);
    searchView.setIconifiedByDefault(false); // 默认展开搜索框

    listView = findViewById(R.id.list_search_results);

    // 点击搜索结果跳转到编辑页
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(NoteSearch.this, NoteEditor.class);
            intent.setAction(Intent.ACTION_EDIT);
            // 拼接选中笔记的URI（id为数据库主键）
            intent.setData(ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, id));
            startActivity(intent);
        }
    });
}
```
解释：

SearchView 配置为默认展开（setIconifiedByDefault(false)），提升用户体验。

结果列表点击事件：通过 ContentUris.withAppendedId 生成选中笔记的唯一 URI，传递给 NoteEditor 实现编辑跳转。

### （2）实时搜索逻辑（NoteSearch.java - onQueryTextChange）

```java
@Override
public boolean onQueryTextChange(String newText) {
    // 搜索条件：标题 LIKE ? OR 内容 LIKE ?（模糊匹配）
    String selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
    // 搜索参数：关键词前后加%，支持中间匹配（如搜索"测试"可匹配"我的测试笔记"）
    String[] selectionArgs = new String[] { "%" + newText + "%", "%" + newText + "%" };

    // 执行查询：调用ContentProvider获取匹配的笔记
    Cursor cursor = getContentResolver().query(
            NotePad.Notes.CONTENT_URI, // 数据URI
            // 查询字段：ID、标题、创建时间（用于展示结果）
            new String[] { NotePad.Notes._ID, NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_CREATE_DATE },
            selection, // 搜索条件
            selectionArgs, // 搜索参数
            NotePad.Notes.DEFAULT_SORT_ORDER // 排序规则
    );

    // 绑定查询结果到ListView
    String[] from = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_CREATE_DATE };
    int[] to = { android.R.id.text1, R.id.text2 }; // 对应Item布局的TextViewID

    SimpleCursorAdapter adapter = new SimpleCursorAdapter(
            this,
            R.layout.noteslist_item, // 复用笔记列表的Item布局
            cursor,
            from,
            to
    );

    // 格式化创建时间（同笔记列表页逻辑）
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
```
解释：

模糊查询语法：LIKE ? 配合 %关键词%，支持 “前缀匹配、后缀匹配、中间匹配”（如关键词 “办” 可匹配 “待办事项”“办理笔记”）。

实时性：onQueryTextChange 会在用户输入每一个字符时触发，动态更新搜索结果。

























































## （三）笔记分类功能（拓展功能一）
### 实现效果

### 点击下拉框可以展现现有所有分类，在创建时默认分配到Defalt分类中（由于没有做UI美化功能，故分类采用的是下拉框的方式而不是分块显示，能节省一些工作量）
<img width="523" height="1158" alt="d504feb48de94c0e1f28cd3208bfb800" src="https://github.com/user-attachments/assets/6baa4de2-2e53-492c-a87e-61e8be9e380d" />

### 打开test1的编辑界面，在分类框中新增分类Category1，并保存
<img width="518" height="1155" alt="539f7f73442c981adff55c981e0443e3" src="https://github.com/user-attachments/assets/2d99725a-4358-47db-9d30-6d635578ce1e" />

### 同理修改test2，3为另外一个新建分类中
<img width="521" height="1160" alt="f71cc03bf98add211b50b67352718960" src="https://github.com/user-attachments/assets/291583fd-7a99-47b8-b5ce-0cdf33060c7c" />

### 可以看到分类列表中多了我刚加的两个分类
<img width="522" height="1156" alt="4fe41f69309134d9970a78378fb4d05f" src="https://github.com/user-attachments/assets/ca985930-4428-460c-83dc-b306828bb3b4" />

### 点击下拉框中Category1分类，可以看到刚刚添加进来的test1笔记
<img width="522" height="1155" alt="b62dbc3fab07eb46357990576e69a818" src="https://github.com/user-attachments/assets/0368b335-8841-4958-bc8d-04401396c4d9" />

### 同理能看到其他两个笔记在另一添加的分类中
<img width="518" height="1151" alt="6bbc92d55c10376a859ed2d75cfa0eaf" src="https://github.com/user-attachments/assets/743ae280-00f3-4f76-b3d2-34bf6da5fdd1" />


### 1. 功能描述
支持为笔记设置分类，在笔记列表页通过下拉框（Spinner）筛选不同分类的笔记，包含 “全部笔记”“待办事项” 和自定义分类。

### 2. 实现原理

分类存储：数据库新增 COLUMN_NAME_CATEGORY 字段，默认值为 “Default”。

分类获取：通过 DISTINCT 关键字查询数据库，获取所有不重复的分类名称（避免重复选项）。

筛选逻辑：下拉框选择分类后，通过 ContentProvider 查询对应分类的笔记，刷新列表展示。

联动待办：将 “待办事项” 作为特殊分类，通过 COLUMN_NAME_IS_TODO=1 筛选。

### 3. 核心代码块及解释
#### （1）分类字段初始化（NotePadProvider.java）

```java
// 建表时设置分类字段默认值为"Default"
db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
        // ... 其他字段 ...
        + NotePad.Notes.COLUMN_NAME_CATEGORY + " TEXT DEFAULT 'Default'," // 分类字段
        + NotePad.Notes.COLUMN_NAME_IS_TODO + " INTEGER DEFAULT 0,"
        + NotePad.Notes.COLUMN_NAME_DUE_DATE + " INTEGER DEFAULT 0"
        + ");");

// 插入数据时补全分类默认值
if (!values.containsKey(NotePad.Notes.COLUMN_NAME_CATEGORY)) {
    values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, "Default");
}
```
解释：

分类字段默认值为 “Default”，避免用户未设置分类时出现空值。

确保所有笔记都有分类，筛选时不会遗漏数据。

#### （2）获取所有分类（NotesList.java - initCategorySpinner）
```java
private void initCategorySpinner() {
    mSpinner = findViewById(R.id.spinner_category);
    List<String> categories = new ArrayList<>();
    categories.add("全部笔记");  // 默认选项1：显示所有笔记
    categories.add("待办事项");  // 默认选项2：特殊分类（筛选待办）

    // 查询所有不重复的分类名称：使用DISTINCT关键字
    Cursor catCursor = getContentResolver().query(
            NotePad.Notes.CONTENT_URI,
            new String[] { "DISTINCT " + NotePad.Notes.COLUMN_NAME_CATEGORY }, // 去重查询分类字段
            null,
            null,
            NotePad.Notes.COLUMN_NAME_CATEGORY + " ASC" // 按分类名称升序排序
    );

    if (catCursor != null) {
        while (catCursor.moveToNext()) {
            String cat = catCursor.getString(0);
            if (cat != null && !cat.isEmpty()) {
                categories.add(cat); // 添加自定义分类到下拉框
            }
        }
        catCursor.close();
    }

    // 设置下拉框适配器
    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSpinner.setAdapter(spinnerAdapter);

    // 监听分类选择事件
    mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selected = parent.getItemAtPosition(position).toString();
            updateNoteList(selected); // 选择后筛选笔记
        }
        @Override public void onNothingSelected(AdapterView<?> parent) {}
    });
}
```
解释：

DISTINCT 关键字：确保查询结果中分类名称不重复（如多个笔记属于 “Category2” 分类，仅返回一次 “Category2”）。

特殊分类 “待办事项”：不通过数据库查询，直接手动添加到下拉框，后续通过 is_todo=1 筛选。

下拉框排序：按分类名称升序（ASC），让分类选项更整齐。

#### （3）分类筛选逻辑（NotesList.java - updateNoteList + onCreateLoader）
```java
// 触发筛选：通知Loader重新加载数据
private void updateNoteList(String category) {
    Bundle bundle = new Bundle();
    if ("待办事项".equals(category)) {
        // 筛选待办：is_todo=1
        bundle.putString("selection", NotePad.Notes.COLUMN_NAME_IS_TODO + "=?");
        bundle.putStringArray("selectionArgs", new String[] { "1" });
    } else if (!"全部笔记".equals(category)) {
        // 筛选自定义分类：category=选中值
        bundle.putString("selection", NotePad.Notes.COLUMN_NAME_CATEGORY + "=?");
        bundle.putStringArray("selectionArgs", new String[] { category });
    }
    // 重启Loader，触发onCreateLoader重新查询
    getLoaderManager().restartLoader(NOTES_LOADER_ID, bundle, this);
}

// Loader查询数据：根据筛选条件构建查询
@Override
public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String selection = null;
    String[] selectionArgs = null;

    // 从Bundle中获取筛选条件
    if (args != null) {
        selection = args.getString("selection");
        selectionArgs = args.getStringArray("selectionArgs");
    }

    return new CursorLoader(
            this,
            getIntent().getData(),
            PROJECTION,
            selection, // 筛选条件（如category='工作'或is_todo=1）
            selectionArgs, // 筛选参数
            NotePad.Notes.DEFAULT_SORT_ORDER
    );
}
```
解释：

筛选核心：通过 selection 和 selectionArgs 构建查询条件（如 “工作” 分类对应 category='工作'，待办对应 is_todo=1）。

异步加载：使用 CursorLoader 在后台线程执行查询，避免阻塞 UI 线程（即使数据量较大也不会卡顿）。

动态刷新：restartLoader 触发 onCreateLoader 重新查询，筛选结果实时更新到列表。






























































## （四）添加待办功能（含闹钟提醒）(拓展功能二)

## 实现效果

### 进入笔记编辑页，勾选为待办后，弹出时间选项框，首先选择年月
<img width="523" height="1156" alt="9cf70d81d5ab02da07007132dcb8c899" src="https://github.com/user-attachments/assets/6f3b7d67-0840-4b37-a334-f79f3a13d804" />

### 而后选择具体时间，且为避免误选，选择时间必须大于现在的两分钟后才会生效，否则会提示选择时间过短，请现在就去做你该做的事
<img width="521" height="1156" alt="cf6af85193d31e9696bc4323bed2383b" src="https://github.com/user-attachments/assets/1a0a0352-b332-410c-9775-24a8e17be729" />

### 选择好时间后笔记上会多出一行红字提醒该笔记为待办，以及待办时间
<img width="525" height="1156" alt="1a87ff6cb1ab9a8c34f5cddfd8dd15f6" src="https://github.com/user-attachments/assets/0ffb5d89-6709-4e74-a241-364733b99068" />

### 进入待办事件分类，可以看到由于修改了笔记状态，所以笔记时间戳也一起改变，且待办事项的分类里多了刚刚添加的待办
<img width="520" height="1154" alt="d60af6100f2428a2286e49ec199c480d" src="https://github.com/user-attachments/assets/d53643cf-73ef-4f39-9897-abb5a91105d3" />

### 即使退出至桌面，刚刚的待办提醒在到预定时间时依然生效，会弹出待办消息提醒
<img width="514" height="1152" alt="c51da9ca21640dc93a576ac8e2d9b17a" src="https://github.com/user-attachments/assets/8fbbcc68-1804-4464-af83-8322eaf492cb" />

### 下拉消息框，可以看到详细消息，此时可以点击待办的消息，则会跳转至对应的笔记页面
<img width="521" height="1155" alt="563da9e301d2737e0537b997f3d80edf" src="https://github.com/user-attachments/assets/a07d2328-df0a-4560-bc45-d859e5443741" />

### 跳转到对应的笔记页面后，会提醒待办预定时间已过，如果需要请重新设置待办
<img width="523" height="1153" alt="356beb822e13519e68ac9b5a081e1415" src="https://github.com/user-attachments/assets/a1f9945d-1e3b-48b1-b27c-c61e7803433a" />




### 1. 功能描述
支持将笔记标记为 “待办事项”，设置提醒时间，到点后通过系统通知提醒用户，点击通知可跳转至笔记编辑页。

### 2. 实现原理

待办标记：数据库 COLUMN_NAME_IS_TODO 字段（0 = 普通笔记，1 = 待办），通过 CheckBox 控制。

时间选择：点击待办 CheckBox 或时间文本，弹出 DatePickerDialog + TimePickerDialog 选择提醒时间，存储为毫秒值（COLUMN_NAME_DUE_DATE）。

闹钟调度：使用 AlarmManager 调度提醒，到点后发送广播，ReminderReceiver 接收广播并发送系统通知。

通知适配：Android 8.0+ 需创建 NotificationChannel，否则通知无法显示。

### 3. 核心代码块及解释
#### （1）待办 UI 交互与时间选择（NoteEditor.java）

```java
// 待办CheckBox点击事件
mTodoCheck.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (mTodoCheck.isChecked()) {
            if (mDueDate == 0) { // 未设置过提醒时间，弹出选择器
                showDateTimePicker();
            }
        } else {
            mDueDate = 0; // 取消待办，清空提醒时间
            updateDueDateDisplay();
        }
    }
});

// 时间文本点击事件（重新选择时间）
mTvDueDate.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        showDateTimePicker();
    }
});

// 日期+时间选择器
private void showDateTimePicker() {
    final Calendar c = Calendar.getInstance();
    if (mDueDate > 0) c.setTimeInMillis(mDueDate); // 已设置过时间则回显

    // 1. 选择日期
    new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // 2. 选择时间（日期选择后触发）
            new TimePickerDialog(NoteEditor.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    c.set(Calendar.MINUTE, minute);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);

                    mDueDate = c.getTimeInMillis(); // 存储选择的时间（毫秒值）

                    // 提示过期时间
                    if (mDueDate < System.currentTimeMillis()) {
                        Toast.makeText(NoteEditor.this, "注意：设置的时间已过期", Toast.LENGTH_SHORT).show();
                    }

                    mTodoCheck.setChecked(true);
                    updateDueDateDisplay(); // 刷新时间显示
                }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }
    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
}

// 刷新时间显示
private void updateDueDateDisplay() {
    if (mDueDate > 0 && mTodoCheck.isChecked()) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm 提醒", Locale.getDefault());
        mTvDueDate.setText(sdf.format(mDueDate));
        mTvDueDate.setVisibility(View.VISIBLE);
    } else {
        mTvDueDate.setVisibility(View.GONE);
    }
}
```
解释：

交互逻辑：标记待办时若未设置时间，自动弹出选择器；已设置时间则直接显示，支持点击重新选择。

时间存储：选择的日期时间转换为毫秒值（mDueDate），便于数据库存储和 AlarmManager 调度。

过期提示：若选择的时间早于当前时间，弹出 Toast 提醒用户，提升体验。

#### （2）闹钟调度（NoteEditor.java - setAlarm 方法）
```java
private void setAlarm(boolean isTodo, long time, String title) {
    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
    long noteId = Long.parseLong(mUri.getLastPathSegment()); // 获取当前笔记ID

    // Android 12+ 精确闹钟权限检查
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!am.canScheduleExactAlarms()) {
            Toast.makeText(this, "警告：精确闹钟权限可能被禁用，提醒可能不准时。", Toast.LENGTH_LONG).show();
        }
    }

    // 构建广播意图：传递笔记ID和标题
    Intent intent = new Intent(this, ReminderReceiver.class);
    intent.putExtra("note_id", noteId);
    intent.putExtra("note_title", title);

    // 创建PendingIntent（延迟执行的意图）
    PendingIntent pi = PendingIntent.getBroadcast(
            this,
            (int) noteId, // 请求码=笔记ID，确保每个笔记的闹钟唯一
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // 适配Android 12+
    );

    if (isTodo && time > System.currentTimeMillis()) {
        // 启用待办且时间未过期：设置精确闹钟
        am.setExact(AlarmManager.RTC_WAKEUP, time, pi);
        // RTC_WAKEUP：休眠时唤醒设备；setExact：确保精确到设置的时间
    } else {
        // 取消待办或时间过期：取消闹钟
        am.cancel(pi);
    }
}
```
解释：

唯一标识：PendingIntent 的请求码设为笔记 ID（(int) noteId），确保每个笔记的闹钟独立，修改或取消时不会影响其他笔记。

权限适配：Android 12+ 要求 SCHEDULE_EXACT_ALARMS 权限，需在清单文件中申请，此处添加检查和提示。

闹钟类型：AlarmManager.RTC_WAKEUP 表示基于系统时间，且会唤醒休眠的设备（确保用户能收到提醒）。

精确性：setExact 确保闹钟在设置的时间精确触发（set 方法可能因系统优化延迟）。

###（3）通知发送（ReminderReceiver.java）
```java
public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "todo_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        long noteId = intent.getLongExtra("note_id", -1);
        String title = intent.getStringExtra("note_title");

        if (noteId != -1) {
            showNotification(context, title, noteId);
        }
    }

    private void showNotification(Context context, String title, long noteId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0+ 必须创建通知通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "待办提醒", // 通道名称（用户可见）
                    NotificationManager.IMPORTANCE_HIGH // 重要性：高（会弹出通知）
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 构建通知：点击跳转至笔记编辑页
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_notes) // 通知图标（需自定义）
                .setContentTitle("待办提醒: " + title) // 通知标题
                .setContentText("是时候处理你的待办事项了！") // 通知内容
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 优先级（Android 7.0-）
                .setAutoCancel(true); // 点击通知后自动取消

        // 点击通知跳转编辑页
        Intent editIntent = new Intent(context, NoteEditor.class);
        editIntent.setAction(Intent.ACTION_EDIT);
        editIntent.setData(ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, noteId));
        editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) noteId,
                editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.setContentIntent(pendingIntent); // 绑定跳转意图

        // 发送通知（通知ID=笔记ID，确保唯一）
        notificationManager.notify((int) noteId, builder.build());
    }
}
```
解释：

通知通道：Android 8.0+ 引入 NotificationChannel，必须创建通道才能显示通知，此处设置通道名称和高重要性（确保通知弹窗）。

跳转逻辑：点击通知时，通过 PendingIntent 启动 NoteEditor 并传递笔记 URI，直接进入编辑页。

通知唯一性：通知 ID 设为笔记 ID，避免多个待办通知重叠。

#### （4）清单文件配置（必要权限与组件声明）
```xml
<!-- 精确闹钟权限（Android 12+） -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARMS" />
<!-- 唤醒设备权限（可选，确保休眠时收到通知） -->
<uses-permission android:name="android.permission.WAKE_LOCK" />

<application>
    <!-- 注册ContentProvider -->
    <provider
        android:name=".NotePadProvider"
        android:authorities="com.google.provider.NotePad"
        android:exported="false" />

    <!-- 注册广播接收器（接收闹钟触发） -->
    <receiver
        android:name=".ReminderReceiver"
        android:exported="false" />

    <!-- 注册Activity -->
    <activity android:name=".NoteEditor" />
    <activity android:name=".NotesList" />
    <activity android:name=".NoteSearch" />
    <activity android:name=".TitleEditor" />
</application>
```
解释：

权限申请：SCHEDULE_EXACT_ALARMS 是 Android 12+ 精确闹钟必需权限，WAKE_LOCK 确保设备休眠时能唤醒并发送通知。

组件声明：ContentProvider、BroadcastReceiver 和所有 Activity 必须在清单文件中注册，否则系统无法识别。


















































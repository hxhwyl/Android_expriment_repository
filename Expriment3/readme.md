# 实验三报告

由于此报告内包含多个小实验，每个实验都新建一个应用较为繁琐，故我采用一个主界面，里面由分别跳转到不同小实验内部的按钮，即如下图

<img width="548" height="755" alt="屏幕截图 2025-10-18 224547" src="https://github.com/user-attachments/assets/16c81c06-85e0-4589-b9d1-82a9afca25bb" />

其中animalpage会跳转至动物列表展示，loginpage会跳转至登录对话框，menupage会跳转至使用xml创建menu，contextmenu会跳转至上下文menu

以下为各个小实验

---

## 一、动物列表展现

### （1）实验设计思路
本实验通过 SimpleAdapter 实现动物列表展示，结合 Toast 提示与系统通知，构建 “列表展示 - 交互反馈 - 通知推送” 的完整流程。
界面层以 ListView 为容器，自定义 “图片 + 文本” 列表项布局，保证信息清晰；数据层用数组存储动物名称与图片资源 ID，通过 HashMap 封装单条数据，
整合为 List<Map<String, Object>> 以适配 SimpleAdapter；交互层为 ListView 绑定点击监听，
触发 Toast 即时反馈与通知发送；兼容性层针对 Android O（API 26）及以上版本创建通知通道，确保通知功能正常生效。
### （2）功能实现
#### 动物列表展示功能
实现流程：定义 list_item_template.xml 布局，包含 ImageView（显示动物图片）和 TextView（显示动物名称）；
在 AnimalActivity 中初始化 ListView，准备 animalNames（动物名称数组）和 iconIds（图片资源 ID 数组）；
将数组数据封装为 List<Map<String, Object>>（键名 “name” 对应名称，“icon” 对应图片 ID）；
创建 SimpleAdapter 关联数据源、布局与控件，设置给 ListView。

实现效果：界面显示 6 个可滚动的动物列表项，左侧为英文名称，右侧为动物图片，布局整齐。

<img width="526" height="1153" alt="屏幕截图 2025-10-18 224557" src="https://github.com/user-attachments/assets/52c4ef48-6c34-4f35-a8dc-7db7c4df52e7" />

#### 列表项点击 Toast 提示功能
实现流程：为 ListView 设置 OnItemClickListener；在 onItemClick 回调中，通过 parent.getItemAtPosition(position) 获取选中项的 Map 数据；提取 “name” 对应的动物名称，调用 Toast.makeText(this, animalName, Toast.LENGTH_SHORT).show() 显示短提示。

实现效果：点击任意列表项（如 “Lion”），屏幕底部弹出提示框，实时显示选中的动物名称。

<img width="534" height="1114" alt="屏幕截图 2025-10-18 224609" src="https://github.com/user-attachments/assets/9be0785a-afb7-4adc-b133-37a743c15a46" />


#### 列表项点击通知发送功能
实现流程：Android O+ 版本中，在 onCreate() 调用 createNotificationChannel() 创建通知通道（ID 为 animal_channel）；构建指向 AnimalActivity 的 Intent 与 PendingIntent（确保点击通知跳转正确）；
用 NotificationCompat.Builder 设置通知图标（应用图标）、标题（动物名称）、内容（“您刚刚选择了 [名称]”），配置 setAutoCancel(true)（点击后自动清除）；通过 NotificationManager.notify(title.hashCode(), builder.build()) 发送通知（用名称哈希值作为唯一 ID）。

实现效果：点击列表项后，状态栏弹出通知，显示应用图标与选中信息；点击通知跳转回 AnimalActivity，且通知自动消失。

<img width="517" height="1152" alt="图片1" src="https://github.com/user-attachments/assets/ad6138ce-b44b-41e2-8437-e4ce2783eea1" />


### （3）关键技术细节
#### SimpleAdapter 数据适配技术核心参数需严格匹配，确保数据正确绑定到控件：

 ```java
SimpleAdapter adapter = new SimpleAdapter(
        this,                  // 上下文
        data,                  // 数据源：List<Map<String, Object>>
        R.layout.list_item_template, // 列表项布局
        new String[]{"name", "icon"}, // 数据源键名
        new int[]{R.id.tv_animal_name, R.id.img_animal_icon} // 控件 ID
);
listView.setAdapter(adapter);
```
注意事项：from 数组（键名）与 to 数组（控件 ID）长度、顺序必须一致，否则绑定失败。

#### 通知通道创建技术（Android O+ 适配）Android O 及以上版本必须创建通知通道，否则通知无法显示：
```java
private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        CharSequence name = "Animal Notifications"; // 通道名称
        String description = "Channel for animal selection notifications"; // 描述
        int importance = NotificationManager.IMPORTANCE_DEFAULT; // 重要性

        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, name, importance
        );
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
```
注意事项：通道 ID 需全局唯一，名称与描述需清晰，方便用户在系统设置中识别。

#### ListView 点击事件监听技术通过回调获取选中项数据，需强制类型转换：
```java
listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String, Object> selectedItem = (Map<String, Object>) parent.getItemAtPosition(position);
        String animalName = (String) selectedItem.get("name");
        Toast.makeText(AnimalActivity.this, animalName, Toast.LENGTH_SHORT).show();
        sendNotification(animalName);
    }
});
```
注意事项：parent.getItemAtPosition(position) 返回数据源原始数据（此处为 Map），需强制转换后提取字段。


---

## 二、登录对话框

### （一）实验设计思路
实现 “启动即弹窗” 的登录功能，Login Activity 启动时不加载常规布局，直接调用 showLoginDialog() 显示自定义登录框；对话框布局包含用户名 / 密码输入框（EditText）与取消 / 登录按钮，满足基础交互需求；
取消按钮点击关闭对话框并销毁 Activity，登录按钮先校验输入（非空判断），再反馈结果并关闭页面；设置 setCancelable(false) 禁止点击外部关闭对话框，避免用户误操作。

### （二）功能实现
#### 启动即弹窗功能
实现流程：onCreate() 中直接调用 showLoginDialog()；通过 LayoutInflater.inflate(R.layout.activity_login, null) 加载自定义布局；用 AlertDialog.Builder 配置标题（“登录”）与布局，调用 dialog.show() 显示；跳转后立即弹出登录框，无多余页面过渡。

实现效果：进入 Login Activity 后，屏幕中央弹出登录对话框，聚焦登录操作。

<img width="535" height="1144" alt="屏幕截图 2025-10-18 224807" src="https://github.com/user-attachments/assets/a700b4e1-f0c5-41ed-ada1-e9375accb99b" />

<img width="547" height="1179" alt="屏幕截图 2025-10-18 224832" src="https://github.com/user-attachments/assets/02666643-3ccd-4cd3-a4c5-a5f510e58a35" />



#### 对话框与页面关闭功能
实现流程：取消按钮点击时，先 dialog.dismiss() 关闭对话框，再 finish() 销毁 Activity；登录成功后同样先关对话框再销毁页面，确保无残留。

实现效果：点击取消或登录成功后，对话框消失且 Activity 关闭，返回上一级页面。

<img width="539" height="1182" alt="屏幕截图 2025-10-18 224849" src="https://github.com/user-attachments/assets/739d0b7a-425f-456d-b184-f984812b785b" />


### （三）关键技术细节
#### 自定义对话框加载技术需通过对话框布局实例查找控件，而非 Activity 直接查找：
```java
private void showLoginDialog() {
    View dialogView = getLayoutInflater().inflate(R.layout.activity_login, null);
    EditText etUsername = dialogView.findViewById(R.id.et_username);
    EditText etPassword = dialogView.findViewById(R.id.et_password);
    Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
    Button btnSignIn = dialogView.findViewById(R.id.btn_sign_in);

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("登录")
           .setView(dialogView)
           .setCancelable(false);

    final AlertDialog dialog = builder.create();
    dialog.show();
}
```
注意事项：控件查找需通过 dialogView.findViewById()，因布局属于对话框，而非 Activity 根布局。
#### 输入框内容校验技术用 trim() 处理输入，避免纯空格被误判为有效内容：
```java
btnSignIn.setOnClickListener(v -> {
    String username = etUsername.getText().toString().trim();
    String password = etPassword.getText().toString().trim();

    if (username.isEmpty() || password.isEmpty()) {
        Toast.makeText(Login.this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
    } else {
        Toast.makeText(Login.this, "登录成功：" + username, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        finish();
    }
});
```

注意事项：必须使用 trim()，否则用户输入纯空格时，isEmpty() 会返回 false，导致校验失效。

#### 对话框与 Activity 关闭联动技术先关闭对话框再销毁 Activity，防止上下文失效：
```java
btnCancel.setOnClickListener(v -> {
    dialog.dismiss();
    finish();
});
```
注意事项：顺序不可颠倒，若先销毁 Activity，对话框可能因上下文丢失抛出异常。


---

## 三、使用 XML 定义菜单

### （一）实验设计思路
通过 XML 定义选项菜单，实现 “字体大小调整”“字体颜色切换”“普通菜单项反馈” 功能。
菜单结构分三级：一级菜单包含 “字体大小”“普通菜单项”“字体颜色”，“字体大小” 子菜单含小（10sp）、中（16sp）、大（20sp）选项，“字体颜色” 子菜单含红、黑选项；界面层添加 Toolbar（菜单显示载体）与 TextView（测试文本）；
交互层通过 onCreateOptionsMenu 加载 XML 菜单，onOptionsItemSelected 处理点击逻辑，实现菜单与文本控件的联动。

### （二）功能实现
#### XML 菜单加载与显示功能
实现流程：在 res/menu/menu_main.xml 中用 <menu> 和 <item> 标签定义菜单层级；XmlMenu Activity 中绑定 Toolbar，通过 setSupportActionBar(toolbar) 将其设为 ActionBar；重写 onCreateOptionsMenu，用 MenuInflater.inflate(R.menu.menu_main, menu) 加载菜单。

实现效果：进入页面后，Toolbar 右侧显示菜单按钮（三个点），点击展开下拉菜单，子菜单点击时自动展开选项。

<img width="532" height="1164" alt="屏幕截图 2025-10-18 224912" src="https://github.com/user-attachments/assets/5d994b18-a395-46dd-99c0-e21429ac2ebc" />

<img width="522" height="1155" alt="image" src="https://github.com/user-attachments/assets/09ed9766-52f6-49d6-9ebd-9027386e08bc" />



#### 字体大小与颜色控制功能
实现流程：点击 “字体大小” 子菜单选项，分别调用 tvTest.setTextSize(10)“16”“20” 调整文本大小；点击 “字体颜色” 子菜单选项，调用 tvTest.setTextColor(Color.RED) 或 Color.BLACK 切换颜色。

实现效果：文本大小、颜色实时变化，视觉反馈直观。

<img width="537" height="1192" alt="屏幕截图 2025-10-18 224940" src="https://github.com/user-attachments/assets/8afa34b9-c370-4350-bd85-f0bbc52b47d2" />

<img width="547" height="1186" alt="屏幕截图 2025-10-18 224930" src="https://github.com/user-attachments/assets/5dac64b4-1145-4ea9-9e8f-0e0e8cd76d5d" />


#### 普通菜单项 Toast 反馈功能
实现流程：点击 “普通菜单项”，调用 Toast.makeText(this, "点击了普通菜单项", Toast.LENGTH_SHORT).show() 弹出提示。

实现效果：点击后屏幕底部弹出短提示，2 秒后自动消失。

<img width="551" height="1173" alt="屏幕截图 2025-10-18 224953" src="https://github.com/user-attachments/assets/17bb3099-5256-420a-ac70-3f572e256e01" />


### （三）关键技术细节
#### XML 菜单结构化定义技术使用 AppCompat 命名空间，确保兼容性：
```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <!-- 字体大小子菜单 -->
    <item android:id="@+id/item_font_size" android:title="字体大小">
        <menu>
            <item android:id="@+id/item_font_small" android:title="小（10号）"/>
            <item android:id="@+id/item_font_medium" android:title="中（16号）"/>
            <item android:id="@+id/item_font_large" android:title="大（20号）"/>
        </menu>
    </item>
    <!-- 普通菜单项 -->
    <item android:id="@+id/item_normal" android:title="普通菜单项" app:showAsAction="never"/>
    <!-- 字体颜色子菜单 -->
    <item android:id="@+id/item_font_color" android:title="字体颜色">
        <menu>
            <item android:id="@+id/item_color_red" android:title="红色"/>
            <item android:id="@+id/item_color_black" android:title="黑色"/>
        </menu>
    </item>
</menu>
```
注意事项：因使用 AppCompatActivity，需添加 xmlns:app 命名空间，菜单项显示规则用 app:showAsAction。

#### Toolbar 与 ActionBar 关联技术Toolbar 是菜单的显示载体，需绑定为 ActionBar：
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_xml_menu);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar); // 关键：关联为 ActionBar
    tvTest = findViewById(R.id.tv_test);
}
```
注意事项：布局中必须包含 Toolbar，且 id 与代码中的 findViewById(R.id.toolbar) 匹配，否则菜单无显示载体。

#### 菜单点击事件分发技术根据菜单项 ID 执行对应逻辑，处理后返回 true：
```java
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.item_font_small) {
        tvTest.setTextSize(10);
        return true;
    } else if (itemId == R.id.item_font_medium) {
        tvTest.setTextSize(16);
        return true;
    } else if (itemId == R.id.item_font_large) {
        tvTest.setTextSize(20);
        return true;
    } else if (itemId == R.id.item_normal) {
        Toast.makeText(this, "点击了普通菜单项", Toast.LENGTH_SHORT).show();
        return true;
    } else if (itemId == R.id.item_color_red) {
        tvTest.setTextColor(Color.RED);
        return true;
    } else if (itemId == R.id.item_color_black) {
        tvTest.setTextColor(Color.BLACK);
        return true;
    } else {
        return super.onOptionsItemSelected(item);
    }
}
```
注意事项：每个分支需返回 true，表示事件已消费，避免父类重复处理；setTextSize 默认单位为 sp，与布局一致。


---


## 四、上下文菜单
### （一）实验设计思路
基于 ListView 实现 ActionMode 形式的上下文菜单（长按列表项触发顶部操作栏）。
列表层用 SimpleAdapter 绑定 “图标 + 文本” 数据，确保展示清晰；多选模式设为 CHOICE_MODE_MULTIPLE_MODAL，支持长按触发多选；MultiChoiceModeListener 监听选中状态变化，触发时创建 ActionMode 栏、加载 XML 菜单、更新选中数量标题；添加 Toolbar 作为载体，兼容 AppCompat 库。

### （二）功能实现

#### 多选列表展示功能
实现流程：定义 list_item_menu.xml 布局（含 ImageView 和 TextView）；ContextMenuActivity 中通过 initData() 封装数据源（One~Five 文本 + 应用图标）；用 SimpleAdapter 绑定数据与布局，设置给 ListView。

实现效果：界面显示 5 个可滚动列表项，长按任意项可触发选中状态（默认显示选中背景）。

<img width="529" height="1159" alt="屏幕截图 2025-10-18 225328" src="https://github.com/user-attachments/assets/c127da51-83b9-4765-8e25-cb55d8dd480d" />


#### ActionMode 菜单触发与选中同步功能
实现流程：配置 listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL) 开启多选模式；设置 MultiChoiceModeListener，在 onItemCheckedStateChanged 中更新选中计数（selectedCount），调用 mode.setTitle(selectedCount + " selected") 同步标题；长按列表项时，系统自动创建 ActionMode 栏，替换 Toolbar 显示。

实现效果：长按单个列表项，顶部弹出 ActionMode 栏显示 “1 selected”；继续点击其他项，标题同步更新为选中数量。

<img width="524" height="1151" alt="屏幕截图 2025-10-18 225647" src="https://github.com/user-attachments/assets/70df765a-97f1-4669-b695-6325fa478bca" />


#### ActionMode 菜单操作功能
实现流程：onCreateActionMode 中加载 context_menu.xml 菜单（含 Delete 选项）；onActionItemClicked 中匹配 R.id.menu_delete，弹出 “Delete selected items” 提示，调用 mode.finish() 关闭 ActionMode；onDestroyActionMode 中重置 selectedCount 为 0。

实现效果：点击 Delete 按钮，弹出提示且 ActionMode 栏关闭。

<img width="534" height="1151" alt="屏幕截图 2025-10-18 225656" src="https://github.com/user-attachments/assets/d82ca84b-0ffb-443f-93c0-9e928ceed8e1" />

### （三）关键技术细节
#### ListView 多选模式配置技术

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_context_menu);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    listView = findViewById(R.id.list_view_context);
    initData();
    initAdapter();
    listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL); // 关键
listView.setMultiChoiceModeListener(...);}
```
注意事项：该模式仅对 `AbsListView` 子类（如 `ListView`）生效，开启后长按列表项会自动触发 `ActionMode`，无需额外设置长按监听。

#### MultiChoiceModeListener 回调逻辑技术

处理选中状态变化、菜单创建与点击，核心代码如下：
```java
listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        // 更新选中计数
        selectedCount = checked ? selectedCount + 1 : selectedCount - 1;
        // 同步 ActionMode 标题
        mode.setTitle(selectedCount + " selected");
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // 加载菜单资源
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return true; // 必须返回 true，否则菜单不显示
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // 无需额外准备，返回 false
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            Toast.makeText(ContextMenuActivity.this, "Delete selected items", Toast.LENGTH_SHORT).show();
            mode.finish(); // 关闭 ActionMode
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        selectedCount = 0; // 重置选中计数
    }
});
```
注意事项：onCreateActionMode 必须返回 true，否则 ActionMode 会立即销毁；mode.setTitle() 仅在 ActionMode 显示期间有效，关闭后标题恢复为 Toolbar 原标题。

#### ActionMode 菜单 XML 定义技术菜单需使用 AppCompat 命名空间，确保兼容性：
```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item
        android:id="@+id/menu_delete"
        android:title="Delete"
        android:icon="@android:drawable/ic_menu_delete"
        app:showAsAction="ifRoom" /> <!-- 有空间则显示在 ActionMode 栏 -->
</menu>
```
注意事项：推荐使用系统自带图标（如 @android:drawable/ic_menu_delete），保持与系统风格一致；app:showAsAction="ifRoom" 确保菜单在空间充足时显示图标 + 文本，空间不足时仅显示文本。

#### SimpleAdapter 数据适配技术确保数据源键名、控件 ID 与适配器参数一一对应：
```java
// 初始化数据源
private void initData() {
    dataList = new ArrayList<>();
    String[] texts = {"One", "Two", "Three", "Four", "Five"};
    for (int i = 0; i < texts.length; i++) {
        Map<String, Object> map = new HashMap<>();
        map.put("icon", R.mipmap.ic_launcher); // 键名“icon”对应图标
        map.put("text", texts[i]);             // 键名“text”对应文本
        dataList.add(map);
    }
}
// 初始化适配器
private void initAdapter() {
    adapter = new SimpleAdapter(
            this,
            dataList,
            R.layout.list_item_menu,
            new String[]{"icon", "text"}, // 数据源键名
            new int[]{R.id.iv_icon, R.id.tv_text} // 列表项控件 ID
    );
    listView.setAdapter(adapter);
}
```
注意事项：数据源键名需与适配器 from 参数一致，控件 ID 需与 to 参数一致，且控件类型匹配（如 “icon” 对应 ImageView，“text” 对应 TextView），否则数据无法绑定。


---

## 总结
本次实验完成了动物列表、登录对话框、XML 菜单、上下文菜单四大功能模块的开发，核心围绕 Android 基础控件（ListView、Dialog、Toolbar）与交互逻辑（点击监听、ActionMode）展开。通过 SimpleAdapter 实现数据与界面的绑定，通过 XML 定义结构化菜单与布局。实验过程中重点解决了数据适配、菜单载体、等关键问题，为后续复杂 Android 应用开发奠定了基础。


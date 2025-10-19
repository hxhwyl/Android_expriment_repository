# 实验三报告

由于此报告内包含多个小实验，每个实验都新建一个应用较为繁琐，故我采用一个主界面，里面由分别跳转到不同小实验内部的按钮，即如下图

<img width="548" height="755" alt="屏幕截图 2025-10-18 224547" src="https://github.com/user-attachments/assets/16c81c06-85e0-4589-b9d1-82a9afca25bb" />

其中animalpage会跳转至动物列表展示，loginpage会跳转至登录对话框，menupage会跳转至使用xml创建menu，contextmenu会跳转至上下文menu

以下为各个小实验

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

<img width="537" height="1192" alt="屏幕截图 2025-10-18 224940" src="https://github.com/user-attachments/assets/8afa34b9-c370-4350-bd85-f0bbc52b47d2" />


#### 字体大小与颜色控制功能
实现流程：点击 “字体大小” 子菜单选项，分别调用 tvTest.setTextSize(10)“16”“20” 调整文本大小；点击 “字体颜色” 子菜单选项，调用 tvTest.setTextColor(Color.RED) 或 Color.BLACK 切换颜色。

实现效果：文本大小、颜色实时变化，视觉反馈直观。

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

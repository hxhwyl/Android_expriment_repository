# 实验二实验报告

## Android界面布局实验

---
### Android布局实验——线性布局

**实现代码在app/src/main/res/layout/line1234.xml中，以下为实现步骤**

1.布局结构设计采用"外层垂直布局 + 内层水平布局" 的嵌套结构实现 4×4 网格。外层使用垂直方向的线性布局作为表格容器，负责容纳所有行元素；每行使用水平方向的线性布局，负责排列该行的 4 个单元格。

2.网格行列实现

行布局：每个水平线性布局作为一行，通过设置layout_margin属性（2dp）控制行与行之间的间距，使布局层次清晰。

单元格布局：每行包含 4 个文本元素，通过权重分配（layout_weight="1"）实现单元格等宽显示，确保在不同屏幕尺寸下保持一致的比例。

3.样式统一设置为所有单元格设置统一样式，深灰色背景、白色文字，通过gravity属性使文字在单元格内居中显示；通过layout_marginStart属性（2dp）控制单元格之间的水平间距，第一个单元格不设置左侧间距以保证整体对齐。

以下为实现的页面

<img width="493" height="644" alt="线性布局实现的界面" src="https://github.com/user-attachments/assets/9beeffad-17de-467b-b852-c1facc488e3c" />


核心代码实现

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- 外层垂直布局作为表格容器 -->
    <LinearLayout
        android:id="@+id/tableContainer"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:layout_margin="8dp">

        <!-- 第一行：水平布局容纳4个单元格 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="2dp">

            <TextView
                android:layout_width="0dp"
                android:layout_height="60dp"
                android:layout_weight="1"  <!-- 权重分配，实现等宽 -->
                android:background="@android:color/darker_gray"
                android:gravity="center"   <!-- 文字居中 -->
                android:text="One,One"
                android:textColor="@android:color/white" />

            <TextView
                android:layout_width="0dp"
                android:layout_height="60dp"
                android:layout_weight="1"
                android:background="@android:color/darker_gray"
                android:gravity="center"
                android:text="One,Two"
                android:textColor="@android:color/white"
                android:layout_marginStart="2dp"/>  <!-- 单元格间距 -->

            <TextView
                android:layout_width="0dp"
                android:layout_height="60dp"
                android:layout_weight="1"
                android:background="@android:color/darker_gray"
                android:gravity="center"
                android:text="One,Three"
                android:textColor="@android:color/white"
                android:layout_marginStart="2dp"/>

            <TextView
                android:layout_width="0dp"
                android:layout_height="60dp"
                android:layout_weight="1"
                android:background="@android:color/darker_gray"
                android:gravity="center"
                android:text="One,Four"
                android:textColor="@android:color/white"
                android:layout_marginStart="2dp"/>
        </LinearLayout>

        <!-- 第二行至第四行结构与第一行一致，仅文本内容不同 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="2dp"
            android:layout_marginTop="2dp">
            <!-- 单元格内容：Two,One ~ Two,Four -->
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="2dp"
            android:layout_marginTop="2dp">
            <!-- 单元格内容：Three,One ~ Three,Four -->
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="2dp">
            <!-- 单元格内容：Four,One ~ Four,Four -->
        </LinearLayout>
    </LinearLayout>
</androidx.constraintlayout.widget.ConstraintLayout>
```
实现细节说明

权重分配核心：通过layout_weight="1"和layout_width="0dp"组合，使每个单元格在水平方向平均分配父容器宽度，实现 “等宽” 效果。

间距控制：行与行之间通过layout_marginTop和layout_marginBottom（2dp）分隔，单元格之间通过layout_marginStart（2dp）分隔，避免布局拥挤。

嵌套布局优势：外层垂直布局控制整体行数，内层水平布局控制每行单元格，层次清晰，便于维护和扩展（如增减行 / 列）。


---
### Android布局实验——表格布局

**实现代码在app/src/main/res/layout/table.xml中，以下为实现步骤**

1. 整体布局结构设计

采用 “外层垂直 LinearLayout + 内层 TableLayout” 的嵌套结构：
外层垂直 LinearLayout 作为根容器，设置黑色背景，自上而下依次容纳标题文本和表格布局，确保整体 UI 垂直排列；
内层 TableLayout 作为表格主体，负责构建行列结构，实现操作项与快捷键的对齐显示。

2. 标题与基础样式设置

在垂直 LinearLayout 顶部添加标题 TextView，设置白色粗体文字（18sp），通过gravity="center_horizontal"实现水平居中，padding="8dp"控制与下方表格的间距，增强界面层次感。
为整个布局统一设置黑色背景，所有有效文本采用白色，与背景形成强烈对比，提升可读性。

3. 表格布局核心实现

（1）表格基础属性配置
为 TableLayout 设置layout_width="match_parent"（占满父容器宽度）、layout_height="wrap_content"（高度自适应内容），并通过padding="4dp"控制表格内部元素与边框的间距；设置shrinkColumns="0"，确保第 0 列（操作项列）在内容过长时自动收缩，避免超出屏幕。

（2）有效操作行实现
通过 TableRow 构建 5 行有效操作（Open...、Save...、Save As...、Import...、Export...、Quit）：
前 3 行（Open...、Save...、Save As...）为 “操作项 + 快捷键” 双列结构：左列 TextView 显示操作名称，右列 TextView 显示对应快捷键，通过gravity="right"使快捷键右对齐，padding="6dp"控制单元格内边距，保证文字与边框间距适中；
最后 1 行（Quit）为单列结构，仅显示操作名称，占据表格全部宽度，与其他双列行形成区分。

（3）分割线与禁用样式设置
利用 View 组件添加水平分割线：设置layout_height="1dp"、深灰色背景，通过layout_marginTop和layout_marginBottom（各 4dp）控制分割线与上下行的间距，将表格划分为 “基础操作”“禁用操作”“退出操作” 3 个区域；
禁用操作（Import...、Export...）通过嵌套 LinearLayout 实现 “X + 操作名” 的禁用标识：左列添加灰色 “X” 符号，操作名和快捷键均设为深灰色，与有效操作的白色文字形成视觉区分，明确标识禁用状态。

以下为实现的页面

<img width="741" height="603" alt="表格实现的界面" src="https://github.com/user-attachments/assets/570b17ae-6633-4d82-83d1-5f00522c10c7" />

核心代码实现

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:background="@android:color/black"> <!-- 黑色背景 -->

        <!-- 标题文本 -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="8dp"
            android:text="Hello TableLayout"
            android:textColor="@android:color/white"
            android:textSize="18sp"
            android:textStyle="bold"
            android:gravity="center_horizontal"/> <!-- 水平居中 -->

        <!-- 表格主体 -->
        <TableLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="4dp"
            android:shrinkColumns="0"> <!-- 第0列内容过长时自动收缩 -->

            <!-- 有效操作行：Open... + Ctrl-O -->
            <TableRow
                android:layout_width="match_parent"
                android:layout_height="wrap_content">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Open..."
                    android:textColor="@android:color/white"
                    android:padding="6dp"/> <!-- 单元格内边距 -->
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Ctrl-O"
                    android:textColor="@android:color/white"
                    android:padding="6dp"
                    android:gravity="right"/> <!-- 快捷键右对齐 -->
            </TableRow>

            <!-- Save... + Ctrl-S 行（结构同上） -->
            <TableRow>...</TableRow>

            <!-- Save As... + Ctrl-Shift-S 行（结构同上） -->
            <TableRow>...</TableRow>

            <!-- 水平分割线 -->
            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:layout_marginTop="4dp"
                android:layout_marginBottom="4dp"
                android:background="@android:color/darker_gray"/> <!-- 深灰色分割线 -->

            <!-- 禁用操作行：Import...（带X标识） -->
            <TableRow>
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal">
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="X "
                        android:textColor="@android:color/darker_gray" <!-- 灰色X标识 -->
                        android:paddingLeft="6dp"
                        android:paddingTop="6dp"
                        android:paddingBottom="6dp"/>
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Import..."
                        android:textColor="@android:color/darker_gray" <!-- 灰色文字标识禁用 -->
                        android:paddingRight="6dp"
                        android:paddingTop="6dp"
                        android:paddingBottom="6dp"/>
                </LinearLayout>
            </TableRow>

            <!-- 禁用操作行：Export...（带X标识，结构同上） -->
            <TableRow>...</TableRow>

            <!-- 水平分割线（同上） -->
            <View>...</View>

            <!-- 退出操作行：Quit（单列） -->
            <TableRow>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Quit"
                    android:textColor="@android:color/white"
                    android:padding="6dp"/>
            </TableRow>
        </TableLayout>
    </LinearLayout>
</androidx.constraintlayout.widget.ConstraintLayout>
```

实现细节说明

表格收缩控制：shrinkColumns="0"确保操作项列（第 0 列）在内容过长时自动换行收缩，避免超出屏幕边缘，提升小屏设备兼容性。

分割线技术：通过View组件模拟分割线，layout_height="1dp"配合深灰色背景，清晰划分功能区域，增强界面层次感。

禁用样式设计：通过灰色文字（@android:color/darker_gray）和前缀 “X” 符号组合，直观标识禁用状态，无需额外逻辑判断。

---
### Android布局实验——约束布局1

**实现代码在app/src/main/res/layout/calculatedemo.xml中，以下为实现步骤**

1. 整体布局结构设计

以 ConstraintLayout 作为根容器，构建计算器 UI 的三层结构：
顶层：输入提示文本（Input）+ 数值显示区域（默认显示 “0.0”）
中层：垂直 / 水平辅助线（Guideline），用于划分按钮网格区域
底层：4 列 4 行的计算器按钮（数字键 0-9、运算符键 ÷×+-、小数点键.、等号键 =）

2. 核心组件实现

（1）输入与显示区域构建
输入提示文本（TextView）：设置文字 “Input”（20sp），通过layout_constraintStart_toStartOf="parent"和layout_constraintTop_toTopOf="parent"绑定到父容器左上角，添加 16dp 边距保证与边框间距。
数值显示区域（TextView）：设置灰色背景，文字右对齐（gravity="center_vertical|end"），字体 36sp 以突出显示。通过constraintTop_toBottomOf绑定到提示文本下方，constraintStart/End_toStart/EndOf="parent"实现水平全屏，宽度设为 0dp（自适应父容器），高度固定 60dp，确保显示区域规整。

（2）辅助线（Guideline）配置
添加 4 条辅助线划分按钮网格，实现 “4 列等宽” 布局：
3 条垂直辅助线（orientation="vertical"）：分别设置layout_constraintGuide_percent="0.25"“0.50”“0.75”，将水平方向平均分为 4 份，作为按钮的左右约束边界。
1 条水平辅助线（orientation="horizontal"）：设置layout_constraintGuide_percent="0.45"，用于辅助定位按钮区域的垂直位置（实际未直接绑定按钮，可作为布局参考）。

（3）计算器按钮网格实现
共 16 个按钮，按 “4 列 4 行” 排列，核心配置逻辑如下：
宽高自适应：所有按钮的layout_width和layout_height均设为 0dp，通过约束自动填充指定区域（“匹配约束” 模式）。
水平约束：每个按钮的左右边界绑定到垂直辅助线或父容器，例如 “7 号键”constraintStart_toStartOf="parent"“constraintEnd_toStartOf="@id/guide_v1"，确保4个按钮平分水平空间；按钮间添加4dp水平边距（layout_marginHorizontal="4dp"`），避免拥挤。
垂直约束：通过constraintTop_toBottomOf和constraintBottom_toTopOf实现按钮上下关联，例如 “4 号键”constraintTop_toBottomOf="@id/btn_7"“constraintBottom_toTopOf="@id/btn_1"，确保垂直方向均匀分布；行与行之间添加8dp垂直边距（layout_marginTop="8dp"`），底部按钮添加 16dp 底边距，保证整体布局平衡。

样式统一：所有按钮使用默认Widget.AppCompat.Button样式，文字清晰，确保交互一致性。

3. 约束逻辑关键技巧

双向约束：每个按钮的位置通过 “左 - 右”“上 - 下” 双向约束固定，例如 “8 号键” 同时绑定到guide_v1（左）和guide_v2（右）、btn_7（上）和btn_7（下），确保与 “7 号键” 等高且位于第二列。

0dp 自适应：按钮宽高设为 0dp 后，尺寸由约束边界自动计算，避免固定尺寸导致的屏幕适配问题（如在不同宽度屏幕上仍能保持 4 列等宽）。

以下为实现的页面

<img width="676" height="516" alt="约束布局1布局实现的界面" src="https://github.com/user-attachments/assets/47592fa3-668c-4ef9-ad5f-d87a65e99b51" />

核心代码实现

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <!-- 输入提示文本 -->
    <TextView
        android:id="@+id/text_input_label"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Input"
        android:textSize="20sp"
        android:layout_marginStart="16dp"
        android:layout_marginTop="16dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <!-- 数值显示区域 -->
    <TextView
        android:id="@+id/text_display"
        android:layout_width="0dp"  <!-- 0dp自适应父容器宽度 -->
        android:layout_height="60dp"
        android:text="0.0"
        android:textSize="36sp"
        android:gravity="center_vertical|end"  <!-- 文字右对齐 -->
        android:paddingEnd="12dp"
        android:background="#D6D8C8"  <!-- 灰色背景 -->
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="8dp"
        app:layout_constraintTop_toBottomOf="@id/text_input_label"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- 垂直辅助线：划分4列等宽区域 -->
    <androidx.constraintlayout.widget.Guideline
        android:id="@+id/guide_v1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        app:layout_constraintGuide_percent="0.25" />  <!-- 25%位置 -->
    <androidx.constraintlayout.widget.Guideline
        android:id="@+id/guide_v2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        app:layout_constraintGuide_percent="0.50" />  <!-- 50%位置 -->
    <androidx.constraintlayout.widget.Guideline
        android:id="@+id/guide_v3"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        app:layout_constraintGuide_percent="0.75" />  <!-- 75%位置 -->

    <!-- 第一行按钮：7、8、9、÷ -->
    <Button
        android:id="@+id/btn_7"
        style="@style/Widget.AppCompat.Button"
        android:text="7"
        app:layout_constraintTop_toBottomOf="@id/text_display"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@id/guide_v1"  <!-- 左到父容器，右到第一条辅助线 -->
        app:layout_constraintBottom_toTopOf="@id/btn_4"
        android:layout_marginTop="16dp"
        android:layout_marginHorizontal="4dp"
        android:layout_width="0dp"  <!-- 宽高0dp，由约束决定 -->
        android:layout_height="0dp" />

    <Button
        android:id="@+id/btn_8"
        style="@style/Widget.AppCompat.Button"
        android:text="8"
        app:layout_constraintTop_toTopOf="@id/btn_7"  <!-- 与7号键顶部对齐 -->
        app:layout_constraintBottom_toBottomOf="@id/btn_7"  <!-- 与7号键底部对齐 -->
        app:layout_constraintStart_toStartOf="@id/guide_v1"
        app:layout_constraintEnd_toStartOf="@id/guide_v2"
        android:layout_marginHorizontal="4dp"
        android:layout_width="0dp"
        android:layout_height="0dp" />

    <!-- 8、9、÷ 按钮及后续行按钮结构与7号键一致，仅约束目标和文本不同 -->
    <Button android:id="@+id/btn_9" ... />
    <Button android:id="@+id/btn_divide" ... />

    <!-- 第二行按钮：4、5、6、× -->
    <Button android:id="@+id/btn_4" ... />
    <Button android:id="@+id/btn_5" ... />
    <Button android:id="@+id/btn_6" ... />
    <Button android:id="@+id/btn_multiply" ... />

    <!-- 第三行按钮：1、2、3、+ -->
    <Button android:id="@+id/btn_1" ... />
    <Button android:id="@+id/btn_2" ... />
    <Button android:id="@+id/btn_3" ... />
    <Button android:id="@+id/btn_add" ... />

    <!-- 第四行按钮：.、0、=、- -->
    <Button android:id="@+id/btn_decimal" ... />
    <Button android:id="@+id/btn_0" ... />
    <Button android:id="@+id/btn_equals" ... />
    <Button android:id="@+id/btn_subtract" ... />

</androidx.constraintlayout.widget.ConstraintLayout>
```
实现细节说明

辅助线百分比定位：通过layout_constraintGuide_percent按父容器比例设置辅助线位置，实现 “4 列等宽” 的自适应布局，在不同尺寸屏幕上保持一致的比例关系。

0dp 自适应机制：按钮宽高设为 0dp 后，尺寸完全由约束条件（左右 / 上下边界）决定，避免固定尺寸导致的适配问题，确保按钮填满指定区域。

双向约束技巧：每个按钮通过 “左 - 右”“上 - 下” 双向约束固定位置，例如 “8 号键” 同时绑定到guide_v1（左）和guide_v2（右）、btn_7（上）和btn_7（下），确保与 “7 号键” 等高且位于第二列，实现整齐的网格排列。

---
### Android布局实验——约束布局2

**实现代码在app/src/main/res/layout/rocketdemo.xml中，以下为实现步骤**

1. 顶部导航栏实现

创建水平 LinearLayout 作为顶部标签栏，设置灰色背景和垂直内边距
内部添加 3 个 TextView，通过layout_weight="1"实现三等分
每个 TextView 设置drawableTop引用对应图标（空间站、火箭、探测器），文字颜色区分选中状态（中间标签为黑色）

2. 核心功能区布局

水平排列 "出发地 (DCA)" 和 "目的地 (MARS)" 按钮，中间添加交换图标按钮
通过约束链（packed样式）组合三个元素，交换按钮与两侧按钮垂直居中对齐
整体约束到导航栏下方，设置适当顶部边距

3. 选项控制区实现

添加 "One Way" 文本和开关控件，通过约束保持垂直对齐
下方添加 "1 Traveller" 文本，与上方保持 24dp 间距，左对齐

4. 装饰图片添加

图片导入步骤：将图片文件（rocket_icon.png、galaxy.png 等）复制到项目的res/drawable目录下
在布局中添加两个 ImageView，通过android:src="@drawable/图片名"引用图片，可以直接通过视图UI微调位置，使其更加美观

5. 底部按钮设计

添加 "DEPART" 按钮，宽设为 0dp 匹配父容器，高 60dp，绿色背景
约束到屏幕底部，设置 16dp 边距，确保不同设备上位置一致

以下为实现的页面

<img width="722" height="565" alt="约束布局2实现的界面" src="https://github.com/user-attachments/assets/62472665-654d-4434-8f63-447ff7b356ec" />


核心代码实现

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <!-- 顶部导航标签栏 -->
    <LinearLayout
        android:id="@+id/header_tabs"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:background="#E0E0E0"  <!-- 灰色背景 -->
        android:paddingVertical="8dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"  <!-- 三等分宽度 -->
            android:text="Space Stations"
            android:gravity="center"
            android:drawableTop="@drawable/space_station_icon"  <!-- 顶部图标 -->
            android:drawablePadding="4dp"  <!-- 图标与文字间距 -->
            android:textColor="@android:color/darker_gray"  <!-- 未选中状态 -->
            android:textSize="12sp" />

        <TextView  <!-- 中间选中标签，文字为黑色 -->
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:drawableTop="@drawable/rocket_icon"
            android:drawablePadding="4dp"
            android:gravity="center"
            android:text="Flights"
            android:textColor="@android:color/black"  <!-- 选中状态 -->
            android:textSize="12sp" />

        <TextView  <!-- 右侧未选中标签 -->
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:drawableTop="@drawable/rover_icon"
            android:drawablePadding="4dp"
            android:gravity="center"
            android:text="Rovers"
            android:textColor="@android:color/darker_gray"
            android:textSize="12sp" />
    </LinearLayout>

    <!-- 核心功能区：出发地 + 交换按钮 + 目的地 -->
    <Button
        android:id="@+id/btn_dca"
        android:layout_width="120dp"
        android:layout_height="60dp"
        android:layout_marginTop="28dp"
        android:backgroundTint="#4CAF50"  <!-- 绿色背景 -->
        android:text="DCA"
        android:textColor="@android:color/white"
        app:layout_constraintEnd_toStartOf="@+id/swap_button"
        app:layout_constraintHorizontal_chainStyle="packed"  <!-- 约束链紧密排列 -->
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/header_tabs" />

    <ImageButton
        android:id="@+id/swap_button"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:src="@drawable/double_arrows"  <!-- 交换图标 -->
        android:background="?attr/selectableItemBackgroundBorderless"  <!-- 无背景点击效果 -->
        app:layout_constraintStart_toEndOf="@+id/btn_dca"
        app:layout_constraintEnd_toStartOf="@+id/btn_mars"
        app:layout_constraintTop_toTopOf="@+id/btn_dca"  <!-- 与左侧按钮顶部对齐 -->
        app:layout_constraintBottom_toBottomOf="@+id/btn_dca"  <!-- 与左侧按钮底部对齐 -->
        android:contentDescription="Swap" />

    <Button  <!-- 目的地按钮，结构与出发地类似 -->
        android:id="@+id/btn_mars"
        android:layout_width="120dp"
        android:layout_height="60dp"
        android:text="MARS"
        android:backgroundTint="#4CAF50"
        android:textColor="@android:color/white"
        app:layout_constraintTop_toBottomOf="@id/header_tabs"
        app:layout_constraintStart_toEndOf="@+id/swap_button"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="32dp" />

    <!-- 选项控制区：One Way 文本 + 开关 -->
    <TextView
        android:id="@+id/tv_one_way"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="One Way"
        android:textSize="16sp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/btn_dca"
        android:layout_marginStart="32dp"
        android:layout_marginTop="32dp" />

    <Switch
        android:id="@+id/switch_one_way"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:checked="true"
        android:thumbTint="#FF9800"  <!-- 开关颜色 -->
        android:trackTint="#FFB74D"
        app:layout_constraintBottom_toBottomOf="@+id/tv_one_way"  <!-- 与文本底部对齐 -->
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="@+id/tv_one_way" />  <!-- 与文本顶部对齐 -->

    <!-- 乘客数量文本 -->
    <TextView
        android:id="@+id/tv_traveller_label"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="1 Traveller"
        android:textSize="16sp"
        app:layout_constraintStart_toStartOf="@+id/tv_one_way"
        app:layout_constraintTop_toBottomOf="@id/tv_one_way"
        android:layout_marginTop="24dp" />  <!-- 与上方保持24dp间距 -->

    <!-- 装饰图片 -->
    <ImageView
        android:id="@+id/planet_system_image"
        android:layout_width="43dp"
        android:layout_height="38dp"
        android:src="@drawable/rocket_icon"  <!-- 引用drawable中的图片 -->
        app:layout_constraintTop_toBottomOf="@id/tv_traveller_label"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintHorizontal_bias="0.232" />  <!-- 水平偏移微调位置 -->

    <!-- 底部按钮 -->
    <Button
        android:id="@+id/btn_depart"
        android:layout_width="0dp"  <!-- 0dp匹配父容器宽度 -->
        android:layout_height="60dp"
        android:text="DEPART"
        android:backgroundTint="#4CAF50"
        android:textColor="@android:color/white"
        app:layout_constraintBottom_toBottomOf="parent"  <!-- 约束到屏幕底部 -->
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginBottom="16dp"
        android:layout_marginHorizontal="16dp" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

实现细节说明

约束链使用：通过app:layout_constraintHorizontal_chainStyle="packed"将 “出发地 - 交换按钮 - 目的地” 组合为紧密排列的约束链，避免元素分散，提升布局紧凑性。

图标与文字整合：drawableTop属性实现图标在文字上方的布局，配合drawablePadding控制间距，无需嵌套布局即可实现复合控件效果。

背景与交互优化：按钮使用backgroundTint设置主题色，开关通过thumbTint和trackTint自定义颜色，selectableItemBackgroundBorderless为图片按钮添加无边界点击反馈，提升交互体验

---

### 总结

本次实验围绕 Android 四种界面布局展开，通过线性布局实现 4×4 网格结构，掌握了嵌套布局与权重分配技巧；利用表格表格布局完成操作项与快捷键的对齐显示，学会分割线与禁用样式设计；约束布局则实现了计算器和旅行应用界面，熟练运用辅助线、约束链及自适应尺寸设置。实验中掌握了图片导入（复制至 drawable 目录）与引用方法，通过边距、对齐方式优化界面美观度。四种布局实践全面覆盖线性、表格、约束布局的核心用法，加深了对不同布局适用场景的理解，为复杂 Android 界面开发奠定基础。

---
以上实验截图已放至picture文件夹中

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


---

### 总结

本次实验围绕 Android 四种界面布局展开，通过线性布局实现 4×4 网格结构，掌握了嵌套布局与权重分配技巧；利用表格表格布局完成操作项与快捷键的对齐显示，学会分割线与禁用样式设计；约束布局则实现了计算器和旅行应用界面，熟练运用辅助线、约束链及自适应尺寸设置。实验中掌握了图片导入（复制至 drawable 目录）与引用方法，通过边距、对齐方式优化界面美观度。四种布局实践全面覆盖线性、表格、约束布局的核心用法，加深了对不同布局适用场景的理解，为复杂 Android 界面开发奠定基础。

---
以上实验截图已放至picture文件夹中

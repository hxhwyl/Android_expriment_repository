package com.example.expriment3;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextMenuActivity extends AppCompatActivity {

    private ListView listView;
    private SimpleAdapter adapter;
    private List<Map<String, Object>> dataList;
    private ActionMode.Callback actionModeCallback;
    private int selectedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // 关联为 ActionBar

        listView = findViewById(R.id.list_view_context);
        initData();
        initAdapter();

        // 配置 ListView 为多选模式
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);

        // 设置 MultiChoiceModeListener，监听选择状态并触发 ActionMode
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // 选中状态变化时更新选中数量
                if (checked) {
                    selectedCount++;
                } else {
                    selectedCount--;
                }
                // 更新 ActionMode 标题（显示选中数量）
                mode.setTitle(selectedCount + " selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                // 创建 ActionMode 时加载菜单布局
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu); // 需创建 context_menu.xml
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // 无需额外准备，返回 false
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // 处理 ActionMode 中菜单项的点击
                if (item.getItemId() ==R.id.menu_delete ) {
                        Toast.makeText(ContextMenuActivity.this, "Delete selected items", Toast.LENGTH_SHORT).show();
                        mode.finish(); // 关闭 ActionMode
                        return true;
                }
                else {
                    return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // ActionMode 关闭时重置选中数量
                selectedCount = 0;
            }
        });
    }

    // 初始化列表数据
    private void initData() {
        dataList = new ArrayList<>();
        String[] texts = {"One", "Two", "Three", "Four", "Five"};
        for (int i = 0; i < texts.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("icon", R.mipmap.ic_launcher); // 图标资源
            map.put("text", texts[i]);
            dataList.add(map);
        }
    }

    // 初始化适配器
    private void initAdapter() {
        adapter = new SimpleAdapter(
                this,
                dataList,
                R.layout.list_item_menu,
                new String[]{"icon", "text"},
                new int[]{R.id.iv_icon, R.id.tv_text}
        );
        listView.setAdapter(adapter);
    }
}
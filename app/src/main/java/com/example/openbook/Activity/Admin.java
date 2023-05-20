package com.example.openbook.Activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.TableAdapter;
import com.example.openbook.R;
import com.example.openbook.View.TableList;

import java.util.ArrayList;

public class Admin extends AppCompatActivity {

    ArrayList<TableList> tableList;
    TableAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);

        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");
        String get_id = getIntent().getStringExtra("get_id");

        TextView appbar_admin_id = findViewById(R.id.appbar_admin_id);
        appbar_admin_id.setText(get_id);

        RecyclerView tableGrid = findViewById(R.id.admin_grid);
        adapter = new TableAdapter();

        //그리드 레이아웃 설정
        tableGrid.setLayoutManager(new GridLayoutManager(this, 2));

        //어댑터 연결
        tableGrid.setAdapter(adapter);

        adapter.setAdapterItem(tableList);


    }
}

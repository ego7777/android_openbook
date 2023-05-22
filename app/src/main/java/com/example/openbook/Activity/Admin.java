package com.example.openbook.Activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.AdminTableAdapter;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.R;

import java.util.ArrayList;

public class Admin extends AppCompatActivity {

    ArrayList<AdminTableList> adminTableList;
    AdminTableAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);

        adminTableList = (ArrayList<AdminTableList>) getIntent().getSerializableExtra("tableList");
        String get_id = getIntent().getStringExtra("get_id");

        TextView appbar_admin_id = findViewById(R.id.appbar_admin_id);
        appbar_admin_id.setText(get_id);

        RecyclerView tableGrid = findViewById(R.id.admin_grid);
        adapter = new AdminTableAdapter();

        //그리드 레이아웃 설정
        tableGrid.setLayoutManager(new GridLayoutManager(this, 3));

        //어댑터 연결
        tableGrid.setAdapter(adapter);

        adapter.setAdapterItem(adminTableList);


    }

    @Override
    protected void onResume() {
        super.onResume();

        Dialog dialog = new Dialog(Admin.this);
        dialog.setContentView(R.layout.admin_receipt_dialog);


        adapter.setOnItemClickListener(new AdminTableAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                dialog.show();
            }
        });

    }
}

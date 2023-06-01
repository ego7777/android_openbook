package com.example.openbook.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.openbook.Data.AdminTableList;
import com.example.openbook.QRcode.MakeQR;
import com.example.openbook.R;

import java.util.ArrayList;

public class AdminModifyMenu extends Activity {

    String get_id;
    ArrayList<AdminTableList> adminTableList;

    TextView cancel;
    ImageView qrCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_modify_menu);

        get_id = getIntent().getStringExtra("get_id");
        adminTableList = (ArrayList<AdminTableList>) getIntent().getSerializableExtra("adminTableList");


        cancel = findViewById(R.id.admin_modify_menu_cancel);
        qrCode = findViewById(R.id.admin_modify_menu_qrCode);

        MakeQR makeQR = new MakeQR();
        qrCode.setImageBitmap(makeQR.adminQr());
    }

    @Override
    protected void onResume() {
        super.onResume();

        cancel.setOnClickListener(view -> {
            Intent intent = new Intent(AdminModifyMenu.this, Admin.class);
            intent.putExtra("get_id", get_id);
            intent.putExtra("adminTableList", adminTableList);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });



    }
}

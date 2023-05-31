package com.example.openbook.Activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.R;
import com.github.mikephil.charting.charts.LineChart;

public class AdminSales extends AppCompatActivity {

    TextView day;
    TextView week;
    TextView month;
    TextView threeMonth;
    TextView year;

    LineChart chart;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_sales);

        day = findViewById(R.id.admin_sales_sidebar_day);
        week = findViewById(R.id.admin_sales_sidebar_week);
        month = findViewById(R.id.admin_sales_sidebar_month);
        threeMonth = findViewById(R.id.admin_sales_sidebar_threeMonth);
        year = findViewById(R.id.admin_sales_sidebar_year);

        chart = findViewById(R.id.admin_sales_chart);

        //db에 접근해서 일단 오늘 매출을 보여준다

    }

    @Override
    protected void onResume() {
        super.onResume();

        day.setOnClickListener(view ->{

        });

        week.setOnClickListener(view ->{

        });

        month.setOnClickListener(view ->{

        });

        threeMonth.setOnClickListener(view ->{

        });

        year.setOnClickListener(view ->{

        });
    }
}

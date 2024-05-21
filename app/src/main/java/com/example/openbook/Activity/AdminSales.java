package com.example.openbook.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.openbook.BuildConfig;
import com.example.openbook.Data.AdminData;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.R;
import com.example.openbook.retrofit.SalesDTO;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AdminSales extends AppCompatActivity {

    String TAG = "AdminSales_TAG";

    AdminData adminData;
    ArrayList<AdminTableList> adminTableList;

    TextView appbarHome, appbarAddMenu, appbarModifyTable;

    ImageView beforeButton, afterButton, appbarGear, drawerHeaderCancel;
    TextView totalSales, drawerHeaderDuration;

    BarChart chart;
    XAxis xAxis;
    YAxis yAxisRight;
    YAxis yAxisLeft;

    RetrofitService service;

    Gson gson;
    String currentDate;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_sales);

        adminData = getIntent().getParcelableExtra("adminData");
        appbarHome = findViewById(R.id.appbar_admin_home);
        appbarGear = findViewById(R.id.appbar_admin_gear);
        appbarGear.setVisibility(View.VISIBLE);

        drawerLayout = findViewById(R.id.admin_drawer_layout);

        if(drawerLayout.isDrawerOpen(GravityCompat.END)){
            drawerLayout.closeDrawer(GravityCompat.END);
        }

        navigationView = findViewById(R.id.drawer_view);

        View header = navigationView.getHeaderView(0);

        drawerHeaderDuration = header.findViewById(R.id.drawer_header_duration);
        drawerHeaderCancel = header.findViewById(R.id.drawer_header_cancel);

        appbarAddMenu = findViewById(R.id.appbar_admin_addMenu);
        appbarModifyTable = findViewById(R.id.appbar_admin_modifyTable);
        appbarAddMenu.setVisibility(View.INVISIBLE);
        appbarModifyTable.setVisibility(View.INVISIBLE);

        chart = findViewById(R.id.admin_sales_chart);

        totalSales = findViewById(R.id.admin_total_sales);
        beforeButton = findViewById(R.id.admin_date_before);
        afterButton = findViewById(R.id.admin_date_after);

        init();

        gson = new Gson();

        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);

        currentDate = LocalDate.now().toString();
        Log.d(TAG, "currentDate: " + currentDate);

        setRetrofitClient("day", currentDate);

    }


    @Override
    protected void onResume() {
        super.onResume();

        navigationView.setNavigationItemSelectedListener(menuItem ->{
            switch (menuItem.getItemId()){
                case R.id.daily_sales:
                    setRetrofitClient("day", currentDate);
                    break;
                case R.id.weekly_sales:
                    setRetrofitClient("week", currentDate);
                    break;
                case R.id.monthly_sales:
                    setRetrofitClient("month", currentDate);
                    break;
                case R.id.yearly_sales:
                    setRetrofitClient("year", currentDate);
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });



        appbarGear.setOnClickListener(view ->{
            if(drawerLayout.isDrawerOpen(GravityCompat.END)){
                drawerLayout.closeDrawer(GravityCompat.END);
            }else{
                drawerLayout.openDrawer(GravityCompat.END);
                drawerHeaderDuration.setText(currentDate);
                drawerHeaderCancel.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END) );
            }
        });

        appbarHome.setOnClickListener(view -> {
            Intent intent = new Intent(AdminSales.this, Admin.class);
            intent.putExtra("adminData", adminData);
            intent.putExtra("adminTableList", adminTableList);
            startActivity(intent);
        });
    }


    public void setRetrofitClient(String duration, String currentDate) {
        Call<SalesDTO> call = service.requestSalesData(duration, currentDate);
        call.enqueue(new Callback<SalesDTO>() {
            @Override
            public void onResponse(Call<SalesDTO> call, Response<SalesDTO> response) {
                Log.d(TAG, "onResponse set: " + response.body().getResult());
                if (response.isSuccessful()) {
                    switch (response.body().getResult()) {
                        case "success":
                            setChart(response.body().getSales(), duration);
                            totalSales.setText("총 금액 : " + response.body().getTotalAmount() + "원");
                            break;

                        case "failed":
                            chart.clear();
                            chart.setNoDataText(getResources().getString(R.string.noSales));
                            chart.setNoDataTextColor(Color.RED);
                            chart.invalidate();
                            break;
                    }
                } else {
                    Log.d(TAG, "onResponse: " + response);
                }
            }

            @Override
            public void onFailure(Call<SalesDTO> call, Throwable t) {
                Log.d(TAG, "onFailure : " + t.getMessage());
            }
        });

    }


    public void setChart(List<SalesDTO.SaleData> saleData, String duration) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        BarData barData = new BarData();
        BarDataSet barDataSet;
        String title = null;

        switch (duration) {
            case "day":
                title = LocalDate.now() + " 매출 데이터";
                xAxis.setAxisMinimum(-1f);
                xAxis.setAxisMaximum(23.9f);
                xAxis.setValueFormatter((value, axis) -> (int) value + "시");

                for (SalesDTO.SaleData entity : saleData) {
                    int date = Integer.parseInt(entity.getDate());
                    int amount = Integer.parseInt(entity.getAmount());
                    entries.add(new BarEntry(date, amount));
                }
                break;

            case "week":
                LocalDate currentDate = LocalDate.now();
                int month = currentDate.getMonthValue();
                int week = currentDate.get(WeekFields.of(Locale.getDefault()).weekOfMonth());
                title = month + "월 " + week + "째주";

                xAxis.setAxisMinimum((float) (-0.9));
                xAxis.setAxisMaximum((float) (7.1));
                xAxis.setSpaceMin(0.1f);
                xAxis.setSpaceMax(0.1f);

                String[] weekdays = {"월", "화", "수", "목", "금", "토", "일"};

                xAxis.setValueFormatter((value, axis) -> {
                    int index = (int) value;
                    return (index >= 0 && index < weekdays.length) ? weekdays[index] : "";
                });

                for (SalesDTO.SaleData entity : saleData) {
                    int date = getDayOfWeekIndex(entity.getDate());
                    int amount = Integer.parseInt(entity.getAmount());

                    entries.add(new BarEntry(date, amount));
                }
                break;

            case "month":
                break;
            case "year":
                break;

        }


//        else if (duration.equals("week")) {
//
//            xAxis.setAxisMinimum((float) -0.9);
//            xAxis.setAxisMaximum((float) salesLists.size());
//
//
//            xAxis.setValueFormatter((value, axis) -> {
//
//                int index = (int) value;
//                Log.d(TAG, "getFormattedValue: " + index);
//
//                if (index >= 0 && index < salesLists.size()) {
//                    int month = (salesLists.get(index).getIntDate() / 10); // 정수 부분은 월
//                    int week = (salesLists.get(index).getIntDate() % 10); // 소수 부분은 주차
//
//                    return month + "월 " + week + "주차";
//                }
//
//                return ""; // 유효하지 않은 인덱스일 경우 빈 문자열 반환
//
//            });
//
//
//            for (int i = 0; i < salesLists.size(); i++) {
//
//                int price = salesLists.get(i).getTotalPrice();
//                Log.d(TAG, "setChart price: " + price);
//
//                entries.add(new BarEntry(i, price));
//            }
//
//
//        } else if (duration.equals("month")) {
//
//            xAxis.setAxisMinimum((float) (salesLists.get(0).getIntDate() - 0.9));
//            xAxis.setAxisMaximum((float) (salesLists.get(salesLists.size() - 1).getIntDate() + 0.9));
//
//            xAxis.setValueFormatter((value, axis) -> (int) value + "월");
//
//            for (int i = 0; i < salesLists.size(); i++) {
//
//                int month = salesLists.get(i).getIntDate();
//
//                int price = salesLists.get(i).getTotalPrice();
//
//                entries.add(new BarEntry(month, price));
//
//            }
//
//        } else if (duration.equals("year")) {
//            xAxis.setAxisMinimum((float) (salesLists.get(0).getIntDate() - 0.9));
//            xAxis.setAxisMaximum((float) (salesLists.get(salesLists.size() - 1).getIntDate() + 0.9));
//            xAxis.setValueFormatter((value, axis) -> (int) value + "년");
//
//            for (int i = 0; i < salesLists.size(); i++) {
//                int year = salesLists.get(i).getIntDate();
//                int price = salesLists.get(i).getTotalPrice();
//
//                entries.add(new BarEntry(year, price));
//
//            }
//        }

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setTextSize(20f);
        legend.setTextColor(Color.BLACK);


        barDataSet = new BarDataSet(entries, title);
        barDataSet.setColor(Color.argb(255, 98, 204, 177));
        barDataSet.setValueTextSize(17);
        barDataSet.setValueTextColor(Color.BLACK);
        barData.addDataSet(barDataSet);
        barData.setBarWidth(0.5f);

        chart.setData(barData);

        runOnUiThread(() -> {
            chart.invalidate(); // 차트 업데이트
        });

    }

    public void init() {
        chart.getDescription().setEnabled(false);

        chart.setNoDataText(getResources().getString(R.string.noSales));
        chart.setNoDataTextColor(Color.RED);

        xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextSize(18);
        xAxis.setTextColor(Color.BLACK);

        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        xAxis.setSpaceMin(1f);

        yAxisRight = chart.getAxisRight();
        yAxisRight.setTextSize(15);
        yAxisRight.setAxisMinimum(0f);
        yAxisRight.setGranularity(1f);

    }


    public int getDayOfWeekIndex(String dateString) {

        String dateParts[] = dateString.split("/");

        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);

        LocalDate date = LocalDate.of(year, month, day);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayOfWeekIndex = dayOfWeek.getValue() % 7;
        Log.d(TAG, "getDayOfWeekIndex: " + dayOfWeekIndex);

        return dayOfWeekIndex;
    }



}

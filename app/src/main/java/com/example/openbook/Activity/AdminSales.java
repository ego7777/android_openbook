package com.example.openbook.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.BuildConfig;
import com.example.openbook.Data.AdminData;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.R;
import com.example.openbook.SalesDTO;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.gson.Gson;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AdminSales extends AppCompatActivity {

    String TAG = "AdminSales_TAG";

    AdminData adminData;
    ArrayList<AdminTableList> adminTableList;

    TextView home, day, week, month, year;
    TextView appbar_admin_addMenu;
    TextView appbar_admin_modifyTable;

    BarChart chart;
    XAxis xAxis;
    YAxis yAxisRight;
    YAxis yAxisLeft;

    RetrofitService service;

    Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_sales);

        adminData = getIntent().getParcelableExtra("adminData");
        home = findViewById(R.id.appbar_admin_home);

        day = findViewById(R.id.admin_sales_sidebar_day);
        week = findViewById(R.id.admin_sales_sidebar_week);
        month = findViewById(R.id.admin_sales_sidebar_month);
        year = findViewById(R.id.admin_sales_sidebar_year);

        appbar_admin_addMenu = findViewById(R.id.appbar_admin_addMenu);
        appbar_admin_modifyTable = findViewById(R.id.appbar_admin_modifyTable);
        appbar_admin_addMenu.setVisibility(View.INVISIBLE);
        appbar_admin_modifyTable.setVisibility(View.INVISIBLE);

        chart = findViewById(R.id.admin_sales_chart);

        init();

        gson = new Gson();

        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);

        setRetrofitClient("day");

    }

    @Override
    protected void onResume() {
        super.onResume();

        home.setOnClickListener(view -> {
            Intent intent = new Intent(AdminSales.this, Admin.class);
            intent.putExtra("adminData", adminData);
            intent.putExtra("adminTableList", adminTableList);
            startActivity(intent);
        });

        day.setOnClickListener(view -> setRetrofitClient("day"));

        week.setOnClickListener(view -> setRetrofitClient("week"));

        month.setOnClickListener(view -> setRetrofitClient("month"));

        year.setOnClickListener(view -> setRetrofitClient("year"));
    }


    public void setRetrofitClient(String duration) {
        Call<SalesDTO> call = service.requestSalesData(duration);
        call.enqueue(new Callback<SalesDTO>() {
            @Override
            public void onResponse(Call<SalesDTO> call, Response<SalesDTO> response) {
                if(response.isSuccessful()){
                    switch (response.body().getResult()){
                        case "success" :
                            setChart(response.body().getSales(), duration);
                            break;

                        case "failed" :
                            chart.clear();
                            chart.setNoDataText(getResources().getString(R.string.noSales));
                            chart.setNoDataTextColor(Color.RED);
                            chart.invalidate();
                            break;
                    }
                }else{
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

        switch (duration){
            case "day" :
                title = LocalDate.now() + " 매출 데이터";
                xAxis.setAxisMinimum(-1f);
                xAxis.setAxisMaximum(23.9f);
                xAxis.setValueFormatter((value, axis) -> (int) value + "시");

                for(SalesDTO.SaleData entity : saleData){
                    int date = Integer.parseInt(entity.getDate());
                    int amount = Integer.parseInt(entity.getAmount());
                    entries.add(new BarEntry(date, amount));
                }
                break;

            case "week":
                title = LocalDate.now().getDayOfWeek() + " 매출 데이터";
                String lastDay = YearMonth.now().atEndOfMonth().toString().split("-")[2];

                xAxis.setAxisMinimum((float) (0.1));
                xAxis.setAxisMaximum((float) (Integer.parseInt(lastDay) + 0.9));
                xAxis.setSpaceMin(0.1f);
                xAxis.setSpaceMax(0.1f);

                xAxis.setValueFormatter((value, axis) -> {

                    int index = (int) value;

//                    if () {
//
//                        return month;
//                    }

                    return ""; // 유효하지 않은 인덱스일 경우 빈 문자열 반환
                });

                for(SalesDTO.SaleData entity : saleData){
                    int date = getDayOfWeekIndex(entity.getDate());
                    int amount = Integer.parseInt(entity.getAmount());
                    entries.add(new BarEntry(date, amount));
                }
                break;

            case "month" :
                break;
            case "year" :
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
        barDataSet.setColor(Color.argb(255,98, 204, 177));
        barDataSet.setValueTextSize(17);
        barDataSet.setValueTextColor(Color.BLACK);
        barData.addDataSet(barDataSet);

        chart.setData(barData);

        runOnUiThread(() -> {
            chart.invalidate(); // 차트 업데이트
        });

    }

    public void init(){
        chart.getDescription().setEnabled(false);

        chart.setNoDataText(getResources().getString(R.string.noSales));
        chart.setNoDataTextColor(Color.RED);

        xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(18);
        xAxis.setTextColor(Color.BLACK);

        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        xAxis.setSpaceMin(1f);

        yAxisRight = chart.getAxisRight();
        yAxisRight.setTextSize(15);
        yAxisRight.setAxisMinimum(0f);
        yAxisRight.setGranularity(1f);

        yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setTextSize(15);
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setGranularity(1f);
    }

    private int getDayOfWeekIndex(String dayOfWeek) {
        switch (dayOfWeek) {
            case "Sunday":
                return 0;
            case "Monday":
                return 1;
            case "Tuesday":
                return 2;
            case "Wednesday":
                return 3;
            case "Thursday":
                return 4;
            case "Friday":
                return 5;
            case "Saturday":
                return 6;
            default:
                return 0;
        }
    }


}

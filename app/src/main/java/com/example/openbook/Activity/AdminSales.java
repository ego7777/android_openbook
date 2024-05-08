package com.example.openbook.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.BuildConfig;
import com.example.openbook.Data.AdminData;
import com.example.openbook.Data.AdminSalesList;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.R;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AdminSales extends AppCompatActivity {

    String TAG = "AdminSales_TAG";

    AdminData adminData;
    ArrayList<AdminTableList> adminTableList;

    TextView home, today, day, week, month, year;
    TextView appbar_admin_addMenu;
    TextView appbar_admin_modifyTable;

    BarChart chart;
    XAxis xAxis;
    YAxis yAxisRight;
    YAxis yAxisLeft;

    RetrofitService service;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_sales);

        adminData = getIntent().getParcelableExtra("adminData");
//        adminTableList = (ArrayList<AdminTableList>) getIntent().getSerializableExtra("adminTableList");

        home = findViewById(R.id.appbar_admin_home);

        today = findViewById(R.id.admin_sales_sidebar_today);
        day = findViewById(R.id.admin_sales_sidebar_day);
        week = findViewById(R.id.admin_sales_sidebar_week);
        month = findViewById(R.id.admin_sales_sidebar_month);
        year = findViewById(R.id.admin_sales_sidebar_year);

        appbar_admin_addMenu = findViewById(R.id.appbar_admin_addMenu);
        appbar_admin_modifyTable = findViewById(R.id.appbar_admin_modifyTable);
        appbar_admin_addMenu.setVisibility(View.INVISIBLE);
        appbar_admin_modifyTable.setVisibility(View.INVISIBLE);


        chart = findViewById(R.id.admin_sales_chart);
        chart.getDescription().setEnabled(false);

        chart.setNoDataText("데이터가 존재하지 않습니다.");
        chart.setNoDataTextColor(Color.RED);


        xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(18);

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

        today.setOnClickListener(view -> setRetrofitClient("today"));

        day.setOnClickListener(view -> setRetrofitClient("day"));

        week.setOnClickListener(view -> setRetrofitClient("week"));

        month.setOnClickListener(view -> setRetrofitClient("month"));

        year.setOnClickListener(view -> setRetrofitClient("year"));
    }


    public void setRetrofitClient(String duration) {
        //db에 접근해서 일단 오늘 매출을 보여준다

        Call<SuccessOrNot> call = service.requestSalesData(duration);

        call.enqueue(new Callback<SuccessOrNot>() {
            @Override
            public void onResponse(Call<SuccessOrNot> call, Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse data: " + response);
                if(response.isSuccessful()){
                    //만약 데이터 없음이면
                    chart.clear();
                    chart.setNoDataText("매출데이터가 존재하지 않습니다.");
                    chart.setNoDataTextColor(Color.RED);
                    chart.invalidate();
//                    getSalesInfo(duration, body);

                }
            }

            @Override
            public void onFailure(Call<SuccessOrNot> call, Throwable t) {

            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getSalesInfo(String duration, String data)  {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        ArrayList<AdminSalesList> salesLists = new ArrayList();


//        JSONArray jsonArray = new JSONArray(data);
//
//        for (int i = 0; i < jsonArray.length(); i++) {
//
//            JSONObject jsonObject = jsonArray.getJSONObject(i);
//
//            LocalDateTime date = LocalDateTime.parse(jsonObject.getString("orderTime"),
//                    dateTimeFormatter);
//
//            int totalPrice = jsonObject.getInt("totalPrice");
//
//            salesLists.add(new AdminSalesList(date, totalPrice));
//            Log.d(TAG, "getSalesInfo: " + salesLists.get(i).getLocalDateTime());
//
//        }

        sortData(salesLists, duration);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sortData(ArrayList<AdminSalesList> salesLists, String duration) {

        //중복되는 날짜 금액 합산
        HashMap<Integer, Integer> hashMap = new HashMap<>();
//        HashMap<String, Integer> testHashMap = new HashMap<>();

        ArrayList<AdminSalesList> salesListFinal = new ArrayList<>();
        String title;


        if (duration.equals("today")) {

            for (int i = 0; i < salesLists.size(); i++) {

                int hour = salesLists.get(i).getLocalDateTime().getHour();

                if (hashMap.get(hour) == null) {

                    hashMap.put(hour, salesLists.get(i).getTotalPrice());

                } else {
                    int duplicatedPrice = hashMap.get(hour) + salesLists.get(i).getTotalPrice();
                    hashMap.replace(hour, duplicatedPrice);
                }
            }

//            title = LocalDate.parse(salesLists.get(0).getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toString();


        } else if (duration.equals("day")) {

            for (int i = 0; i < salesLists.size(); i++) {

                int dayOfMonth = salesLists.get(i).getLocalDateTime().getDayOfMonth();

                if (hashMap.get(dayOfMonth) == null) {

                    hashMap.put(dayOfMonth, salesLists.get(i).getTotalPrice());
                    Log.d(TAG, "sortData null: " + hashMap.get(dayOfMonth));

                } else {
                    int duplicatedPrice = hashMap.get(dayOfMonth) + salesLists.get(i).getTotalPrice();
                    hashMap.replace(dayOfMonth, duplicatedPrice);
                    Log.d(TAG, "sortData not null: " + hashMap.get(dayOfMonth));
                }

            } // for문 끝


        } else if (duration.equals("week")) {

            //중복되는 날짜 금액 합산
            for (int i = 0; i < salesLists.size(); i++) {

                int month = salesLists.get(i).getLocalDateTime().getMonthValue();
//                Log.d(TAG, "month: " + month);

                int weekOfMonth = salesLists.get(i).getLocalDateTime().get(WeekFields.SUNDAY_START.weekOfMonth());
//                Log.d(TAG, "sortData weekOfMonth: " + weekOfMonth);


                String date = String.valueOf(month) + String.valueOf(weekOfMonth);
                Log.d(TAG, "date: " + date);

                int dateKey = Integer.parseInt(date);

                if (hashMap.get(dateKey) == null) {
                    hashMap.put(dateKey, salesLists.get(i).getTotalPrice());
                } else {
                    Integer duplicatedPrice = hashMap.get(dateKey) + salesLists.get(i).getTotalPrice();
                    hashMap.replace(dateKey, duplicatedPrice);
                }

            } // for문 끝


        } else if (duration.equals("month")) {

            //중복되는 날짜 금액 합산
            for (int i = 0; i < salesLists.size(); i++) {

                int month = salesLists.get(i).getLocalDateTime().getMonthValue();

                if (hashMap.get(month) == null) {
                    hashMap.put(month, salesLists.get(i).getTotalPrice());
                } else {
                    int duplicatedPrice = hashMap.get(month) + salesLists.get(i).getTotalPrice();
                    hashMap.replace(month, duplicatedPrice);
                }
            }


        } else if (duration.equals("year")) {

            //중복되는 날짜 금액 합산
            for (int i = 0; i < salesLists.size(); i++) {

                int year = salesLists.get(i).getLocalDateTime().getYear();

                if (hashMap.get(year) == null) {
                    hashMap.put(year, salesLists.get(i).getTotalPrice());
                } else {
                    int duplicatedPrice = hashMap.get(year) + salesLists.get(i).getTotalPrice();
                    hashMap.replace(year, duplicatedPrice);
                }
            }
        }


        hashMap.forEach((Key, Value) -> {

            salesListFinal.add(new AdminSalesList(Key, Value));

        });
//        }


        title = LocalDate.parse(salesLists.get(0).getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) + "   ~   "
                + LocalDate.parse(salesLists.get(salesLists.size() - 1).getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));


        setChart(salesListFinal, duration, title);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setChart(ArrayList<AdminSalesList> salesLists, String duration, String title) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        BarData barData = new BarData();
        BarDataSet barDataSet;


        if (duration.equals("today")) {
            xAxis.setAxisMinimum(-1f);
            xAxis.setAxisMaximum(23.9f);

            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return (int) value + "시";
                }
            });

            for (int i = 0; i < salesLists.size(); i++) {

                int hour = salesLists.get(i).getIntDate();
                int price = salesLists.get(i).getTotalPrice();

                entries.add(new BarEntry(hour, price));


            }


        } else if (duration.equals("day")) {

            xAxis.setAxisMinimum((float) (salesLists.get(0).getIntDate() - 0.9));
            xAxis.setAxisMaximum((float) (salesLists.get(salesLists.size() - 1).getIntDate() + 0.9));
            xAxis.setSpaceMin(0.1f);
            xAxis.setSpaceMax(0.1f);


            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return (int) value + "일";
                }
            });


            for (int i = 0; i < salesLists.size(); i++) {


                int day = salesLists.get(i).getIntDate();
                Log.d(TAG, "day: " + day);


                int price = salesLists.get(i).getTotalPrice();
                Log.d(TAG, "price: " + price);

                entries.add(new BarEntry(day, price));

            }


        } else if (duration.equals("week")) {

            xAxis.setAxisMinimum((float) -0.9);
            xAxis.setAxisMaximum((float) salesLists.size());


            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    int index = (int) value;
                    Log.d(TAG, "getFormattedValue: " + index);

                    if (index >= 0 && index < salesLists.size()) {
                        int month = (salesLists.get(index).getIntDate() / 10); // 정수 부분은 월
                        int week = (salesLists.get(index).getIntDate() % 10); // 소수 부분은 주차

                        return month + "월 " + week + "주차";
                    }

                    return ""; // 유효하지 않은 인덱스일 경우 빈 문자열 반환

                }
            });


            for (int i = 0; i < salesLists.size(); i++) {

//                int week = salesLists.get(i).getIntDate();
//                Log.d(TAG, "setChart week: " + week);

                int price = salesLists.get(i).getTotalPrice();
                Log.d(TAG, "setChart price: " + price);

                entries.add(new BarEntry(i, price));
            }


        } else if (duration.equals("month")) {

//            xAxis.setAxisMinimum(0.1f);
//            xAxis.setAxisMaximum(12.9f);

            xAxis.setAxisMinimum((float) (salesLists.get(0).getIntDate() - 0.9));
            xAxis.setAxisMaximum((float) (salesLists.get(salesLists.size() - 1).getIntDate() + 0.9));


            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return (int) value + "월";
                }
            });


            for (int i = 0; i < salesLists.size(); i++) {

                int month = salesLists.get(i).getIntDate();

                int price = salesLists.get(i).getTotalPrice();

                entries.add(new BarEntry(month, price));

            }

        } else if (duration.equals("year")) {
            xAxis.setAxisMinimum((float) (salesLists.get(0).getIntDate() - 0.9));
            xAxis.setAxisMaximum((float) (salesLists.get(salesLists.size() - 1).getIntDate() + 0.9));
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return (int) value + "년";
                }
            });

            for (int i = 0; i < salesLists.size(); i++) {
                int year = salesLists.get(i).getIntDate();
                int price = salesLists.get(i).getTotalPrice();

                entries.add(new BarEntry(year, price));

            }
        }

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setTextSize(20f);
        legend.setTextColor(Color.BLACK);


        barDataSet = new BarDataSet(entries, title);
        barDataSet.setColor(R.color.blue_purple);
        barDataSet.setValueTextSize(17);
        barData.addDataSet(barDataSet);


        chart.setData(barData);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chart.invalidate(); // 차트 업데이트
            }
        });

    }


}

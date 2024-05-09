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

    TextView home, today, day, week, month, year;
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

        today.setOnClickListener(view -> setRetrofitClient("today"));

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
                            sortData(response.body().getSales(), duration);
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





    public void sortData(List<SalesDTO.SaleData> salesData, String duration) {

        Map<String, Integer> salesMap = new HashMap<>();
        String title = null;

        switch (duration){
            case "today" :
                title = LocalDate.now() + " 매출 데이터";

                for(SalesDTO.SaleData sale : salesData){
                    String[] dateTimeParts = sale.getDate().split(" ");
                    String timePart = dateTimeParts[1].substring(0, 2);
                    int amount = Integer.parseInt(sale.getAmount());
                    salesMap.merge(timePart, amount, Integer::sum);
                }
                break;

            case "day" :
                title = LocalDate.now().getMonth() + " 매출 데이터";
                //일별 데이터 -> 한달을 일로
                //day -> 시간별로 오늘 하루, 넘어가는 형식으로 좌우 넘어가게
                //week -> 일주일 데이터를 보여준다, 근데 넘어가는 형식으로 좌우로 바꿀 수 있게, 일(5/5) 얼마~ 월(5/6) 얼마~
                //month -> 월별 데이터를 1년치 보여준다. -> 1월 얼마~
                //year -> 년도별 데이터를 보여준다.

                for(SalesDTO.SaleData sale : salesData){
                    String[] dateTimeParts = sale.getDate().split(" ");
                    String dayPart = dateTimeParts[0].split("-")[2];
                    int amount = Integer.parseInt(sale.getAmount());
                    salesMap.merge(dayPart, amount, Integer::sum);
                }
                break;

            case "week" :
                title = LocalDate.now().getMonth() + " 매출 데이터";
                //주별 데이터 -> 한달을 주로 (아니면 3개월치를 4주로 해서 12주?)


                break;
            case "month" :
                //월별 데이터 ->
                break;
            case "year" :
                break;
        }

        setChart(salesMap, duration, title);

    }


    public void setChart(Map<String, Integer> sales, String duration, String title) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        BarData barData = new BarData();
        BarDataSet barDataSet;

        switch (duration){
            case "today" :
                xAxis.setAxisMinimum(-1f);
                xAxis.setAxisMaximum(23.9f);
                xAxis.setValueFormatter((value, axis) -> (int) value + "시");

                for(Map.Entry<String, Integer> entry : sales.entrySet()){
                    entries.add(new BarEntry(Integer.parseInt(entry.getKey()), entry.getValue()));
                }
                break;

            case "day":
                String lastDay = YearMonth.now().atEndOfMonth().toString().split("-")[2];

                xAxis.setAxisMinimum((float) (0.1));
                xAxis.setAxisMaximum((float) (Integer.parseInt(lastDay) + 0.9));
                xAxis.setSpaceMin(0.1f);
                xAxis.setSpaceMax(0.1f);
                xAxis.setValueFormatter((value, axis) -> (int) value + "일");

                for(Map.Entry<String, Integer> entry : sales.entrySet()){
                    entries.add(new BarEntry(Integer.parseInt(entry.getKey()), entry.getValue()));
                }
                break;

            case "week" :
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


}

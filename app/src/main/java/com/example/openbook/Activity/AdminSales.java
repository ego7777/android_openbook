package com.example.openbook.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.openbook.BuildConfig;
import com.example.openbook.Data.AdminData;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.DialogManager;
import com.example.openbook.R;
import com.example.openbook.retrofit.SalesDTO;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SalesItemDTO;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    String targetDate, targetDuration;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    DialogManager dialogManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_sales);

        overridePendingTransition(0, 0);

        adminData = getIntent().getParcelableExtra("adminData");
        appbarHome = findViewById(R.id.appbar_admin_home);
        appbarGear = findViewById(R.id.appbar_admin_gear);
        appbarGear.setVisibility(View.VISIBLE);

        drawerLayout = findViewById(R.id.admin_drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
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

        targetDate = LocalDate.now().toString();
        targetDuration = "day";
        drawerHeaderDuration.setText(getDuration());
        requestSalesData(targetDuration, targetDate);

        dialogManager = new DialogManager();
    }


    @Override
    protected void onResume() {
        super.onResume();


        beforeButton.setOnClickListener(view -> {
            switch (targetDuration) {
                case "day":
                    targetDate = LocalDate.parse(targetDate).minusDays(1).toString();
                    break;
                case "week":
                    targetDate = LocalDate.parse(targetDate).minusWeeks(1).toString();
                    break;
                case "month":
                    targetDate = LocalDate.parse(targetDate).minusMonths(1).toString();
                    break;
                case "year":
                    targetDate = LocalDate.parse(targetDate).minusYears(1).toString();
                    break;
            }
            Log.d(TAG, "targetDate: " + targetDate);
            drawerHeaderDuration.setText(getDuration());
            requestSalesData(targetDuration, targetDate);
        });

        afterButton.setOnClickListener(view -> {
            switch (targetDuration) {
                case "day":
                    targetDate = LocalDate.parse(targetDate).plusDays(1).toString();
                    break;
                case "week":
                    targetDate = LocalDate.parse(targetDate).plusWeeks(1).toString();
                    break;
                case "month":
                    targetDate = LocalDate.parse(targetDate).plusMonths(1).toString();
                    break;
                case "year":
                    targetDate = LocalDate.parse(targetDate).plusYears(1).toString();
                    break;
            }
            drawerHeaderDuration.setText(getDuration());
            requestSalesData(targetDuration, targetDate);
        });


        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.daily_sales) {
                targetDuration = "day";
                drawerHeaderDuration.setText(getDuration());
                requestSalesData(targetDuration, targetDate);

            } else if (itemId == R.id.weekly_sales) {
                targetDuration = "week";
                drawerHeaderDuration.setText(getDuration());
                requestSalesData(targetDuration, targetDate);

            } else if (itemId == R.id.monthly_sales) {
                targetDuration = "month";
                drawerHeaderDuration.setText(getDuration());
                requestSalesData(targetDuration, targetDate);

            } else if (itemId == R.id.yearly_sales) {
                drawerHeaderDuration.setText(getDuration());
                targetDuration = "year";
                requestSalesData("year", targetDate);

            } else if (itemId == R.id.max_sales) {
                Log.d(TAG, "date: " + targetDate + "\nduration :" + targetDuration);
                setSalesItems(targetDuration, targetDate, "max");

            } else if (itemId == R.id.min_sales) {
                setSalesItems(targetDuration, targetDate, "min");
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });


        appbarGear.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
                drawerHeaderCancel.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));
            }
        });

        appbarHome.setOnClickListener(view -> {
            Intent intent = new Intent(AdminSales.this, Admin.class);
            intent.putExtra("adminData", adminData);
            intent.putExtra("adminTableList", adminTableList);
            startActivity(intent);
        });
    }

    public void setSalesItems(String duration, String currentDate, String standard) {
        Call<SalesItemDTO> call = service.requestSalesItems(duration, currentDate, standard);
        call.enqueue(new Callback<SalesItemDTO>() {
            @Override
            public void onResponse(@NonNull Call<SalesItemDTO> call, @NonNull Response<SalesItemDTO> response) {
                Log.d(TAG, "onResponse setSalesItems: " + response.body().getResult());
                if (response.isSuccessful()) {
                    switch (response.body().getResult()) {
                        case "success":
                            String header = null;
                            if (standard.equals("max")) {
                                header = drawerHeaderDuration.getText().toString() + " 최다 판매";
                            } else {
                                header = drawerHeaderDuration.getText().toString() + " 최저 판매";
                            }
                            dialogManager.showSalesItemsDialog
                                    (AdminSales.this,
                                            response.body().getSalesItems(), header).show();
                            break;
                        case "failed":
                            dialogManager.positiveBtnDialog
                                    (AdminSales.this,
                                            "데이터가 존재하지 않습니다.").show();
                            break;
                    }
                } else {
                    Log.d(TAG, "setSalesItems is not successful");
                }

            }

            @Override
            public void onFailure(@NonNull Call<SalesItemDTO> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure setSalesItems: " + t.getMessage());
            }
        });
    }


    public void requestSalesData(String duration, String currentDate) {
        Call<SalesDTO> call = service.requestSalesData(duration, currentDate);
        call.enqueue(new Callback<SalesDTO>() {
            @Override
            public void onResponse(@NonNull Call<SalesDTO> call, @NonNull Response<SalesDTO> response) {
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
                            totalSales.setText(drawerHeaderDuration.getText().toString());
                            chart.invalidate();
                            break;
                    }
                } else {
                    Log.d(TAG, "onResponse: " + response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SalesDTO> call, @NonNull Throwable t) {
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
                drawerHeaderDuration.getText().toString();
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
                drawerHeaderDuration.getText().toString();
                xAxis.setAxisMinimum((float) -0.9);
                xAxis.setAxisMaximum((float) 7.1);
                xAxis.setTextSize(15);

                Map<Integer, String> formattedDates = new HashMap<>();

                for (SalesDTO.SaleData entity : saleData) {
                    Pair<Integer, String> weekData = dayOfWeekNumber(entity.getDate());
                    int date = weekData.first;
                    formattedDates.put(date, weekData.second);
                    int amount = Integer.parseInt(entity.getAmount());
                    entries.add(new BarEntry(date, amount));
                }

                xAxis.setValueFormatter((value, axis) -> {
                    int index = (int) value;
                    return formattedDates.getOrDefault(index, "");
                });
                break;

            case "month":
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월");
                title = LocalDate.parse(targetDate).format(formatter);

                xAxis.setAxisMinimum((float) -0.9);
                xAxis.setAxisMaximum((float) saleData.size());
                xAxis.setTextSize(15);

                int index = 0;
                formattedDates = new HashMap<>();
                for (SalesDTO.SaleData entity : saleData) {
                    int amount = Integer.parseInt(entity.getAmount());
                    entries.add(new BarEntry(index, amount));
                    formattedDates.put(index, entity.getDuration());
                    index++;
                }

                xAxis.setValueFormatter((value, axis) -> {
                    int weekOfMonth = (int) value;
                    return formattedDates.getOrDefault(weekOfMonth, "");
                });

                break;

            case "year":
                drawerHeaderDuration.getText().toString();
                xAxis.setAxisMinimum((float) -0.9);
                xAxis.setAxisMaximum((float) 12.1);

                for (SalesDTO.SaleData entity : saleData) {
                    int date = Integer.parseInt(entity.getDate());
                    int amount = Integer.parseInt(entity.getAmount());
                    entries.add(new BarEntry(date, amount));
                }

                xAxis.setValueFormatter((value, axis) -> (int) value + "월");
                break;
        }

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

    public String getDuration() {
        switch (targetDuration) {
            case "day":
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
                return LocalDate.parse(targetDate).format(formatter);

            case "week":
                LocalDate weekDate = LocalDate.parse(targetDate);
                WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
                int year = weekDate.getYear();
                int weekOfMonth = weekDate.get(weekFields.weekOfMonth());
                int month = weekDate.getMonthValue();

                return year + "년" + month + "월 " + weekOfMonth + "째주";

            case "month":
                LocalDate monthDate = LocalDate.parse(targetDate);
                YearMonth yearMonth = YearMonth.from(monthDate);

                formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                String start = yearMonth.atDay(1).format(formatter);
                String end = yearMonth.atEndOfMonth().format(formatter);

                return start + "~" + end;

            case "year":
                int yearDate = LocalDate.parse(targetDate).getYear();
                return yearDate + "년";
        }
        return "";
    }

    public void init() {
        chart.getDescription().setEnabled(false);

        chart.setNoDataText(getResources().getString(R.string.noSales));
        chart.setNoDataTextColor(Color.RED);

        xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(18);
        xAxis.setTextColor(Color.BLACK);

        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        xAxis.setSpaceMin(1f);

        yAxisRight = chart.getAxisRight();
        yAxisLeft = chart.getAxisLeft();

        yAxisRight.setTextSize(15);
        yAxisRight.setAxisMinimum(0f);
        yAxisRight.setGranularity(1f);

        yAxisLeft.setTextSize(15);
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setGranularity(1f);

    }

    public Pair<Integer, String> dayOfWeekNumber(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate localDate = LocalDate.parse(dateString, formatter);

        int dayOfWeekNumber = localDate.getDayOfWeek().getValue() - 1;

        String koreanWeekDay = localDate.getDayOfWeek().getDisplayName(TextStyle.NARROW, Locale.KOREAN);
        formatter = DateTimeFormatter.ofPattern("MM/dd");
        String date = localDate.format(formatter);
        String dayOfWeek = date + "(" + koreanWeekDay + ")";

        return Pair.create(dayOfWeekNumber, dayOfWeek);
    }


}

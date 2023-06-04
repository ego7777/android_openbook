package com.example.openbook.Activity;

import android.content.Entity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.Data.AdminSalesList;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminSales extends AppCompatActivity {

    String TAG = "AdminSales_TAG";

    String get_id;
    ArrayList<AdminTableList> adminTableList;

    TextView home, day, week, month, year;
    TextView appbar_admin_addMenu;
    TextView appbar_admin_modifyTable;

    BarChart chart;

    OkHttpClient okHttpClient;
    RequestBody requestBody;

    Request request;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_sales);

        get_id = getIntent().getStringExtra("get_id");
        adminTableList = (ArrayList<AdminTableList>) getIntent().getSerializableExtra("adminTableList");

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

        setOkHttpClient("day");


    }

    @Override
    protected void onResume() {
        super.onResume();

        home.setOnClickListener(view -> {
            Intent intent = new Intent(AdminSales.this, Admin.class);
            intent.putExtra("get_id", get_id);
            intent.putExtra("adminTableList", adminTableList);
            startActivity(intent);
        });


        day.setOnClickListener(view -> {

            setOkHttpClient("day");
        });

        week.setOnClickListener(view -> {

            setOkHttpClient("week");

        });

        month.setOnClickListener(view -> {
            setOkHttpClient("month");
        });


        year.setOnClickListener(view -> {
            setOkHttpClient("year");
        });
    }

    public void setOkHttpClient(String duration) {
        //db에 접근해서 일단 오늘 매출을 보여준다
        okHttpClient = new OkHttpClient();

        requestBody = new FormBody.Builder()
                .add("duration", duration)
                .build();

        request = new Request.Builder()
                .url("http://3.36.255.141/Sales.php")
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: " + e);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                Log.d(TAG, "onResponse: " + body);

                try {
                    setChart(duration, body);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setChart(String duration, String data) throws JSONException, ParseException {

        JSONArray jsonArray = new JSONArray(data);

        ArrayList<Entity> entity_chart = new ArrayList<>();




        if (duration.equals("day")) {

            //time 00 01 02 ~ 12 까지 (24개의 그래프로 나눠서 array에 넣기)

        } else if (duration.equals("week")) {

            temp(jsonArray, duration);

            //차라리 여기서 리턴 받아서 써라

            // 일 ~ 토 (7개의 그래프로 나눠서 array에 넣기)

        } else if (duration.equals("month")) {

            // 1월 ~ 12월 (12개의 그래프로 나눠서 넣기)

        } else if (duration.equals("year")) {

        }





    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void temp(JSONArray jsonArray, String duration) throws JSONException{

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        ArrayList<AdminSalesList> salesLists = new ArrayList();

        String temp = "";

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);
            LocalDateTime date = LocalDateTime.parse(jsonObject.getString("orderTime"),
                    dateTimeFormatter);

            int totalPrice = jsonObject.getInt("totalPrice");

            salesLists.add(new AdminSalesList(date, totalPrice));

        }

        Log.d(TAG, "salesList: " + salesLists.size());

        if(duration.equals("day")){

            for(int i=0; i<salesLists.size(); i++){
//                if(salesLists.get(i).)
            }
        }


    }
}

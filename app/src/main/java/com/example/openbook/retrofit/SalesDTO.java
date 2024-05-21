package com.example.openbook.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SalesDTO {

    private String result;

    @SerializedName("total_amount")
    private String totalAmount;
    private List<SaleData> sales;

    public String getResult() {
        return result;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public List<SaleData> getSales() {
        return sales;
    }

    public class SaleData {
        private String date;
        private String amount;
        private String duration;

        public String getDate() {
            return date;
        }

        public String getAmount() {
            return amount;
        }

        public String getDuration() {
            return duration;
        }
    }
}

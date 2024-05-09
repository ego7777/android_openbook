package com.example.openbook;

import java.util.List;

public class SalesDTO {

    private String result;
    private List<SaleData> sales;

    public String getResult() {
        return result;
    }

    public List<SaleData> getSales() {
        return sales;
    }

    public class SaleData {
        private String date;
        private String amount;

        public String getDate() {
            return date;
        }

        public String getAmount() {
            return amount;
        }
    }
}

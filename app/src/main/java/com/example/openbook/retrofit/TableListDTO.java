package com.example.openbook.retrofit;

public class TableListDTO {
    String result;
    int tableCount;

    public TableListDTO(String result, int tableCount){
        this.result = result;
        this.tableCount = tableCount;
    }

    public String getResult() {
        return result;
    }

    public int getTableCount() {
        return tableCount;
    }
}

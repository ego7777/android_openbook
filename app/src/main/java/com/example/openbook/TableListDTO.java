package com.example.openbook;

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

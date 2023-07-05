package com.example.openbook.Data;


import java.io.Serializable;

public class TicketData implements Serializable {

    String whoBuy;
    boolean isUsed;
    String useTable;


    public TicketData(String whoBuy, boolean isUsed, String useTable) {
        this.whoBuy = whoBuy;
        this.isUsed = isUsed;
        this.useTable = useTable;

    }

    public String getWhoBuy() {
        return whoBuy;
    }

    public void setWhoBuy(String whoBuy) {
        this.whoBuy = whoBuy;
    }


    public void setIsUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public String getUseTable() {
        return useTable;
    }

    public void setUseTable(String useTable) {
        this.useTable = useTable;
    }

}

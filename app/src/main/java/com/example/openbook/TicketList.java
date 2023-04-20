package com.example.openbook;

import android.os.Parcelable;

import java.io.Serializable;

public class TicketList implements Serializable {

    int whoBuy;
    boolean usage;
    String useTable;

    public TicketList(int whoBuy, boolean usage, String useTable){
        this.whoBuy =whoBuy;
        this.usage = usage;
        this.useTable = useTable;
    }

    public int getWhoBuy() {
        return whoBuy;
    }

    public void setWhoBuy(int whoBuy) {
        this.whoBuy = whoBuy;
    }

    public boolean isUsage() {
        return usage;
    }

    public void setUsage(boolean usage) {
        this.usage = usage;
    }

    public String getUseTable() {
        return useTable;
    }

    public void setUseTable(String useTable) {
        this.useTable = useTable;
    }




}

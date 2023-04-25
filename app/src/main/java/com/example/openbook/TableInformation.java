package com.example.openbook;

import android.os.Parcelable;

import java.io.Serializable;

public class TableInformation implements Serializable {

    String whoBuy;
    boolean usage;
    int useTable;
    boolean chattingAgree;

    public TableInformation(String whoBuy, boolean usage, int useTable, boolean chattingAgree){
        this.whoBuy =whoBuy;
        this.usage = usage;
        this.useTable = useTable;
        this.chattingAgree = chattingAgree;
    }

    public String getWhoBuy() {
        return whoBuy;
    }

    public void setWhoBuy(String whoBuy) {
        this.whoBuy = whoBuy;
    }

    public boolean isUsage() {
        return usage;
    }


    public void setUsage(boolean usage) {
        this.usage = usage;
    }

    public int getUseTable() {
        return useTable;
    }

    public void setUseTable(int useTable) {
        this.useTable = useTable;
    }

    public boolean isChattingAgree() {
        return chattingAgree;
    }

    public void setChattingAgree(boolean chattingAgree) {
        this.chattingAgree = chattingAgree;
    }
}

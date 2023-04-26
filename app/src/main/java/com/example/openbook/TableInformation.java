package com.example.openbook;


import java.io.Serializable;

public class TableInformation implements Serializable {

    String whoBuy;
    boolean usage;
    int useTable;
    boolean chattingAgree;
    boolean block;

    public TableInformation(String whoBuy, boolean usage,
                            int useTable, boolean chattingAgree, boolean block){
        this.whoBuy =whoBuy;
        this.usage = usage;
        this.useTable = useTable;
        this.chattingAgree = chattingAgree;
        this.block = block;
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

    public void setBlock(boolean block) {
        this.block = block;
    }
}

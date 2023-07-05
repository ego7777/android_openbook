package com.example.openbook.Data;

import java.io.Serializable;

public class ChattingData implements Serializable {
    String tableName;
    boolean chattingAgree;
    boolean block;

    public ChattingData(String tableName, boolean chattingAgree, boolean block){
        this.tableName = tableName;
        this.chattingAgree = chattingAgree;
        this.block = block;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isChattingAgree() {
        return chattingAgree;
    }

    public void setChattingAgree(boolean chattingAgree) {
        this.chattingAgree = chattingAgree;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }


}

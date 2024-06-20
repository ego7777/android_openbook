package com.example.openbook.Data;

import com.example.openbook.Category.ChattingCategory;

public class ChattingList {
    String text;
    int imgId;
    ChattingCategory type;
    String time;

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    String read;

    public ChattingList(String text, ChattingCategory type, String time, String read){
        this.text = text;
        this.type =type;
        this.time = time;
        this.read = read;
    }

    public ChattingCategory getType() {
        return type;
    }

    public String getTime(){
        return time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImgId() {
        return imgId;
    }


}

package com.example.openbook.Data;

public class ChattingList {
    String text;
    int imgId;
    int viewType;
    String time;

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    String read;

    public ChattingList(String text, int viewType, String time, String read){
        this.text = text;
        this.viewType =viewType;
        this.time = time;
        this.read = read;
    }

    public String getTime(){
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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


    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}

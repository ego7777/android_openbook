package com.example.openbook.View;

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

    public ChattingList(int imgId,String text, int viewType, String time){
        this.imgId = imgId;
        this.text = text;
        this.viewType = viewType;
        this.time = time;
    }

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

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}

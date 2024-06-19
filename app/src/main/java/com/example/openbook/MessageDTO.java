package com.example.openbook;

public class MessageDTO {

    String to;
    String from;
    String message;
//    int isRead;


    public MessageDTO(String to, String from, String message){
        this.to = to;
        this.from = from;
        this.message = message;
    }

//    public MessageDTO(String from, int isRead){
//        this.from = from;
//        this.isRead = isRead;
//    }
//
//    public void setIsRead(int isRead) {
//        this.isRead = isRead;
//    }
//
//    public int getIsRead() {
//        return isRead;
//    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

}

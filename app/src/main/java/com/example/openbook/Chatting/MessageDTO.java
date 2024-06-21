package com.example.openbook.Chatting;

public class MessageDTO {

    String to;
    String from;
    String message;
    String time;


    public MessageDTO(String to, String from, String message, String time){
        this.to = to;
        this.from = from;
        this.message = message;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setFrom(String from) {
        this.from = from;
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

package com.example.icare;

public class Messages {

    public  String date,time,from,type,messages;

    public Messages()
    {

    }

    public Messages(String date, String time, String from, String type, String message) {
        this.date = date;
        this.time = time;
        this.from = from;
        this.type = type;
        this.messages = message;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getFrom() {
        return from;
    }

    public String getType() {
        return type;
    }

    public String getMessages() {
        return messages;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }
}

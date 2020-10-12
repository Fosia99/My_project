package com.example.icare;

public class Comments {
    public String uid, comment,date,time,username;

    public Comments()
    {

    }

    public Comments(String uid, String comment, String date, String time, String username) {
        this.uid = uid;
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

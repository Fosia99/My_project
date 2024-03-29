package com.example.icare;

public class Posts {
    public String date ,description ,fullname ,time ,uid;
    private String postimage,profileimage;

    public Posts(){

    }

    public Posts(String date, String description, String fullname, String postimage, String profileimage, String time, String uid) {
        this.date = date;
        this.description = description;
        this.fullname = fullname;
        this.postimage = postimage;
        this.profileimage = profileimage;
        this.time = time;
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

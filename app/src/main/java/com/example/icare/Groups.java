package com.example.icare;

public class Groups {

    public String date ,description ,time ,uid,name;
    private String groupIcon;

    public Groups(){

    }

    public Groups(String date, String description, String time, String uid, String groupIcon, String name) {
        this.date = date;
        this.description = description;
        this.time = time;
        this.uid = uid;
        this.name = name;
        this.groupIcon = groupIcon;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public String getUid() {
        return uid;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public String getName() {
        return name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;

    }
}

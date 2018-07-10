package com.example.demo.model;

public class Rating {

    String talkId;

    int value;

    public Rating() {
    }

    public Rating(String talkId, int value) {
        this.talkId = talkId;
        this.value = value;
    }

    public String getTalkId() {
        return talkId;
    }

    public int getValue() {
        return value;
    }
}

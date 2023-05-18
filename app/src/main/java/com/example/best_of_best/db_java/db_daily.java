package com.example.best_of_best.db_java;

public class db_daily {
    private String event;
    private int set;
    private int count;

    public db_daily(String event, int set, int count){
        this.event = event;
        this.set = set;
        this.count = count;
    }
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

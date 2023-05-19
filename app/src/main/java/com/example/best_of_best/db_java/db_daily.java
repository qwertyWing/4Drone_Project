package com.example.best_of_best.db_java;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class db_daily implements Serializable {
    private String event;
    private String set;
    private String count;

    public db_daily(String event, String set, String count){
        this.event = event;
        this.set = set;
        this.count = count;
    }
    public db_daily(){

    }
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

//    public Map<String, Object> toMap() {
//
//        HashMap<String, Object> result = new HashMap<>();
//        result.put("event", event);
//        result.put("set", set);
//        result.put("count", count);
//
//        return result;
//    }
}

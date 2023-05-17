package com.example.best_of_best.db_java;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class login_member implements Serializable {
    private String name;
    private String id;
    private String pw;
    private String email;
    private String birth;

    public login_member(String name, String id, String pw, String email, String birth){
        this.name = name;
        this.id = id;
        this.pw = pw;
        this.email = email;
        this.birth = birth;
    }
    login_member(){

    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return id;
    }

    public void setPw(String pw){
        this.pw = pw;
    }
    public String getPw(){
        return pw;
    }

    public void setEmail(String email){
        this.email = email;
    }
    public String getEmail(){
        return email;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("id", id);
        result.put("pw", pw);
        result.put("email", email);
        result.put("birth", birth);

        return result;
    }
}

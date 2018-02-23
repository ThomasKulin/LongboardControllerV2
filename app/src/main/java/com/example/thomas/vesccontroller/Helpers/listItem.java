package com.example.thomas.vesccontroller.Helpers;

public class listItem{
    String name;
    String value;
    String hint;

    public listItem(){
        this.name = "DEFAULT";
        this.value = "DEFAULT";
        this.hint = "DEFAULT";
    }
    public listItem(String name, String value, String hint){
        this.name = name;
        this.value = value;
        this.hint = hint;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setValue(String value){
        this.value = value;
    }
    public void setHint(String hint){
        this.hint = hint;
    }
    public String getName(){
        return this.name;
    }
    public String getValue(){
        return this.value;
    }
    public String getHint(){
        return this.hint;
    }
}
package com.belajar.trydev.doadzikir.model;

import org.json.JSONObject;

public class Hijriah {
    private String date,month,year;

    public Hijriah(String date, String month, String year) {
        this.date = date;
        this.month = month;
        this.year = year;
    }

    public Hijriah(JSONObject json){
        try{
            this.date = json.getString("day");
            this.month = convertMonth(json.getJSONObject("month").getString("en"));
            this.year = json.getString("year");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String convertMonth(String month){
        switch(month){
            case "Shawwāl" :
                return "Syawal";
            case "Dhū al-Qaʿdah" :
                return "Dzulqa'dah";
            default:
                return month;
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}

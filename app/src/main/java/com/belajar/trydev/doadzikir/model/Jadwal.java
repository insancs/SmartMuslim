package com.belajar.trydev.doadzikir.model;

import org.json.JSONObject;

public class Jadwal {
    private String shubuh;
    private String dhuhur;
    private String ashar;
    private String maghrib;
    private String isya;

    public Jadwal(JSONObject json){
        try{
            this.shubuh = json.getString("Fajr").substring(0,5);
            this.dhuhur = json.getString("Dhuhr").substring(0,5);
            this.ashar = json.getString("Asr").substring(0,5);
            this.maghrib = json.getString("Maghrib").substring(0,5);
            this.isya = json.getString("Isha").substring(0,5);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getShubuh() {
        return shubuh;
    }

    public void setShubuh(String shubuh) {
        this.shubuh = shubuh;
    }

    public String getDhuhur() {
        return dhuhur;
    }

    public void setDhuhur(String dhuhur) {
        this.dhuhur = dhuhur;
    }

    public String getAshar() {
        return ashar;
    }

    public void setAshar(String ashar) {
        this.ashar = ashar;
    }

    public String getMaghrib() {
        return maghrib;
    }

    public void setMaghrib(String maghrib) {
        this.maghrib = maghrib;
    }

    public String getIsya() {
        return isya;
    }

    public void setIsya(String isya) {
        this.isya = isya;
    }
}

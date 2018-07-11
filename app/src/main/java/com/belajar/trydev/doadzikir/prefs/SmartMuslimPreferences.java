package com.belajar.trydev.doadzikir.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.belajar.trydev.doadzikir.model.Hijriah;
import com.belajar.trydev.doadzikir.model.Jadwal;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SmartMuslimPreferences {
    private String KEY_JADWAL = "JADWAL";
    private String KEY_HIJRIAH = "HIJRIAH";
    private String KEY_LONGITUDE = "LONGITUDE";
    private String KEY_LATITUDE = "LATITUDE";
    private String KEY_ADDRESS = "ADDRESS";

    private SharedPreferences preferences;

    public SmartMuslimPreferences(Context context){
        String PREFS_NAME = "SmartMuslimPrefs";
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setListJadwal(ArrayList<Jadwal> listJadwal){
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(listJadwal);
        editor.putString(KEY_JADWAL, json);
        editor.apply();
    }

    public ArrayList<Jadwal> getJadwalList(String key){
        Gson gson = new Gson();
        String json = preferences.getString(key, null);
        Type type = new TypeToken<ArrayList<Jadwal>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void setListHijriah(ArrayList<Hijriah> listHijriah){
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(listHijriah);
        editor.putString(KEY_HIJRIAH, json);
        editor.apply();
    }

    public ArrayList<Hijriah> getHijriahList(String key){
        Gson gson = new Gson();
        String json = preferences.getString(key, null);
        Type type = new TypeToken<ArrayList<Hijriah>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void setLongitude(double longitude){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(KEY_LONGITUDE, (float) longitude);
        editor.apply();
    }

    public double getLongitude(String key){
        return (double) preferences.getFloat(key, 0f);
    }

    public void setLatitude(double latitude){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(KEY_LATITUDE, (float) latitude);
        editor.apply();
    }

    public double getLatitude(String key){
        return (double) preferences.getFloat(key, 0f);
    }

    public void setAddress(String address){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ADDRESS, address);
        editor.apply();
    }

    public String getAddress(String key){
        return preferences.getString(key, null);
    }
}

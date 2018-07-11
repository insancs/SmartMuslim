package com.belajar.trydev.doadzikir;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.belajar.trydev.doadzikir.model.Hijriah;
import com.belajar.trydev.doadzikir.model.Jadwal;
import com.belajar.trydev.doadzikir.prefs.SmartMuslimPreferences;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.security.auth.login.LoginException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_UPDATE_UI = "update_ui";
    public static final String ACTION_STOP = "stop";

    public ArrayList<Jadwal> jadwal;
    public ArrayList<Hijriah> hijriah;
    private String url = "";
    private double longitude,latitude;
    private ProgressDialog pd;
    private ProgressDialog pdGetData;

    private FusedLocationProviderClient client;
    private SmartMuslimPreferences preferences;

    private BroadcastReceiver updateReceiver;

    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.remaining)
    TextView remaining;
    @BindView(R.id.location) TextView tvLocation;
    @BindView(R.id.stop) ImageButton stop;
    Geocoder geocoder;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateReceiver!=null){
            unregisterReceiver(updateReceiver);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        requestPermission();

        jadwal = new ArrayList<>();
        hijriah = new ArrayList<>();

        pd = new ProgressDialog(this);
        pdGetData = new ProgressDialog(this);

        preferences = new SmartMuslimPreferences(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        //cek network first
        if (cekNetwork()){
            Log.i("CEK NETWORK", "MASUK");
            getLocation(this);
            pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if ((float)longitude==preferences.getLongitude("LONGITUDE")
                            && (float)latitude==preferences.getLatitude("LATITUDE")){
                        if (preferences.getHijriahList("HIJRIAH")!=null
                                && preferences.getJadwalList("JADWAL")!=null){
                            jadwal = preferences.getJadwalList("JADWAL");
                            hijriah = preferences.getHijriahList("HIJRIAH");
                            SimpleDateFormat formatter = new SimpleDateFormat("dd");
                            Date dateTime = new Date();
                            setAll(Integer.parseInt(formatter.format(dateTime))-1);

                            tvLocation.setText(preferences.getAddress("ADDRESS"));
                        } else{
                            Log.i("LIST NULL", "MASUK");
                            requestData(url);
                        }
                    } else{
                        Log.i("LATLONG BEDA", "MASUK ");
                        requestData(url);
                    }
                }
            });

        } else{
            if (preferences.getHijriahList("HIJRIAH")!=null
                    && preferences.getJadwalList("JADWAL")!=null){
                jadwal = preferences.getJadwalList("JADWAL");
                hijriah = preferences.getHijriahList("HIJRIAH");
                SimpleDateFormat formatter = new SimpleDateFormat("dd");
                Date dateTime = new Date();
                setAll(Integer.parseInt(formatter.format(dateTime))-1);
            }
            if (preferences.getLatitude("LATITUDE")!=0.0){
                latitude = preferences.getLatitude("LATITUDE");
            }
            if (preferences.getLongitude("LONGITUDE")!=0.0){
                longitude = preferences.getLongitude("LONGTIUDE");
            }
            if (preferences.getAddress("ADDRESS")!=null){
                tvLocation.setText(preferences.getAddress("ADDRESS"));
            }
        }

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AdzanService.mediaPlayerShubuh.isPlaying())
                    AdzanService.mediaPlayerShubuh.stop();
                if (AdzanService.mediaPlayer.isPlaying())
                    AdzanService.mediaPlayer.stop();
            }
        });

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("BROADCAST DITERIMA", "UPDATING..."+intent.getAction());
                setAll(Integer.parseInt(new SimpleDateFormat("dd").format(new Date())));
//                if (intent.getAction().equals(ACTION_STOP)){
//                    if(AdzanService.mediaPlayer.isPlaying())
//                        AdzanService.mediaPlayer.stop();
//                    if (AdzanService.mediaPlayerShubuh.isPlaying())
//                        AdzanService.mediaPlayerShubuh.stop();
//                }
            }
        };
        IntentFilter UIupdater = new IntentFilter();
        UIupdater.addAction(ACTION_UPDATE_UI);
        registerReceiver(updateReceiver,UIupdater);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isServiceRunning()){
                    Log.i("IS SERVICE RUN?", "YES");
                    Intent intent = new Intent(MainActivity.this, AdzanService.class);
                    startService(intent);
                }
            }
        },3000);

    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.belajar.trydev.doadzikir.AdzanService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean cekNetwork(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION},1);
    }

    private void getLocation(Context context){
        pd.setMessage("Sedang mendapatkan lokasi anda...");
        pd.setCancelable(false);
        pd.show();
        Log.i("GET LOCATION", "MASUK");
        client = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pd.dismiss();
            }
        },10000);
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(),1);
                    preferences.setAddress(addresses.get(0).getAddressLine(0));
                    preferences.setLongitude(location.getLongitude());
                    preferences.setLatitude(location.getLatitude());
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    Log.i("GET LOCATION", "SUKSES");
                    tvLocation.setText(addresses.get(0).getAddressLine(0));
                } catch (IOException e) {
                    e.printStackTrace();

                }

                Calendar calendar = Calendar.getInstance();
                Log.i("CALENDAR", "month: "+calendar.get(Calendar.MONTH)+", year: "+calendar.get(Calendar.YEAR));
                url = BuildConfig.BASE_URL+"calendar?latitude="+location.getLatitude()+"&longitude="+location.getLongitude()
                        +"&month="+(calendar.get(Calendar.MONTH)+1)+"&year="+calendar.get(Calendar.YEAR);
                pd.dismiss();
            }
        });
    }

    private void requestData(String url){
        pdGetData.setCancelable(false);
        pdGetData.setMessage("Sedang mengambil data...");
        pdGetData.show();
        Log.i("REQUEST DATA", "REQUESTING");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try{
                    String result = new String(responseBody);
                    JSONArray data = new JSONObject(result).getJSONArray("data");

                    for (int i = 0; i < data.length(); i++) {
                        Jadwal j = new Jadwal(data.getJSONObject(i).getJSONObject("timings"));
                        Hijriah h = new Hijriah(data.getJSONObject(i).getJSONObject("date").getJSONObject("hijri"));
                        jadwal.add(j);
                        hijriah.add(h);
                    }

                    preferences.setListJadwal(jadwal);
                    preferences.setListHijriah(hijriah);
                    pdGetData.dismiss();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                SimpleDateFormat formatter = new SimpleDateFormat("dd");
                Date dateTime = new Date();
                setAll(Integer.parseInt(formatter.format(dateTime))-1);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(MainActivity.this, "Gagal mengambil data.\nPeriksa koneksi internet anda.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void jadwalSholat(String waktu, int tanggal){
        String sholat = "";
        switch (waktu){
            case "Shubuh":
                sholat = jadwal.get(tanggal).getShubuh();
                time.setText(jadwal.get(tanggal).getShubuh().substring(0,5));
                break;
            case "Dhuhur":
                sholat = jadwal.get(tanggal).getDhuhur();
                time.setText(jadwal.get(tanggal).getDhuhur().substring(0,5));
                break;
            case "Ashar":
                sholat = jadwal.get(tanggal).getAshar();
                time.setText(jadwal.get(tanggal).getAshar().substring(0,5));
                break;
            case "Maghrib":
                sholat = jadwal.get(tanggal).getMaghrib();
                time.setText(jadwal.get(tanggal).getMaghrib().substring(0,5));
                break;
            case "Isya":
                sholat = jadwal.get(tanggal).getIsya();
                time.setText(jadwal.get(tanggal).getIsya().substring(0,5));
                break;
            case "Shubuh Spesial":
                sholat = jadwal.get(tanggal).getShubuh();
                time.setText(jadwal.get(tanggal).getShubuh().substring(0,5));
                break;
        }
        updateUI(waktu, sholat);
    }

    private void updateUI(String waktu, String sholat){
        DateFormat format = new SimpleDateFormat("HH:mm");
        try {
            Date date =  format.parse(sholat);
            Date now = format.parse(format.format(new Date()));
            int menit = (int) (date.getTime()-now.getTime())/(1000*60);
            int jam = 0;

            if (waktu.equals("Shubuh Spesial")){
                menit *= -1;
                waktu = "Shubuh";
            }

            Log.i("MENIT", "Menit: "+menit);
            while(menit>=60){
                jam++;
                menit -= 60;
            }
            if (jam>0){
                remaining.setText(String.format(getResources().getString(R.string.time_remains),
                        String.valueOf(jam)+" jam "+String.valueOf(menit)+" menit", waktu));
            }else if(jam>0 && menit<=0){
                remaining.setText(String.format(getString(R.string.time_remains),
                        String.valueOf(jam)+" jam", waktu));
            } else if(jam<=0 && menit>0){
                remaining.setText(String.format(getResources().getString(R.string.time_remains),
                        String.valueOf(menit)+" menit", waktu));
            } else if(jam<=0 && menit<=0){
                remaining.setText("Sekarang saatnya waktu "+waktu);
            } else if (jam<=0 && menit>=59){
                remaining.setText("Waktu "+waktu+" sejak "+String.valueOf(menit)+" yang lalu");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setAll(int tanggal){

        Hijriah index = hijriah.get(tanggal);
        date.setText(String.format(getResources().getString(R.string.date),
                index.getDate() + " " + index.getMonth() + " " + index.getYear()));

        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.HOUR_OF_DAY)<5){
            jadwalSholat("Shubuh", tanggal);
        }
        else if (calendar.get(Calendar.HOUR_OF_DAY)<12){
            jadwalSholat("Dhuhur", tanggal);
        }
        else if (calendar.get(Calendar.HOUR_OF_DAY)<15){
            jadwalSholat("Ashar", tanggal);
        }
        else if (calendar.get(Calendar.HOUR_OF_DAY)<18){
            jadwalSholat("Maghrib", tanggal);
        }
        else if (calendar.get(Calendar.HOUR_OF_DAY)<19){
            jadwalSholat("Isya", tanggal);
        }
        else if (calendar.get(Calendar.HOUR_OF_DAY)>18){
            jadwalSholat("Shubuh Spesial", tanggal);
        }

    }
}

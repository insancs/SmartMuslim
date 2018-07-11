package com.belajar.trydev.doadzikir;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.belajar.trydev.doadzikir.model.Jadwal;
import com.belajar.trydev.doadzikir.prefs.SmartMuslimPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AdzanService extends Service {
    private SmartMuslimPreferences preferences;
    private String sholat;
    public static MediaPlayer mediaPlayer, mediaPlayerShubuh;

    public AdzanService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SERVICE", "SERVICE EXECUTED");
        int delay = 0;
        int period = 60000;

        createNotificationChannel();

        sholat = getResources().getString(R.string.waktu_sholat);

        mediaPlayer = MediaPlayer.create(this, R.raw.adzan);
        mediaPlayerShubuh = MediaPlayer.create(this, R.raw.adzan_shubuh);

        preferences = new SmartMuslimPreferences(this);

        Calendar calendar = Calendar.getInstance();
        final int tanggal = calendar.get(Calendar.DAY_OF_MONTH);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Intent notifyUpdateUI = new Intent(MainActivity.ACTION_UPDATE_UI);
                sendBroadcast(notifyUpdateUI);

                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                String waktu = formatter.format(new Date());
                Jadwal jadwalSholat = preferences.getJadwalList("JADWAL").get(tanggal);
                Log.i("WAKTU DIMULAI", "WAKTU: "+waktu);
                if (waktu.equals("14:49")){
                    Log.i("WAKTU", "MASUK PADA WAKTU: "+waktu);
                    sholat = "BISMILLAH TEST NOTIFICATION";
                    showNotification();
                    mediaPlayer.start();
                }
                if (waktu.equals(jadwalSholat.getShubuh())){
                    sholat = String.format(getString(R.string.waktu_sholat), "Shubuh");
                    showNotification();
                    mediaPlayerShubuh.start();
                } else if (waktu.equals(jadwalSholat.getDhuhur())){
                    sholat = String.format(getString(R.string.waktu_sholat), "Dhuhur");
                    showNotification();
                    mediaPlayer.start();
                } else if (waktu.equals(jadwalSholat.getAshar())){
                    sholat = String.format(getString(R.string.waktu_sholat), "Ashar");
                    showNotification();
                    mediaPlayer.start();
                } else if (waktu.equals(jadwalSholat.getMaghrib())){
                    sholat = String.format(getString(R.string.waktu_sholat), "Maghrib");
                    showNotification();
                    mediaPlayer.start();
                } else if (waktu.equals(jadwalSholat.getIsya())){
                    sholat = String.format(getString(R.string.waktu_sholat), "Isya");
                    showNotification();
                    mediaPlayer.start();
                }

            }
        }, delay, period);

        return START_STICKY;
    }

    private void showNotification(){
        Log.i("WAKTU", "DIJALANKAN PADA WAKTU: "+new SimpleDateFormat("HH:mm").format(new Date()));
        Intent intent = new Intent(this, MainActivity.class);
//        Intent intent = new Intent(MainActivity.ACTION_STOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.setAction(MainActivity.ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        String channelId = getString(R.string.channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_description_48dp)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(sholat)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
//                .addAction(R.drawable.ic_close, "STOP", pendingIntent);
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(100, builder.build());
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            String channelId = getString(R.string.channel_id);
            CharSequence name = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}

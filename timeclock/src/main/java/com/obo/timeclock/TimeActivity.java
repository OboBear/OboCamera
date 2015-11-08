package com.obo.timeclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeActivity extends AppCompatActivity {

    TextView timeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        timeText = (TextView) findViewById(R.id.timeText);

        new firstTimeThread().start();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new timeThread().start();
            }
        },intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_time, menu);
        return true;
    }

    class timeThread extends Thread{

        @Override
        public void run(){
            int num = 0;
            while (num<60*1000)
            {
                num++;
                try {
                    sleep((long) (1.0*1000/1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getTime();
            }

        }
    }

    class firstTimeThread extends Thread{

        @Override
        public void run(){
            int num = 0;
            String sTimeZoneString = "GMT+8:00";
            Calendar cal = Calendar.getInstance(TimeZone
                    .getTimeZone(sTimeZoneString));
            int millisecond = cal.get(Calendar.MILLISECOND);
            int second = cal.get(Calendar.SECOND);
            num = second*1000+millisecond;
            while (num<60*1000)
            {
                num++;
                try {
                    sleep((long) (1.0*1000/1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getTime();
            }

        }
    }

    Handler handler = new Handler();

    void getTime()
    {
        String sTimeZoneString = "GMT+8:00";
        Calendar cal = Calendar.getInstance(TimeZone
                .getTimeZone(sTimeZoneString));
        final int hour = cal.get(Calendar.HOUR);
        final int minute = cal.get(Calendar.MINUTE);
        final int second = cal.get(Calendar.SECOND);
        final int millisecond = cal.get(Calendar.MILLISECOND);
        handler.post(new Runnable() {
            @Override
            public void run() {
                timeText.setText("" + hour + ":" + minute + ":" + second+":"+millisecond);
            }
        });
    }

}

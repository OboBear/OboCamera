package com.obo.takephoto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

public class TakePhotoActivity extends AppCompatActivity {

    File ef = Environment.getExternalStorageDirectory();

    EditText hourEdit, minuteEdit, secondEdit;
    TextView time_text;

    TextView textView;

    RelativeLayout relative;

    PhotoSuerfaceView camera_surfaceview;

    NumberPicker hourPicker, minutePicker, secondPicker;

    LinearLayout pick_layout;

    int choosedHour = -1, choosedMinute = -1, choosedSecond = -1;
    int currentHour = 0, currentMinute = 0, currentSecond = 0;
    boolean photoFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, // 设置全屏显示
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        camera_surfaceview = (PhotoSuerfaceView) findViewById(R.id.camera_surfaceview);

        textView = (TextView) findViewById(R.id.textView);
        time_text = (TextView) findViewById(R.id.time_text);

        hourEdit = (EditText) findViewById(R.id.edit_hour);
        minuteEdit = (EditText) findViewById(R.id.edit_minute);
        secondEdit = (EditText) findViewById(R.id.edit_second);
        relative = (RelativeLayout) findViewById(R.id.relative);


        //计时器
        new FirstTimeThread().start();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new TimeThread().start();
            }
        }, intentFilter);
        getTime();
        init();
    }


    private void init() {

        pick_layout = (LinearLayout) findViewById(R.id.pick_layout);
        pick_layout.setTranslationY(1000);

        hourPicker = (NumberPicker) findViewById(R.id.hourpicker);
        minutePicker = (NumberPicker) findViewById(R.id.minuteicker);
        secondPicker = (NumberPicker) findViewById(R.id.secondpicker);

        hourPicker.setMaxValue(11);
        hourPicker.setMinValue(0);
        hourPicker.setValue(9);

        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);
        minutePicker.setValue(49);

        secondPicker.setMaxValue(60);
        secondPicker.setMinValue(0);
        secondPicker.setValue(49);

        hourPicker.setValue(currentHour);
        minutePicker.setValue(currentMinute);
        secondPicker.setValue(currentSecond);

        time_text.setText("" + getNumberString(currentHour) + ":" + getNumberString(currentMinute) + ":" + getNumberString(currentSecond));
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                if (pick_layout.getTranslationY() == 0) {
                    pick_layout.setTranslationY(1000);
                    choosedHour = hourPicker.getValue();
                    choosedMinute = minutePicker.getValue();
                    choosedSecond = secondPicker.getValue();
                    time_text.setText(getNumberString(choosedHour) + ":" + getNumberString(choosedMinute) + ":" + getNumberString(choosedSecond));
                }
                if (choosedHour < 0) {
                    Toast.makeText(this, "请选择时间", Toast.LENGTH_SHORT).show();
                    return;
                }
                photoFlag = true;
                break;

            case R.id.tip:
            case R.id.time_text:

                if (pick_layout.getTranslationY() == 0) {
                    pick_layout.setTranslationY(1000);
                    choosedHour = hourPicker.getValue();
                    choosedMinute = minutePicker.getValue();
                    choosedSecond = secondPicker.getValue();
                    time_text.setText(getNumberString(choosedHour) + ":" + getNumberString(choosedMinute) + ":" + getNumberString(choosedSecond));
                } else {
                    pick_layout.setTranslationY(0);
                }

                break;
        }
    }

    class TimeThread extends Thread {

        @Override
        public void run() {
            int num = 0;
            while (num < 60 * 1000) {
                num++;
                try {
                    sleep((long) (1.0 * 1000 / 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getTime();
            }

        }
    }

    class FirstTimeThread extends Thread {

        @Override
        public void run() {
            int num = 0;
            String sTimeZoneString = "GMT+8:00";
            Calendar cal = Calendar.getInstance(TimeZone
                    .getTimeZone(sTimeZoneString));
            int millisecond = cal.get(Calendar.MILLISECOND);
            int second = cal.get(Calendar.SECOND);
            num = second * 1000 + millisecond;
            while (num < 60 * 1000) {
                num++;
                try {
                    sleep((long) (1.0 * 1000 / 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getTime();
            }
        }
    }

    Handler handler = new Handler();

    void getTime() {
        String sTimeZoneString = "GMT+8:00";
        Calendar cal = Calendar.getInstance(TimeZone
                .getTimeZone(sTimeZoneString));

        currentHour = cal.get(Calendar.HOUR);
        currentMinute = cal.get(Calendar.MINUTE);
        currentSecond = cal.get(Calendar.SECOND);
        handler.post(new Runnable() {
            @Override
            public void run() {
                textView.setText("" + getNumberString(currentHour) + ":" + getNumberString(currentMinute) + ":" + getNumberString(currentSecond));
            }
        });

        int hh = this.choosedHour;
        if (this.choosedHour > 12) {
            hh -= 12;
        }
        if (photoFlag && hh == currentHour && this.choosedMinute == currentMinute && this.choosedSecond == currentSecond) {
            photoFlag = false;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    camera_surfaceview.takePhoto();

                }
            });
        }
    }


    String getNumberString(int number) {
        return ("" + (number > 10 ? number : ("0") + number));
    }

}

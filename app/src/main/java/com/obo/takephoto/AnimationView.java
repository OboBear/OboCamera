package com.obo.takephoto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by obo on 15/11/8.
 */
public class AnimationView extends View {
    private final  String TAG = AnimationView.class.getCanonicalName();
    Context context;

    public final static String ACTION_FOCUS = "action.focus";
    public final static String ACTION_SCALE_ON = "action.scaleOn";
    public final static String ACTION_SCALE_OFF = "action.scaleOff";

    PointF focusPoint = null;
    PointF centerPoint;
    boolean showFocusPoint = false;
    boolean showScale = false;

    Paint paint;

    int focusWidth = 0;

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        paint = new Paint();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FOCUS);
        intentFilter.addAction(ACTION_SCALE_ON);
        intentFilter.addAction(ACTION_SCALE_OFF);

        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            //聚焦
            if (action.equals(ACTION_FOCUS)){
                Bundle bundle = intent.getBundleExtra("data");
                focusPoint = (PointF) bundle.get("point");
                if (focusPoint != null)
                {
                    if (!showFocusPoint)
                    {
                        new FocusThread().start();
                    }
                }
            }
            else if(action.equals(ACTION_SCALE_ON)) {
                showScale = true;
                if (centerPoint == null)
                {
                    centerPoint = new PointF(getWidth()/2,getHeight()/2);
                }
                postInvalidate();
            }
            else if (action.equals(ACTION_SCALE_OFF)){
                showScale = false;
                postInvalidate();
            }

        }
    };

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        if (showFocusPoint)
        {
            drawRect(canvas,focusPoint,Color.WHITE,focusWidth);
        }
        if (showScale)
        {
            drawRect(canvas,centerPoint,Color.GREEN,130);
        }
    }


    void drawRect(Canvas canvas,PointF pointCenter,int color,int width)
    {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(3);
        canvas.drawRoundRect(new RectF(pointCenter.x - width, pointCenter.y - width, pointCenter.x + width, pointCenter.y + width), 4, 4, paint);
        canvas.drawLine(pointCenter.x - 30, pointCenter.y, pointCenter.x + 30, pointCenter.y, paint);
        canvas.drawLine(pointCenter.x,pointCenter.y-30,pointCenter.x,pointCenter.y+30,paint);
    }


    class FocusThread extends Thread{

        @Override
        public void run(){
            focusWidth = 100;
            showFocusPoint = true;
            while (focusWidth >50)
            {
                focusWidth -- ;
                postInvalidate();
                try {
                    sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            showFocusPoint = false;
            postInvalidate();
        }
    }


    @Override
    public void finalize(){
        context.unregisterReceiver(broadcastReceiver);
    }

}

package com.obo.takephoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by obo on 15/11/3.
 */
public class PhotoSuerfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public String TAG = PhotoSuerfaceView.class.getCanonicalName();

    Context context;
    File ef = Environment.getExternalStorageDirectory();
    Camera camera;

    public PhotoSuerfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        String t = Intent.ACTION_TIMEZONE_CHANGED;
        SurfaceHolder holder = this.getHolder();
        holder.setFixedSize(176, 155);// 设置分辨率
        holder.setKeepScreenOn(true);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        Log.i("i", params.flatten());
        params.setJpegQuality(100);  // 设置照片的质量
        params.setPictureSize(1024, 768);
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        Camera.Parameters mParameters = camera.getParameters();
        List<Camera.Size> size = mParameters.getSupportedPictureSizes();
        if (size.size() > 0) {
            params.setPictureSize(size.get(0).width, size.get(0).height);
        }
        camera.setParameters(params); // 将参数设置给相机
        camera.setDisplayOrientation(90);
        // 设置预览显示
        try {
            camera.setPreviewDisplay(PhotoSuerfaceView.this.getHolder());
            // 开启预览
            camera.startPreview();

            Log.i("", "open success");

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("", "open fail");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void takePhoto() {
        camera.takePicture(shutterCallback, rawCallback, pictureCallback);
    }


    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {}
    };
    private Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
/* 如需要处理 raw 则在这里 写代码 */
        }
    };

    //当拍照后 存储 jpg 文件到 sd卡
    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream outSteam = null;
            try {

                File file = new File(ef.getAbsolutePath() + "/" + "3DPhotos");
                if (!file.exists()) {
                    file.mkdirs();
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String times = format.format((new Date()));
                outSteam = new FileOutputStream(file.getAbsolutePath() + "/" + times + ".jpg");
                outSteam.write(data);
                outSteam.close();
                camera.startPreview();


                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                context.sendBroadcast(intent);

                Toast.makeText(context, "照片存到:"+file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {
                Log.d("Camera", "row");

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    };

    double lastDiff = 0;

    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "MotionEvent ACTION_DOWN");
                return true;
            case MotionEvent.ACTION_MOVE: {
                Log.i(TAG, "MotionEvent ACTION_MOVE");
                if (e.getPointerCount() != 2)
                    return true;
                double currentDiff = getDistance(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1));
                if (lastDiff != 0) {
                    Camera.Parameters cameraParameters = camera.getParameters();
                    int zoom = cameraParameters.getZoom();
                    int scale = (int) (currentDiff - lastDiff);

                    zoom += scale / 15;
                    if (zoom > 0 && zoom <= cameraParameters.getMaxZoom()) {
                        cameraParameters.setZoom(zoom);
                        camera.setParameters(cameraParameters);
                    }
                }
                lastDiff = currentDiff;
            }
            break;

            case MotionEvent.ACTION_UP:
                lastDiff = 0;
                Log.i(TAG, "MotionEvent ACTION_UP");

                //设置焦点的区域
                Camera.Parameters params = camera.getParameters();
                List<Camera.Area> focusArea = new ArrayList< Camera.Area>();
                int rateWidth = -1000 + (int) (e.getY()/params.getPreviewSize().width*2000);
                int rateHeight = -1000 + (int) (e.getX()/params.getPreviewSize().height*2000);
                Rect areaRect = new Rect(new Rect(rateWidth-10,rateHeight-10,rateWidth+10,rateHeight+10));
                if(areaRect.left <= -1000)
                {
                    areaRect.left = -999;
                }
                if(areaRect.top <= -1000)
                {
                    areaRect.top = -999;
                }
                if(areaRect.right >= 1000)
                {
                    areaRect.right = 999;
                }
                if(areaRect.bottom >= 1000)
                {
                    areaRect.bottom = 999;
                }
                Log.i(TAG,"previewSize:"+areaRect.left+":"+areaRect.top+":"+areaRect.right+":"+areaRect.bottom);

                focusArea.add(new Camera.Area(areaRect,200));
                params.setFocusAreas(focusArea);
                camera.setParameters(params);

                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                    }
                });
                break;
        }
        return true;
    }

    double getDistance(float difX, float difY) {
        return Math.sqrt(difX * difX + difY * difY);
    }


}

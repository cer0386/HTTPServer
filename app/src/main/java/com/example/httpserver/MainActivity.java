package com.example.httpserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketServer s;
    private static final int READ_EXTERNAL_STORAGE = 1;

    private Camera mCamera;
    private CameraPreview mPreview;

    static final int TASK_COMPLETE = 4;
    private TextView tv1;
    int permits;
    Timer t;
    TimerTask timerTask;
    static byte[] picture;

    public  Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            CharSequence m = msg.getData().getCharSequence("MAINLIST").toString();
            Log.d("SRV", m.toString());


            tv1.append(m);
            tv1.append("\n");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = (TextView) findViewById(R.id.textView);
        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        tv1.setMovementMethod(new ScrollingMovementMethod());
        tv1.setText("DATA TRANSMITTED \n");


        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        t = new Timer();

                        timerTask= new TimerTask() {
                            @Override
                            public void run() {

                                mCamera.startPreview();
                                mCamera.takePicture(null, null, mPicture);

                            }
                        };

                        //t.schedule(timerTask, 5000);
                        t.scheduleAtFixedRate(timerTask, 0, 1000);
                    }
                }
        );

        Button stopButton = (Button) findViewById(R.id.button_stop);
        stopButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        t.cancel();
                    }
                }
        );


    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            /*String pathSd = Environment.getExternalStorageDirectory().getAbsolutePath();
            File pictureFile = new File(pathSd +"/Pictures/aaa.jpeg");
            if (pictureFile == null){
                Log.d("PIC", "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("PIC", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("PIC", "Error accessing file: " + e.getMessage());
            }*/

            if(data == null)
            {
                return;
            }
            picture = Arrays.copyOf(data, data.length);
        }
    };



    @Override
    public void onClick(View v) {

        EditText tv2 = findViewById(R.id.clientsNumber);
        permits = Integer.parseInt(tv2.getText().toString());

        if (v.getId() == R.id.button1) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            Log.d("SRV", "onClick: "+ PackageManager.PERMISSION_GRANTED );
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            } else {
                s = new SocketServer(myHandler, permits);
                s.start();
            }
        }
        if (v.getId() == R.id.button2) {
            s.close();
            try {
                s.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case READ_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    s = new SocketServer(myHandler, permits);
                    s.start();
                }
                break;

            default:
                break;
        }
    }
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }





}
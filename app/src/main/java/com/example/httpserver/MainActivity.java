package com.example.httpserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketServer s;
    private static final int READ_EXTERNAL_STORAGE = 1;

    static final int TASK_COMPLETE = 4;
    private TextView tv1;
    private static MainActivity sInstance = null;
    //public Handler handler;

    static {

        // Creates a single static instance of MainActivity
        sInstance = new MainActivity();
    }

    public static Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            Log.d("SRV", msg.getData().getCharSequence("MAINLIST").toString());
            //MainActivity.getInstance().setContentView(msg);
        }
    };


    public void setContentView(Message msg){
        //tv1 = (TextView) findViewById(R.id.textView);
        tv1.setText(msg.obj.toString());
    }
    /*private MainActivity () {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                TextView tv1 = findViewById(R.id.textView);
                //Get clientThread task, from incoming message object
                ClientThread clientThread = (ClientThread) inputMessage.obj;
                String sMsg = clientThread.msg;
                tv1.setText(sMsg);
            }
        };
    }*/

    /*public void handleState(ClientThread cT, int state){
        switch (state){
            case TASK_COMPLETE:

                String zprava = cT.getMsg();
                //Message completeMessage = handler.obtainMessage(state, cT);
                Message completeMessage = new Message();
                completeMessage.obj = cT;
                //tv1.setText(zprava);
                //completeMessage.sendToTarget();
                MainActivity.getInstance().handler.handleMessage(completeMessage);
                break;
            // In all other cases, pass along the message without any other action.
            default:
                handler.obtainMessage(state, cT).sendToTarget();
                break;
        }
    }*/

    public static MainActivity getInstance(){
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = (TextView) findViewById(R.id.textView);
        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        /*handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                //Get clientThread task, from incoming message object
                ClientThread clientThread = (ClientThread) inputMessage.obj;
                String sMsg = clientThread.getMsg();
                tv1.setText(sMsg);
                Log.d("SRV", "handleMessage: " + sMsg);

            }
        };*/
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button1) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            Log.d("SRV", "onClick: "+ PackageManager.PERMISSION_GRANTED );
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            } else {
                s = new SocketServer();
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
                    s = new SocketServer();
                    s.start();
                }
                break;

            default:
                break;
        }
    }



}
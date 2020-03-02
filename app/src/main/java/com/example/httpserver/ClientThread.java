package com.example.httpserver;

import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ClientThread extends Thread {

    Socket s;
    String msg;
    private static MainActivity mainActivity;

    public ClientThread(Socket s) {
        this.s = s;
        // Gets a handle to the object that creates the thread pools
        mainActivity = MainActivity.getInstance();
    }

    @Override
    public void run(){

        try{
            OutputStream o = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String pathSd = Environment.getExternalStorageDirectory().getAbsolutePath();
            String sdPath = pathSd + "/OSMZ";
            Log.d("SRV", "absolute path: " + sdPath);

            String tmp = in.readLine();
            String[] pole;
            String uri = "";

            if(tmp != null)
            {
                pole = tmp.split("[\\s+]");
                uri = pole[1];
            }

            Log.d("SRV", "URI: -" + uri + "-");
            String path = sdPath+uri;


            File file = new File(path);
            Log.d("SRV", "file: " + file.getAbsolutePath() + " exists: "+file.exists() + " path: " +file.getPath());
            //TODO zobrazení obrazku a ruzncyh file typu
            if(file.exists()){
                if(file.isFile()) {
                    if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                        out.write("HTTP/1.0 200 OK\n" +
                                "Content-Type: " + getFileType(path) + "\n"+
                                "Content-Length: " + file.length() + "\n" +
                                "\n");
                        out.flush();

                        msg = "URI : "+ uri + "\n Content type: "+ getFileType(path) +"\n Size: "+file.length();
                        //handleState(mainActivity.TASK_COMPLETE);
                        sendMsg(msg);


                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] fileBytes = new byte[2048];
                        while (fileInputStream.read(fileBytes) != 0) {
                            o.write(fileBytes);
                        }
                        o.flush();
                    }
                    BufferedReader reader;
                    reader = new BufferedReader( new FileReader(file));
                    String line;
                    out.write("HTTP/1.0 200 OK\n" +
                            "Content-Type: text/html\n"+
                            "\n");
                    out.flush();

                    msg = "URI : "+ uri + "\n Content type: "+ getFileType(path) +"\n Size: "+file.length();
                    //handleState(mainActivity.TASK_COMPLETE);
                    sendMsg(msg);

                    while((line = reader.readLine()) != null){
                        out.write(line+"\n");
                    }
                    out.flush();
                    reader.close();

                }
                else{ //vypíše obsah složky sdcard

                    File directory = new File(pathSd +"/");
                    File[] files = directory.listFiles();

                    String resultList = "";
                    out.write("HTTP/1.0 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>\n" +
                            "<body>\n");
                    for(int i = 0; i < files.length; i++)
                    {
                        resultList += "<h2>" + files[i].getName()+ "</h2>\n";
                    }
                    resultList += "</body>\n" +
                            "</html>";
                    out.write(resultList);
                    out.flush();

                    msg = "Directory";
                    //handleState(mainActivity.TASK_COMPLETE);
                    sendMsg(msg);

                    Log.d("List", resultList);
                }
            }

            else{
                out.write("HTTP/1.0 404 Not Found \n" +
                        "Content-Type: text/html\n" +
                        "\n" +
                        "<html>\n" +
                        "<body>\n" +
                        "<h1>404 Not Found</h1>");
                out.flush();
                msg = "Not found";
                //handleState(mainActivity.TASK_COMPLETE);
                sendMsg(msg);
            }

            s.close();
            Log.d("SRV", "Socket Closed");

        }
        catch (IOException e) {

            Log.d("SRV", "Error");
            e.printStackTrace();
        }
    }
    public static String getFileType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        Log.d("SRV", "getFileType: " + type);
        return type;
    }


    // Passes the state to MainActivity
    /*void handleState(int state) {

        Message mes = new Message();
        mes.obj = this;
        mainActivity.handler.handleMessage(mes);
    }*/

    public String getMsg(){
        return msg;
    }

        private void sendMsg(String m){
            Message ms = MainActivity.myHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putCharSequence("MAINLIST", m);
            ms.setData(bundle);
            MainActivity.myHandler.sendMessage(ms);
        }
}
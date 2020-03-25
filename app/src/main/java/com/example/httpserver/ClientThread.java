package com.example.httpserver;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import java.util.concurrent.Semaphore;

public class ClientThread extends Thread {

    Socket s;
    String msg;
    Handler myHandler;
    Semaphore sem;

    public ClientThread(Socket s, Handler h, Semaphore se) {
        this.s = s;
        // Gets a handle to the object that creates the thread pools
        myHandler = h;
        sem = se;
    }

    @Override
    public void run(){

        try{
            OutputStream o = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String pathSd = Environment.getExternalStorageDirectory().getAbsolutePath();
            String sdPath = pathSd + "/OSMZ";
            String sdPicPath = pathSd +"/Pictures";
            //Log.d("SRV", "absolute path: " + sdPath);

            String tmp = in.readLine();
            String[] pole;
            String uri = "";

            if(tmp != null)
            {
                pole = tmp.split("[\\s+]");
                uri = pole[1];
            }
            if(uri.contains("?rand")){
                Log.d("SRV", "URI BEFORE: -" + uri + "-");
                StringBuilder temp = new StringBuilder();
                for (char c : uri.toCharArray()) {
                    if(c == '?'){
                        break;
                    }
                    temp.append(c);
                }
                uri = temp.toString();
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
                        int i;
                        while ((i = fileInputStream.read(fileBytes)) != 0) {
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

                    String pathD = pathSd + uri;
                    File directory = new File(pathD +"/");
                    File[] files = directory.listFiles();

                    String resultList = "";
                    out.write("HTTP/1.0 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>\n" +
                            "<body>\n");
                    resultList += "<ul>\n";
                    for(int i = 0; i < files.length; i++)
                    {
                        resultList += "<li><a href="+("/"+files[i].getName())+">"+files[i].getName()+"</a></li>";
                    }
                    resultList += "</ul>\n";
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
                path = pathSd + uri;
                file = new File(path);
                if(file.exists()){
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
                        int i;
                        while ((i = fileInputStream.read(fileBytes)) != 0) {
                            o.write(fileBytes);
                        }
                        o.flush();
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
            }

            s.close();
            Log.d("SRV", "Socket Closed");

        }
        catch (IOException e) {

            Log.d("SRV", "Error");
            e.printStackTrace();
        }
        finally {
            sem.release();
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

    public String getMsg(){
        return msg;
    }

        private void sendMsg(String m){
            Message ms = myHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putCharSequence("MAINLIST", m);
            ms.setData(bundle);
            myHandler.sendMessage(ms);
        }
}
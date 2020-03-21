package com.example.httpserver;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;

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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class SocketServer extends Thread {

    ServerSocket serverSocket;
    Handler handler;
    public final int port = 12345;
    boolean bRunning;
    Semaphore sem;
    int permits;

    public SocketServer(Handler h, int permits) {
        handler = h;
        sem = new Semaphore(permits);
        this.permits = permits;
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("" +
                    "SRV", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

    public void run() {
        try {
            Log.d("SRV", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;

            while (bRunning) {
                Log.d("SRV", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                Log.d("SRV", "Socket Accepted");

                OutputStream o = s.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                boolean bpermit = sem.tryAcquire(0, TimeUnit.SECONDS);
                //sem.acquire();
                if(bpermit) {
                    ClientThread clientThread = new ClientThread(s, handler, sem);
                    clientThread.start();
                }
                else{
                    Log.d("SRV", "SERVER IS BUSY");

                    String s1 = in.readLine();
                    out.write("HTTP/1.0 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "<html>\n " +
                            "<body>\n " +
                            "\n"+
                            "<h1>Server is busy</h1>\n " +
                            "</body>\n " +
                            "</html>");
                    out.flush();

                    out.close();
                    o.close();
                    s.close();
                }
            }
        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SRV", "Normal exit");
            else {
                Log.d("SRV", "Error");
                e.printStackTrace();
            }
        }
        catch (InterruptedException e){
            Log.d("SRV", "Interrupted acquire");
        }
        finally {
            serverSocket = null;
            bRunning = false;
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
        return type;
    }

}


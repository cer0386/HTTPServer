package com.example.httpserver;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import static com.example.httpserver.MainActivity.picture;

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
            //String sdPicPath = pathSd +"/Pictures";
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
            String path = sdPath+uri; //Cela cesta k souboru


            File file = new File(path);
            Log.d("SRV", "file: " + file.getAbsolutePath() + " exists: "+file.exists());


            if(uri.startsWith("/cgi-bin/")){

                String[] cmd = uri.substring("/cgi-bin/".length()).split("%");
                String temp = "";

                if(cmd.length > 1){
                    String[] params = Arrays.copyOfRange(cmd, 1, cmd.length);

                    StringBuilder tempB = new StringBuilder();
                    for(int i = 0; i < params.length; i++){
                        tempB.append(params[i]);
                        tempB.append(",");
                    }
                    temp = tempB.toString();
                    temp = temp.substring(0, temp.length() -1);
                    Log.d("PROC", "Argument: " + temp);
                }

                try{
                    ProcessBuilder pb;
                    if(cmd.length > 1){
                        pb = new ProcessBuilder(cmd[0], temp);
                    }
                    else{
                        pb = new ProcessBuilder(cmd);
                    }

                    final Process p = pb.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    out.write("HTTP/1.0 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>"+
                            "<body>"+
                            "<h1>Vypis CGI skriptu " + uri+"</h1>");
                    out.flush();

                    String line;
                    boolean swap = false;
                    while ((line = br.readLine())!=null){
                        if(swap){
                            out.write( "<p>"+ line + "<p/>\n");
                            swap = false;
                        }
                        else{
                            out.write( "<p><b>"+ line + "</b><p/>\n");
                            swap = true;
                        }
                    }
                    out.flush();

                    out.write("</body>\n" +
                            "</html>");
                    out.flush();

                }
                catch (Exception e){
                    Log.d("PROC", "Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else if(uri.equals("camera/stream") ){
                out.write("HTTP/1.0 200 OK\n" +
                        "Content-Type: multipart/x-mixed-replace; boundary=\"OSMZ_boundary\"\n" +
                        //"Content-Length: " + picture.length + "\n" +
                        "\n");

                msg = "URI : " + uri + "\n Content type: multipart/x-mixed-replace\n Size: " + picture.length;
                Log.d("PIC", "MJPEG STREAM...");
                sendMsg(msg);

                out.flush();
            }
            else if(uri.equals("/camera/snapshot") && picture != null){
                out.write("HTTP/1.0 200 OK\n" +
                        "Content-Type: image/jpeg\n" +
                        "Content-Length: " + picture.length + "\n" +
                        "\n");
                out.flush();

                msg = "URI : " + uri + "\n Content type: image/jpeg \n Size: " + picture.length;
                sendMsg(msg);

                ByteArrayInputStream bai = new ByteArrayInputStream(picture);

                int ch;
                while((ch = bai.read()) != -1)
                {
                    o.write(picture);
                }

                o.flush();
            }
            else {
                if (file.exists()) {
                    if (file.isFile()) {
                        if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) { // čistě obrázek
                            out.write("HTTP/1.0 200 OK\n" +
                                    "Content-Type: " + getFileType(path) + "\n" +
                                    "Content-Length: " + file.length() + "\n" +
                                    "\n");
                            out.flush();

                            msg = "URI : " + uri + "\n Content type: " + getFileType(path) + "\n Size: " + file.length();
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
                        reader = new BufferedReader(new FileReader(file));
                        String line;
                        out.write("HTTP/1.0 200 OK\n" +
                                "Content-Type: text/html\n" +
                                "\n");
                        out.flush();

                        msg = "URI : " + uri + "\n Content type: " + getFileType(path) + "\n Size: " + file.length();
                        //handleState(mainActivity.TASK_COMPLETE);
                        sendMsg(msg);

                        while ((line = reader.readLine()) != null) {
                            out.write(line + "\n");
                        }
                        out.flush();
                        reader.close();

                    }
                    else { //vypíše obsah složky sdcard

                        String pathD = pathSd + uri;
                        File directory = new File(pathD + "/");
                        File[] files = directory.listFiles();

                        String resultList = "";
                        out.write("HTTP/1.0 200 OK\n" +
                                "Content-Type: text/html\n" +
                                "\n" +
                                "<html>\n" +
                                "<body>\n");
                        resultList += "<ul>\n";
                        for (int i = 0; i < files.length; i++) {
                            resultList += "<li><a href=" + ("/" + files[i].getName()) + ">" + files[i].getName() + "</a></li>";
                            Log.d("TST", "Name: "+files[i].getName());
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
                else { // jestli ten obrázek je jinde než v /sdcard/OSMZ/...
                    path = pathSd + uri;
                    file = new File(path);
                    if (file.exists()) {
                        if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                            out.write("HTTP/1.0 200 OK\n" +
                                    "Content-Type: " + getFileType(path) + "\n" +
                                    "Content-Length: " + file.length() + "\n" +
                                    "\n");
                            out.flush();

                            msg = "URI : " + uri + "\n Content type: " + getFileType(path) + "\n Size: " + file.length();
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
                        else{
                            String pathD = pathSd + uri;
                            File directory = new File(pathD + "/");
                            File[] files = directory.listFiles();
                            String resultList = "";
                            out.write("HTTP/1.0 200 OK\n" +
                                    "Content-Type: text/html\n" +
                                    "\n" +
                                    "<html>\n" +
                                    "<body>\n");
                            resultList += "<ul>\n";
                            for (int i = 0; i < files.length; i++) {
                                resultList += "<li><a href=" + ("/" + files[i].getName()) + ">" + files[i].getName() + "</a></li>";
                                Log.d("TST", "Name: "+files[i].getName());
                            }
                            resultList += "</ul>\n";
                            resultList += "</body>\n" +
                                    "</html>";
                            out.write(resultList);
                            out.flush();

                            msg = "Directory";
                            //handleState(mainActivity.TASK_COMPLETE);
                            sendMsg(msg);
                        }
                    }
                    else {

                            out.write("HTTP/1.0 404 Not Found \n" +
                                    "Content-Type: text/html\n" +
                                    "\n" +
                                    "<html>\n" +
                                    "<body>\n" +
                                    "<h1>404 Not Found</h1>" +
                                    "</body>" +
                                    "</html>");
                            out.flush();
                            msg = "Not found";
                            sendMsg(msg);

                    }
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
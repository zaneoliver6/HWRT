package edu.sta.uwi.hwrt;


import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileUploader  {

    private File f;
    private int resp;

    public FileUploader(File file) {
        f = file;
    }

    public int uploadFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
               resp = upload();
            }
        }).start();

        return resp;
    }

    public int upload() {

        int ServerResponseCode = 0;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {

            FileInputStream fileInputStream = new FileInputStream(f);
            URL url = new URL("http://40.71.219.226/upload");

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection","Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("file", f.getName());

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + f.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable,maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer,0,bufferSize);
            while (bytesRead > 0) {
                dos.write(buffer,0,bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            ServerResponseCode = conn.getResponseCode();
            if (ServerResponseCode == 200) {
                System.out.println("File Uploaded");
            }

            fileInputStream.close();
            dos.flush();
            dos.close();


        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        f.delete();

        return ServerResponseCode;

    }

}

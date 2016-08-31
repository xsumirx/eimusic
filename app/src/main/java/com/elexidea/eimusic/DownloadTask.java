package com.elexidea.eimusic;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sumir on 8/29/2016.
 */
public class DownloadTask implements Runnable {


    GlobalData globalData;
    private static final int BUFFER_SIZE = 4096;
    DownloadObject downloadObject;

    public DownloadTask(DownloadObject param)
    {
            downloadObject = param;
    }

    @Override
    public void run() {

        globalData = GlobalData.getInstance();
        downloadObject.STATUS = Constants.DOWNLOAD_STATUS_RUNNING;

        URL url;
        HttpURLConnection httpConn = null;
        int responseCode = -1;
        try {
            url = new URL(downloadObject.url);
            httpConn = (HttpURLConnection) url.openConnection();
            responseCode = httpConn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            downloadObject.STATUS = Constants.DOWNLOAD_STATUS_FAILED;
        }


        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {

            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            downloadObject.size = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    downloadObject.fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                downloadObject.fileName = downloadObject.url.substring(downloadObject.url.lastIndexOf("/") + 1,
                        downloadObject.url.length());
            }

            //System.out.println("Content-Type = " + contentType);
            //System.out.println("Content-Disposition = " + disposition);
            //System.out.println("Content-Length = " + contentLength);
            //System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection

            try {
                //publishProgress(downloadObject);
                InputStream inputStream = httpConn.getInputStream();

                File path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                File f = new File(path, "/" + downloadObject.fileName);
                // opens an output stream to save into file
                if(f.exists())
                {
                    if(!f.delete()) {
                        downloadObject.fileName = "e" + downloadObject.fileName;
                        f = new File(path, "/" + downloadObject.fileName);
                    }
                }



                FileOutputStream outputStream = new FileOutputStream(f);

                int bytesRead = -1;
                downloadObject.sizeDownloaded = 0;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    downloadObject.sizeDownloaded += bytesRead;
                    //publishProgress(downloadObject);
                    //System.out.println("File Progress : "+String.valueOf(progress));
                    if(Thread.interrupted())
                    {
                        outputStream.flush();
                        outputStream.close();
                        inputStream.close();
                        f.delete();
                        throw new InterruptedException("User Stoped");
                    }
                }
                downloadObject.STATUS = Constants.DOWNLOAD_STATUS_SUCCESS;

                if(globalData.listDownloading.remove(downloadObject))
                {
                    globalData.pushDownloadingList();
                    globalData.listSavedObject.add(new SavedObject(downloadObject.fileName,downloadObject.size,downloadObject.location));
                    globalData.pushSavedList();
                }

                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                downloadObject.STATUS = Constants.DOWNLOAD_STATUS_FAILED;
            } catch (InterruptedException e) {
                e.printStackTrace();
                downloadObject.STATUS = Constants.DOWNLOAD_STATUS_FAILED;
            }


            //System.out.println("File downloaded");
        } else {
            //System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            downloadObject.STATUS = Constants.DOWNLOAD_STATUS_FAILED;
        }
        httpConn.disconnect();
    }
}

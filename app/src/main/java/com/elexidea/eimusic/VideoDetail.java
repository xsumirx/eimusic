package com.elexidea.eimusic;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.renderscript.Element;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.fasterxml.jackson.core.JsonParser;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.zip.Inflater;

public class VideoDetail extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    Button btnDownloadMP3;
    TextView txtViewVideoID;
    String videoTitle;
    String videoID;
    CircularProgressButton btnWithText;
    GlobalData globalData;
    String downloadUrl = "";

    final static int REQUEST_WRITE  = 1;
    final static int REQUEST_READ = 2;

    boolean isAllowedRead;
    boolean isAllowedWrite;

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    // YouTube player view
    private YouTubePlayerView youTubeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        videoTitle = "Default";
        globalData = GlobalData.getInstance();
        txtViewVideoID = (TextView) findViewById(R.id.txtViewVideoID);
        //btnDownloadMP3  = (Button)findViewById(R.id.btnDownloadMP3);

        btnWithText = (CircularProgressButton)findViewById(R.id.btnWithText);
        btnWithText.setIndeterminateProgressMode(true);
        btnWithText.setProgress(0);

        isAllowedRead = true;
        isAllowedWrite = true;


        btnWithText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (btnWithText.getProgress())
                {
                    case 0:
                    {
                        btnWithText.setProgress(50);
                        DownloadAsync s = new DownloadAsync();
                        s.execute(videoID, "1");
                        break;
                    }
                    case -1:
                    {
                        // retry Download
                        btnWithText.setProgress(50);
                        DownloadAsync s = new DownloadAsync();
                        s.execute(videoID, "1");
                        break;
                    }
                    case 100:
                    {
                        //Start Download

                        if(downloadUrl.startsWith("http://")) {
                            Intent i = new Intent(Constants.BROADCAST_LINK_GENRATED);
                            i.putExtra(Constants.DOWNLOAD_LINK, downloadUrl);
                            sendBroadcast(i);
                            finish();
                        }else
                        {
                            Toast.makeText(VideoDetail.this, "Download Couldn't Start, Please Try Again", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        break;
                    }
                    default:
                        break;
                }
            }
        });

        Bundle b = getIntent().getExtras();
        videoID = b.getString(Constants.VIDEO_ID_BUNDLE_KEY);
        videoTitle = b.getString(Constants.VIDEO_TITLE_BUNDLE_KEY);
        txtViewVideoID.setText(b.getString(Constants.VIDEO_TITLE_BUNDLE_KEY));
        registerReceiver(receiver, new IntentFilter(Constants.BROADCAST_LINK_GENRATED));


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            isAllowedWrite = false;
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }



        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            isAllowedRead = false;
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        // Initializing video player with developer key
        youTubeView.initialize(Constants.apiKey,this);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    isAllowedWrite = true;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            case REQUEST_READ:
            {

                if ((grantResults.length >0) && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    isAllowedRead = true;
                }

                break;
            }
            default:break;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            /*
            DownloadManager downmMan = (DownloadManager)getSystemService(getApplicationContext().DOWNLOAD_SERVICE);
            String link = intent.getStringExtra(Constants.DOWNLOAD_LINK);
            String tempUrl = Uri.decode(link);
            Uri uri = Uri.parse(tempUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOWNLOADS,videoTitle+".mp3");
            request.setVisibleInDownloadsUi(true);
            request.allowScanningByMediaScanner();
            downmMan.enqueue(request);
            finish();
            */

            if(!isAllowedRead || !isAllowedWrite) {
                Toast.makeText(VideoDetail.this, "You Denied File Permission ! Restart App", Toast.LENGTH_SHORT).show();
                return;
            }
            


            String link = intent.getStringExtra(Constants.DOWNLOAD_LINK);
            String tempUrl = Uri.decode(link);
            DownloadObject object = new DownloadObject(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),tempUrl);
            object.thread = new Thread(new DownloadTask(object));
            globalData.listDownloading.add(object);
            object.thread.start();
        }
    };

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {

            // loadVideo() will auto play video
            // Use cueVideo() method, if you don't want to play it automatically
            youTubePlayer.cueVideo(videoID);

            // Hiding player controls
            //youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        }

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }



    class DownloadAsync extends AsyncTask<String,String,String>
    {


        @Override
        protected String doInBackground(String... params) {
            Integer selectMethod = Integer.parseInt(params[1]);
            String title="";
            String MP3SKULL_urlQueueAndStatus = "https://mp3skull.onl/api/youtube/state?id="+params[0];//"http://www.youtubeinmp3.com/fetch/?format=json&video=http://www.youtube.com/watch?v="+params[0];
            String MP3SKULL_urlDownload = "http://serve01.mp3skull.onl/get?id="+params[0];
            switch (selectMethod) {
                case Constants.SELECTED_METHOD_MP3SKULL: {

                    long time = System.currentTimeMillis();
                    Document son = null;
                    boolean isFinished = false;
                    String jsonData;
                    int error = 0;
                    JsonObject mainObject = null;
                    JsonObject progressObject = null;
                    try {

                        do {
                            jsonData = Jsoup.connect(MP3SKULL_urlQueueAndStatus).ignoreContentType(true).execute().body();
                            mainObject = new com.google.gson.JsonParser().parse(jsonData).getAsJsonObject();
                            if(mainObject != null)
                            {
                                error = mainObject.get("error").getAsInt();
                                isFinished = mainObject.get("finished").getAsBoolean();
                                progressObject = mainObject.getAsJsonObject("progress");
                                if(progressObject != null)
                                {
                                    if(isFinished && error == 0)
                                    {
                                        title = progressObject.get("title").getAsString();
                                    }else
                                    {
                                        if(error != 0)
                                        {
                                            isFinished = true;
                                            title = progressObject.get("title").getAsString();
                                        }
                                    }
                                }else
                                {
                                    isFinished = true;
                                    error = 1;
                                    title = "Failed, Try Again";
                                }
                            }
                            else
                            {
                                isFinished = true;
                                error = 1;
                                title = "Failed, Try Again";
                            }

                            if((time + 120000) < System.currentTimeMillis())
                            {
                                isFinished = true;
                                error = 1;
                                title = "Failed, Timeout";
                            }
                            Thread.sleep(1000);
                        }while (!isFinished);

                        /*if(body == "" || body == null)
                        {
                            Link = son.head().getElementsByTag("meta").attr("content").toString().replace("0; url=", "");
                            son = Jsoup.connect(Link).get();
                            org.jsoup.nodes.Element e = son.body().getElementById("download");
                            if(e != null)
                            {
                                Link = "http://www.youtubeinmp3.com" + son.body().getElementById("download").attr("href").toString();
                            }
                            else
                            {
                                Link = "Problem";
                            }
                        }else
                        {
                            String[] urls = son.body().toString().split("\"");
                            for(String str:urls)
                            {
                                if(str.startsWith("http:"))
                                {
                                    Link = str.replace("\\","");
                                    break;
                                }
                            }


                        }

                        */
                        if(error == 0)
                            publishProgress(MP3SKULL_urlDownload,title);
                        else
                            publishProgress(title);

                    } catch (Exception e) {
                        e.printStackTrace();
                        publishProgress("Internal Error");
                    }

                    break;
                }

                default:
                    break;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[0].startsWith("http://"))
            {
                btnWithText.setProgress(100);
                downloadUrl = values[0];
                //Push a Broadcast
            }else
            {
                btnWithText.setProgress(-1);
                txtViewVideoID.setText(values[0]);
            }

        }

        @Override
        protected void onPostExecute(String integer) {


        }
    }

}

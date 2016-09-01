package com.elexidea.eimusic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class TagEDIT extends Activity {


    Button btnTagEdit;
    ImageView art;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_edit);

        art = (ImageView)findViewById(R.id.imgViewTagEditArt);

        btnTagEdit = (Button)findViewById(R.id.btnTagEditItunes);
        btnTagEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),ItunesArt.class);
                i.putExtra("TITLE","Karmin");
                startActivityForResult(i,1);
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                new ArtFecther().execute(data.getStringExtra("LINK"));
            }
        }
    }





    class ArtFecther extends AsyncTask<String,Integer,Integer>
    {

        @Override
        protected Integer doInBackground(String... params) {

            try {

                String queryTerm = params[0];

                if(bitmap != null)
                    bitmap.recycle();

                bitmap = BitmapFactory.decodeStream((InputStream) new URL(queryTerm.replace("600x600", "200x200")).getContent());
                publishProgress(0);

            } catch (IOException e) {
                System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
                publishProgress(1);
            } catch (Throwable t) {
                t.printStackTrace();
                publishProgress(1);
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(values[0] == 0) {
                art.setImageBitmap(bitmap);
                art.invalidate();
            }
        }
    }
}

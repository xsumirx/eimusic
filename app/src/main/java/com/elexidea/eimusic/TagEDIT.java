package com.elexidea.eimusic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v22Tag;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class TagEDIT extends Activity {


    Button btnArtItunes;
    Button btnArtGallery,btnArtLastFM;
    EditText editTextTitle,editTextAlbum,editTextArtist;
    CheckBox checkBoxRemoveArt;

    ImageButton imgButtonBack,imgButtonSave;

    ImageView art;
    Bitmap bitmap;

    GlobalData globalData;


    private int mPosition;

    private Mp3File mp3File;

    private boolean id3v1tag,id3v2tag,id3custom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_edit);

        art = (ImageView)findViewById(R.id.imgViewTagEditArt);
        imgButtonBack = (ImageButton)findViewById(R.id.imgButtonTagEditorBack);
        imgButtonSave = (ImageButton)findViewById(R.id.imgButtonTagEditorSave);

        btnArtGallery = (Button)findViewById(R.id.btnTagEditGallery);
        btnArtLastFM = (Button)findViewById(R.id.btnTagEditLastFM);
        btnArtItunes = (Button)findViewById(R.id.btnTagEditItunes);

        checkBoxRemoveArt = (CheckBox)findViewById(R.id.checkboxTagEditRemoveArt);

        editTextTitle = (EditText)findViewById(R.id.txtEditTagEditTitle);
        editTextAlbum  = (EditText)findViewById(R.id.txtEditTagEditAlbum);
        editTextArtist = (EditText)findViewById(R.id.txtEditTagEditArtist);


        globalData = GlobalData.getInstance();

        mPosition = getIntent().getIntExtra("INDEX",-1);
        if(mPosition == -1)
            finish();


        btnArtItunes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),ItunesArt.class);
                i.putExtra("TITLE","Karmin");
                startActivityForResult(i, 1);
            }
        });

        imgButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id3v1tag) {
                    //ID3v1Tag tag1 = (ID3v1Tag) mp3File.getId3v1Tag();
                    // tag1.setTitle(editTextTitle.getText().toString());
                    //tag1.setArtist(editTextArtist.getText().toString());
                    // tag1.setAlbum(editTextAlbum.getText().toString());
                }

                if (id3v2tag) {
                    String version = mp3File.getId3v2Tag().getVersion();
                    if (version.equals("4.0")) {
                        ID3v24Tag tag2 = (ID3v24Tag) mp3File.getId3v2Tag();
                        tag2.setTitle(editTextTitle.getText().toString());
                        tag2.setArtist(editTextArtist.getText().toString());
                        tag2.setAlbum(editTextAlbum.getText().toString());
                        if (bitmap != null) {
                            tag2.setAlbumImage(bitmap.getNinePatchChunk(), mp3File.getId3v2Tag().getAlbumImageMimeType());
                        }
                    } else if (version.equals("3.0")) {
                        ID3v23Tag tag2 = (ID3v23Tag) mp3File.getId3v2Tag();
                        tag2.setTitle(editTextTitle.getText().toString());
                        tag2.setArtist(editTextArtist.getText().toString());
                        tag2.setAlbum(editTextAlbum.getText().toString());
                        if (bitmap != null) {
                            tag2.setAlbumImage(bitmap.getNinePatchChunk(), mp3File.getId3v2Tag().getAlbumImageMimeType());
                        }

                    } else if (version.equals("2.0")) {
                        ID3v22Tag tag2 = (ID3v22Tag) mp3File.getId3v2Tag();
                        tag2.setTitle(editTextTitle.getText().toString());
                        tag2.setArtist(editTextArtist.getText().toString());
                        tag2.setAlbum(editTextAlbum.getText().toString());
                        if (bitmap != null) {
                            tag2.setAlbumImage(bitmap.getNinePatchChunk(), mp3File.getId3v2Tag().getAlbumImageMimeType());
                        }
                    }

                }

                SavedObject tempSavedObject = globalData.listSavedObject.get(mPosition);
                File path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                File Oldf = new File(path, "/" + tempSavedObject.fileName);

                File newF = new File(path, "/" + editTextTitle.getText().toString() + ".mp3");

                try {
                    mp3File.save(newF.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NotSupportedException e) {
                    e.printStackTrace();
                }

                Oldf.delete();
                tempSavedObject.fileName = editTextTitle.getText().toString() + ".mp3";
                globalData.pushSavedList();
                finish();
            }
        });


        try
        {

            SavedObject tempSavedObject = globalData.listSavedObject.get(mPosition);
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File f = new File(path, "/" + tempSavedObject.fileName);
            if(f.exists())
            {
                mp3File = new Mp3File(f);
                if(mp3File.hasId3v1Tag())
                    id3v1tag = true;
                else
                    id3v1tag = false;

                if(mp3File.hasId3v2Tag())
                    id3v2tag = true;
                else
                    id3v2tag = false;

                if(mp3File.hasCustomTag())
                    id3custom = true;
                else
                    id3custom = false;


                if(id3v1tag)
                {
                    editTextTitle.setText(mp3File.getId3v1Tag().getTitle());
                    if(editTextTitle.getText().toString() == "")
                        editTextTitle.setText(tempSavedObject.fileName.replace(".mp3", ""));
                    editTextArtist.setText(mp3File.getId3v1Tag().getArtist());
                    editTextAlbum.setText(mp3File.getId3v1Tag().getAlbum());
                }else
                {
                    if(id3v2tag)
                    {
                        editTextTitle.setText(mp3File.getId3v2Tag().getTitle());
                        if(editTextTitle.getText().toString() == "")
                            editTextTitle.setText(tempSavedObject.fileName.replace(".mp3",""));
                        editTextArtist.setText(mp3File.getId3v2Tag().getArtist());
                        editTextAlbum.setText(mp3File.getId3v2Tag().getAlbum());

                        byte[] tempArtByte = mp3File.getId3v2Tag().getAlbumImage();
                        if(tempArtByte != null) {

                            bitmap = BitmapFactory.decodeByteArray(tempArtByte, 0,tempArtByte.length);
                            art.setImageBitmap(bitmap);
                            art.invalidate();
                        }
                    }
                    else
                    {
                        editTextTitle.setText(tempSavedObject.fileName.replace(".mp3",""));
                        editTextArtist.setText("Unknown");
                        editTextAlbum.setText("Unknown");
                    }
                }

            }
            else
            {
                finish();
            }
            //Mp3File mp3File = new Mp3File(globalData.listSavedObject.get(mPosition).)

        }catch (Exception ex)
        {
            ex.printStackTrace();
        }




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

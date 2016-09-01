package com.elexidea.eimusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ItunesArt extends Activity {


    public List<AlbumArtObject> data;
    ImageButton btnSearch;
    EditText queryText;
    AlbumArtFecther fetcher;
    GridView resultGrid;
    AlbumDataAdapter albumDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itunes_art);
        data = new ArrayList<>();

        btnSearch = (ImageButton)findViewById(R.id.btnAlbumArtSearch);
        queryText = (EditText)findViewById(R.id.editFieldAlbumArtQuery);

        String query = getIntent().getStringExtra("TITLE");
        queryText.setText(query);

        albumDataAdapter = new AlbumDataAdapter(getApplicationContext());
        resultGrid = (GridView)findViewById(R.id.gridViewAlbumArt);
        resultGrid.setAdapter(albumDataAdapter);

        resultGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlbumArtObject tempObjet = data.get(position);
                Intent intent = new Intent();
                intent.putExtra("LINK",tempObjet.url);
                setResult(RESULT_OK, intent);
                if(fetcher != null)
                {
                    if(fetcher.getStatus() == AsyncTask.Status.RUNNING)
                        fetcher.cancel(true);
                }
                data.clear();

                finish();
            }
        });


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fetcher != null) {
                    if(fetcher.getStatus() == AsyncTask.Status.RUNNING) {
                        fetcher.cancel(true);
                        fetcher = null;
                    }
                }
                if(queryText.getText().toString() != "") {
                    fetcher = new AlbumArtFecther();
                    fetcher.execute(queryText.getText().toString());
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        data.clear();
    }

    class AlbumArtObject
    {
        public String url;
        public Bitmap image;

        public AlbumArtObject(Bitmap _image,String _url)
        {
            url = _url;
            image = _image;
        }
    }



    class AlbumArtFecther extends AsyncTask<String,Integer,List<AlbumArtObject>>
    {

        @Override
        protected List<AlbumArtObject> doInBackground(String... params) {

            try {
                data.clear();
                publishProgress(0);
                String queryTerm = params[0];
                String MP3SKULL_urlQueueAndStatus = "https://itunesartwork.dodoapps.io/?query="+queryTerm.replace(" ","+")+"&entity=album&country=us";

                //Search Start
                String searchResponse  = Jsoup.connect(MP3SKULL_urlQueueAndStatus).ignoreContentType(true).execute().body();
                JsonArray searchArray = new com.google.gson.JsonParser().parse(searchResponse).getAsJsonArray();

                if(searchArray != null)
                {
                    if(searchArray.size() > 0)
                    {
                        for(int i=0; i<searchArray.size(); i++)
                        {
                            JsonObject tempObject = searchArray.get(i).getAsJsonObject();
                            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(tempObject.get("url").getAsString().replace("600x600","100x100")).getContent());
                            String temopTitle = tempObject.get("url").getAsString();
                            data.add(new AlbumArtObject(bitmap,temopTitle));
                            publishProgress(0);
                        }

                    }
                }

            } catch (IOException e) {
                System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            albumDataAdapter.notifyDataSetChanged();
        }
    }

    class AlbumDataViewHolder{
        public ImageView imgView;
    }

    class AlbumDataAdapter extends BaseAdapter
    {
        private Context mContext;
        public AlbumDataAdapter(Context _mContext)
        {
            mContext = _mContext;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            AlbumDataViewHolder holder = null;

            LayoutInflater mInflater = (LayoutInflater)
                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_album_art, null);
                holder = new AlbumDataViewHolder();
                holder.imgView = (ImageView) convertView.findViewById(R.id.imgViewAlbumArt);
                convertView.setTag(holder);
            }
            else {
                holder = (AlbumDataViewHolder) convertView.getTag();
            }

            AlbumArtObject rowItem = (AlbumArtObject) getItem(position);
            if(rowItem.image != null)
                holder.imgView.setImageBitmap(rowItem.image);
            else
                holder.imgView.setImageResource(R.drawable.karmin_100dp_100dp);

            return convertView;
        }
    }



}

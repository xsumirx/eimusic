package com.elexidea.eimusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.victor.loading.book.BookLoading;

import org.joda.time.format.ISOPeriodFormat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.Inflater;

public class MainActivity extends Activity {


    BookLoading bookLoading;
    ImageButton btnDownloadMan;
    TextView textViewStatus;
    ImageButton btnSearch;
    EditText queryText;
    boolean isBusy;
    ListView searchResultView;
    ListAdpater searchAdapter;
    Searcher s;


    List<SearchItem> data;

    private static final String PROPERTIES_FILENAME = "youtube.properties";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;
    private static YouTube youtube;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalData.getInstance().init(getApplicationContext());
        ActionBar bar = null;//get();
        bookLoading = (BookLoading)findViewById(R.id.bookloading);
        bookLoading.setVisibility(View.GONE);
        bookLoading.invalidate();
        if(bar != null)
        {
            bar.setDisplayShowHomeEnabled(false);
            bar.setDisplayShowTitleEnabled(false);
            View view = LayoutInflater.from(this).inflate(R.layout.action_bar_main,null);
            bar.setCustomView(view);
            bar.setDisplayShowCustomEnabled(true);
        }


        //Hold data into it
        data = new ArrayList<>();
        searchAdapter = new ListAdpater(getApplicationContext());




        try{
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("Youtube").build();
        }catch (Exception ex)
        {

        }


        btnDownloadMan = (ImageButton)findViewById(R.id.btnDownloadManager);
        textViewStatus = (TextView)findViewById(R.id.textViewStatus);
        btnSearch = (ImageButton)findViewById(R.id.btnSearch);
        queryText = (EditText)findViewById(R.id.textQuery);
        searchResultView = (ListView)findViewById(R.id.listViewSearchResult);

        searchResultView.setAdapter(searchAdapter);
        searchResultView.setOnItemClickListener(searchViewResultListener);

        isBusy = false;


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!queryText.getText().toString().replace(" ","").equals("")) {
                    //if (!isBusy) {
                        isBusy = true;
                        //s.cancel(true);
                        if(s != null)
                        {
                            s.cancel(true);
                            s = null;
                        }
                        s = new Searcher();
                        s.execute(new QueryItem(queryText.getText().toString(),1));
                    bookLoading.setVisibility(View.VISIBLE);
                    bookLoading.start();
                    bookLoading.invalidate();
                   // }
                }
            }
        });
        
        btnDownloadMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Download manager Comming Soon", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(),DownloadMan.class);
                startActivity(i);
            }
        });
    }


    public AdapterView.OnItemClickListener searchViewResultListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SearchItem tempSearchItem = (SearchItem)data.get(position);
            Intent intent = new Intent(getApplicationContext(),VideoDetail.class);
            intent.putExtra(Constants.VIDEO_ID_BUNDLE_KEY, tempSearchItem.vidId);
            intent.putExtra(Constants.VIDEO_TITLE_BUNDLE_KEY,tempSearchItem.title);
            startActivity(intent);
        }
    };



    class Searcher extends AsyncTask<QueryItem,Integer,List<SearchResult>>
    {

        @Override
        protected List<SearchResult> doInBackground(QueryItem... params) {

            try {
                data.clear();
                publishProgress(0);
                String queryTerm = params[0].getQuery();
                YouTube.Search.List search = youtube.search().list("id,snippet");
                String apiKey = "AIzaSyBFbvjjVhraRZGY_5WiVkx7lUNB-uVgBRI";
                search.setKey(apiKey);
                search.setQ(queryTerm);
                search.setType("video");
                search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);


                YouTube.Videos.List videoListSearch = youtube.videos().list("contentDetails");
                videoListSearch.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
                videoListSearch.setFields("items(id,contentDetails/duration)");
                videoListSearch.setKey(apiKey);

                String ids = "";

                //Search Start
                SearchListResponse searchResponse = search.execute();
                if(searchResponse.getItems() != null)
                {
                    if(searchResponse.getItems().size() > 0)
                    {

                        for(SearchResult s:searchResponse.getItems())
                        {
                            if (s.getId().getKind().equals("youtube#video")) {
                                data.add(new SearchItem(s, s.getSnippet().getTitle(),s.getId().getVideoId()));
                                ids += s.getId().getVideoId() + ",";
                            }
                        }
                    }
                }

                if (data.size() > 0) {
                    //Just Removed Everything from Existing List, Cause its a new Query
                    publishProgress(1);


                    videoListSearch.setId(ids);
                    VideoListResponse videoListSearchResponse = videoListSearch.execute();
                    if(videoListSearchResponse != null)
                    {
                        if(videoListSearchResponse.getItems().size() > 0)
                        {
                            int i = 0;
                            for(Video vid:videoListSearchResponse.getItems())
                            {
                                if(vid.getId().equals(data.get(i).searchResult.getId().getVideoId()))
                                {
                                    data.get(i).duration = vid.getContentDetails().getDuration();
                                    publishProgress(2);
                                }
                                i++;
                            }
                        }
                    }



                    for(SearchItem item:data)
                    {
                        Thumbnail thumbnail = item.searchResult.getSnippet().getThumbnails().getDefault();
                        Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(thumbnail.getUrl()).getContent());
                        item.thumbnail = bitmap;
                        publishProgress(2);
                    }
                }
                else
                {
                    publishProgress(3);
                }
            } catch (GoogleJsonResponseException e) {
                System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
            } catch (IOException e) {
                System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (values[0])
            {
                case 0:
                {
                    textViewStatus.setText("Wait ! Searching .......");
                    searchAdapter.notifyDataSetChanged();
                    break;
                }
                case 1:
                {
                    bookLoading.setVisibility(View.GONE);
                    bookLoading.stop();
                    bookLoading.invalidate();
                    textViewStatus.setText(data.size() + " items found");
                    searchAdapter.notifyDataSetChanged();
                    break;
                }
                case 2:
                {
                    searchAdapter.notifyDataSetChanged();
                    break;
                }
                case 3:
                {
                    textViewStatus.setText("Nothing Found...");
                    break;
                }
                default:
                {
                    break;
                }
            }
        }

        @Override
        protected void onPostExecute(List<SearchResult> searchResults) {
            isBusy = false;

            /*
            if(searchResults != null)
            {

                Iterator<SearchResult> res = null;//searchResults.iterator();

                if (!res.hasNext()) {
                    textViewResult.setText(" There aren't any results for your query.");
                }

                ResourceId rId = new ResourceId();
                textViewResult.setText("");
                while (res.hasNext()) {

                    SearchResult singleVideo = res.next();
                    rId = singleVideo.getId();
                    // Confirm that the result represents a video. Otherwise, the
                    // item will not contain a video ID.
                    if (rId.getKind().equals("youtube#video")) {
                        Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();


                        textViewResult.append(" Video Id : " + rId.getVideoId() + '\n');
                        textViewResult.append(" Title: " + singleVideo.getSnippet().getTitle()+ '\n');
                        textViewResult.append(" Thumbnail: " + thumbnail.getUrl()+ '\n');
                        textViewResult.append("\n-------------------------------------------------------------\n");
                    }
                }
            }*/
        }
    }


    class ViewHolder
    {
        public TextView title;
        public TextView artist;
        public ImageView thumbnail;
        public TextView duration;
        public TextView size;
    }


    class ListAdpater extends BaseAdapter
    {
        Context mContext;

        public ListAdpater(Context _context)
        {
            mContext = _context;
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
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            LayoutInflater mInflater = (LayoutInflater)
                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_song_view, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.txtViewTitleItem);
                holder.artist = (TextView) convertView.findViewById(R.id.txtViewArtistItem);
                holder.duration = (TextView) convertView.findViewById(R.id.txtViewDurationItem);
                holder.size = (TextView) convertView.findViewById(R.id.txtViewSizeItem);
                holder.thumbnail = (ImageView) convertView.findViewById(R.id.imgViewThumbnailItem);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            SearchItem rowItem = (SearchItem) getItem(position);

            holder.title.setText(rowItem.title);
            holder.artist.setText(rowItem.searchResult.getSnippet().getChannelTitle());
            holder.duration.setText(rowItem.duration);
            holder.size.setText("");
            if(rowItem.thumbnail != null)
                holder.thumbnail.setImageBitmap(rowItem.thumbnail);

            return convertView;
        }
    }


}

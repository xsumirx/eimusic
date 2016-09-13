package com.elexidea.eimusic;

import android.graphics.Bitmap;
import android.os.Parcelable;

import com.google.api.services.youtube.model.SearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sumir on 8/25/2016.
 */
public class SearchItem {


    public String title;
    public Bitmap thumbnail;
    public SearchResult searchResult;
    public String duration;
    public String vidId;
    private String mp3url;
    public String artist;
    public boolean shouldAnimate;

    public SearchItem(SearchResult _item,String _title,String _id)
    {
        searchResult = _item;
        title = _title;
        thumbnail = null;
        duration = "";
        vidId = _id;
        artist  = _item.getSnippet().getChannelTitle();
        shouldAnimate = true;
    }

    public void setMP3Url(String _mp3Url)
    {
        mp3url = _mp3Url;
    }

    public  String getMp3url()
    {
        return mp3url;
    }
}

package com.elexidea.eimusic;

import java.io.Serializable;

/**
 * Created by Sumir on 8/29/2016.
 */
public class DownloadObject implements Serializable {

    public int STATUS;
    public int size;
    public int sizeDownloaded;
    public String fileName;
    public String location;
    public String url;

    transient Thread thread;

    public DownloadObject(String _location,String _url)
    {
        url = _url;
        location = _location;
    }
}

package com.elexidea.eimusic;

/**
 * Created by Sumir on 8/25/2016.
 */
public class QueryItem {

    String _query;
    int _page;

    public QueryItem(String query, int page)
    {
        _query = query;
        _page = page;
    }

    public String getQuery()
    {
        return _query;
    }

    public int getPage()
    {
        return _page;
    }
}

package com.elexidea.eimusic;

import java.io.Serializable;

/**
 * Created by Sumir on 8/29/2016.
 */
public class SavedObject implements Serializable {

    public int size;
    public String fileName;
    public String location;

    public SavedObject(String _name,int _size,String _location)
    {
        fileName = _name;
        _location = location;
        size = _size;
    }
}

package com.elexidea.eimusic;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.google.api.services.youtube.YouTube;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sumir on 8/29/2016.
 */
public class GlobalData {
    private static GlobalData ourInstance = new GlobalData();

    public static RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);



    public static GlobalData getInstance() {
        return ourInstance;
    }

    public List<SearchItem> data;
    private GlobalData() {

    }

    public void init(Context _mContext)
    {
        mContext = _mContext;
        listDownloading = popDownloadingList();
        listSavedObject = popSavedList();

        //Setup anim with desired properties
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE); //Repeat animation indefinitely
        anim.setDuration(700); //Put desired duration per anim cycle here, in milliseconds

        if (data == null)
            data = new ArrayList<>();
    }

    public List<DownloadObject> listDownloading;
    public List<SavedObject> listSavedObject;
    Context mContext;

    public void pushDownloadingList(){

        File f = new File(mContext.getFilesDir() + "listDownloading");
        if(!f.exists())
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(f));
            stream.writeObject(listDownloading);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<DownloadObject> popDownloadingList(){
        List<DownloadObject> tempList = null;
        File f = new File(mContext.getFilesDir() +"listDownloading");
        if(!f.exists())
            return new ArrayList<>();
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
            tempList = (List< DownloadObject>)stream.readObject();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (tempList == null)
            tempList = new ArrayList<>();

        return tempList;
    }

    public List<SavedObject> popSavedList(){
        List<SavedObject> tempList = null;
        File f = new File(mContext.getFilesDir() +"listSaved");
        if(!f.exists())
            return new ArrayList<>();
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
            tempList = (List<SavedObject>)stream.readObject();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (tempList == null)
            tempList = new ArrayList<>();

        return tempList;
    }


    public void pushSavedList(){
        File f = new File(mContext.getFilesDir()+"listSaved");
        if(!f.exists())
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(f));
            stream.writeObject(listSavedObject);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.elexidea.eimusic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

public class DownloadMan extends Activity {





    private GlobalData globalData;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    DownloadingListFragment fragmentDownloading;
    SavedListFragment fragmentSavedList;

    Button btnDownloading;
    Button btnSaved;

    private int selectedDownloadingListPostion;

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_man);
        globalData = GlobalData.getInstance();

        fragmentDownloading = new DownloadingListFragment();
        fragmentSavedList = new SavedListFragment();

        btnDownloading = (Button)findViewById(R.id.btnDownloadManDownloading);
        btnSaved = (Button)findViewById(R.id.btnDownloadSaved);

        btnDownloading.setTextColor(Color.WHITE);
        btnDownloading.setBackgroundResource(R.color.colorPrimary);

        btnSaved.setTextColor(Color.BLACK);
        btnSaved.setBackgroundResource(R.color.colorPrimaryLight);

        btnDownloading.invalidate();
        btnSaved.invalidate();

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.downloadManContainer,fragmentDownloading);
        fragmentTransaction.commit();

        btnDownloading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    btnDownloading.setTextColor(Color.WHITE);
                    btnDownloading.setBackgroundResource(R.color.colorPrimary);

                    btnSaved.setTextColor(Color.BLACK);
                    btnSaved.setBackgroundResource(R.color.colorPrimaryLight);

                    btnDownloading.invalidate();
                    btnSaved.invalidate();

                    fragmentManager.beginTransaction().replace(R.id.downloadManContainer, fragmentDownloading).commit();
                } catch (Exception ex) {
                    int i = 1;
                }
            }
        });

        btnSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    btnSaved.setTextColor(Color.WHITE);
                    btnSaved.setBackgroundResource(R.color.colorPrimary);

                    btnDownloading.setTextColor(Color.BLACK);
                    btnDownloading.setBackgroundResource(R.color.colorPrimaryLight);

                    btnDownloading.invalidate();
                    btnSaved.invalidate();
                    fragmentManager.beginTransaction().replace(R.id.downloadManContainer, fragmentSavedList).commit();
                }catch (Exception ex)
                {

                }
            }
        });


    }




}

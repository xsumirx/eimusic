package com.elexidea.eimusic;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

public class DownloadMan extends Activity {

    public final static int REFRESH_TIME = 500;
    ListView downList;
    ListView savedList;
    DownloadListAdpater adpater;
    SavedListAdpater adapterSavedList;
    Handler tickHadler;
    GlobalData globalData;
    private int selectedDownloadingListPostion;

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_man);

        downList = (ListView)findViewById(R.id.downloadingList);
        adpater = new DownloadListAdpater(this);
        downList.setAdapter(adpater);

        adapterSavedList = new SavedListAdpater(this);
        savedList = (ListView)findViewById(R.id.savedList);
        savedList.setAdapter(adapterSavedList);



        globalData = GlobalData.getInstance();

        adpater.notifyDataSetChanged();
        adapterSavedList.notifyDataSetChanged();

        tickHadler = new Handler();
        refreshUI.run();
        //registerForContextMenu(downList);

        downList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //downList.showContextMenu();
                selectedDownloadingListPostion = position;
            }
        });

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        TextView view = (TextView)v.findViewById(R.id.txtViewitemDownloadingListStatus);
        menu.add(0, v.getId(), 0, "Delete");//groupId, itemId, order, title
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle()=="Delete"){
            if(globalData.listDownloading.get(selectedDownloadingListPostion).STATUS == 1)
            {
                if(globalData.listDownloading.get(selectedDownloadingListPostion).thread != null)
                {
                    globalData.listDownloading.get(selectedDownloadingListPostion).thread.interrupt();
                }
            }

            globalData.listDownloading.remove(selectedDownloadingListPostion);
            adpater.notifyDataSetChanged();

        }else{
            return false;
        }
        return true;
    }

    Runnable refreshUI = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(globalData.listDownloading.size() > 0) {
                        adpater.notifyDataSetChanged();
                        adapterSavedList.notifyDataSetChanged();
                    }
                }
            });
            tickHadler.postDelayed(refreshUI, REFRESH_TIME);
        }
    };



    class DownloadingListViewHolder
    {
        public TextView name;
        public TextView progress;
        public TextView status;
        public TextView size;
        public com.github.lzyzsd.circleprogress.DonutProgress donutProgress;
    }


    class DownloadListAdpater extends BaseAdapter
    {
        Context mContext;
        GlobalData globalData;

        public DownloadListAdpater(Context _context)
        {
            mContext = _context;
            globalData = GlobalData.getInstance();
        }

        @Override
        public int getCount() {
            return globalData.listDownloading.size();
        }

        @Override
        public Object getItem(int position) {
            return globalData.listDownloading.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DownloadingListViewHolder holder = null;

            LayoutInflater mInflater = (LayoutInflater)
                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_downloading_list, null);
                holder = new DownloadingListViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.txtViewitemDownloadingListFileName);
                holder.size = (TextView) convertView.findViewById(R.id.txtViewitemDownloadingListFileSize);
                holder.progress = (TextView) convertView.findViewById(R.id.txtViewitemDownloadingListProgress);
                holder.status = (TextView) convertView.findViewById(R.id.txtViewitemDownloadingListStatus);
                holder.donutProgress = (DonutProgress)convertView.findViewById(R.id.donut_progress);
                convertView.setTag(holder);
            }
            else {

                holder = (DownloadingListViewHolder) convertView.getTag();
            }

            DownloadObject rowItem = (DownloadObject) getItem(position);

            holder.name.setText(rowItem.fileName);
            holder.size.setText(String.valueOf(android.text.format.Formatter.formatShortFileSize(getApplicationContext(), rowItem.size)));
            holder.progress.setText(String.valueOf(android.text.format.Formatter.formatShortFileSize(getApplicationContext(), rowItem.sizeDownloaded)));
            int prog = (int)((((float)rowItem.sizeDownloaded)/((float)rowItem.size))*100.0);
            holder.donutProgress.setProgress(prog);

            if(rowItem.STATUS == 1)
            {
                //Downloading
                holder.donutProgress.setFinishedStrokeColor(R.color.darkGreen);
                holder.status.setText("Downloading");
            }else if(rowItem.STATUS == 0)
            {
                //Finished
                holder.status.setText("Finished");
                holder.donutProgress.setFinishedStrokeColor(R.color.darkBlue);

            }else
            {
                //Error
                holder.status.setText("Error");
                holder.donutProgress.setFinishedStrokeColor(R.color.darkYello);
            }

            return convertView;
        }
    }





    class SavedListViewHolder
    {
        public TextView name;
        public TextView size;
        public TextView duration;
        public ImageView imageView;
    }


    class SavedListAdpater extends BaseAdapter
    {
        Context mContext;
        GlobalData globalData;

        public SavedListAdpater(Context _context)
        {
            mContext = _context;
            globalData = GlobalData.getInstance();
        }

        @Override
        public int getCount() {
            return globalData.listSavedObject.size();
        }

        @Override
        public Object getItem(int position) {
            return globalData.listSavedObject.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SavedListViewHolder holder = null;

            LayoutInflater mInflater = (LayoutInflater)
                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_saved_list, null);
                holder = new SavedListViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.txtViewItemSavedTitle);
                holder.size = (TextView) convertView.findViewById(R.id.txtViewItemSavedSize);
                holder.imageView = (ImageView) convertView.findViewById(R.id.txtViewItemSavedImage);
                holder.duration = (TextView) convertView.findViewById(R.id.txtViewItemSavedDuration);
                convertView.setTag(holder);
            }
            else {

                holder = (SavedListViewHolder) convertView.getTag();
            }

            SavedObject rowItem = (SavedObject) getItem(position);

            holder.name.setText(rowItem.fileName);
            holder.size.setText(String.valueOf(android.text.format.Formatter.formatShortFileSize(getApplicationContext(), rowItem.size)));
            holder.duration.setText("");
            return convertView;
        }
    }
}

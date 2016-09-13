package com.elexidea.eimusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import java.io.File;


public class DownloadingListFragment extends Fragment {


    public final static int REFRESH_TIME = 500;

    Handler tickHadler;
    GlobalData globalData;
    ListView downList;
    DownloadListAdpater adpater;

    public DownloadingListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_downloading_list, container, false);

        downList = (ListView)v.findViewById(R.id.downloadingList);
        adpater = new DownloadListAdpater(getActivity().getApplicationContext());
        downList.setAdapter(adpater);

        globalData = GlobalData.getInstance();

        TextView emptyView = new TextView(getActivity());
        emptyView.setText("No Active Downloadings");
        downList.setEmptyView(emptyView);
        registerForContextMenu(downList);

        downList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //downList.showContextMenu();
                downList.setTag(position);
                downList.showContextMenu();
            }
        });

        tickHadler = new Handler();
        tickHadler.postDelayed(refreshUI, REFRESH_TIME);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();
        tickHadler = null;
    }


    Runnable refreshUI = new Runnable() {
        @Override
        public void run() {
            if(tickHadler != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //if (globalData.listDownloading.size() > 0) {
                        if (tickHadler != null && adpater != null) {
                            adpater.notifyDataSetChanged();
                        }
                    }
                });
                if (tickHadler != null)
                    tickHadler.postDelayed(refreshUI, REFRESH_TIME);
            }
        }
    };


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.downloadingList) {
            TextView view = (TextView) v.findViewById(R.id.txtViewitemDownloadingListStatus);
            menu.add(0, v.getId(), 0, "Delete");//groupId, itemId, order, title
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle()=="Delete"){

            AdapterView.AdapterContextMenuInfo minfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            int tempPosition = -1;
            if(minfo == null)
            {
                tempPosition = (Integer)downList.getTag();
            }else
            {
                tempPosition = minfo.position;
            }
            if(tempPosition != -1) {
                try {
                    File musicFile2Delete = new File(GlobalData.getInstance().listDownloading.get(tempPosition).location+"/"+GlobalData.getInstance().listDownloading.get(tempPosition).fileName);
                    if (musicFile2Delete.exists()) {
                        musicFile2Delete.delete();
                    }
                    GlobalData.getInstance().listDownloading.remove(tempPosition);
                    GlobalData.getInstance().pushDownloadingList();
                    adpater.notifyDataSetChanged();
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

        }else{
            return false;
        }
        return true;
    }


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
            holder.size.setText(String.valueOf(android.text.format.Formatter.formatShortFileSize(getActivity().getApplicationContext(), rowItem.size)));
            holder.progress.setText(String.valueOf(android.text.format.Formatter.formatShortFileSize(getActivity().getApplicationContext(), rowItem.sizeDownloaded)));
            int prog = (int)((((float)rowItem.sizeDownloaded)/((float)rowItem.size))*100.0);
            holder.donutProgress.setProgress(prog);

            if(rowItem.STATUS == 1)
            {
                //Downloading
                holder.donutProgress.setUnfinishedStrokeColor(R.color.darkGreen);
                holder.status.setText("Downloading");
            }else if(rowItem.STATUS == 0) {
                //Finished
                holder.status.setText("Finished");
                holder.donutProgress.setUnfinishedStrokeColor(R.color.darkBlue);
            }else {
                //Error
                holder.status.setText("Error");
                holder.donutProgress.setUnfinishedStrokeColor(R.color.darkYello);
            }

            return convertView;
        }
    }

}

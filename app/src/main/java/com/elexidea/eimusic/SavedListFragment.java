package com.elexidea.eimusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
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
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class SavedListFragment extends Fragment {

    GlobalData globalData;
    ListView savedList;
    SavedListAdpater adapterSavedList;
    AdapterView.AdapterContextMenuInfo lastMenuInfo;
    public int listPosition;

    public SavedListFragment() {
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
        View v =  inflater.inflate(R.layout.fragment_saved_list, container, false);

        adapterSavedList = new SavedListAdpater(getActivity());
        savedList = (ListView)v.findViewById(R.id.savedList);
        savedList.setAdapter(adapterSavedList);

        globalData = GlobalData.getInstance();

        registerForContextMenu(savedList);

        savedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                savedList.setTag(position);
                savedList.showContextMenu();
            }
        });




        return v;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.savedList)
        {
            menu.add(0, v.getId(), 1, "Tag");//groupId, itemId, order, title
            menu.add(0, v.getId(), 0, "Open");
            menu.add(0, v.getId(), 2, "Clear");
            menu.add(0, v.getId(), 3, "Clear & Delete");
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle() == "Tag")
        {
            /*AdapterView.AdapterContextMenuInfo minfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            int tempPosition = -1;
            if(minfo == null)
            {
                tempPosition = (Integer)savedList.getTag();
            }else
            {
                tempPosition = minfo.position;
            }
            Intent i = new Intent(getActivity(),TagEDIT.class);
            i.putExtra("INDEX",tempPosition);
            startActivity(i);*/
            Toast.makeText(getActivity(), "Feature will available in Next Update", Toast.LENGTH_SHORT).show();
        }else if(item.getTitle() == "Open") {

            AdapterView.AdapterContextMenuInfo minfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            int tempPosition = -1;
            if(minfo == null)
            {
                tempPosition = (Integer)savedList.getTag();
            }else
            {
                tempPosition = minfo.position;
            }
            if(tempPosition != -1) {

                try {
                    File musicFile2Play = new File(GlobalData.getInstance().listSavedObject.get(tempPosition).location+"/"+GlobalData.getInstance().listSavedObject.get(tempPosition).fileName);
                    if (musicFile2Play.exists()) {
                        Intent i2 = new Intent();
                        i2.setAction(android.content.Intent.ACTION_VIEW);
                        i2.setDataAndType(Uri.fromFile(musicFile2Play), "audio/mp3");
                        startActivity(i2);
                    }
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }else if(item.getTitle() == "Clear") {
            AdapterView.AdapterContextMenuInfo minfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            int tempPosition = -1;
            if(minfo == null)
            {
                tempPosition = (Integer)savedList.getTag();
            }else
            {
                tempPosition = minfo.position;
            }
            if(tempPosition != -1) {
                GlobalData.getInstance().listSavedObject.remove(tempPosition);
                GlobalData.getInstance().pushSavedList();
                adapterSavedList.notifyDataSetChanged();
            }

        }else if(item.getTitle() == "Clear & Delete") {
            AdapterView.AdapterContextMenuInfo minfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            int tempPosition = -1;
            if(minfo == null)
            {
                tempPosition = (Integer)savedList.getTag();
            }else
            {
                tempPosition = minfo.position;
            }
            if(tempPosition != -1) {

                try {
                    File musicFile2Delete = new File(GlobalData.getInstance().listSavedObject.get(tempPosition).location+"/"+GlobalData.getInstance().listSavedObject.get(tempPosition).fileName);
                    if (musicFile2Delete.exists()) {
                        musicFile2Delete.delete();
                    }
                    GlobalData.getInstance().listSavedObject.remove(tempPosition);
                    GlobalData.getInstance().pushSavedList();
                    adapterSavedList.notifyDataSetChanged();
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


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
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
            holder.size.setText(String.valueOf(android.text.format.Formatter.formatShortFileSize(getActivity(), rowItem.size)));
            holder.duration.setText("");
            return convertView;
        }
    }

}

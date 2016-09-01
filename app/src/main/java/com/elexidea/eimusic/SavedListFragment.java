package com.elexidea.eimusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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


public class SavedListFragment extends Fragment {

    GlobalData globalData;
    ListView savedList;
    SavedListAdpater adapterSavedList;

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
            menu.add(0, v.getId(), 0, "Edit");//groupId, itemId, order, title
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle() == "Edit")
        {
            AdapterView.AdapterContextMenuInfo minfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            Intent i = new Intent(getActivity(),TagEDIT.class);
            //i.putExtra("INDEX",minfo.position);
            startActivity(i);
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

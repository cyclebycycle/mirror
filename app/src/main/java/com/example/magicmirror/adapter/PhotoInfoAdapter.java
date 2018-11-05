package com.example.magicmirror.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.example.magicmirror.R;
import com.example.magicmirror.bean.Photo;

import java.util.ArrayList;


public class PhotoInfoAdapter extends BaseAdapter {
    private ArrayList<Photo> photos;
    private LayoutInflater mInflator;
    private Context context;

    public PhotoInfoAdapter(Context context) {
        super();
        photos=new ArrayList<Photo>();
        this.context=context;
        mInflator =LayoutInflater.from(context);
    }

    public void addphoto(Photo photo) {
        if(!photos.contains(photo)) {
            photos.add(photo);
        }
    }

    public Photo getphotos(int position) {
        return photos.get(position);
    }

    public void clear() {
        photos.clear();
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int i) {
        return photos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        System.out.println("General ListView optimization code.");
        if (view == null) {
            if(mInflator==null){
                System.out.println("mInflator为空");
            }
            view = mInflator.inflate(R.layout.item_photos_info, null);
            viewHolder = new ViewHolder();
            viewHolder.it_photo_time = (TextView) view.findViewById(R.id.it_photo_time);
            viewHolder.it_photo_result = (TextView) view.findViewById(R.id.it_photo_result);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Photo photo = photos.get(i);
        String photo_time = photo.getTime();//名字
        String photo_result = photo.getResult();//电话
        if (photo_time != null && photo_time.length() > 0)
            viewHolder.it_photo_time.setText(photo_time);
        if (photo_result != null && photo_result.length() > 0)
            viewHolder.it_photo_result.setText(photo_result);
        return view;
    }

    /**
     * 设置Listview的高度
     */
    public void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if(listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    class ViewHolder {
        //
        TextView it_photo_time;
        TextView it_photo_result;
    }
}





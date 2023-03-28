package com.example.openbook.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openbook.R;
import com.example.openbook.View.CartList;
import com.example.openbook.View.SideList;

import java.util.ArrayList;

public class SideListViewAdapter extends BaseAdapter {
    ArrayList<SideList> items = new ArrayList<SideList>();

    @Override
    public int getCount() {
        return items.size();
    }

    public void addItem(SideList item){
        items.add(item);
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convetView, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();
        final SideList sideList = items.get(position);

        if(convetView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convetView = inflater.inflate(R.layout.side_listview_item, viewGroup, false);


            TextView menu_section = convetView.findViewById(R.id.menu_section);

            menu_section.setText(sideList.getNavigation());

        }else{
            View view = new View(context);
            view = convetView;
        }

        convetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String where = sideList.getNavigation();

                if(myListener !=null){
                    myListener.onItemClick(view, where);
                }

            }
        });





        return convetView;

    }


    public interface OnItemClickListener{
        void onItemClick(View view, String where);
    }

    private SideListViewAdapter.OnItemClickListener myListener = null;

    public void setOnItemClickListener(SideListViewAdapter.OnItemClickListener listener){
        this.myListener = listener;
    }

}

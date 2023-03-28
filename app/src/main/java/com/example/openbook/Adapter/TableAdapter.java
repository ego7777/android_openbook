package com.example.openbook.Adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.R;
import com.example.openbook.View.TableList;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {

    ArrayList<TableList> table = new ArrayList<TableList>();
    String TAG = "TableAdapter";



    /**
     * 커스텀 리스너 인터페이스 정의
     */
    public interface OnItemClickListener{
        void onItemClick(View view , int position);

    }

    private TableAdapter.OnItemClickListener myListener = null;

    public void setOnItemClickListener(TableAdapter.OnItemClickListener listener){
        this.myListener = listener;
    }


    @NonNull
    @Override
    /**
     * 리스트 아이템을 가져와서 레이아웃을 실체화 해줌
     * 실체화를 해주는 아이가 Inflater
     * **/
    public TableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_gridview_item, parent, false);
        return new TableAdapter.ViewHolder(view, viewType);
    }



    /**
     * 액티비티에서 받아온 데이터를 바인딩해줌.
     * **/
    @Override
    public void onBindViewHolder(@NonNull TableAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.onBind(table.get(position));
    }


    /**
     * 리사이클러뷰 리스트 사이즈를 불러옴
     * **/
    @Override
    public int getItemCount() {
        if(table == null){
            return  0;
        }

        return table.size();
    }

    public  void setAdapterItem(ArrayList<TableList> items){
        this.table = items;

        notifyDataSetChanged();
    }




    /**
     * 뷰홀더 생성
     * **/
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tableNum;
        TextView table_item_gender;
        TextView table_item_guestNum;

        int position;
        private int viewType;


        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType =viewType;

            tableNum = itemView.findViewById(R.id.tableNum);
            table_item_gender = itemView.findViewById(R.id.table_item_gender);
            table_item_guestNum = itemView.findViewById(R.id.table_item_guestNum);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        if (myListener != null) {
                            myListener.onItemClick(view, position);
                            notifyDataSetChanged();
                        }
                    }
                }
            });

        }


        void onBind(TableList items) {
            if(viewType == 0){
                onBindMine(items);
            }else if(viewType == 1){
                onBindOthers(items);
            }

        }

        void onBindOthers(TableList items){
            tableNum.setText(String.valueOf(items.getTableNum()));
            tableNum.setBackground(items.getTableColor());

            table_item_gender.setText(items.getTableGender());
            table_item_gender.setBackground(items.getTableColor());

            table_item_guestNum.setText(items.getTableGuestNum());
            table_item_guestNum.setBackground(items.getTableColor());
        }

        void onBindMine(TableList items){
            tableNum.setText(items.getMyTable());
            tableNum.setBackground(items.getTableColor());

            table_item_gender.setText(items.getTableGender());
            table_item_gender.setBackground(items.getTableColor());

            table_item_guestNum.setText(items.getTableGuestNum());
            table_item_guestNum.setBackground(items.getTableColor());

        }

    }

    @Override
    public int getItemViewType(int position) {
        if (table.get(position).getViewType() == 0) {
            return 0;
        } else {
            return 1;
        }
    }

}

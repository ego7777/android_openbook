package com.example.openbook.Adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.R;
import com.example.openbook.Data.TableList;

import java.util.ArrayList;
import java.util.HashMap;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {

    int myTable;
    ArrayList<TableList> table;
    String TAG = "TableAdapterTAG";

    private int lastClickedPosition = -1;
    private HashMap<Integer, Integer> positionColorMap = new HashMap<>();


    public TableAdapter(ArrayList<TableList> table, int myTable) {
        this.table = table;
        this.myTable = myTable;
        Log.d(TAG, "TableAdapter get_id: " + myTable);
    }

    public void changeItemColor(int position, int color){
        positionColorMap.put(position, color);
        Log.d(TAG, "changeItemColor: " + position);
        notifyDataSetChanged();
    }


    /**
     * 커스텀 리스너 인터페이스 정의
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);

    }

    private TableAdapter.OnItemClickListener myListener = null;

    public void setOnItemClickListener(TableAdapter.OnItemClickListener listener) {
        this.myListener = listener;
    }


    @NonNull
    @Override
    /**
     * 리스트 아이템을 가져와서 레이아웃을 실체화 해줌
     * 실체화를 해주는 아이가 Inflater
     * **/
    public TableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.table_gridview_item, parent, false);

        return new TableAdapter.ViewHolder(view, viewType);
    }


    /**
     * 액티비티에서 받아온 데이터를 바인딩해줌.
     **/
    @Override
    public void onBindViewHolder(@NonNull TableAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {


        holder.onBind(table.get(position));
        int color;

        if (position == lastClickedPosition) {
            //여기가 1순위여야함
            color = holder.itemView.getContext().getColor(R.color.blue_purple);
            holder.itemView.setBackgroundColor(color);
        } else if (position == myTable - 1) {
            color = holder.itemView.getContext().getColor(R.color.flower_pink);
            holder.itemView.setBackgroundColor(color);
        } else {
            color = holder.itemView.getContext().getColor(R.color.light_gray);
            holder.itemView.setBackgroundColor(color);
        }


        Integer orderedTable = positionColorMap.get(position);
        Log.d(TAG, "orderTable: " + orderedTable);



        if(orderedTable != null){
            if (position == lastClickedPosition) {
                Log.d(TAG, "equal ");
                color = holder.itemView.getContext().getColor(R.color.blue_purple);
                holder.itemView.setBackgroundColor(color);

            } else {
                Log.d(TAG, "not equal: " + position);
                holder.itemView.setBackgroundColor(orderedTable);
            }


        }


    }


    /**
     * 리사이클러뷰 리스트 사이즈를 불러옴
     **/
    @Override
    public int getItemCount() {
        if (table == null) {
            return 0;
        }

        return table.size();
    }

    public void setAdapterItem(ArrayList<TableList> items) {
        this.table = items;

        notifyDataSetChanged();
    }


    /**
     * 뷰홀더 생성
     **/
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tableNum;
        TextView table_grid_gender;
        TextView table_grid_guestNum;

        int position;
        private int viewType;


        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            tableNum = itemView.findViewById(R.id.tableNum);
            table_grid_gender = itemView.findViewById(R.id.table_grid_gender);
            table_grid_guestNum = itemView.findViewById(R.id.table_grid_guestNum);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        if (myListener != null) {
                            myListener.onItemClick(view, position);
                            notifyDataSetChanged();

                            notifyItemChanged(lastClickedPosition);
                            lastClickedPosition = position;
                            Log.d(TAG, "lastClickedPosition: " + lastClickedPosition);
                            notifyItemChanged(position);
                        }
                    }
                }
            });

        }


        void onBind(TableList items) {
            if (viewType == 0) {
                onBindMine(items);
            } else if (viewType == 1) {
                onBindOthers(items);
            }

        }

        void onBindOthers(TableList items) {
            tableNum.setText(String.valueOf(items.getTableNum()));
            table_grid_gender.setText(items.getTableGender());
            table_grid_guestNum.setText(items.getTableGuestNum());
        }

        void onBindMine(TableList items) {
            tableNum.setText(items.getMyTable());
            table_grid_gender.setText(items.getTableGender());
            table_grid_guestNum.setText(items.getTableGuestNum());

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

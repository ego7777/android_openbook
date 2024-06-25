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
import com.example.openbook.Category.TableCategory;

import java.util.ArrayList;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {

    int myTable;
    ArrayList<TableList> table;
    String TAG = "TableAdapterTAG";

    private int lastClickedPosition = -1;

    public TableAdapter(ArrayList<TableList> table, int myTable) {
        this.table = table;
        this.myTable = myTable;
        Log.d(TAG, "TableAdapter myTable: " + myTable);
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);

    }

    private TableAdapter.OnItemClickListener myListener = null;

    public void setOnItemClickListener(TableAdapter.OnItemClickListener listener) {
        this.myListener = listener;
    }


    @NonNull
    @Override
    public TableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.table_gridview_item, parent, false);

        return new TableAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.onBind(table.get(position));

        int color;

        if (position == lastClickedPosition) {
            color = holder.itemView.getContext().getColor(R.color.blue_purple);
            holder.itemView.setBackgroundColor(color);

        } else if (table.get(position).getCategory() == TableCategory.MY) {
            color = holder.itemView.getContext().getColor(R.color.flower_pink);
            holder.itemView.setBackgroundColor(color);

        } else if(table.get(position).getCategory() == TableCategory.ACTIVE){
            color = holder.itemView.getContext().getColor(R.color.skyblue);
            holder.itemView.setBackgroundColor(color);

        } else {
            color = holder.itemView.getContext().getColor(R.color.light_gray);
            holder.itemView.setBackgroundColor(color);
        }
    }

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


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tableNum;
        TextView tableGridGender;
        TextView tableGridGuestNum;

        int position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tableNum = itemView.findViewById(R.id.tableNum);
            tableGridGender = itemView.findViewById(R.id.table_grid_gender);
            tableGridGuestNum = itemView.findViewById(R.id.table_grid_guestNum);

            itemView.setOnClickListener(view -> {
                position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    if (myListener != null) {
                        myListener.onItemClick(view, position);
                    }
                }
            });

        }


        void onBind(TableList items) {
            switch (items.getCategory()){
                case MY:
                    onBindMine(items);
                    break;
                case OTHER:
                case ACTIVE:
                    onBindOthers(items);
                    break;

            }

        }

        void onBindOthers(TableList items) {
            tableNum.setText(String.valueOf(items.getTableNumber()));
            tableGridGender.setText(items.getTableGender());
            tableGridGuestNum.setText(items.getTableGuestNum());
        }

        void onBindMine(TableList items) {
            tableNum.setText(items.getMyTable());
            tableGridGender.setText(items.getTableGender());
            tableGridGuestNum.setText(items.getTableGuestNum());

        }

    }

    @Override
    public int getItemViewType(int position) {
        if (table.get(position).getCategory() == TableCategory.MY) {
            return 0;
        } else {
            return 1;
        }
    }

    public void setLastClickedPosition(int position, boolean focus) {
        if (focus) {

            int previousClickedPosition = lastClickedPosition;
            lastClickedPosition = position;
            notifyItemChanged(previousClickedPosition);
            notifyItemChanged(lastClickedPosition);

        } else {
            lastClickedPosition = -1;
            notifyItemChanged(position);
        }
    }
}

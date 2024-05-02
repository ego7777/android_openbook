package com.example.openbook.Adapter;


import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Data.AdminTableList;
import com.example.openbook.PaymentCategory;
import com.example.openbook.R;

import java.util.ArrayList;

public class AdminTableAdapter extends RecyclerView.Adapter<AdminTableAdapter.ViewHolder> {

    String TAG = "adminTableAdapterTAG";
    ArrayList<AdminTableList> table = new ArrayList<>();

    private int lastClickedPosition = -1;


    public interface onItemClickListener {
        void onItemClick(View view, int position);
    }

    private AdminTableAdapter.onItemClickListener myListener = null;

    public void setOnItemClickListener(AdminTableAdapter.onItemClickListener listener) {
        this.myListener = listener;
    }

    @NonNull
    @Override
    public AdminTableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(getViewSrc(viewType), parent, false);

        return new AdminTableAdapter.ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminTableAdapter.ViewHolder holder, int position) {
        holder.onBind(table.get(position));

        if(position == lastClickedPosition){
            int color = ContextCompat.getColor(holder.itemView.getContext(), R.color.salmon);
            holder.itemView.setBackgroundColor(color);
        }else{
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        if (table == null) {
            return 0;
        }
        return table.size();
    }

    public void setAdapterItem(ArrayList<AdminTableList> items) {
        this.table = items;
        Log.d(TAG, "setAdapterItem: " + items.get(0).getAdminTableMenu());
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView adminGridNumber;
        TextView adminGridPrice;
        TextView adminGridGender;
        TextView adminGridGuestNum;
        TextView adminGridStatement;

        int position, viewType;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            if(viewType == TYPE_LATER){
                adminGridNumber = itemView.findViewById(R.id.admin_grid_number);
                adminGridPrice = itemView.findViewById(R.id.admin_grid_price);
                adminGridGender = itemView.findViewById(R.id.admin_grid_gender);
                adminGridGuestNum = itemView.findViewById(R.id.admin_grid_guestNum);

            }else if(viewType == TYPE_NOW){
                adminGridNumber = itemView.findViewById(R.id.admin_grid_number_before);
                adminGridPrice = itemView.findViewById(R.id.admin_grid_price);
                adminGridStatement = itemView.findViewById(R.id.admin_grid_statement);
            }


            itemView.setOnClickListener(v -> {
                position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    if (myListener != null ) {
                        myListener.onItemClick(v, position);
                        notifyDataSetChanged();
                        notifyItemChanged(lastClickedPosition);
                        lastClickedPosition = position;
                        notifyItemChanged(position);
                    }
                }

            });
        }

        void onBind(AdminTableList item){
            Log.d(TAG, "onBind menuName: " + item.getAdminTableMenu());
            if(viewType == TYPE_LATER){
                onBindLater(item);
            }else if(viewType == TYPE_NOW){
                onBindNow(item);
            }
        }

        void onBindLater(AdminTableList item){
            adminGridNumber.setText(item.getAdminTableNumber());
            adminGridPrice.setText(item.getAdminTablePrice());
            adminGridGender.setText(item.getAdminTableGender());
            adminGridGuestNum.setText(item.getAdminTableGuestNumber());
        }

        void onBindNow(AdminTableList item){
            adminGridNumber.setText(item.getAdminTableNumber());
            adminGridStatement.setText(item.getAdminTableStatement());
            adminGridStatement.setTextColor(Color.RED);
            adminGridPrice.setText(item.getAdminTablePrice());
        }

    }

    private final int TYPE_LATER = 101;
    private final int TYPE_NOW = 102;

    private int getViewSrc(int viewType){
        if(viewType == TYPE_LATER){
            return R.layout.admin_table_gridview_item_after;

        }else {
            return R.layout.admin_table_gridview_itme_before;

        }
    }

    @Override
    public int getItemViewType(int position) {
        if(table.get(position).getPaymentType()
                == PaymentCategory.LATER.getValue()){
            return TYPE_LATER;

        }else{
            return TYPE_NOW;
        }
    }
}

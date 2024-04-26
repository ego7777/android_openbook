package com.example.openbook.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
            int color = holder.itemView.getContext().getResources().getColor(R.color.salmon);
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
        notifyDataSetChanged();

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView admin_grid_number;
        TextView admin_grid_menu;
        TextView admin_grid_price;
        TextView admin_grid_gender;
        TextView admin_grid_guestNum;

        TextView admin_grid_statement;

        int position, viewType;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            if(viewType == TYPE_LATER){
                admin_grid_number = itemView.findViewById(R.id.admin_grid_number);
                admin_grid_menu = itemView.findViewById(R.id.admin_grid_menu);
                admin_grid_price = itemView.findViewById(R.id.admin_grid_price);
                admin_grid_gender = itemView.findViewById(R.id.admin_grid_gender);
                admin_grid_guestNum = itemView.findViewById(R.id.admin_grid_guestNum);

            }else if(viewType == TYPE_NOW){
                admin_grid_number = itemView.findViewById(R.id.admin_grid_number_before);
                admin_grid_statement = itemView.findViewById(R.id.admin_grid_statement);
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
            if(viewType == TYPE_LATER){
                onBindLater(item);
            }else if(viewType == TYPE_NOW){
                onBindNow(item);
            }
        }

        void onBindLater(AdminTableList item){
            admin_grid_number.setText(item.getAdminTableNumber());
            admin_grid_menu.setText(item.getAdminTableMenu());
            admin_grid_price.setText(item.getAdminTablePrice());
            admin_grid_gender.setText(item.getAdminTableGender());
            admin_grid_guestNum.setText(item.getAdminTableGuestNumber());
        }

        void onBindNow(AdminTableList item){
            admin_grid_number.setText(item.getAdminTableNumber());
            admin_grid_statement.setText(item.getAdminTableStatement());
            admin_grid_statement.setTextColor(Color.RED);
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

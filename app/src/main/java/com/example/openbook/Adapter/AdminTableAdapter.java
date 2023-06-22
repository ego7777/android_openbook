package com.example.openbook.Adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Data.AdminTableList;
import com.example.openbook.R;

import java.util.ArrayList;

public class AdminTableAdapter extends RecyclerView.Adapter<AdminTableAdapter.ViewHolder> {

    String TAG = "adminTableAdapter_TAG";
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
                .inflate(R.layout.admin_table_gridview_item, parent, false);

        return new AdminTableAdapter.ViewHolder(view);
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

        int position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            admin_grid_number = itemView.findViewById(R.id.admin_grid_number);
            admin_grid_menu = itemView.findViewById(R.id.admin_grid_menu);
            admin_grid_price = itemView.findViewById(R.id.admin_grid_price);
            admin_grid_gender = itemView.findViewById(R.id.admin_grid_gender);
            admin_grid_guestNum = itemView.findViewById(R.id.admin_grid_guestNum);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    Log.d(TAG, "position: " + position);

                    if (position != RecyclerView.NO_POSITION) {
                        if (myListener != null ) {
                            myListener.onItemClick(v, position);
                            notifyDataSetChanged();
                            notifyItemChanged(lastClickedPosition);
                            lastClickedPosition = position;
                            notifyItemChanged(position);
                        }
                    }

                }
            });
        }

        void onBind(AdminTableList items){


            admin_grid_number.setText(items.getAdminTableNumber());
            admin_grid_menu.setText(items.getAdminTableMenu());
            admin_grid_price.setText(items.getAdminTablePrice());
            admin_grid_gender.setText(items.getAdminTableGender());
            admin_grid_guestNum.setText(items.getAdminTableGuestNumber());

            if(items!= null && items.getAdminTableMenu() != null){
                if(items.getAdminTableMenu().contains("선불")){

                    admin_grid_menu.setTextColor(Color.RED);
                }
            }



        }

    }

}

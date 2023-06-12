package com.example.openbook.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Data.OrderList;
import com.example.openbook.R;

import java.util.ArrayList;

public class AdminPopUpAdapter extends RecyclerView.Adapter<AdminPopUpAdapter.ViewHolder>{

    ArrayList<OrderList> orderList = new ArrayList<>();

    public interface onItemClickListener {
        void onItemClick(View view, int position);
    }

    private AdminPopUpAdapter.onItemClickListener myListener = null;

    public void setOnItemClickListener(AdminPopUpAdapter.onItemClickListener listener) {
        this.myListener = listener;
    }


    @NonNull
    @Override
    public AdminPopUpAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(getViewSrc(viewType), parent, false);

        return new AdminPopUpAdapter.ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminPopUpAdapter.ViewHolder holder, int position) {
        holder.onBind(orderList.get(position));
    }

    @Override
    public int getItemCount() {
        if (orderList == null) {
            return 0;
        }
        return orderList.size();
    }

    public void setAdapterItem(ArrayList<OrderList> items) {
        this.orderList = items;
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView admin_popup_payment;
        TextView admin_popup_menu_name;
        TextView admin_popup_menu_quantity;
        TextView admin_popup_menu_price;

        int position;
        int viewType;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType =viewType;

            admin_popup_payment = itemView.findViewById(R.id.admin_popup_payment_body);

            admin_popup_menu_name = itemView.findViewById(R.id.admin_popup_menu_name);
            admin_popup_menu_quantity = itemView.findViewById(R.id.admin_popup_menu_quantity);
            admin_popup_menu_price = itemView.findViewById(R.id.admin_popup_menu_price);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        if (myListener != null) {
                            myListener.onItemClick(v, position);
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }

        void onBind(OrderList items){
            if(viewType == TYPE_STATEMENT){
                onBindStatement(items);
            }else if(viewType == TYPE_MENU){
                onBindMenu(items);
            }

        }

        void onBindStatement(OrderList items){
            admin_popup_payment.setText(items.getStatement());
        }

        void onBindMenu(OrderList items){
            admin_popup_menu_name.setText(items.getMenu());
            admin_popup_menu_quantity.setText(String.valueOf(items.getQuantity()));
            admin_popup_menu_price.setText(String.valueOf(items.getPrice()));
        }

    }

    // view type
    private int TYPE_STATEMENT = 101;
    private int TYPE_MENU = 102;

    private int getViewSrc(int viewType) {
        if (viewType == TYPE_STATEMENT) {
            return R.layout.admin_popup_enter_out;
        } else {
            return R.layout.admin_popup_menu;
        }
    }




    @Override
    public int getItemViewType(int position) {
        if (orderList.get(position).getViewType() == 0) {
            return TYPE_STATEMENT;
        } else {
            return TYPE_MENU;
        }
    }
}

package com.example.openbook.Adapter;

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
        TextView admin_receipt_dialog_item_menuName;
        TextView admin_receipt_dialog_item_menuCount;
        TextView admin_receipt_dialog_item_menuPrice;

        int position;
        int viewType;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType =viewType;

            admin_popup_payment = itemView.findViewById(R.id.admin_popup_payment_body);

            admin_receipt_dialog_item_menuName = itemView.findViewById(R.id.admin_receipt_dialog_item_menuName);
            admin_receipt_dialog_item_menuCount = itemView.findViewById(R.id.admin_receipt_dialog_item_menuCount);
            admin_receipt_dialog_item_menuPrice = itemView.findViewById(R.id.admin_receipt_dialog_item_menuPrice);


            itemView.setOnClickListener(v -> {
                position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    if (myListener != null) {
                        myListener.onItemClick(v, position);
                        notifyDataSetChanged();
                    }
                }
            });
        }

        void onBind(OrderList items){
            if(viewType == TYPE_NOW){
                onBindNow(items);
            }else if(viewType == TYPE_LATER){
                onBindLater(items);
            }

        }

        void onBindNow(OrderList items){
            admin_popup_payment.setText(items.getStatement());
        }

        void onBindLater(OrderList items){
            admin_receipt_dialog_item_menuName.setText(items.getMenu());
            admin_receipt_dialog_item_menuCount.setText(String.valueOf(items.getQuantity()));
            admin_receipt_dialog_item_menuPrice.setText(String.valueOf(items.getPrice()));
        }

    }

    // view type
    private int TYPE_NOW = 101;
    private int TYPE_LATER = 102;

    private int getViewSrc(int viewType) {
        if (viewType == TYPE_NOW) {
            return R.layout.admin_popup_enter_out;
        } else {
            return R.layout.admin_receipt_dialog_item;
        }
    }




    @Override
    public int getItemViewType(int position) {
        if (orderList.get(position).getPaymentType() == 0) {
            return TYPE_NOW;
        } else {
            return TYPE_LATER;
        }
    }
}

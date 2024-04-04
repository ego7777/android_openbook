package com.example.openbook.Adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.R;
import com.example.openbook.Data.CartList;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    ArrayList<CartList> items;
    String TAG = "CartAdapter";


    public interface OnItemClickListener {
        void onPlusClick(View view, int position);

        void onMinusClick(View view, int position);

        void onDeleteClick(View view, int position);

    }

    private CartAdapter.OnItemClickListener myListener = null;

    public void setOnItemClickListener(CartAdapter.OnItemClickListener listener) {
        this.myListener = listener;
    }


    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartAdapter.ViewHolder(view, viewType);
    }


    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.onBind(items.get(position));
        Log.d(TAG, "onCreateViewHolder: onBIndViewHolder");
    }


    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }

        return items.size();
    }

    public void setAdapterItem(ArrayList<CartList> items) {
        this.items = items;
        notifyDataSetChanged();
    }


    /**
     * 뷰홀더 생성
     **/
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView menuName, menuPrice, menuQuantity;

        ImageButton plus, minus, delete;
        private int viewType;


        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            menuName = itemView.findViewById(R.id.cart_item_menuName);
            menuQuantity = itemView.findViewById(R.id.cart_item_count);
            menuPrice = itemView.findViewById(R.id.cart_item_price);

            plus = itemView.findViewById(R.id.plus);
            minus = itemView.findViewById(R.id.minus);
            delete = itemView.findViewById(R.id.delete);


            delete.setOnClickListener(view -> {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    if (myListener != null) {
                        myListener.onDeleteClick(view, position);
                    }
                }
            });


            plus.setOnClickListener(view -> {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    if (myListener != null) {
                        myListener.onPlusClick(view, position);
                    }
                }
            });


            minus.setOnClickListener(view -> {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    if (myListener != null) {
                        myListener.onMinusClick(view, position);
                    }
                }
            });

        }


        void onBind(CartList items) {
            switch (viewType) {
                case MENU:
                    onBindMenu(items);
                    break;

                case SERVER:
                    onBindServer(items);
                    break;

                case ADMIN:
                    onBindAdmin(items);
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + viewType);
            }
        }


        void onBindMenu(CartList items) {
            menuName.setText(items.getMenuName());
            menuPrice.setText(String.valueOf(items.getMenuPrice()));
            menuQuantity.setText(String.valueOf(items.getMenuQuantity()));
        }


        void onBindServer(CartList items) {
            menuName.setText(items.getMenuName());
            menuQuantity.setText(String.valueOf(items.getMenuQuantity()));
            menuPrice.setVisibility(View.GONE);
        }

        void onBindAdmin(CartList items) {
            menuName.setText(items.getMenuName());
            menuQuantity.setText(String.valueOf(items.getMenuQuantity()));

            plus.setVisibility(View.INVISIBLE);
            minus.setVisibility(View.INVISIBLE);
            delete.setVisibility(View.INVISIBLE);
        }
    }


    // view type
    // 1 -> CallServer.class,
    // 2 -> Menu.class,
    // 3 -> Admin.class
    private final int SERVER = 101;
    private final int MENU = 102;
    private final int ADMIN = 103;


    @Override
    public int getItemViewType(int position) {
        switch (items.get(position).getCartCategory().getValue()) {
            case 0:
                return SERVER;
            case 1:
                return MENU;
            case 2:
                return ADMIN;
            default:
                throw new IllegalStateException("Unexpected value: " + items.get(position).getCartCategory());
        }

    }
}

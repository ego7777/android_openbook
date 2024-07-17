package com.example.openbook.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.openbook.R;
import com.example.openbook.Data.MenuList;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    ArrayList<MenuList> menuItem;
    String TAG = "RecyclerViewAdapter";

    /**
     * 커스텀 리스너 인터페이스 정의
     */
    public interface OnItemClickListener {
        void onItemClick(View view, String name, int price, int category, int position);

    }

    private OnItemClickListener myListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.myListener = listener;
    }


    @NonNull
    @Override
    /**
     * 리스트 아이템을 가져와서 레이아웃을 실체화 해줌
     * 실체화를 해주는 아이가 Inflater
     * **/
    public MenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);

        return new MenuAdapter.ViewHolder(view);
    }

    /**
     * 액티비티에서 받아온 데이터를 바인딩해줌.
     **/
    @Override
    public void onBindViewHolder(@NonNull MenuAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.onBind(menuItem.get(position), holder.itemView.getContext());
    }

    @Override
    public int getItemCount() {
        if (menuItem == null) {
            return 0;
        }

        return menuItem.size();
    }

    public void setAdapterItem(ArrayList<MenuList> menu) {
        this.menuItem = menu;
        notifyDataSetChanged();
    }


    /**
     * 뷰홀더 생성
     **/
    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView menuImage;
        TextView menuName, menuPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            menuImage = itemView.findViewById(R.id.menu_image);
            menuName = itemView.findViewById(R.id.menu_name);
            menuPrice = itemView.findViewById(R.id.menu_price);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                String name = menuItem.get(position).getMenuName();
                int price = menuItem.get(position).getMenuPrice();
                int category = menuItem.get(position).getMenuCategory();

                if (position != RecyclerView.NO_POSITION) {
                    if (myListener != null) {
                        myListener.onItemClick(view, name, price, category, position);
                    }
                }
            });


        }

        void onBind(MenuList items, Context context) {
            Glide.with(context).load(items.getUrl()).into(menuImage);
            menuName.setText(items.getMenuName());
            menuPrice.setText(String.valueOf(items.getMenuPrice()));
        }

    }


}


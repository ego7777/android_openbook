package com.example.openbook.Adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        void onItemClick(View view, int id, String name, int price, int position);

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
        View view = LayoutInflater.from(parent.getContext()).inflate(getViewSrc(viewType), parent, false);

        return new ViewHolder(view, viewType);
    }

    /**
     * 액티비티에서 받아온 데이터를 바인딩해줌.
     **/
    @Override
    public void onBindViewHolder(@NonNull MenuAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.onBind(menuItem.get(position));
//        Log.d(TAG, "onCreateViewHolder: onBIndViewHolder");
    }


    /**
     * 리사이클러뷰 리스트 사이즈를 불러옴
     **/
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

        ImageView menu_image;
        TextView menu_name;
        TextView menu_price;
        private int viewType;


        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            if(viewType == TYPE_YES){

                menu_image = itemView.findViewById(R.id.menu_image);
                menu_name = itemView.findViewById(R.id.menu_name);
                menu_price = itemView.findViewById(R.id.menu_price);
                /**
                 * 아이템뷰 클릭
                 */
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();
                        int id = menuItem.get(position).getRedId();
                        String name = menuItem.get(position).getMenu_name();
                        int price = menuItem.get(position).getMenu_price();
                        Log.d(TAG, "onClick: 2");

                        if (position != RecyclerView.NO_POSITION) {
                            Log.d(TAG, "onClick: 3");
                            if (myListener != null) {
                                myListener.onItemClick(view, id, name, price, position);
                            }
                        }
                    }
                });
            }


        }

        void onBind(MenuList item) {
            if (viewType == TYPE_NO) {
                onBindNo(item);
            } else if (viewType == TYPE_YES) {
                onBindYes(item);
            }
        }

        void onBindYes(MenuList items) {
            menu_image.setImageResource(items.getRedId());
            menu_name.setText(items.getMenu_name());
            menu_price.setText(String.valueOf(items.getMenu_price()));
        }

        void onBindNo(MenuList items) {
            ImageView temp_img = itemView.findViewById(R.id.temp_img);
            TextView temp_name = itemView.findViewById(R.id.temp_name);
//            TextView temp_price = itemView.findViewById(R.id.temp_price);
            temp_img.setImageResource(items.getRedId());
            temp_name.setText(items.getMenu_name());
//            temp_price.setText(String.valueOf(items.getMenu_price()));
        }

    }

        // view type
        private int TYPE_NO = 101;
        private int TYPE_YES = 102;


        private int getViewSrc(int viewType) {
            if (viewType == TYPE_NO) {
                return R.layout.menu_gridview_item_empty;
            } else {
                return R.layout.menu_gridview_item;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (menuItem.get(position).getViewType() == 0) {
                return TYPE_NO;
            } else {
                return TYPE_YES;
            }
        }
    }


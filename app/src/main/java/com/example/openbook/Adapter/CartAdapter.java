package com.example.openbook.Adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.R;
import com.example.openbook.Data.CartList;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    ArrayList<CartList> items;
    String TAG = "CartAdapter";



    /**
     * 커스텀 리스너 인터페이스 정의
     */
    public interface OnItemClickListener{
        void onPlusClick(View view, int position);
        void onMinusClick(View view, int position);
        void onDeleteClick(View view, int position);

    }

    private CartAdapter.OnItemClickListener myListener = null;

    public void setOnItemClickListener(CartAdapter.OnItemClickListener listener){
        this.myListener = listener;
    }



    @NonNull
    @Override
    /**
     * 리스트 아이템을 가져와서 레이아웃을 실체화 해줌
     * 실체화를 해주는 아이가 Inflater
     * **/
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_listview_item, parent, false);
//        Log.d(TAG, "onCreateViewHolder: view");
        return new CartAdapter.ViewHolder(view, viewType);
    }

    /**
     * 액티비티에서 받아온 데이터를 바인딩해줌.
     * **/
    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.onBind(items.get(position));
        Log.d(TAG, "onCreateViewHolder: onBIndViewHolder");
    }



    /**
     * 리사이클러뷰 리스트 사이즈를 불러옴
     * **/
    @Override
    public int getItemCount() {
        if(items == null){
            return  0;
        }

        return items.size();
    }

    public  void setAdapterItem(ArrayList<CartList> items){
        this.items = items;
        notifyDataSetChanged();
    }


    /**
     * 뷰홀더 생성
     * **/
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView menu_name;
        TextView menu_price;
        TextView menu_count;
        Button plus;
        Button minus;
        Button delete;
        private int viewType;





        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType =viewType;

            menu_name = itemView.findViewById(R.id.cart_menu_name);
            menu_price = itemView.findViewById(R.id.cart_menu_price);
            menu_count = itemView.findViewById(R.id.menu_count);
            plus = itemView.findViewById(R.id.plus);
            minus = itemView.findViewById(R.id.minus);
            delete = itemView.findViewById(R.id.delete);


            /**
             * 아이템뷰 삭제하는거 만들어야함
             */
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (myListener != null) {
                            myListener.onDeleteClick(view, position);
                        }
                    }
                }
            });


            /**
             * 아이템뷰 플러스 마이너스 만들기
             */

            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (myListener != null) {
                            myListener.onPlusClick(view, position);
                        }
                    }
                }
            });


            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (myListener != null) {
                            myListener.onMinusClick(view, position);
                        }
                    }
                }
            });

        }


        void onBind(CartList items) {
            if (viewType == TYPE_NO) {
                onBindNo(items);
            } else if (viewType == TYPE_YES) {
                onBindYes(items);
            }
        }


        void onBindYes(CartList items) {
            menu_name.setText(items.getMenu_name());
            menu_price.setText(String.valueOf(items.getMenu_price()));
            menu_count.setText(String.valueOf(items.getMenu_count()));
        }

        void onBindNo(CartList items) {
            menu_name.setText(items.getMenu_name());
            menu_count.setText(String.valueOf(items.getMenu_count()));
            menu_price.setVisibility(itemView.INVISIBLE);


        }
    }


    // view type
    private int TYPE_NO = 101;
    private int TYPE_YES = 102;




    @Override
    public int getItemViewType(int position) {
        if (items.get(position).getViewType() == 0) {
            return TYPE_NO;
        } else {
            return TYPE_YES;
        }
    }
}

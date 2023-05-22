package com.example.openbook.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.R;
import com.example.openbook.Data.ChattingList;

import java.util.ArrayList;

public class ChattingAdapter extends RecyclerView.Adapter<ChattingAdapter.ViewHolder>{

    ArrayList<ChattingList> chat = new ArrayList<>();
    String TAG = "ChattingAdapter";

    @NonNull
    @Override
    /**
     * 리스트 아이템을 가져와서 레이아웃을 실체화 해줌
     * 실체화를 해주는 아이가 Inflater
     * **/
    public ChattingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getViewSrc(viewType), parent, false);
        return new ChattingAdapter.ViewHolder(view, viewType);
    }

    /**
     * 액티비티에서 받아온 데이터를 바인딩해줌.
     * **/
    @Override
    public void onBindViewHolder(@NonNull ChattingAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.onBind(chat.get(position));
    }



    /**
     * 리사이클러뷰 리스트 사이즈를 불러옴
     * **/
    @Override
    public int getItemCount() {
        if(chat == null){
            return  0;
        }

        return chat.size();
    }

    public  void setAdapterItem(ArrayList<ChattingList> items){
        this.chat = items;
        notifyDataSetChanged();
    }


    /**
     * 뷰홀더 생성
     * **/
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView chatting_text;
        ImageView chatting_image;
        TextView chatting_time;
        TextView chatting_read;

        private int viewType;


        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType =viewType;

            if(viewType == TYPE_Receive){
                chatting_text = itemView.findViewById(R.id.chatting_receiver_text);
                chatting_image =itemView.findViewById(R.id.chatting_receiver_image);
                chatting_time = itemView.findViewById(R.id.chatting_receiver_time);
            }else if(viewType == TYPE_Send){
                chatting_text = itemView.findViewById(R.id.chatting_sender_text);
                chatting_time = itemView.findViewById(R.id.chatting_sender_time);
                chatting_read = itemView.findViewById(R.id.chatting_sender_read);

            }


        }


        void onBind(ChattingList items) {
            if (viewType == TYPE_Receive) {
                onBindReceive(items);
            } else if (viewType == TYPE_Send) {
                onBindSend(items);
            }
        }


        void onBindReceive(ChattingList items) {

            chatting_image.setImageResource(items.getImgId());
            chatting_text.setText(items.getText());
            chatting_time.setText(items.getTime());

        }

        void onBindSend(ChattingList items) {
            chatting_text.setText(items.getText());
            chatting_time.setText(items.getTime());
            chatting_read.setText(items.getRead());

        }
    }


    // view type
    private int TYPE_Receive = 101;
    private int TYPE_Send = 102;

    private int getViewSrc(int viewType) {
        if (viewType == TYPE_Receive) {
            return R.layout.chatting_item_receive;
        } else {
            return R.layout.chatting_item_sender;
        }
    }




    @Override
    public int getItemViewType(int position) {
        if (chat.get(position).getViewType() == 0) {
            return TYPE_Receive;
        } else {
            return TYPE_Send;
        }
    }
}


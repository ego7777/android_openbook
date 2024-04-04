package com.example.openbook.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.R;
import com.example.openbook.Data.RequestList;


import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    ArrayList<RequestList> requestLists = new ArrayList<>();
    String TAG = "requestAdapter";


    /**
     * 커스텀 리스너 인터페이스 정의
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position, String name);

    }

    private RequestAdapter.OnItemClickListener myListener = null;

    public void setOnItemClickListener(RequestAdapter.OnItemClickListener listener) {
        this.myListener = listener;
    }


    @NonNull
    @Override
    /**
     * 리스트 아이템을 가져와서 레이아웃을 실체화 해줌
     * 실체화를 해주는 아이가 Inflater
     * **/
    public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_service, parent, false);
        return new RequestAdapter.ViewHolder(view);
    }

    /**
     * 액티비티에서 받아온 데이터를 바인딩해줌.
     **/
    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.onBind(requestLists.get(position));

    }


    /**
     * 리사이클러뷰 리스트 사이즈를 불러옴
     **/
    @Override
    public int getItemCount() {
        if (requestLists == null) {
            return 0;
        }

        return requestLists.size();
    }

    public void setAdapterItem(ArrayList<RequestList> items) {
        this.requestLists = items;
        notifyDataSetChanged();
    }

    /**
     * 뷰홀더 생성
     **/
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView request;
        int position;
        String name;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            request = itemView.findViewById(R.id.request_item_list);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    position = getAdapterPosition();
                    name = requestLists.get(position).getRequest();

                    if (position != RecyclerView.NO_POSITION) {
                        if (myListener != null) {
                            myListener.onItemClick(view,position, name);
                                    notifyDataSetChanged();
                        }
                    }
                }
            });

        }


        void onBind(RequestList items) {
            request.setText(items.getRequest());

        }
    }
}

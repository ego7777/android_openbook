package com.example.openbook.Deco;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.R;

public class menu_recyclerview_deco extends RecyclerView.ItemDecoration {
     String TAG = "Deco";
    private Context context;
    Drawable mDivider;

    public menu_recyclerview_deco(Context context) {
        this.context = context;
        mDivider = context.getResources().getDrawable(R.drawable.line_divider);
    }


    //각 항목을 배치할 때 호출된다
    //Rect - 항목을 구성하기 위한 사각형 정보가 호출
    //view - 항목을 구성하기 위한 view 호출
//    @Override
//    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
//                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//        super.getItemOffsets(outRect, view, parent, state);
//
//        //항목의 index값 획득
//        int index = parent.getChildAdapterPosition(view);
//
//        if(index%3==3){
//            //인덱스가 3으로 나누어 떨어지는 경우, 즉 항목3개 마다 세로방향 여백을 60을 준다
//            outRect.set(10, 20, 10, 20 );
//        }
//
//
//        //배경색을 지정함
//        view.setBackgroundColor(context.getResources().getColor(R.color.white));
//
//        //전달되는 데이터 타입별로 떠있는 효과를 다르게 줌
//        ViewCompat.setElevation(view, 20.0f); //떠있는 효과를 준다
//
//    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

        Paint paint = new Paint();
        paint.setColor(Color.GRAY);

        int left = parent.getPaddingStart();
        int right = parent.getWidth()-parent.getPaddingRight();


        for(int i=0; i<parent.getChildCount(); i++){
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }

    }

}

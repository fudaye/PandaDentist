package com.pandadentist.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.pandadentist.R;
import com.pandadentist.entity.SelectEntity;
import com.pandadentist.listener.OnFilterClickListener;
import com.pandadentist.util.AnimationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Ford on 2016/8/4.
 * <p>
 * 筛选  指示器
 */
public class IndicatorFilterView extends LinearLayout {


    private int mTextColorNormal;
    private int mTextColor;
    private int mTextSize = 14;
    private int mDefaultPosition = 0;
    private SparseArray<SelectPopWindow> mPopWins = new SparseArray<>();
    private Paint dividerPaint;
    private int dividerPadding = 10;
    private int dividerWidth = 1;
    private int isGridPosition = -1;//是否是以GridView的形式展示
    private OnFilterClickListener l;
    private Context mContext;

    public IndicatorFilterView(Context context) {
        super(context);
        this.mContext = context;
    }

    public IndicatorFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(context, attrs);
    }

    public IndicatorFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IndicatorFilterView);
        mTextColor = ta.getColor(R.styleable.IndicatorFilterView_text_color, context.getResources().getColor(R.color.themeColor));
        mTextSize = (int) ta.getDimension(R.styleable.IndicatorFilterView_text_size, mTextSize);
        ta.recycle();
        mTextColorNormal = context.getResources().getColor(android.R.color.white);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        dividerPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dividerPadding,dm);
        dividerWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dividerWidth,dm);
        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(dividerWidth);
    }

    public void setLabels(HashMap<String, ArrayList<SelectEntity>> labels) {
        this.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        int size = labels.size();
        Iterator<String> keys = labels.keySet().iterator();
        for (int i = 0; i < size; i++) {
            View v = inflater.inflate(R.layout.custom_fuindicator, this, false);
            v.setTag(i);
            v.setOnClickListener(new ClickTab());
            TextView tv = (TextView) v.findViewById(R.id.tv);
            ImageView iv = (ImageView) v.findViewById(R.id.iv);
            String key = keys.next();
            tv.setText(key);
            if (mDefaultPosition == i) {
                tv.setTextColor(mTextColor);
                iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_indicator_filter_f));
            } else {
                tv.setTextColor(mTextColorNormal);
            }
            SelectPopWindow spw;
            if (isGridPosition == i) {
                spw = new SelectPopWindow(mContext, labels.get(key), i, new GridLayoutManager(mContext, 4), true);
            } else {
                spw = new SelectPopWindow(mContext, labels.get(key), i, new LinearLayoutManager(mContext), false);
            }
            spw.setOnItemClickListener(new PopWindowClick());
            spw.setOnDismissListener(new PopDismiss(v));
            mPopWins.put(i,spw);
            addView(v);
        }
    }

    public void setDefaultPositon(int positon) {
        this.mDefaultPosition = positon;
    }

    public void setGridViewPosition(int position) {
        this.isGridPosition = position;
    }

    private class ClickTab implements OnClickListener {

        @Override
        public void onClick(View v) {
            clearStyle();
            selectStyle(v);
            int posi = (int) v.getTag();
            mPopWins.get(posi).showPopupWindow(v);
            ImageView iv = (ImageView) v.findViewById(R.id.iv);
            AnimationUtil.rotateUpView(getContext(),iv);
        }
    }

    private class PopWindowClick implements SelectPopWindow.OnItemClickListener{
        @Override
        public void onItemClick(View view, int parentPosition, int childPosition, String label) {
            ((TextView)getChildAt(parentPosition).findViewById(R.id.tv)).setText(label);
            if(l!= null){
                l.onClickItem(view,parentPosition,childPosition);
            }
        }
    }

    private class PopDismiss implements PopupWindow.OnDismissListener{

        private View view;

        public PopDismiss(View view) {
            this.view = view;
        }

        @Override
        public void onDismiss() {
            ImageView iv = (ImageView) view.findViewById(R.id.iv);
            AnimationUtil.rotateDownView(getContext(),iv);
        }
    }

    private void selectStyle(View v){
        TextView tv = (TextView) v.findViewById(R.id.tv);
        ImageView iv = (ImageView) v.findViewById(R.id.iv);
        tv.setTextColor(mTextColor);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_indicator_filter_f));
    }

    private void clearStyle() {
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View v = getChildAt(i);
            TextView tv = (TextView) v.findViewById(R.id.tv);
            ImageView iv = (ImageView) v.findViewById(R.id.iv);
            tv.setTextColor(mTextColorNormal);
            iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_indicator_filter_n));
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        dividerPaint.setColor(getResources().getColor(R.color.themeColor));
        int size = getChildCount();
        final int height = getHeight();
        for (int i = 0; i <size  - 1; i++) {
            View tab = getChildAt(i);
            canvas.drawLine(tab.getRight(),dividerPadding , tab.getRight(), height-dividerPadding, dividerPaint);
        }
    }

    public void setOnFilterClickListener(OnFilterClickListener l){
        this.l = l;
    }
}

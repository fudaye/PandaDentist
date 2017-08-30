package com.pandadentist.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.pandadentist.R;
import com.pandadentist.listener.OnProgressListener;
import com.pandadentist.util.DensityUtil;

/**
 *   渐变色进度条
 */

public class ColorProgressBar extends View {

    private static final String TAG = ColorProgressBar.class.getSimpleName();
    private Context mContext;

    //进度条高度
    private int mProgressHeight;
    private int mProgressWidth;
    //绘制进度条
    private Paint mProgressPaint;
    //线性渐变色
    private LinearGradient linearGradient;
    //渐变颜色
    private static  int DEFAULT_START_COLOR = Color.parseColor("#20CBE7");
    private static int DEFAULT_END_COLOR = Color.parseColor("#FFAAC2");
    //当前进度
    private float mCurrentProgressNum = 0;
    //动画时间
    private long mAnimTime;
    //属性动画
    private ValueAnimator mAnimator;
    //绘制进度条背景
    private Paint mProgressBgPaint;
    private int mProgressBgColor ;
    private int mMax = 100;
    private float mPercent;
    private OnProgressListener onProgressListener;



    public ColorProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mAnimator = new ValueAnimator();
        mProgressBgColor =context.getResources().getColor(R.color.gray);
        initPaint();
        setValue(mCurrentProgressNum);
    }



    private void initPaint() {
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        // 设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE
        mProgressPaint.setColor(Color.parseColor("#FF618E"));
        mProgressPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔粗细
        mProgressPaint.setStrokeWidth(DensityUtil.dp(mProgressHeight));

        mProgressBgPaint = new Paint();
        mProgressBgPaint.setAntiAlias(true);
        mProgressBgPaint.setColor(mProgressBgColor);
        mProgressBgPaint.setStyle(Paint.Style.STROKE);
        mProgressBgPaint.setStrokeWidth(DensityUtil.dp(mProgressHeight));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMySize(100, widthMeasureSpec);
        int height = getMySize(100, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int getMySize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED: {//如果没有指定大小，就设置为默认大小
                mySize = defaultSize;
                break;
            }
            case MeasureSpec.AT_MOST: {//如果测量模式是最大取值为size
                //我们将大小取最大值,你也可以取其他值
                mySize = size;
                break;
            }
            case MeasureSpec.EXACTLY: {//如果是固定的大小，那就不要去改变它
                mySize = size;
                break;
            }
        }
        return mySize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged: w = " + w + "; h = " + h + "; oldw = " + oldw + "; oldh = " + oldh);
        mProgressWidth = w;
        mProgressHeight = h;
        initPaint();
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgress(canvas);
    }


    private void drawProgress(Canvas canvas) {
        canvas.save();
        //绘制背景
        canvas.drawLine(0, 0, mProgressWidth, 0, mProgressBgPaint);
        //绘制当前进度
        canvas.drawLine(0, 0, mProgressWidth/mMax*mCurrentProgressNum, 0, mProgressPaint);
        linearGradient = new LinearGradient(0, 0, mProgressWidth, 0, DEFAULT_START_COLOR, DEFAULT_END_COLOR, Shader.TileMode.CLAMP);
        mProgressPaint.setShader(linearGradient);
        canvas.restore();
    }


    /**
     * 设置当前值
     *
     * @param value
     */
    public void setValue(float value) {
        if(value>mMax){
            value = mMax;
        }
        this.mCurrentProgressNum = DensityUtil.dp((int) value);
        float start = mPercent;
        float end = value / mMax;

        startAnimator(start,end);
    }

    private void startAnimator(float start, float end) {
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(mAnimTime);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPercent = (float) animation.getAnimatedValue();
                mCurrentProgressNum = mPercent * mMax;
                float f = mProgressWidth/mMax*mCurrentProgressNum;
                Log.d("onAnimationUpdate", "mPercent-->" + mPercent +"mCurrentProgressNum-->"+mCurrentProgressNum+"f-->"+f);
                if(onProgressListener != null){

                    onProgressListener.onProgress((int)mCurrentProgressNum,mPercent*100);
                }
                invalidate();
            }
        });
        mAnimator.start();
    }

    /**
     * 重置
     */
    public void reset() {
        this.mCurrentProgressNum = 0;
        startAnimator(mPercent, 0.0f);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //释放资源
    }

    public void setOnProgressListener(OnProgressListener onProgressListener){
        this.onProgressListener = onProgressListener;
    }

    public void setProgressColor(){
        //#FF618E
        DEFAULT_START_COLOR = Color.parseColor("#FF618E");
        DEFAULT_END_COLOR = Color.parseColor("#FF618E");
        mProgressBgColor =Color.parseColor("#FF618E");
        initPaint();
        invalidate();
    }
}

package com.anton46.stepsview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class StepsViewIndicator extends View {
    private static final int THUMB_SIZE = 50;

    private Paint paint = new Paint();
    private Paint selectedPaint = new Paint();
    private Paint textSelectPaint = new Paint();
    private Paint textDefaultPaint = new Paint();
    private boolean needShowNumber = true;
    private int mNumOfStep = 2;
    //默认文字的大小
    float numberTextSize = 40.0f;
    //进度的线高度
    private float mLineHeight;
    private float mThumbRadius;
    //实心圆的圆半径
    private float mCircleRadius;
    //实心圆与外围透明圆的偏移量，这里讲的主要是现对于的实心圆的倍数
    private float outerCircleOffset = 1.2f;
    private float mPadding;
    private int mProgressColor = Color.YELLOW;
    private int mBarColor = Color.BLACK;
//    public String[] labels = {"1", "2", "3"};
    public String[] labels;
    private int circleSelectColor;
    private int circleDefaultColor;
    private float mCenterY;
    private float mLeftX;
    private float mLeftY;
    private float mRightX;
    private float mRightY;
    private float mDelta;
    private List<Float> mThumbContainerXPosition = new ArrayList<>();
    private int mCompletedPosition;
    private OnDrawListener mDrawListener;
    private int textSelectColor = Color.WHITE;
    private int textDefaultColor;

    public void updateLabels(String[] str){
        labels = str;
    }

    public StepsViewIndicator(Context context) {
        this(context, null);

    }

    public StepsViewIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepsViewIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.StepsViewIndicator);
        mNumOfStep = a.getInt(R.styleable.StepsViewIndicator_numOfSteps, 0);
        a.recycle();

        init();
    }

    private void init() {
        mLineHeight = 0.1f * THUMB_SIZE;
        mThumbRadius = 0.7f * THUMB_SIZE;
        mCircleRadius = 0.8f * mThumbRadius;
        mPadding = 0.8f * THUMB_SIZE;
    }

    public void setStepSize(int size) {
        mNumOfStep = size;
        invalidate();
    }

    public void setDrawListener(OnDrawListener drawListener) {
        mDrawListener = drawListener;
    }

    public List<Float> getThumbContainerXPosition() {
        return mThumbContainerXPosition;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCenterY = 0.5f * getHeight();
        mLeftX = mPadding;
        mLeftY = mCenterY - (mLineHeight / 2);
        mRightX = getWidth() - mPadding;
        mRightY = 0.5f * (getHeight() + mLineHeight);
        mDelta = (mRightX - mLeftX) / (mNumOfStep - 1);

        mThumbContainerXPosition.add(mLeftX);
        for (int i = 1; i < mNumOfStep - 1; i++) {
            mThumbContainerXPosition.add(mLeftX + (i * mDelta));
        }
        mThumbContainerXPosition.add(mRightX);
        mDrawListener.onReady();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getWidth();
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = THUMB_SIZE + 20;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    public void setCompletedPosition(int position) {
        mCompletedPosition = position;
    }

    public void reset() {
        setCompletedPosition(0);
    }

    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
    }

    public void setBarColor(int barColor) {
        mBarColor = barColor;
    }

    public void setNeedShowNumber(boolean needShowNumber) {
        this.needShowNumber = needShowNumber;
    }

    public void setNumOfStep(int mNumOfStep) {
        this.mNumOfStep = mNumOfStep;
    }

    public void setNumberTextSize(float numberTextSize) {
        this.numberTextSize = numberTextSize;
    }

    public void setmLineHeight(float mLineHeight) {
        this.mLineHeight = mLineHeight;
    }

    public void setmThumbRadius(float mThumbRadius) {
        this.mThumbRadius = mThumbRadius;
    }

    public void setOuterCircleOffset(float outerCircleOffset) {
        this.outerCircleOffset = outerCircleOffset;
    }

    public void setmProgressColor(int mProgressColor) {
        this.mProgressColor = mProgressColor;
    }

    public void setmBarColor(int mBarColor) {
        this.mBarColor = mBarColor;
    }

    public void setCircleSelectColor(int circleSelectColor) {
        this.circleSelectColor = circleSelectColor;
    }

    public void setCircleDefaultColor(int circleDefaultColor) {
        this.circleDefaultColor = circleDefaultColor;
    }

    public void setTextDefaultColor(int textDefaultColor) {
        this.textDefaultColor = textDefaultColor;
    }

    public void setTextSelectColor(int textSelectColor) {
        this.textSelectColor = textSelectColor;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawListener.onReady();
        // Draw rect bounds
        paint.setAntiAlias(true);
        paint.setColor(mBarColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);

        selectedPaint.setAntiAlias(true);
        selectedPaint.setColor(mProgressColor);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(2);
        selectedPaint.setStyle(Paint.Style.FILL);

        //number选中的文字颜色
        textSelectPaint.setColor(textSelectColor);
        textSelectPaint.setTextSize(numberTextSize); //以px为单位

        //number默认文字显示
//        textDefaultColor = mBarColor;
        textDefaultPaint.setColor(textDefaultColor);
        textDefaultPaint.setTextSize(numberTextSize); //以px为单位

        //画进度条
        for (int i = 0; i < mThumbContainerXPosition.size() - 1; i++) {
            final float pos = mThumbContainerXPosition.get(i)+35;
            final float pos2 = mThumbContainerXPosition.get(i + 1)-35;
            canvas.drawRect(pos, mLeftY, pos2, mRightY,
                    (i < mCompletedPosition) ? selectedPaint : paint);
        }

        //绘制外面的带透明的圆
        paint.setColor(circleDefaultColor);
        selectedPaint.setColor(circleSelectColor);
        for (int i = 0; i < mThumbContainerXPosition.size(); i++) {
            final float pos = mThumbContainerXPosition.get(i);
            canvas.drawCircle(pos, mCenterY, mCircleRadius * outerCircleOffset, (i <= mCompletedPosition) ? selectedPaint : paint);
        }

        //绘制圆环，SelectPaint选择表示绘制红色以及内部的进度的数字
        selectedPaint.setColor(mProgressColor);
        paint.setColor(Color.WHITE);
        for (int i = 0; i < mThumbContainerXPosition.size(); i++) {
            canvas.drawCircle(mThumbContainerXPosition.get(i), mCenterY, mCircleRadius,
                    (i <= mCompletedPosition) ? selectedPaint : paint);
            if(needShowNumber) {
                canvas.drawText(labels[i], mThumbContainerXPosition.get(i) - 12, mCenterY + 13,
                        (i <= mCompletedPosition) ? textSelectPaint : textDefaultPaint);
            }
        }
    }

    public interface OnDrawListener {
        public void onReady();
    }
}

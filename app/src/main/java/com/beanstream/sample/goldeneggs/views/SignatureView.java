package com.beanstream.sample.goldeneggs.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 */
public class SignatureView extends View {

    private static final float TOUCH_TOLERANCE = 4;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private int backgndColor = Color.LTGRAY;

    private float mX, mY;

    private boolean hasSignature = false;

    public SignatureView(Context c, AttributeSet a) {
        super(c, a);

        mPaint = new Paint(Paint.DITHER_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2);

        mPath = new Path();

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int height = metrics.heightPixels / 2;// 219;
        int width = metrics.widthPixels / 2; // 714;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        mCanvas = new Canvas(mBitmap);

        clear();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.drawPath(mPath, mPaint);
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            hasSignature = true;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void clear() {

        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(backgndColor);
        RectF border = new RectF(1, 1, mBitmap.getWidth() - 1, mBitmap.getHeight() - 1);
        mCanvas.drawRoundRect(border, 12f, 12f, mPaint);

        mPath = new Path();
        invalidate();
        hasSignature = false;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {


        int curW = mBitmap != null ? mBitmap.getWidth() : 714;
        int curH = mBitmap != null ? mBitmap.getHeight() : 219;
        if (curW >= width && curH >= height) {
            return;
        }else{
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            clear();
        }
    }

    public Bitmap getSignatureBitMap(){
        return mBitmap;
    }

    public boolean isSigned() {
        return hasSignature;
    }
}


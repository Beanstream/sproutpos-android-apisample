package com.beanstream.sample.goldeneggs.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 */
public class SignatureView extends View {

    private static final float TOUCH_TOLERANCE = 4;

    private Bitmap bitmap;
    private Canvas canvas;
    private Path path;
    private Paint paint;

    private float mX, mY;

    int height, width;

    private boolean hasSignature = false;

    public signatureCallback callback;

    public interface signatureCallback {
        void hasSignature(boolean hasSignature);
    }

    public SignatureView(Context c, AttributeSet a) {
        super(c, a);

        paint = new Paint(Paint.DITHER_FLAG);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(2);
        paint.setFilterBitmap(true);

        path = new Path();
    }

    /**
     * This should be called once we know the size of the view holding the bitmap
     *
     * @param width  Width of bitmap
     * @param height Height of bitmap
     */
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        clear();
    }


    public void hasSignature(signatureCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
            canvas.drawPath(path, paint);
        }
    }

    private void touch_start(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            hasSignature = true;
            if (callback != null) {
                callback.hasSignature(true);
            }
        }
    }

    private void touch_up() {
        path.lineTo(mX, mY);
        // commit the path to our offscreen
        canvas.drawPath(path, paint);
        // kill this so we don't double draw
        path.reset();
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

        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.LTGRAY);
        RectF border = new RectF(1, 1, width - 1, height - 1);
        canvas.drawRoundRect(border, 12f, 12f, paint);

        path = new Path();
        invalidate();
        hasSignature = false;
        if (callback != null) {
            callback.hasSignature(false);
        }
    }

    public Bitmap getSignatureBitMap() {
        float scalePercent = 0.4f;
        return resizeBitmap(bitmap, (int) (bitmap.getWidth() * scalePercent), (int) (bitmap.getHeight() * scalePercent), scalePercent);
    }

    public boolean isSigned() {
        return hasSignature;
    }

    public Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight, float scalePercent) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.RGB_565);

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scalePercent, scalePercent, 0, 0);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }
}



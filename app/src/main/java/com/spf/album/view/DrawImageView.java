package com.spf.album.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spf.album.utils.LogUtils;
import com.spf.album.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class DrawImageView extends View {
    private static final String TAG = DrawImageView.class.getSimpleName();
    public static final int MODE_LINE = 1;
    public static final int MODE_WORD = 2;
    public static final int MODE_MOSAIC = 3;
    private Bitmap mSourceBitmap;

    private int mode;
    private Paint mLinePaint;
    private Path mLinePath;
    private List<Path> mLinePathList;

    public DrawImageView(@NonNull Context context) {
        this(context, null);
    }

    public DrawImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(ScreenUtils.dp2px(3));
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePathList = new ArrayList<>();
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setImageBitmap(Bitmap bitmap) {
        mSourceBitmap = bitmap;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onTouchEnd();
                break;
        }
        invalidate();
        return true;
        //return super.onTouchEvent(event);
    }

    private void onTouchDown(float x, float y) {
        switch (mode) {
            case MODE_LINE:
                mLinePath = new Path();
                mLinePath.moveTo(x, y);
                break;
        }
    }

    private void onTouchMove(float x, float y) {
        switch (mode) {
            case MODE_LINE:
                mLinePath.lineTo(x, y);
                break;
        }
    }

    private void onTouchEnd() {
        switch (mode) {
            case MODE_LINE:
                mLinePathList.add(mLinePath);
                mLinePath = null;
                break;
        }
    }

    private void drawLine(Canvas canvas) {
        if (mLinePath != null) {
            canvas.drawPath(mLinePath, mLinePaint);
        }
        for (Path path : mLinePathList) {
            canvas.drawPath(path, mLinePaint);
        }
    }

    public void cancelEdit() {
        switch (mode) {
            case MODE_LINE:
                if (mLinePathList.isEmpty()) {
                    return;
                }
                mLinePathList.remove(mLinePathList.size() - 1);
                invalidate();
                break;
        }
    }

    public void saveImage() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSourceBitmap == null || mSourceBitmap.isRecycled()) {
            LogUtils.d(TAG, "Bitmap is null or recycled");
            return;
        }
        canvas.drawBitmap(mSourceBitmap, 0, 0, null);
        switch (mode) {
            case MODE_LINE:
                drawLine(canvas);
                break;
        }
    }
}
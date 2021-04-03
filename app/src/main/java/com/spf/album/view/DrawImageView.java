package com.spf.album.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
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
    public static final int MODE_NONE = 0;
    public static final int MODE_LINE = 1;
    public static final int MODE_WORD = 2;
    public static final int MODE_MOSAIC = 3;
    private Bitmap mSourceBitmap;

    private int mode = MODE_NONE;
    private Paint mLinePaint;
    private Path mLinePath;
    private Word mClickWord;
    private float mLastTouchX, mLastTouchY;
    private final List<Path> mLinePathList = new ArrayList<>();
    private final List<Word> mWordList = new ArrayList<>();
    private final List<Integer> mTraceList = new ArrayList<>();

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
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setImageBitmap(Bitmap bitmap) {
        mSourceBitmap = bitmap;
        postInvalidate();
    }

    public void addWord(String content, int color) {
        Word word = new Word(content, color);
        int width = word.rect.width();
        int height = word.rect.height();
        int left = (getWidth() - width) / 2;
        int top = (getHeight() - height) / 2;
        word.rect.set(left, top, left + width, top + height);
        mWordList.add(word);
        mTraceList.add(MODE_WORD);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchX = event.getX();
                mLastTouchY = event.getY();
                onTouchDown(mLastTouchX, mLastTouchY);
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
    }

    private void onTouchDown(float x, float y) {
        if (isClickWord(x, y)) {
            return;
        }
        switch (mode) {
            case MODE_LINE:
                mLinePath = new Path();
                mLinePath.moveTo(x, y);
                break;
        }
    }

    private void onTouchMove(float x, float y) {
        if (mClickWord != null) {
            int xDistance = (int) (x - mLastTouchX);
            int yDistance = (int) (y - mLastTouchY);
            int left = mClickWord.rect.left + xDistance;
            int right = mClickWord.rect.right + xDistance;
            int top = mClickWord.rect.top + yDistance;
            int bottom = mClickWord.rect.bottom + yDistance;
            if (left > 5 && right < getWidth() - 5 && top > 5 && bottom < getHeight() - 5) {
                mClickWord.rect.set(left, top, right, bottom);
            }
            mLastTouchX = x;
            mLastTouchY = y;
            return;
        }
        switch (mode) {
            case MODE_LINE:
                mLinePath.lineTo(x, y);
                break;
        }
    }

    private void onTouchEnd() {
        if (mClickWord != null) {
            return;
        }
        switch (mode) {
            case MODE_LINE:
                mLinePathList.add(mLinePath);
                mTraceList.add(MODE_LINE);
                mLinePath = null;
                break;
        }
    }

    private boolean isClickWord(float x, float y) {
        mClickWord = null;
        if (mWordList.isEmpty()) {
            return false;
        }
        int intX = (int) x;
        int intY = (int) y;
        for (Word word : mWordList) {
            if (word.rect.contains(intX, intY)) {
                mClickWord = word;
                return true;
            }
        }
        return false;
    }

    private void drawLine(Canvas canvas) {
        if (mLinePath != null) {
            canvas.drawPath(mLinePath, mLinePaint);
        }
        for (Path path : mLinePathList) {
            canvas.drawPath(path, mLinePaint);
        }
    }

    private void drawWords(Canvas canvas) {
        for (Word word : mWordList) {
            canvas.drawText(word.content, word.rect.left, word.rect.bottom, word.paint);
        }
    }

    public void cancelEdit() {
        if (mTraceList.isEmpty()) {
            return;
        }
        int mode = mTraceList.remove(mTraceList.size() - 1);
        switch (mode) {
            case MODE_LINE:
                if (mLinePathList.isEmpty()) {
                    return;
                }
                mLinePathList.remove(mLinePathList.size() - 1);
                break;
            case MODE_WORD:
                if (mWordList.isEmpty()) {
                    return;
                }
                mWordList.remove(mWordList.size() - 1);
                break;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSourceBitmap == null || mSourceBitmap.isRecycled()) {
            LogUtils.d(TAG, "Bitmap is null or recycled");
            return;
        }
        canvas.drawBitmap(mSourceBitmap, 0, 0, null);
        drawLine(canvas);
        drawWords(canvas);
    }

    static class Word {
        Paint paint;
        String content;
        Rect rect = new Rect();

        public Word(String content, int color) {
            this.content = content;
            this.paint = new Paint();
            this.paint.setTextSize(ScreenUtils.dp2px(20));
            //this.paint.setAntiAlias(true);
            this.paint.setColor(color);
            //this.paint.setStyle(Paint.Style.STROKE);
            this.paint.getTextBounds(content, 0, content.length(), rect);
        }
    }
}
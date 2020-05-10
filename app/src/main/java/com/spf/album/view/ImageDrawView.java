package com.spf.album.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.spf.album.ImageFile;
import com.spf.album.event.LatLntImageClickEvent;
import com.spf.album.utils.LogUtils;
import com.spf.album.R;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class ImageDrawView extends View {
    private static final String TAG = "ImageDrawView";
    private Paint textPaint;
    private float textDistance;
    public static int radius;
    private Map<ImageFile, AreaImageInfo> mImagePointMap = new HashMap<>();

    public ImageDrawView(Context context) {
        super(context);
        initView();
    }

    public ImageDrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setBackgroundColor(Color.TRANSPARENT);
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.sp_16));
        textPaint.setFakeBoldText(true);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textDistance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;

        radius = getContext().getResources().getDimensionPixelOffset(R.dimen.dp_30);
    }

    public void setPoints(Map<ImageFile, AreaImageInfo> points) {
        mImagePointMap = points;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            for (Map.Entry<ImageFile, AreaImageInfo> entry : mImagePointMap.entrySet()) {
                if (entry.getValue().area.contains(x, y)) {
                    return true;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            for (Map.Entry<ImageFile, AreaImageInfo> entry : mImagePointMap.entrySet()) {
                if (entry.getValue().area.contains(x, y)) {
                    EventBus.getDefault().post(new LatLntImageClickEvent(entry.getKey()));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (AreaImageInfo info : mImagePointMap.values()) {
            if (info.getBitmap() == null || info.getBitmap().isRecycled()) {
                LogUtils.d(TAG, "bitmap recycled - " + info.getImageUri());
            } else {
                canvas.drawBitmap(info.bitmap, info.area.left, info.area.top, null);
                canvas.drawText(String.valueOf(info.count), info.area.centerX(), info.area.centerY() + textDistance, textPaint);
            }
        }
    }

    public static class AreaImageInfo {
        private Rect area;
        private Uri imageUri;
        private Bitmap bitmap;
        private int count;

        public AreaImageInfo(Point point, Uri imageUri) {
            this.area = new Rect(point.x - radius, point.y - radius,
                    point.x + radius, point.y + radius);
            this.imageUri = imageUri;
            this.count = 1;
        }

        public boolean intersects(Point point) {
            return area.intersects(point.x - radius, point.y - radius,
                    point.x + radius, point.y + radius);
        }

        public void setArea(Point point) {
            this.area.set(point.x - radius, point.y - radius, point.x + radius, point.y + radius);
        }

        public Uri getImageUri() {
            return imageUri;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public void addCount() {
            count++;
        }
    }
}
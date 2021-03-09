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

import com.spf.album.IScreenLocation;
import com.spf.album.model.ImageFile;
import com.spf.album.event.LatLntImageClickEvent;
import com.spf.album.utils.LogUtils;
import com.spf.album.R;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkLayout extends View {
    private static final String TAG = "ImageDrawView";
    private Paint textPaint;
    private float textDistance;
    public static int radius;
    private Map<ImageFile, MarkInfo> mMarkMap = new HashMap<>();
    private IScreenLocation iScreenLocation;
    private volatile boolean isSettingMarks = false;

    public MarkLayout(Context context) {
        super(context);
        initView();
    }

    public MarkLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarkLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

    public void setScreenLocation(IScreenLocation iScreenLocation) {
        this.iScreenLocation = iScreenLocation;
    }

    public void updateMarks() {
        for (Map.Entry<ImageFile, MarkLayout.MarkInfo> entry : mMarkMap.entrySet()) {
            entry.getValue().setArea(iScreenLocation.toScreenLocation(entry.getKey()));
        }
        invalidate();
    }

    public void setMarks(Map<ImageFile, MarkInfo> marks) {
        mMarkMap.clear();
        if(marks != null && marks.size() > 0) {
            mMarkMap.putAll(marks);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            for (Map.Entry<ImageFile, MarkInfo> entry : mMarkMap.entrySet()) {
                if (entry.getValue().area.contains(x, y)) {
                    return true;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            for (Map.Entry<ImageFile, MarkInfo> entry : mMarkMap.entrySet()) {
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
        for (MarkInfo mark : mMarkMap.values()) {
            if (mark.getBitmap() == null || mark.getBitmap().isRecycled()) {
                LogUtils.d(TAG, "bitmap recycled - " + mark.getImageUri());
            } else {
                canvas.drawBitmap(mark.bitmap, mark.area.left, mark.area.top, null);
                canvas.drawText(String.valueOf(mark.count), mark.area.centerX(), mark.area.centerY() + textDistance, textPaint);
            }
        }
    }

    public static class MarkInfo {
        private Rect area;
        private Uri imageUri;
        private Bitmap bitmap;
        private int count;

        public MarkInfo(Point point, Uri imageUri) {
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
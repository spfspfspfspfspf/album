package com.spf.album.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

import com.spf.album.utils.LogUtils;
import com.spf.album.utils.ScreenUtils;

import java.util.Locale;

public class DragTextView extends AppCompatTextView {
    private int width;
    private int height;

    //是否拖动
    private boolean isDrag = false;
    private float downX;
    private float downY;

    private Rect rect;

    public DragTextView(Context context) {
        super(context);
    }

    public DragTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isDrag() {
        return isDrag;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    private void checkRect() {
        if (rect == null) {
            rect = new Rect();
            rect.left = 0;
            rect.right = ScreenUtils.getScreenWidth();
            rect.top = 0;
            rect.bottom = ScreenUtils.getScreenHeight();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (this.isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDrag = false;
                    downX = event.getX();
                    downY = event.getY();
                    checkRect();
                    break;
                case MotionEvent.ACTION_MOVE:
                    LogUtils.d("spf", "rect: " + rect);
                    final float xDistance = event.getX() - downX;
                    final float yDistance = event.getY() - downY;
                    int l, r, t, b;
                    //当水平或者垂直滑动距离大于10,才算拖动事件
                    if (Math.abs(xDistance) > 10 || Math.abs(yDistance) > 10) {
                        isDrag = true;
                        l = (int) (getLeft() + xDistance);
                        r = l + getWidth();
                        t = (int) (getTop() + yDistance);
                        b = t + getHeight();
                        //不划出边界判断,此处应按照项目实际情况,因为本项目需求移动的位置是手机全屏,
                        // 所以才能这么写,如果是固定区域,要得到父控件的宽高位置后再做处理
                        LogUtils.d("spf", String.format(Locale.CHINA, "111 - l: %d, r: %d, t: %d, b: %d",
                                l, r, t, b));
                        if (l < rect.left) {
                            l = rect.left;
                            r = l + getWidth();
                        } else if (r > rect.right) {
                            r = rect.right;
                            l = r - getWidth();
                        }
                        if (t < rect.top) {
                            t = rect.top;
                            b = t + getHeight();
                        } else if (b > rect.bottom) {
                            b = rect.bottom;
                            t = b - getHeight();
                        }
                        LogUtils.d("spf", String.format(Locale.CHINA, "222 - l: %d, r: %d, t: %d, b: %d",
                                l, r, t, b));
                        this.layout(l, t, r, b);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    setPressed(false);
                    break;
            }
            return true;
        }
        return false;
    }

}
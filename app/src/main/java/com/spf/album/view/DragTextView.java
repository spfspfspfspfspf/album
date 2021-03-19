package com.spf.album.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

import com.spf.album.utils.ScreenUtils;

public class DragTextView extends AppCompatTextView {
    private int width;
    private int height;
    private int screenWidth;
    private int screenHeight;

    //是否拖动
    private boolean isDrag = false;
    private float downX;
    private float downY;


    public DragTextView(Context context) {
        super(context);
    }

    public DragTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isDrag() {
        return isDrag;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        screenWidth = ScreenUtils.getScreenWidth();
        screenHeight = ScreenUtils.getScreenHeight() - ScreenUtils.getStatusBarHeight();
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
                    break;
                case MotionEvent.ACTION_MOVE:
                    final float xDistance = event.getX() - downX;
                    final float yDistance = event.getY() - downY;
                    int l, r, t, b;
                    //当水平或者垂直滑动距离大于10,才算拖动事件
                    if (Math.abs(xDistance) > 10 || Math.abs(yDistance) > 10) {
                        isDrag = true;
                        l = (int) (getLeft() + xDistance);
                        r = l + width;
                        t = (int) (getTop() + yDistance);
                        b = t + height;
                        //不划出边界判断,此处应按照项目实际情况,因为本项目需求移动的位置是手机全屏,
                        // 所以才能这么写,如果是固定区域,要得到父控件的宽高位置后再做处理
                        if (l < 0) {
                            l = 0;
                            r = l + width;
                        } else if (r > screenWidth) {
                            r = screenWidth;
                            l = r - width;
                        }
                        if (t < 0) {
                            t = 0;
                            b = t + height;
                        } else if (b > screenHeight) {
                            b = screenHeight;
                            t = b - height;
                        }

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
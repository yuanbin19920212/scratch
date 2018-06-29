package com.bin.yuan.lib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by yuanbin on 2018/6/29.
 */

public class ScratchAnimator extends ValueAnimator {

    static long DEFAULT_DURATION = 0x400;
    private static final Interpolator DEFAULT_INTERPOLATOR = new AccelerateInterpolator(0.6f);
    private static final float END_VALUE = 1.4f;
    private Paint mPaint;

    private Bitmap mRestBitmap;

    private Bitmap mScratchedBitmap;

    private Rect mRestBound;

    private Rect mScratchedBound;

    private View mContainer;

    private float mContainerHeight;

    public ScratchAnimator(View container, Bitmap restBitmap, Rect restBound,
                           Bitmap scratchedBitmap,Rect scratchedBound) {
        mPaint = new Paint();
        mRestBound = new Rect(restBound);
        mScratchedBound = new Rect(scratchedBound);
        mRestBitmap = restBitmap;
        mScratchedBitmap = scratchedBitmap;

        mContainer = container;
        mContainerHeight = container.getMeasuredHeight();
        setFloatValues(0f, END_VALUE);
        setInterpolator(DEFAULT_INTERPOLATOR);
        setDuration(DEFAULT_DURATION);
    }

    public boolean draw(Canvas canvas) {
        if (!isStarted()) {
            return false;
        }
        /***
         * 先绘制剩余的Bitmap
         */
        canvas.save();
        canvas.translate(mRestBound.left,mRestBound.top);
        canvas.drawBitmap(mRestBitmap,0,0,mPaint);
        canvas.restore();

        /***
         * 绘制被撕毁的Bitmap
         */
        canvas.save();
        canvas.translate(mScratchedBound.left,mScratchedBound.top);
        float f = (float) getAnimatedValue();

        if (f <= 0.5f) {
            canvas.rotate(f * 90, 0, mScratchedBound.height());
        }else {
            canvas.translate(0,(int)((f-0.5)*(mContainerHeight-mScratchedBound.top)));
            canvas.rotate(0.5f * 90, 0, mScratchedBound.height());
        }
        canvas.drawBitmap(mScratchedBitmap,0,0,mPaint);
        canvas.restore();
        /***
         * 再次重新绘制
         */
        mContainer.invalidate();
        return true;
    }

    @Override
    public void start() {
        super.start();
        mContainer.invalidate();
    }

    public void destroy(){
        if (mRestBitmap != null){
            mRestBitmap.recycle();
            mRestBitmap = null;
        }

        if (mScratchedBitmap != null){
            mScratchedBitmap.recycle();
            mScratchedBitmap = null;
        }
    }
}

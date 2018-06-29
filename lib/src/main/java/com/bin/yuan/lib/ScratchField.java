package com.bin.yuan.lib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuanbin on 2018/6/29.
 */

public class ScratchField extends View {

    private List<ScratchAnimator> scratchAnimators = new ArrayList<>();

    private Map<ScratchAnimator,OnScratchListener> listenerMap = new HashMap<>();

    public ScratchField(Context context) {
        super(context);
        init();
    }

    public ScratchField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScratchField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (ScratchAnimator scratchAnimator : scratchAnimators) {
            scratchAnimator.draw(canvas);
        }
    }

    public void scratch(Bitmap restBitmap, Rect restBound,
                        Bitmap scratchedBitmap, Rect scratchedBound,
                        long startDelay, long duration,OnScratchListener onScratchListener) {

        final ScratchAnimator scratch =
                new ScratchAnimator(this, restBitmap, restBound,
                        scratchedBitmap, scratchedBound);
        listenerMap.put(scratch,onScratchListener);
        scratch.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scratchAnimators.remove(animation);
                OnScratchListener listener = listenerMap.get(scratch);
                if (listener != null){
                    listener.end();
                    listenerMap.remove(scratch);
                }
            }
        });
        scratch.setStartDelay(startDelay);
        scratch.setDuration(duration);
        scratchAnimators.add(scratch);
        scratch.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (ScratchAnimator scratchAnimator : scratchAnimators) {
            if (scratchAnimator == null)continue;
            scratchAnimator.cancel();
            scratchAnimator.destroy();
            scratchAnimator = null;
        }
    }

    /***
     *
     * @param scratchedView 被撕毁的View
     * @param view 整个View
     */
    public void scratch(View view,final View scratchedView,OnScratchListener onScratchListener) {
        Rect r = new Rect();
        view.getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
        r.offset(-location[0], -location[1]);

        Rect scratchRect = new Rect();
        scratchedView.getGlobalVisibleRect(scratchRect);
        int[] scratchLocation = new int[2];
        getLocationOnScreen(scratchLocation);
        scratchRect.offset(-scratchLocation[0], -scratchLocation[1]);

        Rect restRect = new Rect();
        restRect.set(r.left,r.top,scratchRect.left,r.bottom);

        int startDelay = 100;

        ValueAnimator valueAnimator = ObjectAnimator.ofInt("","",
                1,0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    scratchedView.setScaleX(f);
                    scratchedView.setScaleY(f);
                    scratchedView.setAlpha(f*255);
                }
            }
        });
        valueAnimator.setDuration(150);
        valueAnimator.setStartDelay(startDelay);
        valueAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                scratchedView.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
        scratch(Utils.createBitmapFromView(view,new Rect(0,0,
                        restRect.width(),restRect.height())),
                restRect,
                Utils.createBitmapFromView(view,new Rect(restRect.width(),0,
                        r.width(),restRect.height())),
                scratchRect,
                startDelay,
                ScratchAnimator.DEFAULT_DURATION,onScratchListener);
    }

    public void clear() {
        scratchAnimators.clear();
        invalidate();
    }

    public static ScratchField attach2Window(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        ScratchField explosionField = new ScratchField(activity);
        rootView.addView(explosionField, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return explosionField;
    }


    public interface OnScratchListener{
        void end();
    }
}

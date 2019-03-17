package com.novaapps;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fake.domain.adamlopresto.godo.R;

/**
 * Created by charry on 2015/6/11. https://gist.github.com/douo/dfde289778a9b3b6918f and modified by Tristan Wiley
 */
@SuppressWarnings ("ALL")
public class FloatingActionMenu extends ViewGroup {

    static final TimeInterpolator DEFAULT_OPEN_INTERPOLATOR = new OvershootInterpolator();
    static final TimeInterpolator DEFAULT_CLOSE_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private FloatingActionButton mMenuButton;
    private final ArrayList<FloatingActionButton> mMenuItems;
    private final ArrayList<TextView> mMenuItemLabels;
    private final ArrayList<ItemAnimator> mMenuItemAnimators;
    private final AnimatorSet mOpenAnimatorSet = new AnimatorSet();
    private final AnimatorSet mCloseAnimatorSet = new AnimatorSet();
    private final ImageView mIcon;

    private boolean mOpen;
    private boolean animating;
    private final boolean mIsSetClosedOnTouchOutside = true;
    private long duration = 300;
    private boolean isCircle = false;
    private int mRadius = 256;
    private float multipleOfFB = 0;
    private int mItemGap = 0;

    private OnMenuItemClickListener onMenuItemClickListener;
    private OnMenuToggleListener onMenuToggleListener;
    GestureDetector mGestureDetector = new GestureDetector(getContext(),
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    return mIsSetClosedOnTouchOutside && isOpened();
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    close();
                    return true;
                }
            });
    private final OnClickListener mOnItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof FloatingActionButton) {
                int i = mMenuItems.indexOf(v);
                if (onMenuItemClickListener != null) {
                    onMenuItemClickListener
                            .onMenuItemClick(FloatingActionMenu.this, i, (FloatingActionButton) v);
                }
            } else if (v instanceof TextView) {
                int i = mMenuItemLabels.indexOf(v);
                if (onMenuItemClickListener != null) {
                    onMenuItemClickListener.onMenuItemClick(FloatingActionMenu.this, i, mMenuItems.get(i));
                }
            }
            close();
        }
    };


    public FloatingActionMenu(Context context) {
        this(context, null, 0);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMenuItems = new ArrayList<>(5);
        mMenuItemAnimators = new ArrayList<>(5);
        mMenuItemLabels = new ArrayList<>(5);
        mIcon = new ImageView(context);
    }

    @Override
    protected void onFinishInflate() {
        bringChildToFront(mMenuButton);
        bringChildToFront(mIcon);
        super.onFinishInflate();
    }

    @Override
    public void addView(@NonNull View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        if (getChildCount() > 1) {
            if (child instanceof FloatingActionButton) {
                addMenuItem((FloatingActionButton) child);
            }
        } else {
            mMenuButton = (FloatingActionButton) child;
            mIcon.setImageDrawable(mMenuButton.getDrawable());
            addView(mIcon);
            mMenuButton.setImageDrawable(mMenuButton.getDrawable());
            createDefaultIconAnimation();
            mMenuButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle();
                }
            });
        }
    }

    public void toggle() {
        if (!mOpen) {
            open();
        } else {
            close();
        }
    }

    public void open() {
        d("open");
        startOpenAnimator();
        mOpen = true;
        if (onMenuToggleListener != null) {
            onMenuToggleListener.onMenuToggle(true);
        }
    }

    public void close() {
        startCloseAnimator();
        mOpen = false;
        if (onMenuToggleListener != null) {
            onMenuToggleListener.onMenuToggle(true);
        }
    }

    protected void startCloseAnimator() {
        mCloseAnimatorSet.start();
        for (ItemAnimator anim : mMenuItemAnimators) {
            anim.startCloseAnimator();
        }
    }

    protected void startOpenAnimator() {
        mOpenAnimatorSet.start();
        for (ItemAnimator anim : mMenuItemAnimators) {
            anim.startOpenAnimator();
        }
    }

    public void addMenuItem(FloatingActionButton item) {
        mMenuItems.add(item);
        mMenuItemAnimators.add(new ItemAnimator(item));

        TextView button = new TextView(getContext());

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(params);

        button.setBackgroundResource(R.drawable.rounded_corners);

        button.setTextColor(Color.WHITE);
        button.setText(item.getContentDescription());

        Integer paddingSize = (int)button.getTextSize() / 3;

        button.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);

        addView(button);
        mMenuItemLabels.add(button);
        item.setTag(button);
        item.setOnClickListener(mOnItemClickListener);
        button.setOnClickListener(mOnItemClickListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width;
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height;
        final int count = getChildCount();
        int maxChildWidth = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        for (int i = 0; i < mMenuItems.size(); i++) {
            FloatingActionButton fab = mMenuItems.get(i);
            TextView label = mMenuItemLabels.get(i);
            maxChildWidth = Math.max(maxChildWidth,
                    label.getMeasuredWidth() + fab.getMeasuredWidth());

        }

        maxChildWidth = Math.max(mMenuButton.getMeasuredWidth(), maxChildWidth);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = maxChildWidth + 30;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            int heightSum = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                heightSum += child.getMeasuredHeight();
            }
            height = heightSum + 20;
        }

        setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (mIsSetClosedOnTouchOutside) {
            return mGestureDetector.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        System.out.println("onLayout:" + changed);
        if (changed) {
            int right = r - getPaddingRight();
            int bottom = b - getPaddingBottom();
            int top = bottom - mMenuButton.getMeasuredHeight();
            mMenuButton.layout(right - mMenuButton.getMeasuredWidth(), top, right, bottom);
            int dw = (mMenuButton.getMeasuredWidth() - mIcon.getMeasuredWidth()) / 2;
            int dh = (mMenuButton.getMeasuredHeight() - mIcon.getMeasuredHeight()) / 2;
            mIcon.layout(right - mIcon.getMeasuredWidth() - dw,
                    bottom - mIcon.getMeasuredHeight() - dh, right - dw, bottom - dh);

            if (isCircle) {
                if (mMenuItems.size() < 2) {
                    Log.e("onLayout", "Floating Action Buttons must more then one!");
                    return;
                }
                double angle = Math.PI/2d/(mMenuItems.size() - 1);
                for (int i = 0; i < mMenuItems.size(); i++) {
                    FloatingActionButton itemFB = mMenuItems.get(i);
                    int fbWidth = itemFB.getMeasuredWidth();
                    int fbHeight = itemFB.getMeasuredHeight();
                    if (0 != multipleOfFB) {
                        mRadius = (int) (fbWidth * multipleOfFB);
                    }
                    int itemDw = (mMenuButton.getMeasuredWidth() - fbWidth) / 2;
                    int itemDh = (mMenuButton.getMeasuredHeight() - fbHeight) / 2;
                    int itemX = (int) (mRadius*Math.cos(i*angle));
                    int itemY = (int) (mRadius*Math.sin(i*angle));
                    itemFB.layout(right - itemX - fbWidth - itemDw, bottom - itemY - fbHeight - itemDh,
                            right - itemX - itemDw, bottom - itemY - itemDh);

                    if (!animating) {
                        if (!mOpen) {
                            itemFB.setTranslationY(mMenuButton.getTop() - itemFB.getTop());
                            itemFB.setTranslationX(mMenuButton.getLeft() - itemFB.getLeft());
                            itemFB.setVisibility(GONE);
                        } else {
                            itemFB.setTranslationY(0);
                            itemFB.setTranslationX(0);
                            itemFB.setVisibility(VISIBLE);
                        }
                    }
                }
            } else {
                for (int i = 0; i < mMenuItems.size(); i++) {
                    FloatingActionButton item = mMenuItems.get(i);
                    TextView label = mMenuItemLabels.get(i);

                    label.setBackgroundResource(R.drawable.rounded_corners);
                    bottom = top -= mItemGap;

                    top -= item.getMeasuredHeight();
                    int width = item.getMeasuredWidth();
                    int d = (mMenuButton.getMeasuredWidth() - width) / 2;
                    item.layout(right - width - d, top, right - d, bottom);
                    d = (item.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

                    label.layout(item.getLeft() - label.getMeasuredWidth() - 24,
                            item.getTop() + d, item.getLeft(),
                            item.getTop() + d + label.getMeasuredHeight());
                    if (!animating) {
                        if (!mOpen) {
                            item.setTranslationY(mMenuButton.getTop() - item.getTop());
                            item.setVisibility(GONE);
                            label.setVisibility(GONE);
                        } else {
                            item.setTranslationY(0);
                            item.setVisibility(VISIBLE);
                            label.setVisibility(VISIBLE);
                        }
                    }
                }
            }
            if (!animating && getBackground() != null) {
                if (!mOpen) {
                    getBackground().setAlpha(0);
                } else {
                    getBackground().setAlpha(0xff);
                }
            }
        }
    }

    private void createDefaultIconAnimation() {
        Animator.AnimatorListener listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(
                mIcon,
                "rotation",
                135f,
                0f
        );

        ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(
                mIcon,
                "rotation",
                0f,
                135f
        );

        if (getBackground() != null) {


            ValueAnimator hideBackgroundAnimator = ValueAnimator.ofInt(0xff, 0);
            hideBackgroundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer alpha = (Integer) animation.getAnimatedValue();
                    //System.out.println(alpha);
                    getBackground().setAlpha(alpha > 0xff ? 0xff : alpha);
                }
            });
            ValueAnimator showBackgroundAnimator = ValueAnimator.ofInt(0, 0xff);
            showBackgroundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    Integer alpha = (Integer) animation.getAnimatedValue();
                    //System.out.println(alpha);
                    getBackground().setAlpha(alpha > 0xff ? 0xff : alpha);
                }
            });

            mOpenAnimatorSet.playTogether(expandAnimator, showBackgroundAnimator);
            mCloseAnimatorSet.playTogether(collapseAnimator, hideBackgroundAnimator);
        } else {
            mOpenAnimatorSet.playTogether(expandAnimator);
            mCloseAnimatorSet.playTogether(collapseAnimator);
        }

        mOpenAnimatorSet.setInterpolator(DEFAULT_OPEN_INTERPOLATOR);
        mCloseAnimatorSet.setInterpolator(DEFAULT_CLOSE_INTERPOLATOR);

        mOpenAnimatorSet.setDuration(duration);
        mCloseAnimatorSet.setDuration(duration);

        mOpenAnimatorSet.addListener(listener);
        mCloseAnimatorSet.addListener(listener);
    }

    public boolean isOpened() {
        return mOpen;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        d("onSaveInstanceState");
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putBoolean("mOpen", mOpen);
        // ... save everything
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        d("onRestoreInstanceState");
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mOpen = bundle.getBoolean("mOpen");
            // ... load everything
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDetachedFromWindow() {
        d("onDetachedFromWindow");
        //getBackground().setAlpha(bgAlpha);//reset default alpha
        super.onDetachedFromWindow();
    }

    @Override
    public void setBackground(Drawable background) {
        if (background instanceof ColorDrawable) {
            // after activity finish and relaucher , background drawable state still remain?
            int bgAlpha = Color.alpha(((ColorDrawable) background).getColor());
            d("bg:" + Integer.toHexString(bgAlpha));
            super.setBackground(background);
        } else {
            throw new IllegalArgumentException("floating only support color background");
        }
    }

    public OnMenuToggleListener getOnMenuToggleListener() {
        return onMenuToggleListener;
    }

    public void setOnMenuToggleListener(OnMenuToggleListener onMenuToggleListener) {
        this.onMenuToggleListener = onMenuToggleListener;
    }

    public OnMenuItemClickListener getOnMenuItemClickListener() {
        return onMenuItemClickListener;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    protected void d(String msg) {
        Log.d("FAM", msg == null ? null : msg);
    }

    public interface OnMenuToggleListener {
        void onMenuToggle(boolean opened);
    }


    public interface OnMenuItemClickListener {
        void onMenuItemClick(FloatingActionMenu fam, int index, FloatingActionButton item);
    }

    private class ItemAnimator implements Animator.AnimatorListener {
        private final View mView;
        private boolean playingOpenAnimator;

        public ItemAnimator(View v) {
            v.animate().setListener(this);
            mView = v;
        }

        public void startOpenAnimator() {
            mView.animate().cancel();
            playingOpenAnimator = true;
            mView.animate()
                    .translationY(0)
                    .translationX(0)
                    .setInterpolator(DEFAULT_OPEN_INTERPOLATOR)
                    .start();
            mMenuButton.animate()
                    .rotation(135f)
                    .setInterpolator(DEFAULT_OPEN_INTERPOLATOR)
                    .start();
        }

        public void startCloseAnimator() {
            mView.animate().cancel();
            playingOpenAnimator = false;
            mView.animate()
                    .translationX(mMenuButton.getLeft() - mView.getLeft())
                    .translationY((mMenuButton.getTop() - mView.getTop()))
                    .setInterpolator(DEFAULT_CLOSE_INTERPOLATOR)
                    .start();
            mMenuButton.animate()
                    .rotation(0f)
                    .setInterpolator(DEFAULT_CLOSE_INTERPOLATOR)
                    .start();
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (playingOpenAnimator) {
                if (mView.isEnabled()) {
                    mView.setVisibility(VISIBLE);
                }
            } else {
                ((TextView) mView.getTag()).setVisibility(GONE);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mView.isEnabled()) {
                if (!playingOpenAnimator) {
                    mView.setVisibility(GONE);
                } else {
                    ((TextView) mView.getTag()).setVisibility(VISIBLE);
                }
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

    /**
     * set as circle(default) or line pattern
     * @param isCircle
     */
    public void setIsCircle(boolean isCircle) {
        this.isCircle = isCircle;
    }

    /**
     * set the radius of menu, default 256
     * @param mRadius
     */
    public void setmRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    /**
     * set radius as multiple of width of floating action button
     * @param multipleOfFB
     */
    public void setMultipleOfFB(float multipleOfFB) {
        this.multipleOfFB = multipleOfFB;
    }

    /**
     * duration of anim, default 300
     * @param duration
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Only usefully in Line pattern
     * @param mItemGap
     */
    public void setmItemGap(int mItemGap) {
        this.mItemGap = mItemGap;
    }
}
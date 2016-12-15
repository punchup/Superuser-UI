package com.koushikdutta.superuser.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;

public class RecyclerViewSwipeable extends RecyclerView {

    //FragmentMain fragment;
    //ViewPager viewPager;

    //RecyclerItemClickListener.OnItemClickListener listener;


    //SharedPreferences pref;


    //private float firstX, firstY, rangeX1, rangeX2, rangeY, swipeX, swipeY;

    //boolean inRange = false;
    //private static boolean shouldScroll = true;



    public RecyclerViewSwipeable(Context context) {
        super(context);
        //init();
    }


    public RecyclerViewSwipeable(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //init();
    }


    public RecyclerViewSwipeable(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }


   /* private void init() {
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float width = metrics.widthPixels;

        rangeX1 = width / 3.5f;
        rangeX2 = (5 * width) / 7;
        rangeY = metrics.heightPixels - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);;

        swipeX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);
        swipeY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17.5f, metrics);
    }*/


    /*public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }*/


    /*public void setListener(RecyclerItemClickListener.OnItemClickListener listener) {
        this.listener = listener;
    }


    public void setFragment(FragmentMain fragment) {
        this.fragment = fragment;
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                float currentX = e.getRawX();
                float currentY = e.getRawY();

                shouldScroll = true;

                if (inRange && currentY > rangeY) {

                    if (Math.abs(currentY - firstY) < swipeY && Math.abs(currentX - firstX) > swipeX) {

                        if (fragment != null) {
                            //if (currentX > firstX) fragment.setAllowed();
                            //else fragment.setDenied();
                        }

                    } else if (Math.abs(currentX - firstX) < swipeX) {
                        float top = getResources().getDisplayMetrics().heightPixels - getHeight();

                        View child = findChildViewUnder(currentX, currentY - top);

                        if (child != null && listener != null)
                            listener.onItemClick(child, getChildAdapterPosition(child));
                    }

                    //return true;
                }
        }

        return super.onTouchEvent(e);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {

        if (!pref.getBoolean("fab", true)) {
            switch (e.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    firstX = e.getRawX();
                    firstY = e.getRawY();

                    if (firstY > rangeY && firstX > rangeX1 && firstX < rangeX2) {
                        viewPager.requestDisallowInterceptTouchEvent(true);
                        shouldScroll = false;

                        inRange = true;

                        return true;

                    } else inRange = false;
            }
        }

       return super.onInterceptTouchEvent(e);
    }*/


    //https://gist.github.com/Musenkishi/8df1ab549857756098ba#file-gridrecyclerview
    @Override
    protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {

        if (getAdapter() != null && getLayoutManager() instanceof GridLayoutManager){

            GridLayoutAnimationController.AnimationParameters animationParams =
                    (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;

            if (animationParams == null) {
                animationParams = new GridLayoutAnimationController.AnimationParameters();
                params.layoutAnimationParameters = animationParams;
            }

            int columns = ((GridLayoutManager) getLayoutManager()).getSpanCount();

            animationParams.count = count;
            animationParams.index = index;
            animationParams.columnsCount = columns;
            animationParams.rowsCount = count / columns;

            final int invertedIndex = count - 1 - index;
            animationParams.column = columns - 1 - (invertedIndex % columns);
            animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns;

        } else {
            super.attachLayoutAnimationParameters(child, params, index, count);
        }
    }



    public static class LayoutManagerSwipeable extends GridLayoutManager {

        public LayoutManagerSwipeable(Context context, int spanCount) {
            super(context, spanCount);
        }

        public LayoutManagerSwipeable(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public LayoutManagerSwipeable(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        /*@Override
        public boolean canScrollVertically() {
            return shouldScroll && super.canScrollVertically();
        }*/
    }
}
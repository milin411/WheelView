package com.mysiga.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mysiga.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * {https://github.com/wangjiegulu/WheelView.git}
 * Number Wheel View
 */
public class NumWheelView extends ScrollView {
    private static final int DEFAULT_SHOW_ITEM_COUNT = 5;
    private static final int TEXT_SIZE = 28;//dp
    private static final int TEXT_HEIGHT = 66;//dp
    private boolean isEnable = true;
    private int mOffset; // 偏移量（需要在最前面和最后面补全）

    private Context mContext;
    private LinearLayout mViews;
    private List<String> items;
    private int mShowItemCount = DEFAULT_SHOW_ITEM_COUNT;

    private int mSelectedIndex = 2;
    private int mStopActionY;

    private Runnable scrollerTask;
    private int delayMillis = 50;
    private int itemHeight = 0;//dp

    private OnSelectedListener mOnSelectedViewListener;

    public NumWheelView(Context context) {
        super(context);
        init(context);
    }

    public NumWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NumWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isEnable) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        updateItemView(getPosition(t));
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnable) {
            return false;
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            startScrollerTask();
        }
        return super.onTouchEvent(ev);
    }


    @Override
    public void setEnabled(boolean enabled) {
        isEnable = enabled;
        super.setEnabled(enabled);
    }

    private void init(Context context) {
        this.mContext = context;
        this.setVerticalScrollBarEnabled(false);
        mViews = new LinearLayout(context);
        mViews.setOrientation(LinearLayout.VERTICAL);
        this.addView(mViews);

        scrollerTask = new Runnable() {
            public void run() {
                int nowY = getScrollY();
                if (mStopActionY - nowY == 0) { // stopped
                    final int remainder = mStopActionY % itemHeight;
                    mSelectedIndex = getPosition(mStopActionY);
                    if (remainder != 0) {
                        NumWheelView.this.post(new Runnable() {
                            @Override
                            public void run() {
                                if (remainder > itemHeight / 2) {
                                    NumWheelView.this.smoothScrollTo(0, mStopActionY - remainder + itemHeight);
                                } else {
                                    NumWheelView.this.smoothScrollTo(0, mStopActionY - remainder);
                                }
                                if (mOnSelectedViewListener != null) {
                                    mOnSelectedViewListener.onSelected(mSelectedIndex, items.get(mSelectedIndex));
                                }
                            }
                        });
                    }
                    if (mOnSelectedViewListener != null && remainder == 0) {
                        mOnSelectedViewListener.onSelected(mSelectedIndex, items.get(mSelectedIndex));
                    }
                } else {
                    startScrollerTask();
                }
            }
        };
    }

    private List<String> getItems() {
        return items;
    }

    /**
     * @param list
     */
    public void setItems(List<String> list) {
        if (items == null) {
            items = new ArrayList<String>();
        }
        items.clear();
        mViews.removeAllViews();
        items.addAll(list);

        mOffset = (mShowItemCount - 1) / 2;
        // fill head and end
        for (int i = 0; i < mOffset; i++) {
            items.add(0, "");
            items.add("");
        }

        for (String item : items) {
            mViews.addView(createView(item));
        }
        updateItemView(mSelectedIndex);
    }

    private void startScrollerTask() {
        mStopActionY = getScrollY();
        this.postDelayed(scrollerTask, delayMillis);
    }

    private TextView createView(String item) {
        //// FIXME: 2017/3/9 重复创建textView耗内存
        TextView numView = new TextView(mContext);
        numView.setWidth((int) dip2px(TEXT_HEIGHT));
        numView.setHeight((int) dip2px(TEXT_HEIGHT));
        numView.setText(item);
        numView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
        numView.setGravity(Gravity.CENTER);
        if (itemHeight == 0) {
            itemHeight = getViewMeasuredHeight(numView);
            mViews.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight * mShowItemCount));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.getLayoutParams();
            this.setLayoutParams(new LinearLayout.LayoutParams(layoutParams.width, itemHeight * mShowItemCount));
        }
        return numView;
    }


    private void updateItemView(int position) {
        int childSize = mViews.getChildCount();
        for (int i = 0; i < childSize; i++) {
            TextView itemView = (TextView) mViews.getChildAt(i);
            if (itemView == null) {
                return;
            }
            if (position == i) {
                itemView.setTextColor(getResources().getColor(isEnable ? R.color.num_select_color : R.color.num_un_select_color));
            } else {
                itemView.setTextColor(getResources().getColor(R.color.num_un_select_color));
            }
        }
    }

    /**
     * @param verticalScrollY Current vertical scroll origin
     * @return
     */
    private int getPosition(int verticalScrollY) {
        int remainder = verticalScrollY % itemHeight;
        int divided = verticalScrollY / itemHeight;
        if (remainder != 0 && remainder > itemHeight / 2) {
            return divided + mOffset + 1;
        }
        return divided + mOffset;
    }

    /**
     * select position item
     *
     * @param position
     */
    public void setSelection(final int position) {
        mSelectedIndex = position + mOffset;
        this.post(new Runnable() {
            @Override
            public void run() {
                NumWheelView.this.scrollTo(0, position * itemHeight);
            }
        });

    }

    public String getSeletedItem() {
        return items.get(mSelectedIndex);
    }

    public int getSeletedIndex() {
        return mSelectedIndex - mOffset;
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.mOnSelectedViewListener = onSelectedListener;
    }

    private float dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    private int getViewMeasuredHeight(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);
        view.measure(width, expandSpec);
        return view.getMeasuredHeight();
    }

    public interface OnSelectedListener {
        /**
         * select current
         *
         * @param selectedIndex
         * @param item
         */
        void onSelected(int selectedIndex, String item);
    }
}

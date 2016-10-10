package patrik.com.pullto.activity.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.RotateDrawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import patrik.com.pullto.R;

/**
 * usage
 *
 * @author 骆胜华.
 * @date 2016/10/10.
 * ==============================================
 * Copyright (c) 2016 TRANSSION.Co.Ltd.
 * All rights reserved.
 */
public class PullToRefreshView extends FrameLayout {
    private String TAG = this.getClass().getSimpleName();
    private TextView mTvHeaderLoading;
    private ImageView mIvHeaderLoading;
    private TextView mTvFooterLoading;
    private ImageView mIvFooterLoading;
    RotateDrawable rotateDrawable;
    private int mId_header;
    private int mHeaderHeight;
    private View mHeadLayout;
    private int mId_footer;
    private int mFooterHeight;
    private View mFooterLayout;
    private View contentView;
    private int screenHeight;
    public PullToRefreshView(Context context) {
        this(context,null);
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PullToRefreshView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getMetrics(dm);
        screenHeight = dm.heightPixels;

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PullRefreshView);
//        for (int n = 0; n < typedArray.getIndexCount(); n++) {
//            int attr = typedArray.getIndex(n);
//            switch (attr) {
//                case R.styleable.PullRefreshView_id_header:
//                    mId_header = typedArray.getResourceId(attr, 0);
//                    break;
//            }
//        }
        mId_header = typedArray.getResourceId(R.styleable.PullRefreshView_id_header,0);
        mId_footer = typedArray.getResourceId(R.styleable.PullRefreshView_id_footer,0);
        typedArray.recycle();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(getChildCount()!=1){
                    Log.e(TAG,"child must be 1 ,now child size = " + getChildCount());
                }else{
                    contentView = getChildAt(0);
                }
                if(getWidth()!= 0 && getHeight()!=0){
                    if (mHeadLayout == null && mId_header != 0) {
                        View view = LayoutInflater.from(context).inflate(mId_header,PullToRefreshView.this,true);
                        mTvHeaderLoading = (TextView) view.findViewById(R.id.tv_top_loading);
                        mIvHeaderLoading = (ImageView)view.findViewById(R.id.iv_top_loading);
                        rotateDrawable = (RotateDrawable) mIvHeaderLoading.getBackground();
                        mHeadLayout = getChildAt(1);
                        mHeadLayout.measure(MeasureSpec.makeMeasureSpec(getWidth(),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(getHeight(),MeasureSpec.UNSPECIFIED));
                        mHeaderHeight = mHeadLayout.getMeasuredHeight();
                        mHeadLayout.setY(-mHeaderHeight);
                    }
                    if(mFooterLayout == null && mId_footer != 0){
                        View view = LayoutInflater.from(context).inflate(mId_footer,PullToRefreshView.this,true);
                        mTvFooterLoading = (TextView) view.findViewById(R.id.tv_footer_loading);
                        mIvFooterLoading = (ImageView)view.findViewById(R.id.iv_footer_loading);
                        rotateDrawable = (RotateDrawable) mIvFooterLoading.getBackground();
                        mFooterLayout = getChildAt(2);
                        mFooterLayout.measure(MeasureSpec.makeMeasureSpec(getWidth(),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(getHeight(),MeasureSpec.UNSPECIFIED));
                        mFooterHeight = mFooterLayout.getMeasuredHeight();
                        mFooterLayout.setY(screenHeight+mFooterHeight);
                    }
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }else{
                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!ViewCompat.canScrollVertically(contentView, -1)) {
            //无法向下滚动时
            return true;
        }
        return false;
    }
float lastY = 0;
float touchSlop = 0;
    float yOffset;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                yOffset = event.getY()-lastY;
                if(yOffset > 0){
                    if(yOffset>touchSlop){
                        setPullOffsetY(yOffset);
                    }
                    //pull
                }else{
                    //push
                    if(-yOffset>touchSlop){
                        setPushOffsetY(yOffset);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(yOffset > 0){
                    //下拉
                    if(yOffset>mHeaderHeight){
                        exePullTask();
                    }else{
                        scroolToTop();
                    }
                }else{
                    //上拉
                    if( -yOffset>mFooterHeight){
                        exePushTask();
                    }else{
                        scroolToBtm();
                    }
                }
                break;
        }
        return true;
    }
    int rotateTicks = 0;
    private void setPullOffsetY(float yOffset){
        mHeadLayout.setY(-mHeaderHeight+yOffset);
        contentView.setY(yOffset);
        if(mTvHeaderLoading!=null && yOffset<mHeaderHeight){
            mTvHeaderLoading.setText(R.string.str_pull_refresh);
        }else if(mTvHeaderLoading!=null && yOffset>mHeaderHeight){
            mTvHeaderLoading.setText(R.string.str_release_refresh);
        }else{
            mTvHeaderLoading.setText(R.string.str_refreshing);
        }
        rotateTicks = (int) (yOffset * 50);
        if (rotateDrawable != null) {
            rotateDrawable.setLevel(rotateTicks);
        }
    }
    private void setPushOffsetY(float yOffset){
        mFooterLayout.setY(screenHeight+mFooterHeight+yOffset);
        contentView.setY(yOffset);

        if(mTvFooterLoading!=null && (-yOffset)<2*mFooterHeight){
            mTvFooterLoading.setText(R.string.str_up_load);
        }else if(mTvFooterLoading!=null && (-yOffset)>2*mFooterHeight){
            mTvFooterLoading.setText(R.string.str_release_load);
        }else{
            mTvFooterLoading.setText(R.string.str_loading);
        }
        rotateTicks = (int) (yOffset * 50);
        if (rotateDrawable != null) {
            rotateDrawable.setLevel(rotateTicks);
        }
    }
    private void scroolToTop(){
        ObjectAnimator topAnim = ObjectAnimator.ofFloat(PullToRefreshView.this, "pullOffsetY", yOffset, 0);
        topAnim.setDuration(200);
        topAnim.start();
    }
    private void exePullTask(){
        ObjectAnimator topAnim = ObjectAnimator.ofFloat(PullToRefreshView.this, "pullOffsetY", yOffset, mHeaderHeight);
        topAnim.setDuration(200);
        topAnim.start();
    }

    private void scroolToBtm(){
        ObjectAnimator topAnim = ObjectAnimator.ofFloat(PullToRefreshView.this, "pushOffsetY", yOffset,0);
        topAnim.setDuration(200);
        topAnim.start();
    }
    private void exePushTask(){
        ObjectAnimator topAnim = ObjectAnimator.ofFloat(PullToRefreshView.this, "pushOffsetY", yOffset,-2*mFooterHeight);
        topAnim.setDuration(200);
        topAnim.start();
    }
}

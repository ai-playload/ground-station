package java.com.example.ground_station.data.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.ground_station.R;

public class MaxLayout extends LinearLayout {

    private int maxHeight;
    private int maxWidth;

    public MaxLayout(Context context) {
        this(context, null);
    }

    public MaxLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaxLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaxLayout);
            maxWidth = a.getDimensionPixelOffset(R.styleable.MaxLayout_max_width, 0);
            maxHeight = a.getDimensionPixelOffset(R.styleable.MaxLayout_max_height, 0);
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 是否设置了比例
//        boolean setRatio = isSetRatio();
//        // 没有设置最大宽度，高度，宽高比例，不需要调整，直接返回
//        if (mMaxWidth <= DEF_VALUE && mMaxHeight <= DEF_VALUE && !setRatio) {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            return;
//        }

        // 拿到原来宽度，高度的 mode 和 size
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        if (maxWidth > 0 && widthSize > 0) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(maxWidth, widthSize), MeasureSpec.EXACTLY);
        }
        if (maxHeight > 0  && heightSize > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(maxHeight, heightSize), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//
//        Log.d(TAG, "origin onMeasure: widthSize =" + widthSize + "heightSize = " + heightSize);
//
//        if (mRatioStandrad == W_H) { // 当模式已宽度为基准
//            widthSize = getWidth(widthSize);
//
//            if (mRatio >= 0) {
//                heightSize = (int) (widthSize * mRatio);
//            }
//
//            heightSize = getHeight(heightSize);
//            int maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
//            int maxWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
//            super.onMeasure(maxWidthMeasureSpec, maxHeightMeasureSpec);
//
//        } else if (mRatioStandrad == H_W) { // 当模式已高度为基准
//            heightSize = getHeight(heightSize);
//
//            if (mRatio >= 0) {
//                widthSize = (int) (heightSize * mRatio);
//            }
//
//            widthSize = getWidth(widthSize);
//
//
//        } else { // 当没有设定比例的时候
//            widthSize = getWidth(widthSize);
//            heightSize = getHeight(heightSize);
//            int maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
//            int maxWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
//            super.onMeasure(maxWidthMeasureSpec, maxHeightMeasureSpec);
//
//        }
    }


}

package com.smartgiant.circleratioview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.smartgiant.circleratioview.R;

/**
 * Created by shiyu on 2018/1/24.
 */

public class RatioCircleView extends View {
    //一些默认参数
    private final int DEFAULT_RADIUS = dip2px(200);
    private final float DEFAULT_GAP_WIDTH = dip2px(20);
    private final float DEFAULT_STROKE_WIDTH = 1;
    private final int DEFAULT_STROKE_COLOR = Color.DKGRAY;
    private final int DEFAULT_FILLED_COLOR = Color.BLUE;
    private final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private final int DEFAULT_UNFILLED_COLOR = Color.parseColor("#dddddd");
    private final float DEFAULT_START_ANGLE = 270f;
    private final String ABOVE_TEXT = "打卡人数/总人数";
    private final String BELOW_TEXT = "人未打卡";

    private Context mContext;

    private Paint circleFilledPaint;
    private Paint circleStrokePaint;
    private Paint ratioPaint;
    private Paint circleAnglePaint;

    private Paint textPaint;

    private int filledGapColor;
    private int unfilledGapColor;
    private int strokeColor;

    private int aboveTextColor;
    private int belowTextColor;
    private int centerTextColor;
    private int restCountColor;

    private float radius;           //最外圆的半径
    private float gapWidth;         //内圆与外圆之间的间隙大小
    //圆心坐标
    private float cx;
    private float cy;

    private float strokeWidth;  //圆的边框宽度
    private float startAngle;   //代表所占比例的圆弧开始绘制的角度

    private float ratio;        //所占比例
    private int total;          //总数
    private int coveredCount;   //占有数量

    private boolean enableAnimation;
    private AnimationThread animationThread;
    private float animationRatio = 0f;

    public RatioCircleView(Context context) {
        super(context);
        mContext = context;

    }

    public RatioCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    public RatioCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(attrs);
    }
    private void init(AttributeSet attrs) {

        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.RatioCircleView);

        gapWidth = ta.getDimension(R.styleable.RatioCircleView_gap_width,DEFAULT_GAP_WIDTH);
        strokeWidth = ta.getDimension(R.styleable.RatioCircleView_stroke_width,DEFAULT_STROKE_WIDTH);

        filledGapColor = ta.getColor(R.styleable.RatioCircleView_filled_gap_color, DEFAULT_FILLED_COLOR);
        unfilledGapColor = ta.getColor(R.styleable.RatioCircleView_unfilled_gap_color, DEFAULT_UNFILLED_COLOR);
        strokeColor = ta.getColor(R.styleable.RatioCircleView_unfilled_gap_color, DEFAULT_STROKE_COLOR);

        startAngle = ta.getFloat(R.styleable.RatioCircleView_start_angle,DEFAULT_START_ANGLE);

        total = ta.getInteger(R.styleable.RatioCircleView_total,0);
        coveredCount = ta.getInteger(R.styleable.RatioCircleView_covered_count,0);

        restCountColor = ta.getColor(R.styleable.RatioCircleView_rest_count_color,DEFAULT_TEXT_COLOR);
        aboveTextColor = ta.getColor(R.styleable.RatioCircleView_above_text_color,DEFAULT_TEXT_COLOR);
        centerTextColor = ta.getColor(R.styleable.RatioCircleView_center_text_color,DEFAULT_TEXT_COLOR);
        belowTextColor = ta.getColor(R.styleable.RatioCircleView_below_text_color,DEFAULT_TEXT_COLOR);

        enableAnimation = ta.getBoolean(R.styleable.RatioCircleView_enable_animation,true);
        ratio = calculateRatio();
        ta.recycle();

        initPaint();
    }

    private float calculateRatio() {
        if(total <= 0 || coveredCount <= 0){
            total = 0;
            coveredCount = 0;
            return 0;
        }
        ratio = coveredCount / (float)total;
//        Log.i("比例",ratio + "");
        return ratio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = getSize(widthMeasureSpec,DEFAULT_RADIUS);
        int heightSize = getSize(heightMeasureSpec,DEFAULT_RADIUS);

        super.onMeasure(widthSize, heightSize);
    }

    private int getSize(int measureSpec,int most){
        int size  = MeasureSpec.getSize(measureSpec);
        int sizeMode = MeasureSpec.getMode(measureSpec);

        if (sizeMode == MeasureSpec.EXACTLY) {
            size = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        } else if (sizeMode == MeasureSpec.AT_MOST) {
            size = MeasureSpec.makeMeasureSpec(most, MeasureSpec.AT_MOST);
        } else {
            size = MeasureSpec.makeMeasureSpec(most, MeasureSpec.EXACTLY);
        }
        return size;
    }


    private void initPaint() {
        circleStrokePaint = new Paint();
        circleStrokePaint.setStrokeWidth(strokeWidth);
        circleStrokePaint.setAntiAlias(true);
        circleStrokePaint.setColor(strokeColor);
        circleStrokePaint.setStyle(Paint.Style.STROKE);

        circleFilledPaint = new Paint();
        circleFilledPaint.setStrokeWidth(gapWidth - strokeWidth);
        circleFilledPaint.setAntiAlias(true);
        circleFilledPaint.setColor(unfilledGapColor);
        circleFilledPaint.setStyle(Paint.Style.STROKE);

        ratioPaint = new Paint();
        ratioPaint.setStrokeWidth((float) (gapWidth + 0.75 * strokeWidth));
        ratioPaint.setAntiAlias(true);
        ratioPaint.setColor(filledGapColor);
        ratioPaint.setStyle(Paint.Style.STROKE);

        circleAnglePaint = new Paint();
        circleAnglePaint.setStrokeWidth(1);
        circleAnglePaint.setAntiAlias(true);
        circleAnglePaint.setColor(filledGapColor);
        circleAnglePaint.setStyle(Paint.Style.FILL);
    }
    @SuppressLint("all")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.i("位置","left:" + getLeft() + ",top:" + getTop() +
//                ",right:" + getRight() + ",bottom:" + getBottom() + ",宽:" + getWidth() + "高:" + getHeight());

        cx = getWidth() / 2;
        cy = getHeight() / 2;

        radius = getWidth() > getHeight() ? getHeight() / 2 : getWidth() / 2;

        canvas.drawCircle(cx, cy, radius - strokeWidth / 2, circleStrokePaint);
        canvas.drawCircle(cx, cy, radius - gapWidth - strokeWidth / 2, circleStrokePaint);
        canvas.drawCircle(cx, cy, radius - gapWidth / 2 - strokeWidth / 2 , circleFilledPaint);

        ratio = calculateRatio();
        if(ratio <= 0){
            return;
        }

        if(enableAnimation){
            drawArc(canvas,animationRatio);
            drawText(canvas,animationRatio);
            arcAnimate();
        }else{
            drawText(canvas,-1f);
            drawArc(canvas,ratio);
        }


    }

    private void drawArc(Canvas canvas,float rat) {

        double gapCircleRadius = (float) (gapWidth  * 0.5);

        double tan = gapCircleRadius / (radius - gapCircleRadius);
        double atan = Math.atan(tan);
        float degree = (float) (atan / Math.PI  * 180.0);

        float l = (float) (cx - radius + gapWidth / 2 + 0.5 * strokeWidth);
        float t = (float) (cy - radius + gapWidth / 2 + 0.5 * strokeWidth);
        float r = (float) (cx + radius - gapWidth / 2 - 0.5 * strokeWidth);
        float b = (float) (cy + radius - gapWidth / 2 - 0.5 * strokeWidth);
        RectF oval = new RectF(l,t,r,b);

        float sweepAngle = 360 * rat - 2 * degree;
        if(sweepAngle <= 0 || rat >= 1){
            sweepAngle = 360 * rat;
            canvas.drawArc(oval, startAngle + degree,sweepAngle, false, ratioPaint);
            return;
        }

        canvas.drawArc(oval, startAngle + degree,sweepAngle, false, ratioPaint);

        //画第一个圆角
        //正常坐标系是算圆角逆时针的，但是android里面是顺时针的，所以需要加上90度进行转换
        double startRadiusAngle = startAngle + degree + 90;
        PointF ce1 = calculateCircleCenter(gapCircleRadius,startRadiusAngle);
        canvas.drawCircle(ce1.x, ce1.y, (float) gapCircleRadius, circleAnglePaint);

        //画第二个圆角
        //正常坐标系是算圆角逆时针的，但是android里面是顺时针的，所以需要加上90度进行转换
        double endRadiusAngle = startAngle + sweepAngle + degree + 90;
        PointF ce2 = calculateCircleCenter(gapCircleRadius,endRadiusAngle);
        canvas.drawCircle(ce2.x, ce2.y, (float) gapCircleRadius, circleAnglePaint);
    }

    private float textPadding = 5;

    private void drawText(Canvas canvas,float animationRatio) {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);

        //绘制中间的字符串
        textPaint.setColor(centerTextColor);
        String centerText;
        if(ratio == -1f || animationRatio >= ratio){
            centerText = String.valueOf(coveredCount) + "/" + String.valueOf(total);
        }else{
            centerText = String.valueOf((int)(total * animationRatio)) + "/" + String.valueOf(total);
        }
//        String centerText = String.valueOf(coveredCount) + "/" + String.valueOf(total);
        float r = radius - gapWidth - strokeWidth - textPadding;
        float centerTextSize = 2 * r / 5f;
        textPaint.setTextSize(centerTextSize);
        float baseLineCenter = cy + centerTextSize * 0.25f;
        canvas.drawText(centerText,cx,baseLineCenter,textPaint);

        //绘制上边的字符串
        textPaint.setColor(aboveTextColor);
        float aboveDistance = 0.75f * centerTextSize + centerTextSize * 0.6f;        //圆心与弦的距离
        float result = (float) (Math.pow(r,2f) - Math.pow(aboveDistance,2));
        float aboveStringLength = (float) (2f *  Math.pow(result,0.5));               //计算弦长
        float aboveTextSize = aboveStringLength / 8.0f;
        textPaint.setTextSize(aboveTextSize);
        float baseLineAbove = (cy - aboveDistance) + aboveDistance/ 4.0f;
        canvas.drawText(ABOVE_TEXT,cx,baseLineAbove,textPaint);

        //绘制下边的字符串
        textPaint.setTextAlign(Paint.Align.LEFT);
        String belowText1 = String.valueOf(total - coveredCount);
        String belowText2 =  BELOW_TEXT;
        float belowTextSize = aboveTextSize;
        float belowDistance = aboveDistance + 0.75f * aboveTextSize;
        textPaint.setTextSize(belowTextSize);

        textPaint.setColor(restCountColor);
        float baseLineBelow = cy + belowDistance - 0.25f * belowDistance;
        float textSize1 = textPaint.measureText(belowText1);
        float textSize2 = textPaint.measureText(belowText2);
        float x1 = cx - (textSize1 + textSize2) / 2.0f;
        float x2 = x1 + textSize1;
        canvas.drawText(belowText1,x1,baseLineBelow,textPaint);
        textPaint.setColor(belowTextColor);
        canvas.drawText(belowText2,x2,baseLineBelow,textPaint);

    }

    private  PointF calculateCircleCenter(double gapCircleRadius,double radiusAngle) {
        PointF result = new PointF();
        double radians = radiusAngle / 360 * 2 * Math.PI;  //转化成弧度值
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double r = radius - gapCircleRadius;
        //已知圆心和半径，和圆心角，求圆上一点
        result.x = (float) (cx + r * sin);
        //因为坐标系不一样所以不是cy + r * cos,而是cy - r * cos，
        //y轴方向与正常坐标系相反
        result.y = (float) (cy - r * cos);

        return result;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
        postInvalidate();
    }

    public int getCoveredCount() {
        return coveredCount;
    }

    public void setCoveredCount(int coveredCount) {
        this.coveredCount = coveredCount;
        postInvalidate();
    }

    public void setData(int coveredCount,int total){
        this.coveredCount = coveredCount;
        this.total = total;
        postInvalidate();
    }

    public void increaseByOne(){
        if(this.coveredCount >= total){
            return;
        }
        this.coveredCount++;
        postInvalidate();
    }

    private void arcAnimate(){
        if(animationThread == null || animationRatio >= ratio){
            animationThread = new AnimationThread();
            animationThread.start();
        }
    }
    public int px2dip(float pxValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public int dip2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    class AnimationThread extends Thread{

        @Override
        public void run() {
            while (animationRatio < ratio){
                animationRatio += 1.0f / 360f;
//                Log.i("动画比例:",animationRatio + "");
                postInvalidate();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}


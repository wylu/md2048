package com.admin.md2048;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by admin on 2017/10/3.
 */
public class CellView extends View {

    private int TEXT_BLACK;
    private int TEXT_WHITE;
    private int TEXT_BROWN;
    private int cellSize;

    private Context context;
    private Drawable cellBackgroundRect;

    /**
     * view上的数字
     */
    private int number = 2;
    private String numberStr = "2";
    private Paint paint;

    /**
     * 绘制文字的区域
     */
    private Rect textRect;

    public CellView(Context context) {
        this(context, null);
    }

    public CellView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle);
        TEXT_WHITE = ContextCompat.getColor(context, R.color.text_white);
        TEXT_BLACK = ContextCompat.getColor(context, R.color.text_black);
        TEXT_BROWN = ContextCompat.getColor(context, R.color.text_brown);
        paint = new Paint();
        textRect = new Rect();
    }

    public void setNumber(int number) {
        this.number = number;
        this.numberStr = String.valueOf(number);
        invalidate();
    }

    public int getNumber() {
        return number;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        cellSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        setMeasuredDimension(cellSize, cellSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (number) {
            case 2:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_2);
                break;
            case 4:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_4);
                break;
            case 8:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_8);
                break;
            case 16:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_16);
                break;
            case 32:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_32);
                break;
            case 64:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_64);
                break;
            case 128:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_128);
                break;
            case 256:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_256);
                break;
            case 512:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_512);
                break;
            case 1024:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_1024);
                break;
            case 2048:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_2048);
                break;
            case 4096:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle_4096);
                break;
            default:
                cellBackgroundRect = ContextCompat.getDrawable(context, R.drawable.cell_rectangle);
                break;
        }
        cellBackgroundRect.setBounds(0, 0, cellSize, cellSize);
        cellBackgroundRect.draw(canvas);

        if (number != 0) {
            drawText(canvas);
        }
    }

    /**
     * 绘制文字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        paint.setTextSize((float) (getWidth() * 0.45));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.getTextBounds(numberStr, 0, numberStr.length(), textRect);
        if (number <= 4) {
            paint.setColor(TEXT_BLACK);
        } else if (number <= 32) {
            paint.setColor(TEXT_BROWN);
        } else {
            paint.setColor(TEXT_WHITE);
        }
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        //为基线到字体上边框的距离,即上图中的top
        float top = fontMetrics.top;
        //为基线到字体下边框的距离,即上图中的bottom
        float bottom = fontMetrics.bottom;
        //基线中间点的y轴计算公式
        int baseLineY = (int) (cellSize / 2.0 - (top + bottom) / 2.0);

        canvas.drawText(numberStr, (float) (cellSize / 2.0), baseLineY, paint);
    }
}

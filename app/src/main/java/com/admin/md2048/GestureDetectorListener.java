package com.admin.md2048;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Created by admin on 2017/10/4.
 */
public class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

    private Context context;
    private Game game;

    public GestureDetectorListener(Context context, Game game) {
        this.context = context;
        this.game = game;
    }

    /**
     * 触发条件 ：
     * X轴或Y轴的坐标位移大于FLING_MIN_DISTANCE
     */
    private final int FLING_MIN_DISTANCE = 50;

//    /**
//     * 最小滑动速度大于FLING_MIN_VELOCITY个像素/秒
//     */
//    private final int FLING_MIN_VELOCITY = 20;

    /**
     * @param e1        第1个ACTION_DOWN MotionEvent
     * @param e2        最后一个ACTION_MOVE MotionEvent
     * @param velocityX velocityX：X轴上的移动速度，像素/秒
     * @param velocityY velocityY：Y轴上的移动速度，像素/秒
     * @return
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float offsetX = e2.getX() - e1.getX();
        float offsetY = e2.getY() - e1.getY();

        if (offsetX > FLING_MIN_DISTANCE && Math.abs(offsetX) > Math.abs(offsetY)) {
            game.move(Constants.ACTION_RIGHT,true);
//            Toast.makeText(context, "right", Toast.LENGTH_SHORT).show();
        } else if (offsetX < -FLING_MIN_DISTANCE && Math.abs(offsetX) > Math.abs(offsetY)) {
            game.move(Constants.ACTION_LEFT,true);
//            Toast.makeText(context, "left", Toast.LENGTH_SHORT).show();
        } else if (offsetY > FLING_MIN_DISTANCE && Math.abs(offsetX) < Math.abs(offsetY)) {
            game.move(Constants.ACTION_DOWN,true);
//            Toast.makeText(context, "down", Toast.LENGTH_SHORT).show();
        } else if (offsetY < -FLING_MIN_DISTANCE && Math.abs(offsetX) < Math.abs(offsetY)) {
            game.move(Constants.ACTION_UP,true);
//            Toast.makeText(context, "up", Toast.LENGTH_SHORT).show();
        }

//        if (offsetX > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
//            game.move(Constants.ACTION_RIGHT);
//        } else if (offsetX < -FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
//            game.move(Constants.ACTION_LEFT);
//        } else if (offsetY > FLING_MIN_DISTANCE && Math.abs(velocityX) < Math.abs(velocityY)) {
//            game.move(Constants.ACTION_DOWN);
//        } else if (offsetY < -FLING_MIN_DISTANCE && Math.abs(velocityX) < Math.abs(velocityY)) {
//            game.move(Constants.ACTION_UP);
//        }

        return true;
    }
}

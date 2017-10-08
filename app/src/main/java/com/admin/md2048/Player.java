package com.admin.md2048;

import android.os.Handler;

/**
 * Created by admin on 2017/10/7.
 */
public class Player {

    private static final int PLAYING = 0;
    private static final int PAUSE = 1;
    private static final int STOP = 2;
    private static final int GO_ON =3;

    private static Thread autoPlayThread;
    /**
     * 对象锁
     */
    private static final Object obj = new Object();

    /**
     * 游戏者状态,线程装态标志位
     */
    private static int playerStatus;

    /**
     * 饿汉式单例模式
     */
    private Player() {
        playerStatus = STOP;
    }

    private static final Player player = new Player();

    /**
     * 静态工厂方法
     *
     * @return
     */
    public static Player getInstance() {
        return player;
    }

    public void play(final Game game, final Handler handler) {
        playerStatus = PLAYING;
        autoPlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!game.isOver()) {
                    synchronized (obj) {
                        try {
                            game.move(Constants.ACTION_UP, false);
                            handler.sendMessage(handler.obtainMessage(Constants.GENERAL_STRATEGY));
                            Thread.sleep(300);
                            if (playerStatus == STOP) break;
                            if (playerStatus == PAUSE) obj.wait();
                            game.move(Constants.ACTION_RIGHT, false);
                            handler.sendMessage(handler.obtainMessage(Constants.GENERAL_STRATEGY));
                            Thread.sleep(300);
                            if (playerStatus == STOP) break;
                            if (playerStatus == PAUSE) obj.wait();
                            game.move(Constants.ACTION_DOWN, false);
                            handler.sendMessage(handler.obtainMessage(Constants.GENERAL_STRATEGY));
                            Thread.sleep(300);
                            if (playerStatus == STOP) break;
                            if (playerStatus == PAUSE) obj.wait();
                            game.move(Constants.ACTION_LEFT, false);
                            handler.sendMessage(handler.obtainMessage(Constants.GENERAL_STRATEGY));
                            Thread.sleep(300);
                            if (playerStatus == STOP) break;
                            if (playerStatus == PAUSE) obj.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        autoPlayThread.start();
    }

    /**
     * 暂停
     * 需要调用goOn()重新唤醒
     */
    public void pause() {
        if (playerStatus == PLAYING) {
            playerStatus = PAUSE;
        }
    }

    /**
     * 停止
     * 结束当前线程
     */
    public void stop() {
        if (playerStatus != STOP) {
            playerStatus = STOP;
        }
    }

    /**
     * 继续
     * 继续执行该线程
     */
    public void goOn() {
        if (playerStatus == PAUSE) {
            synchronized (obj){
                obj.notify();
            }
        }
    }


    public static int getPlayerStatus() {
        return playerStatus;
    }

}

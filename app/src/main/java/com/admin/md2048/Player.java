package com.admin.md2048;

import android.os.Handler;
import com.admin.md2048.AI.AI;

/**
 * Created by admin on 2017/10/7.
 */
public class Player {

    private static final int PLAYING = 0;
    private static final int STOP = 1;

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
        Thread autoPlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!game.isOver()) {
                    synchronized (obj) {
                        try {
                            game.move(new AI(new GameState(game.getCurCellsMatrix())).getBestMove(), false);
                            handler.sendMessage(handler.obtainMessage(Constants.GENERAL_STRATEGY));
                            Thread.sleep(20);
                            if (playerStatus == STOP) break;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                stop();
            }
        });
        autoPlayThread.start();
    }

    /**
     * 停止
     * 结束当前线程
     */
    public void stop() {
        if (playerStatus == PLAYING) {
            playerStatus = STOP;
        }
    }


    public static int getPlayerStatus() {
        return playerStatus;
    }

}

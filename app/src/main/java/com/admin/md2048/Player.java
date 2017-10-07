package com.admin.md2048;

import android.os.Handler;

/**
 * Created by admin on 2017/10/7.
 */
public class Player {

    private static  Thread autoPlayThread;

    public static void play(final Game game, final Handler handler) {
        autoPlayThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!game.isOver()) {
                        game.move(Constants.ACTION_UP,false);
                        handler.sendMessage(handler.obtainMessage(Constants.GENERAL_STRATEGY));
                        Thread.sleep(300);
                        game.move(Constants.ACTION_RIGHT,false);
                        handler.sendMessage(handler.obtainMessage(Constants.GENERAL_STRATEGY));
                        Thread.sleep(300);
                        game.move(Constants.ACTION_DOWN,false);
                        handler.sendMessage(handler.obtainMessage(Constants.GENERAL_STRATEGY));
                        Thread.sleep(300);
                        game.move(Constants.ACTION_LEFT,false);
                        handler.sendMessage(handler.obtainMessage(Constants.GENERAL_STRATEGY));
                        Thread.sleep(300);
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        autoPlayThread.start();
    }

}

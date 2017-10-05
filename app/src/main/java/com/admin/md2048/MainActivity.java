package com.admin.md2048;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String PRE_SCORE = "pre_score";
    private static final String CUR_SCORE = "cur_score";
    private static final String HIGH_SCORE = "high_score";
    private static final String GRID_WIDTH = "grid_width";
    private static final String GRID_HEIGHT = "grid_height";
    private static final String PRE_STATE = "pre_state";
    private static final String CUR_STATE = "cur_state";
    private static final String SAVE_STATE = "save_state";

    private List<CellView> cellViewList;
    private TextView scoreView;
    private TextView highScoreView;
    private ImageView refreshView;
    private ImageView undoView;
    private TextView gameOverView;
    private GestureDetector gestureDetector;

    private Game game;
    private boolean isGameOver;
    private int highScore;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initData();
        initView();

//        Log.d("==onCreate==", "========onCreate>>>>>>>>>>>>>");
        isGameOver = false;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (!settings.getBoolean(SAVE_STATE, false)) {
            playGame();
        }

    }

    private void saveState() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        int[][] preState = game.getPreCellsMatrix();
        int[][] curState = game.getCurCellsMatrix();
        editor.putInt(GRID_WIDTH, preState.length);
        editor.putInt(GRID_HEIGHT, preState[0].length);
        for (int x = 0; x < preState.length; x++) {
            for (int y = 0; y < preState[0].length; y++) {
                editor.putInt(PRE_STATE + x + "_" + y, preState[x][y]);
                editor.putInt(CUR_STATE + x + "_" + y, curState[x][y]);
            }
        }
        editor.putInt(PRE_SCORE, game.getPreScore());
        editor.putInt(CUR_SCORE, game.getCurScore());

        if (isGameOver){
            editor.putBoolean(SAVE_STATE, false);
        }else {
            editor.putBoolean(SAVE_STATE,true);
        }
        editor.commit();
    }

    @Override
    protected void onResume() {
//        Log.d("==onResume==", "==========onRusume()========");
        super.onResume();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean(SAVE_STATE, false)) {
            loadSaveState();
        }
    }

    private void loadSaveState() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int row = settings.getInt(GRID_WIDTH, -1);
        int col = settings.getInt(GRID_HEIGHT, -1);
        int[][] preState = new int[row][col];
        int[][] curState = new int[row][col];
        for (int x = 0; x < row; x++) {
            for (int y = 0; y < col; y++) {
                preState[x][y] = settings.getInt(PRE_STATE + x + "_" + y, -1);
                curState[x][y] = settings.getInt(CUR_STATE + x + "_" + y, -1);
            }
        }
        int preScore = settings.getInt(PRE_SCORE, -1);
        int curScore = settings.getInt(CUR_SCORE, -1);
        recoverGame(preState, curState, preScore, curScore);
    }

    /**
     * 恢复游戏
     *
     * @param preState
     * @param curState
     * @param preScore
     * @param curScore
     */
    private void recoverGame(int[][] preState, int[][] curState, int preScore, int curScore) {
        if (game == null) {
            playGame();
        }
        game.recover(preState, curState, preScore, curScore);
//        isGameOver = false;
//        while (!isGameOver){
//            game.move(0);
//            game.move(1);
//            game.move(2);
//            game.move(3);
//        }
    }

    @Override
    protected void onPause() {
//        Log.d("==onPause==", "===========>>>>>>>>>>>>>");
        super.onPause();
        saveState();
    }

    private void initData() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        highScore = getHighScoreFromSpf();
    }

    private void playGame() {
        game = new Game(this, cellViewList);
        gestureDetector = new GestureDetector(this, new GestureDetectorListener(this, game));
        game.setScoreChangeListener(new Game.ScoreChangeListener() {
            @Override
            public void changeScore(int score) {
                scoreView.setText(String.valueOf(score));
            }
        });
        game.setGameOverListener(new Game.GameOverListener() {
            @Override
            public void gameOver(int score) {
                if (score > highScore) {
                    highScore = score;
                    saveHighScore();
                }
                isGameOver = true;
                gameOverView.setVisibility(View.VISIBLE);
                refreshView.setBackgroundResource(R.drawable.cell_rectangle_2048);
            }
        });
        game.run();
    }

    private void initView() {
        cellViewList = getAllCellView();
        scoreView = (TextView) findViewById(R.id.score);
        highScoreView = (TextView) findViewById(R.id.high_score);
        highScoreView.setText(String.valueOf(highScore));
        refreshView = (ImageView) findViewById(R.id.refresh);
        undoView = (ImageView) findViewById(R.id.undo);
        gameOverView = (TextView) findViewById(R.id.game_over);

        refreshView.setOnClickListener(this);
        undoView.setOnClickListener(this);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                game.move(Constants.ACTION_DOWN);
//                Toast.makeText(this, "down", Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                game.move(Constants.ACTION_UP);
//                Toast.makeText(this, "up", Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                game.move(Constants.ACTION_LEFT);
//                Toast.makeText(this, "left", Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                game.move(Constants.ACTION_RIGHT);
//                Toast.makeText(this, "right", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private List<CellView> getAllCellView() {
        List<CellView> cellViews = new ArrayList<>();
        cellViews.add((CellView) findViewById(R.id.cell_00));
        cellViews.add((CellView) findViewById(R.id.cell_01));
        cellViews.add((CellView) findViewById(R.id.cell_02));
        cellViews.add((CellView) findViewById(R.id.cell_03));
        cellViews.add((CellView) findViewById(R.id.cell_10));
        cellViews.add((CellView) findViewById(R.id.cell_11));
        cellViews.add((CellView) findViewById(R.id.cell_12));
        cellViews.add((CellView) findViewById(R.id.cell_13));
        cellViews.add((CellView) findViewById(R.id.cell_20));
        cellViews.add((CellView) findViewById(R.id.cell_21));
        cellViews.add((CellView) findViewById(R.id.cell_22));
        cellViews.add((CellView) findViewById(R.id.cell_23));
        cellViews.add((CellView) findViewById(R.id.cell_30));
        cellViews.add((CellView) findViewById(R.id.cell_31));
        cellViews.add((CellView) findViewById(R.id.cell_32));
        cellViews.add((CellView) findViewById(R.id.cell_33));
        return cellViews;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.refresh:
                gameOverView.setVisibility(View.GONE);
                refreshView.setBackgroundResource(R.drawable.background_rectangle);
                highScore = getHighScoreFromSpf();
                highScoreView.setText(String.valueOf(highScore));
                playGame();
                break;
            case R.id.undo:
                gameOverView.setVisibility(View.GONE);
                refreshView.setBackgroundResource(R.drawable.background_rectangle);
                game.undoMove();
                break;
        }
    }

    /**
     * 从SharedPreferences中获取最高分数
     *
     * @return
     */
    private int getHighScoreFromSpf() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getInt(HIGH_SCORE, 0);
    }

    /**
     * 保存最高分数到SharedPreferences
     */
    private void saveHighScore() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(HIGH_SCORE, highScore);
        editor.commit();
    }
}

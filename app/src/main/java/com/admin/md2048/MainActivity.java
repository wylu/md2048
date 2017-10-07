package com.admin.md2048;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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
    //刷新
    private ImageView refreshView;
    //撤销
    private ImageView undoView;
    //提示
    private TextView hintView;
    private ImageView showHintView;
    //自动
    private TextView autoView;
    //游戏结束时视图
    private TextView gameOverView;
    //检测手势
    private GestureDetector gestureDetector;

    private Game game;
    private boolean isGameOver;
    private int highScore;
    private int currentScore;

    /**
     * 侧滑菜单
     */
    private DrawerLayout mDrawerLayout;
    private LinearLayout menuResetHighScore;
    private LinearLayout menuGeneralStrategy;
    private LinearLayout menuAdvancedStrategy;


    /**
     * 处理子线程的视图更新操作
     */
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constants.GENERAL_STRATEGY:
                    game.manualUpdateView();
                    break;
                case Constants.ADVANCED_STRATEGY:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化数据
        initData();
        //初始化主布局
        initView();
        //初始化侧滑菜单
        initDrawerLayoutMenu();

//        Log.d("==onCreate==", "========onCreate>>>>>>>>>>>>>");
        isGameOver = false;
        //判断是否存在保存的游戏状态，如果没有则重新创建一个游戏；如果有则在执行onResume()时恢复游戏状态
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

        if (isGameOver) {
            editor.putBoolean(SAVE_STATE, false);
        } else {
            editor.putBoolean(SAVE_STATE, true);
        }
        editor.commit();
    }

    @Override
    protected void onResume() {
//        Log.d("==onResume==", "==========onRusume()========");
        super.onResume();
        //如果有保存游戏状态，则恢复之前的游戏状态
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
    }

    @Override
    protected void onPause() {
//        Log.d("==onPause==", "===========>>>>>>>>>>>>>");
        super.onPause();
        saveState();
    }

    /**
     * 初始化数据，获取程序运行需要的数据
     */
    private void initData() {
        highScore = getHighScoreFromSpf();
    }

    private void playGame() {
        game = new Game(this, cellViewList);
        gestureDetector = new GestureDetector(this, new GestureDetectorListener(this, game));
        game.setScoreChangeListener(new Game.ScoreChangeListener() {
            @Override
            public void changeScore(int score) {
                scoreView.setText(String.valueOf(score));
                currentScore = score;
                //如果当前分数比最高分大，则同步更新最高分
                if (currentScore > highScore) {
                    highScore = currentScore;
                    saveHighScore(highScore);
                    highScoreView.setText(String.valueOf(highScore));
                }
            }
        });
        game.setGameOverListener(new Game.GameOverListener() {
            @Override
            public void gameOver(int score) {
                isGameOver = true;
                if (game.isWin()) {
                    gameOverView.setBackgroundResource(R.color.you_win);
                    gameOverView.setText(R.string.game_win);
                    gameOverView.setTextColor(R.color.md_white_1000);
                }
                gameOverView.setVisibility(View.VISIBLE);
                refreshView.setBackgroundResource(R.drawable.cell_rectangle_2048);
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (game.isWin()){
//                            gameOverView.setText(R.string.game_win);
//                        }else {
//                            gameOverView.setText(R.string.game_lose);
//                        }
//                    }
//                },1000);
            }
        });
        game.run();
    }

    /**
     * 初始化主布局视图
     */
    private void initView() {
        cellViewList = getAllCellView();
        scoreView = (TextView) findViewById(R.id.score);
        highScoreView = (TextView) findViewById(R.id.high_score);
        highScoreView.setText(String.valueOf(highScore));
        refreshView = (ImageView) findViewById(R.id.refresh);
        undoView = (ImageView) findViewById(R.id.undo);
        gameOverView = (TextView) findViewById(R.id.game_over);

        hintView = (TextView) findViewById(R.id.hint);
        showHintView = (ImageView) findViewById(R.id.arrow);
        autoView = (TextView) findViewById(R.id.auto);
        hintView.setOnClickListener(this);
        autoView.setOnClickListener(this);

        refreshView.setOnClickListener(this);
        undoView.setOnClickListener(this);
    }

    /**
     * 初始化侧滑菜单布局视图
     */
    private void initDrawerLayoutMenu() {
        //侧滑菜单
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //设置Toolbar标题
        toolbar.setTitle("");
        //设置标题颜色
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(toolbar);
        //打开-关闭监听
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        //侧滑菜单项
        menuResetHighScore = (LinearLayout) findViewById(R.id.menu_reset_high_score);
        menuResetHighScore.setOnClickListener(this);
        menuGeneralStrategy = (LinearLayout) findViewById(R.id.menu_general_strategy);
        menuGeneralStrategy.setOnClickListener(this);
        menuAdvancedStrategy = (LinearLayout) findViewById(R.id.menu_advanced_strategy);
        menuAdvancedStrategy.setOnClickListener(this);
    }

    /**
     * 获取所有的方块
     *
     * @return
     */
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
                gameOverView.setText(R.string.game_over);
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
            case R.id.menu_reset_high_score:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                resetHighScore();
                break;
            case R.id.menu_general_strategy:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Player.play(game,handler);
                break;
            case R.id.auto:
                Player.play(game,handler);
                break;
            case R.id.hint:
                break;
        }
    }

    /**
     * 重置最高分数
     */
    private void resetHighScore() {
        highScore = 0;
        saveHighScore(highScore);
        highScoreView.setText(String.valueOf(highScore));
        Toast.makeText(this, "重置成功!", Toast.LENGTH_SHORT).show();
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
    private void saveHighScore(int mark) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(HIGH_SCORE, mark);
        editor.commit();
    }

    /**
     * 在事件分发之前，先处理手势操作，否则drawerlayout会拦截并消费手势事件，导致子view无响应
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mDrawerLayout.isDrawerOpen(GravityCompat.START) && !mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 响应按键输入操作
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                game.move(Constants.ACTION_DOWN,true);
//                Toast.makeText(this, "down", Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                game.move(Constants.ACTION_UP,true);
//                Toast.makeText(this, "up", Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                game.move(Constants.ACTION_LEFT,true);
//                Toast.makeText(this, "left", Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                game.move(Constants.ACTION_RIGHT,true);
//                Toast.makeText(this, "right", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}

package com.admin.md2048;

import android.content.Context;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by admin on 2017/10/4.
 */
public class Game {

    private int columnSize;
    private Context context;
    private CellView cellViewsMatrix[][];



    /**
     * 保存前一个状态，用于撤销操作
     */
    private int preCellsMatrix[][];
    private int preScore;
    /**
     * 当前状态
     */
    private int curCellsMatrix[][];
    private int curScore;
    private ScoreChangeListener scoreChangeListener;
    private GameOverListener gameOverListener;

    /**
     * 用于判断cellview有没有移动过，如果没有则不随机生成新的数字
     */
    private boolean isCellHaveMoved;

    /**
     * 游戏是否结束
     */
    private boolean gameStatus;

    /**
     * 记录合并数字的坐标
     * 用于绘制合并的动画效果
     */
    private int animation[][];

    public Game(Context context, List<CellView> cellViewList) {
        this.context = context;
        curScore = 0;
        preScore = 0;
        gameStatus = false;
        columnSize = (int) Math.sqrt(cellViewList.size());
        cellViewsMatrix = new CellView[columnSize][columnSize];
        curCellsMatrix = new int[columnSize][columnSize];
        animation = new int[columnSize][columnSize];

        for (int i = 0; i < cellViewList.size(); i++) {
            int row = i / columnSize, col = i % columnSize;
            cellViewsMatrix[row][col] = cellViewList.get(i);
            curCellsMatrix[row][col] = 0;

            cellViewsMatrix[row][col].setNumber(0);
        }
    }

    public void run() {
        scoreChangeListener.changeScore(curScore);
        generateRandomNum();
        generateRandomNum();
        preCellsMatrix = new int[columnSize][columnSize];
        ArrayUtil.matrixCopy(curCellsMatrix, preCellsMatrix, columnSize, columnSize);
    }

    /**
     * 恢复游戏
     *
     * @param preState
     * @param curState
     * @param preScore
     * @param curScore
     */
    public void recover(int[][] preState, int[][] curState, int preScore, int curScore) {
        gameStatus = false;
        ArrayUtil.matrixCopy(preState, preCellsMatrix, preState.length, preState[0].length);
        ArrayUtil.matrixCopy(curState, curCellsMatrix, curState.length, curState[0].length);
        this.preScore = preScore;
        this.curScore = curScore;
        scoreChangeListener.changeScore(curScore);
        updateView();
    }

    /**
     * 根据用户手势合并数值
     * 关于isAutoUpdateView补充说明：
     * 该参数为了避免新开子线程中调用该方法时，更新UI线程而引起的异常(CalledFromWrongThreadException)
     * 而使用子线程是为了AI程序能够模拟人类玩家
     *
     * @param action
     * @param isAutoUpdateView 是否自动更新视图，如果传入false，需要手动更新视图
     */
    public void move(int action, boolean isAutoUpdateView) {
        ArrayUtil.intiMatrixToZero(animation);
//        ArrayUtil.printMatrix(animation);
        if (isGameOver()) {
            gameStatus = true;
            if (isAutoUpdateView) {
                gameOverListener.gameOver(curScore);
            }
        } else {
            //移动前，先保存当前状态( 网格状态、分数 )
            int saveState[][] = new int[columnSize][columnSize], saveScore = curScore;
            ArrayUtil.matrixCopy(curCellsMatrix, saveState, columnSize, columnSize);

            isCellHaveMoved = false;
            switch (action) {
                case Constants.ACTION_UP:
                    actionUpMove();
                    break;
                case Constants.ACTION_RIGHT:
                    actionRightMove();
                    break;
                case Constants.ACTION_DOWN:
                    actionDownMove();
                    break;
                case Constants.ACTION_LEFT:
                    actionLeftMove();
                    break;
            }
            //如果发生过移动则更新前一个状态；如果未发生移动，前一个状态不变
            if (isCellHaveMoved) {
                preScore = saveScore;
                ArrayUtil.matrixCopy(saveState, preCellsMatrix, columnSize, columnSize);
                //自动更新视图
                if (isAutoUpdateView) {
                    //通知监听者分数发生了改变
                    scoreChangeListener.changeScore(curScore);
                    //更新视图
                    updateView();
                    //随机生成新的数字
                    generateRandomNum();
                }
            }
        }
    }

    /**
     * 手动更新视图
     * 判断游戏是否结束
     * 结束：显示结束视图
     * 未结束：更新score、方块的视图
     */
    public void manualUpdateView() {
        if (isOver()) {
            gameOverListener.gameOver(curScore);
        } else {
            if (isCellHaveMoved) {
                scoreChangeListener.changeScore(curScore);
                updateView();
                generateRandomNum();
            }
        }
    }

    /**
     * 更新视图
     */
    private void updateView() {
        //绘制缩放动画效果
        Animation myAnimation = AnimationUtils.loadAnimation(context, R.anim.merge_scale);
//        ArrayUtil.printMatrix(animation);
        for (int i = 0; i < columnSize; i++) {
            for (int j = 0; j < columnSize; j++) {
                cellViewsMatrix[i][j].setNumber(curCellsMatrix[i][j]);
                if (animation[i][j] != 0) {
                    cellViewsMatrix[i][j].startAnimation(myAnimation);
                }
            }
        }
    }

    /**
     * 撤销操作(允许撤销一次)
     */
    public void undoMove() {
        ArrayUtil.intiMatrixToZero(animation);
        curScore = preScore;
        ArrayUtil.matrixCopy(preCellsMatrix, curCellsMatrix, columnSize, columnSize);
        scoreChangeListener.changeScore(curScore);
        updateView();
    }

    /**
     * 向左滑
     */
    private void actionLeftMove() {
        for (int i = 0; i < columnSize; i++) {
            mergeCells(curCellsMatrix[i], Constants.ACTION_LEFT, i);
        }
    }

    /**
     * 向右滑
     */
    private void actionRightMove() {
        for (int i = 0; i < columnSize; i++) {
            mergeCells(curCellsMatrix[i], Constants.ACTION_RIGHT, i);
        }
    }

    /**
     * 向上滑
     */
    private void actionUpMove() {
        ArrayUtil.antiClockwiseRotate90(curCellsMatrix, columnSize);
        for (int i = 0; i < columnSize; i++) {
            mergeCells(curCellsMatrix[i], Constants.ACTION_UP, i);
        }
        ArrayUtil.clockwiseRotate90(curCellsMatrix, columnSize);
        ArrayUtil.clockwiseRotate90(animation, columnSize);
    }

    /**
     * 向下滑
     */
    private void actionDownMove() {
        ArrayUtil.antiClockwiseRotate90(curCellsMatrix, columnSize);
        for (int i = 0; i < columnSize; i++) {
            mergeCells(curCellsMatrix[i], Constants.ACTION_DOWN, i);
        }
        ArrayUtil.clockwiseRotate90(curCellsMatrix, columnSize);
        ArrayUtil.clockwiseRotate90(animation, columnSize);
    }

    /**
     * 合并相同的数字
     * 关于新增参数line的补充说明：
     * 方便记录需要绘制动画效果的方块的坐标
     *
     * @param row
     * @param action
     * @param line   表示当前传入的row[]是矩阵的第几行数据
     */
    private void mergeCells(int[] row, int action, int line) {
        //是否需要绘制动画标记
        int animMark[] = new int[row.length];

        int mergeRow[] = new int[row.length];
        System.arraycopy(row, 0, mergeRow, 0, row.length);

        int moveRow[] = new int[row.length];
        if (action == Constants.ACTION_LEFT || action == Constants.ACTION_UP) {
            //进行合并，如 2 2 4 4，合并后为 4 0 8 0
            for (int i = 0; i < mergeRow.length - 1; i++) {
                if (mergeRow[i] == 0) continue;
                for (int j = i + 1; j < mergeRow.length; j++) {
                    if (mergeRow[j] == 0) continue;
                    if (mergeRow[i] == mergeRow[j]) {
                        mergeRow[i] *= 2;
                        mergeRow[j] = 0;
                        //记录动画标记
                        animMark[i] = 1;
                        //分数增加
                        curScore += mergeRow[i];
                    }
                    break;
                }
            }

            int k = 0;
            //移动，如 4 0 8 0，移动后为 4 8 0 0
            for (int i = 0; i < mergeRow.length; i++) {
                if (mergeRow[i] != 0) {
                    //移动动画标记
                    if (animMark[i] != 0) animation[line][k] = animMark[i];
                    moveRow[k++] = mergeRow[i];
                }
            }
        }
        if (action == Constants.ACTION_RIGHT || action == Constants.ACTION_DOWN) {
            //进行合并，如 2 2 4 4，合并后为 0 4 0 8
            for (int i = mergeRow.length - 1; i > 0; i--) {
                if (mergeRow[i] == 0) continue;
                for (int j = i - 1; j >= 0; j--) {
                    if (mergeRow[j] == 0) continue;
                    if (mergeRow[i] == mergeRow[j]) {
                        mergeRow[i] *= 2;
                        mergeRow[j] = 0;
                        //记录动画标记
                        animMark[i] = 1;
                        //分数增加
                        curScore += mergeRow[i];
                    }
                    break;
                }
            }

            int k = row.length - 1;
            //移动，如 0 4 0 8，移动后为 0 0 4 8
            for (int i = k; i >= 0; i--) {
                if (mergeRow[i] != 0) {
                    //移动动画标记
                    if (animMark[i] != 0) animation[line][k] = animMark[i];
                    moveRow[k--] = mergeRow[i];
                }
            }
        }

        if (!ArrayUtil.isEquals(mergeRow, row) || !ArrayUtil.isEquals(moveRow, mergeRow)) {
            isCellHaveMoved = true;
        }

        System.arraycopy(moveRow, 0, row, 0, moveRow.length);
    }

    /**
     * 随机生成2或4
     */
    private void generateRandomNum() {
        List<Integer> zeroCells = new ArrayList<>();
        for (int i = 0; i < columnSize * columnSize; i++) {
            if (curCellsMatrix[i / columnSize][i % columnSize] == 0) {
                zeroCells.add(i);
            }
        }
        Random random = new Random();
        int next = random.nextInt(zeroCells.size());
        int newNum = Math.random() < 0.9 ? 2 : 4;
        curCellsMatrix[zeroCells.get(next) / columnSize][zeroCells.get(next) % columnSize] = newNum;
        cellViewsMatrix[zeroCells.get(next) / columnSize][zeroCells.get(next) % columnSize].setNumber(newNum);
        //绘制缩放动画效果
        Animation myAnimation = AnimationUtils.loadAnimation(context, R.anim.scale);
        cellViewsMatrix[zeroCells.get(next) / columnSize][zeroCells.get(next) % columnSize].startAnimation(myAnimation);
    }

    /**
     * 游戏是否结束
     * 填满数字的情况下，检测相邻的没有相同的数字
     *
     * @return
     */
    private boolean isGameOver() {
        if (!isFull()) {
            return false;
        }
        for (int i = 0; i < columnSize; i++) {
            for (int j = 0; j < columnSize; j++) {
                //与上边相邻数字是否相同
                if (i - 1 >= 0 && curCellsMatrix[i][j] == curCellsMatrix[i - 1][j]) {
                    return false;
                }
                //与右边相邻数字是否相同
                if (j + 1 < columnSize && curCellsMatrix[i][j] == curCellsMatrix[i][j + 1]) {
                    return false;
                }
                //与下边相邻数字是否相同
                if (i + 1 < columnSize && curCellsMatrix[i][j] == curCellsMatrix[i + 1][j]) {
                    return false;
                }
                //与左边相邻数字是否相同
                if (j - 1 >= 0 && curCellsMatrix[i][j] == curCellsMatrix[i][j - 1]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 是否填满数字
     *
     * @return
     */
    private boolean isFull() {
        for (int i = 0; i < columnSize; i++) {
            for (int j = 0; j < columnSize; j++) {
                if (curCellsMatrix[i][j] == 0)
                    return false;
            }
        }
        return true;
    }

    /**
     * 判断游戏是否胜利
     *
     * @return 胜利返回true; 失败返回false
     */
    public boolean isWin() {
        for (int i = 0; i < columnSize; i++) {
            for (int j = 0; j < columnSize; j++) {
                if (curCellsMatrix[i][j] >= 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置分数改变监听器
     *
     * @param listener
     */
    public void setScoreChangeListener(ScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }

    /**
     * 分数监听回调接口
     */
    public interface ScoreChangeListener {
        //回调方法
        void changeScore(int curScore);
    }

    /**
     * 设置游戏结束监听器
     *
     * @param listener
     */
    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    /**
     * 游戏结束回调接口
     */
    public interface GameOverListener {
        void gameOver(int curScore);
    }

    public int[][] getPreCellsMatrix() {
        return preCellsMatrix;
    }

    public int[][] getCurCellsMatrix() {
        return curCellsMatrix;
    }

    public int getPreScore() {
        return preScore;
    }

    public int getCurScore() {
        return curScore;
    }

    /**
     * 游戏是否结束
     *
     * @return 返回游戏状态
     */
    public boolean isOver() {
        return gameStatus;
    }
}

package com.admin.md2048;

/**
 * Created by admin on 2017/10/8.
 */
public class GameState {

    private int score;
    private int[][] cellMatrix;

    public GameState(int score,int[][] cellMatrix){
        this.score = score;
        this.cellMatrix = new int[cellMatrix.length][cellMatrix[0].length];
        ArrayUtil.copyMatrix(cellMatrix,this.cellMatrix,cellMatrix.length,cellMatrix[0].length);
    }

    public int getScore() {
        return score;
    }

    public int[][] getCellMatrix() {
        return cellMatrix;
    }
}

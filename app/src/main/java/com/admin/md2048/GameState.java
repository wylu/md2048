package com.admin.md2048;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/10/8.
 */
public class GameState {

    private int score;
    private int[][] cellMatrix;

    public boolean playerTurn = true;

    private int[][] vectors = {
            {0, -1}, // up
            {1, 0},  // right
            {0, 1},  // down
            {-1, 0}   // left
    };

    private boolean[][] marked;

    public GameState(int[][] cellMatrix) {
        this.cellMatrix = new int[cellMatrix.length][cellMatrix[0].length];
        ArrayUtil.copyMatrix(cellMatrix, this.cellMatrix, cellMatrix.length, cellMatrix[0].length);
    }

    public GameState(int score, int[][] cellMatrix) {
        this.score = score;
        this.cellMatrix = new int[cellMatrix.length][cellMatrix[0].length];
        ArrayUtil.copyMatrix(cellMatrix, this.cellMatrix, cellMatrix.length, cellMatrix[0].length);
    }

    public int getScore() {
        return score;
    }

    public int[][] getCellMatrix() {
        return cellMatrix;
    }

    private boolean isCellAvailable(int cnt_x, int cnt_y) {
        return cellMatrix[cnt_x][cnt_y] == 0;
    }

    private boolean isInBounds(int cnt_x, int cnt_y) {
        return cnt_x >= 0 && cnt_x < 4 && cnt_y >= 0 && cnt_y < 4;
    }

    // measures how smooth the grid is (as if the values of the pieces
    // were interpreted as elevations). Sums of the pairwise difference
    // between neighboring tiles (in log space, so it represents the
    // number of merges that need to happen before they can merge).
    // Note that the pieces can be distant
    public double smoothness() {
        int smoothness = 0;
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (this.cellMatrix[x][y] != 0) {
                    double value = Math.log(this.cellMatrix[x][y]) / Math.log(2);
                    for (int direction = 1; direction <= 2; direction++) {
                        int[] vector = this.vectors[direction];
                        int cnt_x = x, cnt_y = y;
                        do {
                            cnt_x += vector[0];
                            cnt_y += vector[1];
                        } while (isInBounds(cnt_x, cnt_y) && isCellAvailable(cnt_x, cnt_y));
                        if (isInBounds(cnt_x, cnt_y)) {
                            if (cellMatrix[cnt_x][cnt_y] != 0) {
                                double targetValue = Math.log(cellMatrix[cnt_x][cnt_y]) / Math.log(2);
                                smoothness -= Math.abs(value - targetValue);
                            }
                        }
                    }
                }
            }
        }
        return smoothness;
    }

    // measures how monotonic the grid is. This means the values of the tiles are strictly increasing
    // or decreasing in both the left/right and up/down directions
    public double monotonicity() {
        // scores for all four directions
        int[] totals = {0, 0, 0, 0};

        // left/right direction
        for (int x = 0; x < 4; x++) {
            int current = 0;
            int next = current + 1;
            while (next < 4) {
                while (next < 4 && this.cellMatrix[x][next] == 0) next++;
                if (next >= 4) next--;
                double currentValue = (this.cellMatrix[x][current] != 0) ? Math.log(this.cellMatrix[x][current]) / Math.log(2) : 0;
                double nextValue = (this.cellMatrix[x][next] != 0) ? Math.log(this.cellMatrix[x][next]) / Math.log(2) : 0;
                if (currentValue > nextValue) {
                    totals[0] += nextValue - currentValue;
                } else if (nextValue > currentValue) {
                    totals[1] += currentValue - nextValue;
                }
                current = next;
                next++;
            }
        }

        // up/down direction
        for (int y = 0; y < 4; y++) {
            int current = 0;
            int next = current + 1;
            while (next < 4) {
                while (next < 4 && this.cellMatrix[next][y] == 0) next++;
                if (next >= 4) next--;
                double currentValue = (this.cellMatrix[current][y] != 0) ? Math.log(this.cellMatrix[current][y]) / Math.log(2) : 0;
                double nextValue = (this.cellMatrix[next][y] != 0) ? Math.log(this.cellMatrix[next][y]) / Math.log(2) : 0;
                if (currentValue > nextValue) {
                    totals[2] += nextValue - currentValue;
                } else if (nextValue > currentValue) {
                    totals[3] += currentValue - nextValue;
                }
                current = next;
                next++;
            }
        }

        return Math.max(totals[0], totals[1]) + Math.max(totals[2], totals[3]);
    }

    public double maxValue() {
        return Math.log(ArrayUtil.getMax(cellMatrix)) / Math.log(2);
    }

    public boolean move(int direction) {
        int[][] preMatrix = new int[cellMatrix.length][cellMatrix[0].length];
        ArrayUtil.copyMatrix(cellMatrix, preMatrix, 4, 4);

        boolean moved = false;

        switch (direction) {
            case Constants.ACTION_UP:
                ArrayUtil.antiClockwiseRotate90(cellMatrix, 4);
                for (int i = 0; i < 4; i++) merge(cellMatrix[i], Constants.ACTION_UP);
                ArrayUtil.clockwiseRotate90(cellMatrix, 4);
                break;
            case Constants.ACTION_RIGHT:
                for (int i = 0; i < 4; i++) merge(cellMatrix[i], Constants.ACTION_RIGHT);
                break;
            case Constants.ACTION_DOWN:
                ArrayUtil.antiClockwiseRotate90(cellMatrix, 4);
                for (int i = 0; i < 4; i++) merge(cellMatrix[i], Constants.ACTION_DOWN);
                ArrayUtil.clockwiseRotate90(cellMatrix, 4);
                break;
            case Constants.ACTION_LEFT:
                for (int i = 0; i < 4; i++) merge(cellMatrix[i], Constants.ACTION_LEFT);
                break;
        }

        if (!ArrayUtil.isMatrixEquals(preMatrix, cellMatrix)) {
            moved = true;
            this.playerTurn = false;
        }

        return moved;
    }

    /**
     * 合并相同的数字
     *
     * @param row
     * @param action
     */
    private void merge(int[] row, int action) {

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
                    }
                    break;
                }
            }
            int k = 0;
            //移动，如 4 0 8 0，移动后为 4 8 0 0
            for (int i = 0; i < mergeRow.length; i++) {
                if (mergeRow[i] != 0) moveRow[k++] = mergeRow[i];
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
                    }
                    break;
                }
            }
            int k = row.length - 1;
            //移动，如 0 4 0 8，移动后为 0 0 4 8
            for (int i = k; i >= 0; i--) {
                if (mergeRow[i] != 0) moveRow[k--] = mergeRow[i];
            }
        }

        System.arraycopy(moveRow, 0, row, 0, moveRow.length);
    }

    public boolean isWin() {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (cellMatrix[x][y] == 2048) return true;
            }
        }
        return false;
    }

    public List<int[]> getAvailableCells() {
        List<int[]> cells = new ArrayList<>();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (cellMatrix[x][y] == 0) {
                    int[] tmp = {x, y};
                    cells.add(tmp);
                }
            }
        }
        return cells;
    }

    public void insertTitle(int x, int y, int value) {
        this.cellMatrix[x][y] = value;
    }

    // counts the number of isolated groups.
    public int islands() {
        int islands = 0;

        marked = new boolean[4][4];
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (this.cellMatrix[x][y] != 0) {
                    this.marked[x][y] = false;
                }
            }
        }
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (this.cellMatrix[x][y] != 0 && !this.marked[x][y]) {
                    islands++;
                    mark(x, y, this.cellMatrix[x][y]);
                }
            }
        }

        return islands;
    }

    private void mark(int x, int y, int value) {
        if (x >= 0 && x <= 3 && y >= 0 && y <= 3 && (this.cellMatrix[x][y] != 0)
                && (this.cellMatrix[x][y] == value) && (!this.marked[x][y])) {
            this.marked[x][y] = true;
            for (int direction = 0; direction < 4; direction++) {
                int[] vector = this.vectors[direction];
                mark(x + vector[0], y + vector[1], value);
            }
        }
    }

    public void removeTile(int x, int y) {
        this.cellMatrix[x][y] = 0;
    }
}

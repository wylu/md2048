package com.admin.md2048.AI;

import com.admin.md2048.ArrayUtil;
import com.admin.md2048.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2018/1/8.
 */
public class AI {

    private GameState grid;

    public AI(GameState grid) {
        this.grid = new GameState(grid.getCellMatrix());
    }

    public int getEmptyNum(int[][] matrix) {
        int sum = 0;
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[0].length; j++)
                if (matrix[i][j] == 0) sum++;
        return sum;
    }

    public double evaluate() {
        double smoothWeight = 0.1,
                monoWeight = 1.3,
                emptyWeight = 2.7,
                maxWeight = 1.0;
//        double res1 = grid.smoothness();
//        double res2 = grid.monotonicity();
//        double res3 = getEmptyNum(grid.getCellMatrix());
//        double res4 = grid.maxValue();
//        double result = res1 * smoothWeight
//                + res2 * monoWeight
//                + Math.log(res3) * emptyWeight
//                + res4 * maxWeight;
//        System.out.println("============================= " + result);
        return grid.smoothness() * smoothWeight
                + grid.monotonicity() * monoWeight
                + Math.log(getEmptyNum(grid.getCellMatrix())) * emptyWeight
                + grid.maxValue() * maxWeight;
    }

    public SearchResult search(int depth, double alpha, double beta, int positions, int cutoffs) {
        double bestScore;
        int bestMove = -1;
        SearchResult result = new SearchResult();
        int[] directions = {0, 1, 2, 3};

        // the maxing player
        if (this.grid.playerTurn) {
            bestScore = alpha;
            for (int direction : directions) {
                GameState newGrid = new GameState(this.grid.getCellMatrix());
                if (newGrid.move(direction)) {
                    positions++;
//                    if (newGrid.isWin()) {
//                        return new SearchResult(direction, 10000, positions, cutoffs);
//                    }
                    AI newAI = new AI(newGrid);
                    newAI.grid.playerTurn = false;

                    if (depth == 0) {
//                        System.out.println("---------- depth 0 ------>>>>>>>>>>>>>>>>>>>");
                        result.move = direction;
                        result.score = newAI.evaluate();
                    } else {
//                        System.out.println("---------------->>>>>>>>>>>>>>>>>>> depth testing");
//                            newAI.grid.playerTurn = false;
                        result = newAI.search(depth - 1, bestScore, beta, positions, cutoffs);
                        if (result.score > 9900) { // win
                            result.score--; // to slightly penalize higher depth from win
                        }
                        positions = result.positions;
                        cutoffs = result.cutoffs;
                    }

                    if (result.score > bestScore) {
                        bestScore = result.score;
                        bestMove = direction;
                    }
                    if (bestScore > beta) {
                        cutoffs++;
                        return new SearchResult(bestMove, beta, positions, cutoffs);
                    }
                }
            }
        } else { // computer's turn, we'll do heavy pruning to keep the branching factor low
            bestScore = beta;

            // try a 2 and 4 in each cell and measure how annoying it is
            // with metrics from eval
            List<Candidate> candidates = new ArrayList<>();
            List<int[]> cells = this.grid.getAvailableCells();
            int[] fill = {2, 4};
            List<Double> scores_2 = new ArrayList<>();
            List<Double> scores_4 = new ArrayList<>();
//            double[][] scores = new double[5][16];
            for (int value : fill) {
                for (int i = 0; i < cells.size(); i++) {
                    this.grid.insertTitle(cells.get(i)[0], cells.get(i)[1], value);
                    if (value == 2) {
                        scores_2.add(i, -this.grid.smoothness() + this.grid.islands());
                    }
                    if (value == 4) {
                        scores_4.add(i, -this.grid.smoothness() + this.grid.islands());
                    }
//                    System.out.println("============================= " + this.grid.islands());
//                    scores[value][i] = -this.grid.smoothness() + this.grid.islands();
                    this.grid.removeTile(cells.get(i)[0], cells.get(i)[1]);
                }
            }

            // now just pick out the most annoying moves
//            double maxScore = ArrayUtil.getMax(scores);
            double maxScore = Math.max(Collections.max(scores_2), Collections.max(scores_4));
            for (int value : fill) {
                if (value == 2) {
                    for (Double fitness : scores_2) {
                        if (fitness == maxScore) {
                            int index = scores_2.indexOf(fitness);
                            candidates.add(new Candidate(cells.get(index)[0], cells.get(index)[1], value));
                        }
                    }
                }
                if (value == 4) {
                    for (Double fitness : scores_4) {
                        if (fitness == maxScore) {
                            int index = scores_4.indexOf(fitness);
                            candidates.add(new Candidate(cells.get(index)[0], cells.get(index)[1], value));
                        }
                    }
                }
//                for (int i = 0; i < scores[value].length; i++) {
//                    if (scores[value][i] == maxScore) {
//                        candidates.add(new Candidate(cells.get(i)[0], cells.get(i)[1], value));
//                    }
//                }
            }

            // search on each candidate
            for (int i = 0; i < candidates.size(); i++) {
                int pos_x = candidates.get(i).x;
                int pos_y = candidates.get(i).y;
                int value = candidates.get(i).value;
                GameState newGrid = new GameState(this.grid.getCellMatrix());
                newGrid.insertTitle(pos_x, pos_y, value);
                positions++;
                AI newAI = new AI(newGrid);
                newAI.grid.playerTurn = true;
                result = newAI.search(depth, alpha, bestScore, positions, cutoffs);
                positions = result.positions;
                cutoffs = result.cutoffs;

                if (result.score < bestScore) {
                    bestScore = result.score;
                }
                if (bestScore < alpha) {
                    cutoffs++;
                    return new SearchResult(-1, alpha, positions, cutoffs);
                }
            }
        }

        return new SearchResult(bestMove, bestScore, positions, cutoffs);
    }

    // performs a search and returns the best move
    public int getBestMove() {
        return this.iterativeDeep(100);
    }

    // performs iterative deepening over the alpha-beta search
    private int iterativeDeep(long minSearchTime) {
        long start = new Date().getTime();
        int depth = 0;
        int best = -1;
        do {
            SearchResult newBest = this.search(depth, -10000, 10000, 0, 0);
            if (newBest.move == -1) break;
            else best = newBest.move;
            depth++;
        } while (new Date().getTime() - start < minSearchTime);
        return best;
    }

}

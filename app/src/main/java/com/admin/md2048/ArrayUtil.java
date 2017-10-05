package com.admin.md2048;

/**
 * Created by admin on 2017/10/4.
 */
public class ArrayUtil {

    /**
     * 复制矩阵内容
     *
     * @param srcMatrix
     * @param destMatrix
     * @param row
     * @param col
     */
    public static void matrixCopy(int[][] srcMatrix, int[][] destMatrix, int row, int col) {
        for (int i = 0; i < row; i++) {
            System.arraycopy(srcMatrix[i], 0, destMatrix[i], 0, col);
        }
    }

    /**
     * 判断两个数组的内容是否相等
     *
     * @param arr1
     * @param arr2
     * @return
     */
    public static boolean isEquals(int[] arr1, int[] arr2) {
        if (arr1.length != arr2.length)
            return false;
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i])
                return false;
        }
        return true;
    }

    public static void reverseArray(int[] array) {
        int newArray[] = new int[array.length];
        int k = 0;
        for (int i = array.length - 1; i >= 0; i--) {
            newArray[k++] = array[i];
        }
        System.arraycopy(newArray, 0, array, 0, newArray.length);
    }


    /**
     * 将矩阵逆时针旋转90度
     *
     * @param matrix
     * @param scale
     */
    public static void antiClockwiseRotate90(int[][] matrix, int scale) {
        int newMatrix[][] = new int[scale][scale];
        for (int p = scale - 1, i = 0; i < scale; p--, i++) {
            for (int q = 0, j = 0; j < scale; q++, j++) {
                newMatrix[p][q] = matrix[j][i];
            }
        }
        for (int i = 0; i < scale; i++) {
            System.arraycopy(newMatrix[i], 0, matrix[i], 0, scale);
        }
    }

    /**
     * 将矩阵顺时针旋转90度
     *
     * @param matrix
     * @param scale
     */
    public static void clockwiseRotate90(int[][] matrix, int scale) {
        int newMatrix[][] = new int[scale][scale];
        for (int p = 0, i = 0; i < scale; p++, i++) {
            for (int q = scale - 1, j = 0; j < scale; q--, j++) {
                newMatrix[p][q] = matrix[j][i];
            }
        }
        for (int i = 0; i < scale; i++) {
            System.arraycopy(newMatrix[i], 0, matrix[i], 0, scale);
        }
    }

    /**
     * 将矩阵逆时针旋转90度
     *
     * @param matrix
     * @param row
     * @param col
     */
    public static int[][] antiClockwiseRotate90(int[][] matrix, int row, int col) {
        int newMatrix[][] = new int[col][row];
        for (int p = col - 1, i = 0; i < col; p--, i++) {
            for (int q = 0, j = 0; j < row; q++, j++) {
                newMatrix[p][q] = matrix[j][i];
            }
        }
        return newMatrix;
    }

    /**
     * 将矩阵顺时针旋转90度
     *
     * @param matrix
     * @param row
     * @param col
     */
    public static int[][] clockwiseRotate90(int[][] matrix, int row, int col) {
        int newMatrix[][] = new int[col][row];
        for (int p = 0, i = 0; i < col; p++, i++) {
            for (int q = row - 1, j = 0; j < row; q--, j++) {
                newMatrix[p][q] = matrix[j][i];
            }
        }
        return newMatrix;
    }

}

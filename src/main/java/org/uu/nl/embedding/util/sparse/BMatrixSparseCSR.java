package org.uu.nl.embedding.util.sparse;

import java.util.Arrays;

/**
 * This sparse boolean matrix is designed for fast concurrent insertion of rows. It is written in the compressed sparse
 * row (CSR) format.
 */
public class BMatrixSparseCSR {

    private double growthFactor = 1.25;

    public static void main(String[] args) {

        //COL_INDEX = [ ]
        //ROW_INDEX = [ 0 0 0 0 0 ]

        // addRow(0, (0,1))

        //COL_INDEX = [ [0 1] ]
        //ROW_INDEX = [ 0 (0+2) (0+2) (0+2) (0+2)]

        //COL_INDEX = [ 0 1 ]
        //ROW_INDEX = [ 0 2 2 2 2 ]

        // addRow(3, (5))

        //COL_INDEX = [ 0 1 [5] ]
        //ROW_INDEX = [ 0 2 2 2 3 ]

        // addRow(2, (2,3,4))

        //COL_INDEX = [ 0 1 [2 3 4] 5 ]
        //ROW_INDEX = [ 0 0 2 (2+3) (3+3) ]

        //COL_INDEX = [ 0 1 2 3 4 5 ]
        //ROW_INDEX = [ 0 2 2 5 6 ]

        // addRow(1 (1,3))

        //COL_INDEX = [ 0 1 [1 3] 2 3 4 5 ]
        //ROW_INDEX = [ 0 2 (2+2) (5+2) (6+2) ]

        //COL_INDEX = [ 0 1 1 3 2 3 4 5 ]
        //ROW_INDEX = [ 0 2 4 7 8 ]

        BMatrixSparseCSR matrix = new BMatrixSparseCSR(4, 6);

        System.out.println(matrix);

        matrix.setRowIndexes(0, new int[]{0,1});
        matrix.setRowIndexes(3, new int[]{5});

        System.out.println(matrix);

        matrix.setRowIndexes(2, new int[]{2,3,4});
        matrix.setRowIndexes(1, new int[]{1,3});

        System.out.println(matrix);

        System.out.println("--------------");
        BMatrixSparseCSR growingMatrix = new BMatrixSparseCSR();
        growingMatrix.setGrowthFactor(2);
        System.out.println(growingMatrix);

        growingMatrix.setRowIndexes(0, new int[]{0,1});
        growingMatrix.setRowIndexes(3, new int[]{5});

        System.out.println(growingMatrix);

        growingMatrix.setRowIndexes(2, new int[]{2,3,4});
        growingMatrix.setRowIndexes(1, new int[]{1,3});

        System.out.println(growingMatrix);

        growingMatrix.appendRow(new int[]{1,3,5,6});

        System.out.println(growingMatrix);

        for(int i = 0; i < growingMatrix.nColumns; i++) {
            System.out.println(Arrays.toString(growingMatrix.getColumnIndexes(i)));
        }
    }

    private int nonZeroElements;
    private int[] rowIndexes;
    private int[] columnIndexes;
    private int nRows;
    private int nColumns;


    public BMatrixSparseCSR() {
        this(0,0);
    }

    public BMatrixSparseCSR(int nRows, int nColumns) {
        this.nRows = nRows;
        this.nColumns = nColumns;
        this.rowIndexes = new int[nRows + 1];
        this.columnIndexes = new int[nColumns];
        this.nonZeroElements = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sparse boolean matrix of size ").append(nRows).append(" x ").append(nColumns).append("\n");
        for(int i = 0; i < nRows; i++) {
            sb.append(i).append(":\t").append(Arrays.toString(getRowIndexes(i))).append("\n");
        }
        return sb.toString();
    }

    public void setRowIndexes(int row, int[] data) {

        // Insert (2,4,5) at row 7

        // Before
        //COL_INDEX = [ 0 1 2 3 4 5 ]
        //ROW_INDEX = [ 0 2 2 5 6 ]

        // After
        //COL_INDEX = [ 0 1 2 3 4 5 (2 4 5) x x x ]
        //ROW_INDEX = [ 0 2 2 5 6 [6 6 6] (6+3) x x x x x]

        // Append one or more rows at the end
        if(row >= nRows) {

            int newRows = row - nRows + 1;

            // Grow the row indices if necessary
            while(nRows + newRows + 1 >= rowIndexes.length) {
                rowIndexes = Arrays.copyOf(rowIndexes, (int) ((rowIndexes.length + 1) * growthFactor));
            }
            // Make sure it is updated correctly later on
            Arrays.fill(rowIndexes, nRows + 1, nRows + newRows + 1, nonZeroElements );
            nRows += newRows;
        }

        // Make room for new data
        while(nonZeroElements + data.length >= columnIndexes.length) {
            columnIndexes = Arrays.copyOf(columnIndexes, (int) ((columnIndexes.length + 1) * growthFactor));
        }

        // If we are inserting a row somewhere in the middle of the matrix
        int elementsToMove = nonZeroElements - rowIndexes[row];
        if(elementsToMove > 0) {
            // Move old data to end of array
            System.arraycopy(columnIndexes, rowIndexes[row + 1], columnIndexes, rowIndexes[row + 1] + data.length, elementsToMove);
        }

        // Update row index
        for(int i = row + 1; i <= nRows; i++) {
            rowIndexes[i] += data.length;
        }

        // Insert new data
        for(int i = 0; i < data.length; i++) {
            // Number of columns in variable, update if necessary
            nColumns = Math.max(nColumns, data[i] + 1);
            // Insert columns that are non-zero
            columnIndexes[rowIndexes[row] + i] = data[i];
        }

        nonZeroElements += data.length;
    }

    public void setGrowthFactor(double growthFactor){
        this.growthFactor = growthFactor;
    }

    public void appendRow(int[] data) {
        setRowIndexes(nRows, data);
    }

    public int[] getColumnIndexes(int colIndex) {

        int[] data = new int[nRows];
        int i = 0;
        for(int row = 0; row < nRows; row++) {

            int begin = rowIndexes[row];
            int end = rowIndexes[row + 1];

            for(int col = begin; col < end; col++) {
                if(columnIndexes[col] == colIndex) {
                    data[i] = row;
                    i++;
                    break;
                }
            }

        }
        return Arrays.copyOf(data, i);
    }

    public int[] getRowIndexes(int rowIndex) {

        int start = rowIndexes[rowIndex];
        int end   = rowIndexes[rowIndex + 1];

        int[] rowElements = new int[end - start];

        System.arraycopy(columnIndexes, start, rowElements, 0, rowElements.length);

        return rowElements;
    }

    public int getNonZeroElements() {
        return nonZeroElements;
    }

    public int nRows() {
        return nRows;
    }

    public int nColumns() {
        return nColumns;
    }
}

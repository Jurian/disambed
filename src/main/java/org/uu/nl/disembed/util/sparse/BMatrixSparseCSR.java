package org.uu.nl.embedding.util.sparse;

import java.util.Arrays;
import java.util.Set;

/**
 * This sparse boolean matrix is designed for fast concurrent insertion of rows. It is written in the compressed sparse
 * row (CSR) format.
 */
public class BMatrixSparseCSR {

    private double growthFactor = 1.25;

    private int nonZeroElements;
    private int[] rowIndexes;
    private int[] columnIndexes;
    private int nRows;
    private int nColumns;

    public BMatrixSparseCSR transpose() {
        BMatrixSparseCSR t = new BMatrixSparseCSR();
        t.nRows = nColumns;
        t.nColumns = nRows;
        t.nonZeroElements = nonZeroElements;
        t.columnIndexes = new int[nonZeroElements];
        t.rowIndexes = new int[nColumns + 2];

        // count per column
        for (int i = 0; i < nonZeroElements; ++i) {
            ++t.rowIndexes[columnIndexes[i] + 2];
        }

        // from count per column generate new rowPtr (but shifted)
        for (int i = 2; i < t.rowIndexes.length; ++i) {
            // create incremental sum
            t.rowIndexes[i] += t.rowIndexes[i - 1];
        }

        // perform the main part
        for (int i = 0; i < nRows; ++i) {
            for (int j = rowIndexes[i]; j < rowIndexes[i + 1]; ++j) {
                // calculate index to transposed matrix at which we should place current element, and at the same time build final rowPtr
                int new_index = t.rowIndexes[columnIndexes[j] + 1]++;
                t.columnIndexes[new_index] = i;
            }
        }

        // pop that one extra
        System.arraycopy(t.rowIndexes, 0, t.rowIndexes, 0, t.rowIndexes.length-1);

        return t;
    }

    public static void main(String[] args) {
        BMatrixSparseCSR m = new BMatrixSparseCSR();



        m.setRowIndexes(0, new int[] {0,1});
        m.setRowIndexes(2, new int[] {2,3,4});
        m.setRowIndexes(3, new int[] {5});

        //COL_INDEX = [ 0 1 2 3 4 5 x x x ]
        //ROW_INDEX = [ 0 2 2 5 6 x x x x x]


        // Insert (2,4,5) at row 7
        m.setRowIndexes(7, new int[]{2,4,5});

        // Before
        //COL_INDEX = [ 0 1 2 3 4 5 x x x ]
        //ROW_INDEX = [ 0 2 2 5 6 x x x x x]

        // After
        //COL_INDEX = [ 0 1 2 3 4 5 (2 4 5) x x x ]
        //ROW_INDEX = [ 0 2 2 5 6 [6 6 6] (6+3) x x x x x]


        // Update (1,2,3,6) at row 2
        m.setRowIndexes(2, new int[]{1,2,3,7});

        // Before
        //COL_INDEX = [ 0 1 2 3 4 5 2 4 5 x x x ]
        //ROW_INDEX = [ 0 2 2 5 6 6 6 6 9 x x x x x]

        // After
        //COL_INDEX = [ 0 1 (1 2 3 6) 5 2 4 5 x x x ]
        //ROW_INDEX = [ 0 2 2 [6 7 7 7 7] (9+1) x x x x x]


        // Update (1,6) at row 2
        m.setRowIndexes(2, new int[]{1,3});

        // Before
        //COL_INDEX = [ 0 1 1 2 3 6 5 2 4 5 x x x ]
        //ROW_INDEX = [ 0 2 2 6 7 7 7 7 10 x x x x x]

        // After
        //COL_INDEX = [ 0 1 1 3 5 2 4 5 x x x ]
        //ROW_INDEX = [ 0 2 2 [4 5 5 5 5] (10-2) x x x x x]



        System.out.println(m);

    }

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

        final int insertSize = data.length;

        // Make room for new data
        while(nonZeroElements + insertSize >= columnIndexes.length) {
            columnIndexes = Arrays.copyOf(columnIndexes, (int) ((columnIndexes.length + 1) * growthFactor));
        }

        final int rowSize = rowIndexes[row + 1] - rowIndexes[row];
        final int shift = insertSize - rowSize;
        boolean recalculateNcol = false;

        if(shift < 0) { // We are removing columns in this row, nColumns may be affected

            int maxColInsert = 0;
            for(int i : data) {
                maxColInsert = Math.max(maxColInsert, i + 1);
            }

            if(maxColInsert < nColumns) {
                // Number of columns is more than the maximum column of the inserted row
                // getColumnIndexes is kind of expensive, prefer not to do this too often
                int[] maxCol = getColumnIndexes(nColumns-1);

                // If the rightmost column only has one nonzero value, and it is this row
                if(maxCol.length == 1 && maxCol[0] == row) {
                    // We will have to recalculate the number of columns, as we may have removed one or more
                    // Empty columns are allowed, but not at the rightmost end of the matrix
                    recalculateNcol = true;
                }
            }
        }

        if(shift != 0) {
            final int length = nonZeroElements - rowIndexes[row];
            System.arraycopy(columnIndexes, rowIndexes[row + 1], columnIndexes, rowIndexes[row + 1] + shift, length);

            // Update row index
            for(int i = row + 1; i <= nRows; i++) {
                rowIndexes[i] += shift;
            }
        }

        // Insert new data
        for(int i = 0; i < insertSize; i++) {
            if(shift > 0) {
                // We may have added columns
                nColumns = Math.max(nColumns, data[i] + 1);
            }
            // Insert columns that are non-zero
            columnIndexes[rowIndexes[row] + i] = data[i];
        }

        nonZeroElements += shift;

        if(recalculateNcol) {
            // We have reduced the number of columns
            // This is really expensive, so only do it when absolutely necessary

            int c = nColumns - 1;

            while(getColumnIndexes(c).length == 0 && c >= 0) {
                c--;
            }

            nColumns = c+1;
        }
    }

    public void setGrowthFactor(double growthFactor){
        this.growthFactor = growthFactor;
    }

    public void appendRow(int[] data) {
        setRowIndexes(nRows, data);
    }

    public void fillColumnIndexes(int colIndex, Set<Integer> candidates, int[] mapToGraph) {

        for(int row = 0; row < nRows; row++) {

            int begin = rowIndexes[row];
            int end = rowIndexes[row + 1];

            for(int col = begin; col < end; col++) {
                if(columnIndexes[col] == colIndex) {
                    candidates.add(mapToGraph[row]);
                    break;
                }
            }

        }
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

    public boolean[] getColumn(int colIndex) {
        boolean[] col = new boolean[nRows];
        for(int i : getColumnIndexes(colIndex)){
            col[i] = true;
        }
        return col;
    }

    public int[] getRowIndexes(int rowIndex) {

        int start = rowIndexes[rowIndex];
        int end   = rowIndexes[rowIndex + 1];

        int[] rowElements = new int[end - start];

        System.arraycopy(columnIndexes, start, rowElements, 0, rowElements.length);

        return rowElements;
    }

    public boolean[] getRow(int rowIndex) {
        boolean[] row = new boolean[nColumns];
        for(int i : getRowIndexes(rowIndex)){
            row[i] = true;
        }
        return row;
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

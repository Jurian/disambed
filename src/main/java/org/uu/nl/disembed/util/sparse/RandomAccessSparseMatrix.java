package org.uu.nl.embedding.util.sparse;

import com.carrotsearch.hppc.FloatArrayList;
import com.carrotsearch.hppc.IntArrayList;

/**
 * This format is good for incrementally constructing a sparse matrix in random order,
 * but poor for iterating over non-zero values in lexicographical order.
 * This class is written for easy filling and efficient random access of elements.
 */
public class RandomAccessSparseMatrix {

    private final IntArrayList rowIndex;
    private final IntArrayList columnIndex;
    private final FloatArrayList values;

    private final int rows, columns;

    private int nonZero = 0;

    public RandomAccessSparseMatrix(int nRows, int nColumns, int nonZero) {
        this.rowIndex = new IntArrayList(nonZero);
        this.columnIndex = new IntArrayList(nonZero);
        this.values = new FloatArrayList(nonZero);
        this.rows = nRows;
        this.columns = nColumns;
    }

    public void add(int row, int column, float value){
        rowIndex.add(row);
        columnIndex.add(column);
        values.add(value);
        nonZero++;
    }

    public int count32BitNumbers() {
        // We assume Float as T
        return getNonZero() * 3;
    }

    public int getNonZero() {
        return this.nonZero;
    }

    public int columns() {
        return this.columns;
    }

    public int rows() {
        return this.rows;
    }

    public int size() {
        return values.size();
    }

    public float getValue(int i) {
        return values.get(i);
    }

    public int getRow(int i) {
        return rowIndex.get(i);
    }

    public int getColumn(int i) {
        return columnIndex.get(i);
    }

}

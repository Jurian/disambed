package org.uu.nl.embedding.util.sparse;

import java.util.ArrayList;

/**
 * This format is good for incrementally constructing a sparse matrix in random order,
 * but poor for iterating over non-zero values in lexicographical order.
 * This class is written for easy filling and efficient random access of elements.
 * @param <T> Type of elements in matrix, e.g. Float or Integer
 */
public class RandomAccessSparseMatrix<T> {

    private final ArrayList<Integer> rowIndex;
    private final ArrayList<Integer> columnIndex;
    private final ArrayList<T> values;

    private final int rows, columns;

    private int nonZero = 0;

    public RandomAccessSparseMatrix(int nRows, int nColumns, int nonZero) {
        this.rowIndex = new ArrayList<>(nonZero);
        this.columnIndex = new ArrayList<>(nonZero);
        this.values = new ArrayList<>(nonZero);
        this.rows = nRows;
        this.columns = nColumns;
    }

    public void add(int row, int column, T value){
        rowIndex.add(row);
        columnIndex.add(column);
        values.add(value);
        nonZero++;
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

    public T getValue(int i) {
        return values.get(i);
    }

    public int getRow(int i) {
        return rowIndex.get(i);
    }

    public int getColumn(int i) {
        return columnIndex.get(i);
    }

}

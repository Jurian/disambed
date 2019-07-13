package org.uu.nl.embedding.util.compare;

public class NearestNeighbors<T> {

    private final T key;
    private final Distance<T> metric;
    private final int size;
    private final int[] indexes;
    private final double[] distances;

    public NearestNeighbors(T key, Distance<T> metric, int size) {
        this.key = key;
        this.metric = metric;
        this.size = size;
        this.indexes = new int[size];
        this.distances = new double[size];
    }

    public void add(T newNeighbor, int newIndex) {
        assert newIndex > 0: "Index must be non-zero positive integer";

        double newDistance = metric.distance(key, newNeighbor);
        double shiftDistance = -1;
        int shiftIndex = -1;
        boolean shift = false;

        for(int i = 0; i < size; i++) {

            // If there is an empty spot, just fill it in and stop
            if(indexes[i] == 0) {
                distances[i] = shift ? shiftDistance : newDistance;
                indexes[i] = shift ? shiftIndex : newIndex;
                break;
            }

            if(shift) {

                // Shift all values up one spot

                double td = distances[i];
                int ti = indexes[i];

                distances[i] = shiftDistance;
                indexes[i] = shiftIndex;

                shiftDistance = td;
                shiftIndex = ti;

            } else if(newDistance < distances[i]) {

                // Replace this value and shift all next values

                shift = true;

                shiftDistance = distances[i];
                shiftIndex = indexes[i];

                distances[i] = newDistance;
                indexes[i] = newIndex;
            }
        }
    }
}

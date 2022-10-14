package org.uu.nl.embedding.cluster;

import com.carrotsearch.hppc.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import org.apache.commons.math.util.FastMath;

import java.util.ArrayList;
import java.util.List;

public class Util {

    private static float sumOfProducts(float[] a, float[] b) {
        float sum = 0;
        for(int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public static float cosineSimilarity(float[] a, float[] b) {
        return (float) (sumOfProducts(a, b) / (FastMath.sqrt(sumOfProducts(a,a)) * FastMath.sqrt(sumOfProducts(b,b))));
    }

    public static float weight(int i, int j, float[][] vectors, float theta, float epsilon, float penalty) {
        float sim = cosineSimilarity(vectors[i], vectors[j]) - theta - penalty;
        return (sim == 0) ? epsilon : sim;
    }

    public static int combinationToIndex(int a, int b, int n) {
        a++;b++; // This only works with 1-indexing
        if(a > b) return ((b - 1) * n - b * (b + 1) / 2 + a) - 1; // Subtract 1 to revert to 0-indexing
        else return ((a - 1) * n - a * (a + 1) / 2 + b) - 1; // Subtract 1 to revert to 0-indexing
    }

    public static List<int[]> choose(int n, int r) {

        final List<int[]> combinations = new ArrayList<>();
        int[] combination = new int[r];

        // Initialize with the lowest lexicographic combination
        for (int i = 0; i < r; i++) {
            combination[i] = i;
        }

        while (combination[r - 1] < n) {
            combinations.add(combination.clone());

            // Generate next combination in lexicographic order
            int t = r - 1;
            while (t != 0 && combination[t] == n - r + t) {
                t--;
            }
            combination[t]++;
            for (int i = t + 1; i < r; i++) {
                combination[i] = combination[i - 1] + 1;
            }
        }

        return combinations;
    }

    public static int[][] possiblePairs(int[] input) {

        final int k = 2;
        final int n = input.length;

        int[][] subsets = new int[(n * (n-1))/2][2];

        int[] s = new int[k];
        // here we'll keep indices
        // pointing to elements in input array

        if (k <= input.length) {
            // first index sequence: 0, 1, 2, ...
            //noinspection StatementWithEmptyBody
            for (int i = 0; (s[i] = i) < k - 1; i++);

            subsets[0] = getSubset(input, s);

            for(int j = 1;;j++) {
                int i;
                // find position of item that can be incremented
                //noinspection StatementWithEmptyBody
                for (i = k - 1; i >= 0 && s[i] == input.length - k + i; i--);
                if (i < 0) {
                    break;
                }
                s[i]++;                    // increment this item
                for (++i; i < k; i++) {    // fill up remaining items
                    s[i] = s[i - 1] + 1;
                }
                subsets[j] = getSubset(input, s);
            }
        }

        return subsets;
    }

    private static int[] getSubset(int[] input, int[] subset) {
        int[] result = new int[subset.length];
        for (int i = 0; i < subset.length; i++)
            result[i] = input[subset[i]];
        return result;
    }

    private static IntArrayList[] adjacencyList(int n, int[][] index) {

        int nEdges = index.length;
        boolean[] processed = new boolean[n];
        IntArrayList[] adjList = new IntArrayList[n];

        for (int[] pair : index) {

            int u = pair[0];
            int v = pair[1];

            if (processed[u]) {
                adjList[u].add(v);
            } else {
                IntArrayList adj = new IntArrayList();
                adj.add(v);
                adjList[u] = adj;

                processed[u] = true;
            }

            if (processed[v]) {
                adjList[v].add(u);
            } else {
                IntArrayList adj = new IntArrayList();
                adj.add(u);
                adjList[v] = adj;

                processed[v] = true;
            }
        }
        return adjList;
    }

    private static IntArrayList bfs(int v, IntArrayList[] adjlist, boolean[] visited) {

        IntArrayFIFOQueue q = new IntArrayFIFOQueue();
        IntArrayList component = new IntArrayList();
        q.enqueue(v);
        visited[v] = true;

        while (!q.isEmpty()) {
            int u = q.dequeueInt();
            component.add(u);
            if(adjlist[u] == null) {
                adjlist[u] = new IntArrayList();
            }
            IntArrayList adjV = adjlist[u];
            int n = adjV.size();

            for (int i = 0; i < n; i++) {
                int nextVertex = adjV.get(i);
                if (!visited[nextVertex]) {
                    q.enqueue(nextVertex);
                    visited[nextVertex] = true;
                }
            }

        }
        return component;
    }

    public static int[][] connectedComponents(int n, int[][] index) {

        final IntArrayList[] adjList = adjacencyList(n, index);

        boolean[] visited = new boolean[n];
        List<int[]> components = new ArrayList<>();

        for(int i = 0; i < n; i++) {
            if (!visited[i]) {
                int[] component = bfs(i, adjList, visited).toArray();
                // Remove singleton components
                if(component.length > 1) components.add(component);
            }
        }

        return components.toArray(int[][]::new);
    }

    public static float[] componentWeights(int[] component, float[][] vectors, float theta, float epsilon, float[] penalties) {

            final int n = component.length;

            float[] weights = new float[((n-1) * n)/2];
            for(int i = 0, k = 0; i < n ; i++) {
                for(int j = i + 1; j < n; j++) {
                    weights[k] = weight(component[i], component[j], vectors, theta, epsilon, penalties[k]);
                    k++;
                }
            }
            return weights;
    }

    public static int[][] triangles(int n) {

        final List<int[]> combinations = choose(n, 3);
        final int nTriangles = combinations.size();

        // Output matrix
        final int[][] triangles = new int[nTriangles][3];

        for (int i = 0; i < nTriangles; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = j + 1; k < 3; k++) {

                    // Take an edge from the triangle
                    int a = combinations.get(i)[j];
                    int b = combinations.get(i)[k];

                    triangles[i][j + k - 1] = combinationToIndex(a, b, n);
                }
            }
        }
        return triangles;
    }
}

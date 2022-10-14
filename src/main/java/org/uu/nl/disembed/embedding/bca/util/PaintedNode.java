package org.uu.nl.disembed.embedding.bca.util;

/**
 * Used for storing information about how we got to a focus node
 *
 * @author Jurian Baas
 */
public class PaintedNode implements Comparable<PaintedNode> {

    public static final int SKIP = -1;

    /**
     * The ID of this node
     */
    public final int nodeID;

    private float paint;

    public PaintedNode(int nodeID, float startPaint) {
        this.nodeID = nodeID;
        this.paint = startPaint;
    }

    public void addPaint(float paint) {
        this.paint += paint;
    }

    public float getPaint() {
        return this.paint;
    }

    @Override
    public int compareTo(PaintedNode o) {
        return Float.compare(this.paint, o.paint);
    }

    @Override
    public int hashCode() {
        return this.nodeID;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PaintedNode)
            return this.nodeID == ((PaintedNode) obj).nodeID;
        return false;
    }

}

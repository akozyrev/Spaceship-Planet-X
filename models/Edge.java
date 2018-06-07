package models;

import java.util.Objects;

import util.Util;

/** Edges are weighted undirected connections between two Nodes. The weight
 * of an edge is the rounded distance between both Nodes but always > 0.
 * 
 * Edge implements BoardElement, indicating that it is a component of
 * Board and has some user-facing representation.
 * 
 * An instance represents a graph edge between two vertices. */
public class Edge implements BoardElement {

    /* The two Nodes connected by this Edge, in no particular order. */
    private Node[] exits= new Node[2];

    /* The length of this Edge, equal to the distance between its exits. */
    public final int length;
    
    private int visits; // The number of times this Edge has been visited.

    /** Constructor: An edge with end nodes n1 and n2.
     * Precondition: n1 and n2 are non-null, non-equal Nodes. length > 0. */
    Edge(Node n1, Node n2) {
        if (n1 == null)
            throw new IllegalArgumentException("null Node n1");
        if (n2 == null)
            throw new IllegalArgumentException("null Node n2");
        if (n1.equals(n2))
            throw new IllegalArgumentException("equal Nodes n1 and n2");

        exits[0]= n1;
        exits[1]= n2;
        
        double d= Util.distance(n1.x(), n1.y(), n2.x(), n2.y()) + 0.5;
        length= d <= 1 ? 1 : (int) d;
        visits= 0;
    }

    /** Return the first exit of this Edge.
     * The order of exits is arbitrary. */
    public Node firstExit() {
        return exits[0];
    }

    /** Return the second exit of this Edge.
     * The order of exits is arbitrary. */
    public Node secondExit() {
        return exits[1];
    }

    /** Return true iff node is one of the exits of this Edge. */
    public boolean isExit(Node node) {
        return exits[0].equals(node) || exits[1].equals(node);
    }

    /** Return the shared exit of this and e, or null if they don't share
     * an exit. */
    public Node sharedExit(Edge e) {
        if (exits[0].equals(e.exits[0]) || exits[0].equals(e.exits[1]))
            return exits[0];
        if (exits[1].equals(e.exits[0]) || exits[1].equals(e.exits[1]))
            return exits[1];
        return null;
    }

    /** Return true iff this Edge and e share an exit. */
    public boolean sharesExit(Edge e) {
        return sharedExit(e) != null;
    }

    /** Return the other exit that is not equal to n.
     * Precondition: n is an exit of this Edge. */
    public Node getOther(Node n) {
        if (exits[0].equals(n))
            return exits[1];
        if (exits[1].equals(n))
            return exits[0];

        throw new IllegalArgumentException("This edge does not have Node n");
    }
    
    /** Return the amount of fuel needed to travel on this Edge. */
    public int fuelNeeded() {
    	return length;
    }

    /** Return true iff: this edge and ob are the same object, or
     * if this edge and ob connect the same two Nodes. */
    @Override
    public boolean equals(Object ob) {
        if (ob == this) return true;
        if (ob == null || getClass() != ob.getClass())  return false;
        Edge e= (Edge) ob;
        return (exits[0].equals(e.exits[1]) && exits[1].equals(e.exits[0]))
            || (exits[0].equals(e.exits[0]) && exits[1].equals(e.exits[1]));
    }

    @Override
    public int hashCode() {
        return Objects.hash(exits[0], exits[1]);
    }

    /**
     * Return the names of the Nodes connected to this edge, delimited by the
     * String "---".
     */
    @Override
    public String toString() {
        return exits[0].name() + "---" + exits[1].name();
    }

    /** Return a String to print when this object is drawn on a GUI. */
    public String name() {
        return String.valueOf(length);
    }

    /** Return the x location of the center of this Edge. */
    public int x() {
        int x1= exits[0].x();
        int x2= exits[1].x();
        return (int) (((x1 + x2) / 2.0) + 0.5);
    }

    /** Return the y location of the center of this Edge. */
    public int y() {
        int y1= exits[0].y();
        int y2= exits[1].y();
        return (int) (((y1 + y2) / 2.0) + 0.5);
    }
    
    /** Increase the amount of times this Edge has been visited by 1. */
    void visit() {
        ++visits;
    }
    
    /** Return the number of times this Edge has been visited. */
    public int getVisits() {
        return visits;
    }
}

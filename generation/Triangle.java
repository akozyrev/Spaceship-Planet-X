package generation;

import java.util.Queue;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Set;

/** Helper class used for Delaunay Triangulation. Uses Barycentric coordinates
 * to determine if a point is inside a given triangle and then tracks any
 * sub-triangles created inside, as well as Triangles sharing an edge with
 * this Triangle.
 * 
 * An instance maintains the three vertices of a triangle and recursively
 * tracks one layer of sub-triangles within it.
 */
public class Triangle {

    /* The three vertices defining this Triangle, in no particular order. */
    private Point[] vs= new Point[3];

    /* The three SharedEdges of this Triangle, in no particular order. Any
     * element can be null if the edge is unshared. */
    private SharedEdge[] es= new SharedEdge[3];

    /* The HistoryEvent resulting in the next step of this triangulation.
     * Null if no events have occurred for this Triangle. */
    private HistoryEvent event;

    /* Determinant from Barycentric coordinate conversion used to find if a
     * point is inside of this Triangle */
    private double detT;

    /** Constructor: a Triangle with three vertices p1, p2, and p3, no
     * sub-Triangles, and no shared-edge Triangles. */
    public Triangle(Point p1, Point p2, Point p3) {
        vs[0]= p1;
        vs[1]= p2;
        vs[2]= p3;
        detT= (vs[1].y - vs[2].y) * (vs[0].x - vs[2].x)
                + (vs[2].x - vs[1].x) * (vs[0].y - vs[2].y);
    }

    /** If this Triangle contains p, adds p to this Triangle, splitting the
     * appropriate innermost Triangle, adjusting the triangulation set ts as
     * needed, and maintaining the Delaunay condition, then returns true.
     * Otherwise, returns false.
     * 
     * N.B. This method allows Points to be put on edges, as well as 4 points to
     * be on an innermost circumcircle; if either of these occur, the resulting
     * Delaunay Triangulation will not be unique.
     * 
     * Precondition: p's coordinates are unique. outer is the outermost non-null
     * Triangle of a triangulation. */
    public boolean addPoint(Point p, Set<Triangle> ts) {
        PointLocation pLoc= getInnermost(p);
        switch (pLoc.getLocation()) {
            case OUT:
            case VERTEX: return false;
            case EDGE:
                pLoc.getTriangle().addOnEdge(p, pLoc.getPoint(), ts);
                return true;
            default: break;
        }
        Triangle t= pLoc.getTriangle();

        // add the newly-created sub-Triangles. These will have the order
        // sub[0]: v0, v1, p
        // sub[1]: v1, v2, p
        // sub[2]: v2, v0, p
        Triangle[] sub= new Triangle[3];
        for (int i= 0; i != 3; ++i) {
            sub[i]= new Triangle(t.vs[i], t.vs[(i + 1) % 3], p);
            ts.add(sub[i]);
        }

        // create the HistoryEvent for this addition
        t.event= new HistoryEvent(sub);

        // the queue that maintains possible Quads to flip
        Queue<Quad> flipQueue= new LinkedList<Quad>();

        // from the sub-Triangle order, the guaranteed SharedEdges are:
        // sub[0], sub[1] share edge v1, p
        // sub[1], sub[2] share edge v2, p
        // sub[2], sub[0] share edge v0, p

        // the remaining three SharedEdges are as follows (from the original t)
        // sub[0].es[2] is edge v0, v1
        // sub[1].es[0] is edge v1, v2
        // sub[2].es[1] is edge v2, v0
        // however (from the invariant) we have no easy way to reference these
        for (int i= 0; i != 3; ++i) {
            int i1= (i + 1) % 3;
            int i2= (i + 2) % 3;
            sub[i].es[i1]= new SharedEdge(t.vs[i1], p, sub[i], sub[i1]);
            sub[i1].es[i1]= sub[i].es[i1];
            for (SharedEdge e : t.es) {
                if (e != null && !e.contains(t.vs[i2])) {
                    e.update(t, sub[i]);
                    sub[i].es[i2]= e;
                    flipQueue.add(new Quad(e, p));
                }
            }
        }

        // flip bad edges until the condition is universally true
        while (!flipQueue.isEmpty()) {
            Quad q= flipQueue.remove();
            q.flipBadSharedEdge(flipQueue, ts);
        }

        ts.remove(t);
        return true;
    }

    /** Add p to an edge of this Triangle, splitting both this Triangle and its
     * particular neighbor if necessary, adjusting the triangulation set ts as
     * needed, and maintaining the Delaunay condition.
     * 
     * N.B. The resulting triangulation will not be unique.
     * 
     * Precondition: this Triangle is an innermost Triangle. p lies on an edge
     * of this Triangle. */
    private void addOnEdge(Point p, Point offEdge, Set<Triangle> ts) {
        // decide if we need a 4-way split or just a 2-way split
        SharedEdge edge= null;
        for (SharedEdge e : es) {
            if (e != null && !e.contains(offEdge))
                edge= e;
        }

        // need to add either 2 or 4 Triangles
        if (edge != null) {
            SharedEdge[] line= new SharedEdge[2];
            Triangle t1= edge.t1();
            Triangle t2= edge.t2();
            Point offEdge1= edge.unsharedVertex(t1);
            Point offEdge2= edge.unsharedVertex(t2);
            line= t1.splitIntoTwo(p, offEdge1, ts, line);
            t2.splitIntoTwo(p, offEdge2, ts, line);
        } else {
            splitIntoTwo(p, offEdge, ts, null);
        }
    }

    /** Add p to an edge of this Triangle, splitting this Triangle, adjusting
     * the triangulation set ts as needed, and maintaining the Delaunay
     * condition.
     * 
     * If line is null, don't try to add any SharedEdges on the split edge.
     * If line[0] is a null SharedEdge, create two new SharedEdges.
     * Otherwise, line contains two SharedEdges; update them. Return line.
     * 
     * Precondition: this Triangle is an innermost Triangle. p lies on
     * an edge of this Triangle. line is null or line.length = 2. */
    private SharedEdge[] splitIntoTwo(Point p, Point offEdge, Set<Triangle> ts,
            SharedEdge[] line) {
        Point[] a= selectPoint(offEdge);
        // sub[i]: on-edge, p, offEdge
        Triangle[] sub= new Triangle[2];
        for (int i= 0; i != 2; ++i) {
            sub[i]= new Triangle(a[i + 1], p, offEdge);
            ts.add(sub[i]);
        }

        event= new HistoryEvent(sub);

        Queue<Quad> flipQueue= new LinkedList<Quad>();
        // SharedEdges per sub[i]: a[i + 1], p as well as offEdge, p
        sub[0].es[0]= new SharedEdge(offEdge, p, sub[0], sub[1]);
        sub[1].es[0]= sub[0].es[0];
        for (SharedEdge e : es) {
            if (e != null) {
                if (!e.contains(offEdge)) {
                    line= setColinearSharedEdges(line, sub);
                } else if (e.contains(a[1])) {
                    e.update(this, sub[0]);
                    sub[0].es[1]= e;
                    flipQueue.add(new Quad(e, p));
                } else { // implies: if (e.contains(a[2])) {
                    e.update(this, sub[1]);
                    sub[1].es[1]= e;
                    flipQueue.add(new Quad(e, p));
                }
            }
        }

        // flip bad edges until the condition is universally true
        while (!flipQueue.isEmpty()) {
            Quad q= flipQueue.remove();
            q.flipBadSharedEdge(flipQueue, ts);
        }

        ts.remove(this);
        return line;
    }

    /** If line is null, return. If line[0] is a null SharedEdge, create
     * two new SharedEdges containing sub and null. Otherwise, line
     * contains two SharedEdges; update them using sub. Return line.
     * 
     * Precondition: line is null or line.length = 2. sub.length = 2. The first
     * vertex of each sub Triangle MUST be an endpoint of the colinear edge. The
     * second vertex of each subTriangle MUST be the Point on the edge.  */
    private static SharedEdge[] setColinearSharedEdges(SharedEdge[] line,
            Triangle[] sub) {
        if (line == null) { // outer edge case
            return null;
        } else if (line[0] == null) { // inner edge, first Triangle
            for (int i= 0; i != 2; ++i) {
                sub[i].es[2]= new SharedEdge(sub[i].vs[0], sub[i].vs[1], null,
                        sub[i]); // use null as a temporary dummy
                line[i]= sub[i].es[2];
            }
        } else { // inner edge, second Triangle
            for (SharedEdge e : line) {
                if (e.contains(sub[0].vs[0])) {
                    sub[0].es[2]= e;
                    e.update(null, sub[0]); // replace dummy null
                } else {
                    sub[1].es[2]= e;
                    e.update(null, sub[1]); // replace dummy null
                }
            }
        }
        return line;
    }

    /** Return the angle (in radians) between edges pointing out of vertex p.
     * Precondition: p is a vertex of this Triangle. */
    public double angle(Point p) {
        // a[0] is the vertex p; a[1] and a[2] are the other two points
        Point[] a= selectPoint(p);

        // vector u is (1 - 0), v is (2 - 0).
        int ux= a[1].x - a[0].x;
        int uy= a[1].y - a[0].y;
        int vx= a[2].x - a[0].x;
        int vy= a[2].y - a[0].y;

        // find the dot product of u and v
        double dot= ux * vx + uy * vy;

        // find the product of magnitudes || u || * || v ||
        double mag= Math.sqrt(ux * ux + uy * uy) * Math.sqrt(vx * vx + vy * vy);

        // cos t= u dot v / (|| u || || v ||)
        return Math.acos(dot / mag);
    }

    /** Return a PointLocation for p relative to this Triangle. */
    public PointLocation pointLocation(Point p) {
        double l1= ((vs[1].y - vs[2].y) * (p.x - vs[2].x)
                + (vs[2].x - vs[1].x) * (p.y - vs[2].y)) / detT;
        double l2= ((vs[2].y - vs[0].y) * (p.x - vs[2].x)
                + (vs[0].x - vs[2].x) * (p.y - vs[2].y)) / detT;
        double l3= 1 - l1 - l2;

        if (l1 < 0 || l2 < 0 || l3 < 0) return PointLocation.makeOut();
        if ((l1 == 0 && l2 == 0) || (l2 == 0 && l3 == 0)
                || (l3 == 0 && l1 == 0))
            return PointLocation.makeVertex(this, p);
        if (l1 == 0) return PointLocation.makeEdge(this, vs[0]);
        if (l2 == 0) return PointLocation.makeEdge(this, vs[1]);
        if (l3 == 0) return PointLocation.makeEdge(this, vs[2]);
        return PointLocation.makeIn(this);
    }

    /** Return a PointLocation for the location of p within this Triangle.
     * The PointLocation will be on the innermost Triangle possible. */
    private PointLocation getInnermost(Point p) {
        PointLocation pLoc= pointLocation(p);
        switch (pLoc.getLocation()) {
            case EDGE:
            case IN:
                HistoryEvent he= pLoc.getTriangle().event;
                if (he == null) return pLoc;
                pLoc= he.select(p);
                if (pLoc.isNotOut())
                    return pLoc.getTriangle().getInnermost(p);
                else
                    return pLoc;
            default:
                return pLoc;
        }
    }

    /** Return a 3-element Point array a where p is a[0] and the
     * remaining vertices of this Triangle are a[1] and a[2].
     * Precondition: p is a vertex of this Triangle. */
    private Point[] selectPoint(Point p) {
        int i= 0;
        while (i < 2 && p != vs[i])
            ++i;
        return new Point[] { vs[i], vs[(i + 1) % 3], vs[(i + 2) % 3] };
    }

    /** Add this Triangle's UEdges to the given Set, only.
     * Precondition: edges is a mutable set. */
    public void addUEdgesToSet(Set<UEdge> edges) {
        for (int i= 0; i != 3; ++i)
            edges.add(new UEdge(vs[i], vs[(i + 1) % 3]));
    }

    /** Return this Triangle's three vertices, delimited by hyphens. */
    @Override public String toString() {
        return vs[0] + "-" + vs[1] + "-" + vs[2];
    }

    /** An instance represents a pair of Triangles with a possibly bad edge,
     * i.e. an edge that may require a flip. */
    private static class Quad {

        /* The (possibly) bad edge of this Quad */
        private SharedEdge edge;

        /* The Triangle which contains the newly added Point */
        private Triangle inner;

        /* The Triangle which does NOT contain the newly added Point */
        private Triangle outer;

        /* The Point just added to the Triangulation. New edges containing this
         * Point cannot be bad. This Point is the unshared vertex of inner. */
        private Point add;

        /* The unshared vertex of outer, which is also the farthest from add. */
        private Point far;

        /*** Constructor: a bad edge and an added Point. */
        public Quad(SharedEdge edge, Point add) {
            this.add= add;
            this.edge= edge;
            if (edge.unsharedVertex(edge.t1()).equals(add)) {
                inner= edge.t1();
                outer= edge.t2();
            } else {
                inner= edge.t2();
                outer= edge.t1();
            }
            far= edge.unsharedVertex(outer);
        }

        /** Return true iff this Quad satisfies the Delaunay condition. */
        private boolean isDelaunay() {
            return inner.angle(add) + outer.angle(far) <= Math.PI;
        }

        /*** If needed, flips this bad edge, creating two new Triangles and adding new
         * possibly bad edges to the flip queue. Otherwise, does nothing. */
        public void flipBadSharedEdge(Queue<Quad> queue, Set<Triangle> ts) {
            if (isDelaunay()) return;

            // switch the shared and unshared Points
            Triangle flip1= new Triangle(add, far, edge.p1());
            Triangle flip2= new Triangle(add, far, edge.p2());

            ts.remove(inner);
            ts.remove(outer);
            ts.add(flip1);
            ts.add(flip2);

            // make an event for this flip
            inner.event= new HistoryEvent(new Triangle[] { flip1, flip2 });
            outer.event= inner.event;

            // add the newly flipped edge
            flip1.es[0]= new SharedEdge(add, far, flip1, flip2);
            flip2.es[0]= flip1.es[0];

            // update the old inner edges to the new flipped Triangles
            for (SharedEdge e : inner.es) {
                if (e != null && e != edge) {
                    if (e.contains(edge.p1())) {
                        e.update(inner, flip1);
                        flip1.es[1]= e;
                    } else {
                        e.update(inner, flip2);
                        flip2.es[1]= e;
                    }
                }
            }

            // update the old outer edges to the new flipped Triangles
            for (SharedEdge e : outer.es) {
                if (e != null && e != edge) {
                    if (e.contains(edge.p1())) {
                        e.update(outer, flip1);
                        flip1.es[2]= e;
                        queue.add(new Quad(e, add));
                    } else {
                        e.update(outer, flip2);
                        flip2.es[2]= e;
                        queue.add(new Quad(e, add));
                    }
                }
            }
        }
    }

    /** An instance represents an edge shared between two Triangles.
     * The order of the two connected Points and Triangles is arbitrary. */
    private static class SharedEdge {
        /* The two endpoints of this segment */
        private Point p1, p2;

        /* The two Triangles sharing this edge. Neither Triangle should be
         * null. */
        private Triangle t1, t2;

        /** Constructor: the Points bounding this edge, and two adjacent
         * Triangles.*/
        public SharedEdge(Point p1, Point p2, Triangle t1, Triangle t2) {
            if (p1.equals(p2))
                throw new IllegalArgumentException("equal Points");
            else if (t1 == t2)
                throw new IllegalArgumentException("same Triangles");
            this.p1= p1;
            this.p2= p2;
            this.t1= t1;
            this.t2= t2;
        }

        /** Return the first endpoint of this edge. */
        public Point p1() {
            return p1;
        }

        /** Return the second endpoint of this edge. */
        public Point p2() {
            return p2;
        }

        /** Return the first Triangle sharing this edge. */
        public Triangle t1() {
            return t1;
        }

        /** Return the second Triangle sharing this edge. */
        public Triangle t2() {
            return t2;
        }

        /** Returns true iff this contains p. */
        public boolean contains(Point p) {
            return p.equals(p1) || p.equals(p2);
        }

        /** Change oldT to newT in this edge.
         * Precondition: oldT has this edge. */
        public void update(Triangle oldT, Triangle newT) {
            if (t1 == oldT) {
                t1= newT;
                return;
            } else if (t2 == oldT) {
                t2= newT;
                return;
            }
            throw new IllegalArgumentException("oldT doesn't have this edge");
        }

        /** Returns the Point in Triangle t that doesn't lie on this edge.
         * Precondition: t has this edge. */
        public Point unsharedVertex(Triangle t) {
            if (t != t1 && t != t2)
                throw new IllegalArgumentException("t doesn't have this edge");
            for (Point p : t.vs)
                if (!contains(p)) return p;
            throw new IllegalStateException("No unshared vertices");
        }
    }
}

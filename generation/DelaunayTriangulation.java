package generation;

import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/** An instance creates a Delaunay triangulation represented by
 * immutable vertex and edge sets. */
public class DelaunayTriangulation {
	/* The set of Points representing vertices in this triangulation */
	private Set<Point> vertices;

	/* The set of UEdges representing the final triangulation */
	private Set<UEdge> edges;

	/** Constructor: a Delaunay Triangulation with n Points, placed using RNG r,
	 * where the Points are bound by a rectangle with lower-left point (0, 0)
	 * parallel to the x- and y-axes with dimensions w x h. */
	public DelaunayTriangulation(int n, Random r, int w, int h) {
		Iterator<Point> iter= new Iterator<Point>() {
			@Override public boolean hasNext() {
				return vertices.size() < n;
			}

			@Override public Point next() {
				return new Point(r.nextInt(w + 1), r.nextInt(h + 1));
			}
		};
		triangulate(iter, new Point(0, 0), new Point(w, h));
	}

	/** Constructor: a Delaunay triangulation built from the Points
	 * in pts. */
	public DelaunayTriangulation(Collection<Point> pts) {
		// The max Y, max X, min Y, min X values
		int t= Integer.MIN_VALUE, r = Integer.MIN_VALUE;
		int b= Integer.MAX_VALUE, l = Integer.MAX_VALUE;
		for (Point p : pts) {
			if (p.x > r) r= p.x;
			if (p.x < l) l= p.x;
			if (p.y > t) t= p.y;
			if (p.y < b) b= p.y;
		}

		triangulate(pts.iterator(), new Point(l, b), new Point(r, t));
	}

	/** Constructor: a Delaunay triangulation built from Points in iterator pts
	 * bound in the axis-aligned rectangle with bottom-left coordinate bl and
	 * top-right coordinate tr. Points outside of this area are omitted. */
	public DelaunayTriangulation(Iterator<Point> pts, Point bl, Point tr) {
		triangulate(pts, bl, tr);
	}

	/** Use iterator pts to make a Delaunay triangulation within the
	 * axis-aligned rectangle with bottom-left coordinate bl and top-right
	 * coordinate tr. Points outside this area are omitted. */
	private void triangulate(Iterator<Point> pts, Point bl, Point tr) {
		vertices= new HashSet<Point>();

		/* Triangulation set: set of all final Triangles */
		HashSet<Triangle> triangles= new HashSet<Triangle>();

		/* set up initial Triangle - these TEMPORARY points allow the
		 * triangulation to span the entire graph */
		int OUTER_BOUND= Math.max(tr.x - bl.x, tr.y - bl.y);
		Point p1= new Point(-OUTER_BOUND + bl.x, -OUTER_BOUND + bl.y);
		Point p2= new Point(-OUTER_BOUND + bl.x, 3 * OUTER_BOUND + bl.y);
		Point p3= new Point(3 * OUTER_BOUND + bl.x, -OUTER_BOUND + bl.y);
		Triangle outer= new Triangle(p1, p2, p3);
		triangles.add(outer);

		// add Points until none are left
		while (pts.hasNext()) {
			Point p= pts.next();
			if (outer.addPoint(p, triangles))
				vertices.add(p);
		}

		// remove the bounding Triangle from the triangulation set
		triangles.remove(outer);

		edges= allUndirectedEdges(triangles);

		// remove any edges to the bounding Triangle
		edges.removeIf(
			(UEdge e) -> e.contains(p1) || e.contains(p2) || e.contains(p3));
	}

	/** Return this triangulation's immutable vertex set. */
	public Set<Point> getVertices() {
		return Collections.unmodifiableSet(vertices);
	}

	/** Return this triangulation's immutable edge set. */
	public Set<UEdge> getEdges() {
		return Collections.unmodifiableSet(edges);
	}

	/** Return a set of all unique edges in the triangulation set ts.
	 * Edges are represented as UEdges to avoid redundancy. */
	private static Set<UEdge> allUndirectedEdges(HashSet<Triangle> ts) {
		HashSet<UEdge> edges= new HashSet<UEdge>();
		for (Triangle t : ts)
			t.addUEdgesToSet(edges);
		return edges;
	}
}

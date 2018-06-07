package generation;

import java.awt.Point;
import java.util.Objects;

/**\ An instance is a unique undirected edge between two points.
 * Uniqueness: the segment (p1, p2) equals (p2, p1) for all Points. */
public class UEdge {

	/* The two endpoints of this segment. p1 is the "smaller" end,
	 * i.e. lower x and then y value. */
	private Point p1, p2;

	/** Constructor: two endpoints of a segment. */
	public UEdge(Point p1, Point p2) {
		if (p1.x < p2.x || (p1.x == p2.x && p1.y < p2.y)) {
			this.p1= p1;
			this.p2= p2;
		} else {
			this.p1= p2;
			this.p2= p1;
		}
	}

	/** Return the first (leftmost, then bottom) Point of this segment.*/
	public Point p1() {
		return p1;
	}

	/** Return the second (rightmost, then top) Point of this segment. */
	public Point p2() {
		return p2;
	}

	/** Return the endpoint other than p of this edge.
	 * Precondition: p is non-null and is an endpoint of this edge. */
	public Point getOther(Point p) {
		if (p.equals(p1)) return p2;
		if (p.equals(p2)) return p1;
		throw new IllegalArgumentException("p is not in this edge");
	}

	/** Return true iff p is an endpoint of this segment. */
	public boolean contains(Point p) {
		return p1.equals(p) || p2.equals(p);
	}

	/** Return true iff: ob is this, or ob is an UndirectedSegment
	 * with the same endpoints (in any order). */
	@Override public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || !(obj instanceof UEdge)) return false;
		UEdge seg= (UEdge) obj;
		return p1 == seg.p1  &&  p2 == seg.p2;
	}

	@Override public int hashCode() {
		return Objects.hash(p1, p2);
	}
}

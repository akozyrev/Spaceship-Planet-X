package generation;

import java.awt.Point;

/**
 * Used to represent events occurring during incremental Delaunay
 * triangulation. By tracing through events, a particular Triangle containing an
 * arbitrary Point can be found in logarithmic time, instead of linear time.
 * Events consist of Point addition events or flip events.
 * 
 * An instance maintains Triangles resulting from an event, and can select a
 * Triangle containing an arbitrary Point.
 */
public class HistoryEvent {
	/* The Triangles associated with this event. There are either 3 (for an
	 * addition) or 2 (for a flip). */
	private Triangle[] ts;

	/**
	 * Constructor: an array of 3 Triangles (for an add event) or 2 Triangles
	 * (for a flip event).
	 */
	public HistoryEvent(Triangle[] ts) {
		if (ts.length != 3 && ts.length != 2)
			throw new IllegalArgumentException("Illegal event: " + ts.length);
		this.ts = ts;
	}

	/**
	 * Returns a PointLocation for p based on the Triangles in this event.
	 */
	public PointLocation select(Point p) {
		for (Triangle t : ts) {
			PointLocation pLoc = t.pointLocation(p);
			if (pLoc.isNotOut())
				return pLoc;
		}
		return PointLocation.makeOut();
	}
}

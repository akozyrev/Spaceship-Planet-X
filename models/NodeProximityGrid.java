package models;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

import util.Util;

import java.awt.geom.Point2D;

/** An instance maintains Nodes in a 2D rectangle and can return the closest Node
 * to a given Point. It maintains an internal set of rectangles containing Nodes
 * based on their coordinates. */
public class NodeProximityGrid implements Iterable<Node> {

	/* a 2D array(list) representing rectangles that contain a list of Nodes.
	 * [0][0] is the bottom-left rectangle. rs will always have at least one
	 * rectangle, and will always be rectangular (i.e. not ragged). */
	private ArrayList<ArrayList<List<Node>>> rs;

	/* The dimensions of an individual rectangle. */
	private static final int RECT_WIDTH= 64, RECT_HEIGHT= 64;

	/* The minimum x- and y-values of this NodeProximitySet. */
	private int x, y;

	/** Constructor: a NodeProximityMap spanning the axis-aligned rectangle
	 * with bottom-left coordinates (x, y) and the given dimensions. */
	public NodeProximityGrid(int x, int y, int width, int height) {
		this.x= x;
		this.y= y;

		int w= width / RECT_WIDTH + 1;
		int h= height / RECT_HEIGHT + 1;
		rs= new ArrayList<>(h);
		for (int i= 0; i < h; ++i) {
			rs.add(new ArrayList<>(h));
			for (int j= 0; j < w; ++j) {
				rs.get(i).add(new LinkedList<Node>());
			}
		}
	}

	/** Add Node n to this NodeProximityGrid.
	 * Precondition: n is within the bounds of this NodeProximityGrid. */
	public void addNode(Node n) {
		int ri = (n.y() - y) / RECT_HEIGHT;
		int rj = (n.x() - x) / RECT_WIDTH;

		rs.get(ri).get(rj).add(n);
	}

	/** Return the closest Node to p.
	 * Precondition: this map is not empty. */
	public Node closestNode(Point2D p) {
		int ri= (int) (p.getY() - y) / RECT_HEIGHT;
		int rj= (int) (p.getX() - x) / RECT_WIDTH;

		if (ri >= rows())
			ri= rows() - 1;
		else if (ri < 0)
			ri= 0;

		if (rj >= cols())
			rj= cols() - 1;
		else if (rj < 0)
			rj= 0;

		Node n= closestOfList(rs.get(ri).get(rj), p);

		int imin= ri - 1 >= 0 ? ri - 1 : 0;
		int imax= ri + 1 < rows() ? ri + 1 : rows() - 1;
		int jmin= rj - 1 >= 0 ? rj - 1 : 0;
		int jmax= rj + 1 < cols() ? rj + 1 : cols() - 1;
		do {
			List<Node> ns= new LinkedList<Node>();
			ns.add(n);
			for (int i= imin; i <= imax; ++i) {
				ns.add(closestOfList(rs.get(i).get(jmin), p));
				ns.add(closestOfList(rs.get(i).get(jmax), p));
			}
			for (int j= jmin + 1; j < jmax; ++j) {
				ns.add(closestOfList(rs.get(imin).get(j), p));
				ns.add(closestOfList(rs.get(imax).get(j), p));
			}

			n= closestOfList(ns, p);

			imin= imin - 1 >= 0 ? imin - 1 : imin;
			imax= imax + 1 < rows() ? imax + 1 : imax;
			jmin= jmin - 1 >= 0 ? jmin - 1 : jmin;
			jmax= jmax + 1 < cols() ? jmax + 1 : jmax;
		} while (n == null);
		return n;
	}

	/** Return the Node in ns closest to (x, y) or null if ns is null/empty. */
	private static Node closestOfList(List<Node> ns, Point2D p) {
		if (ns.isEmpty())
			return null;

		Node closest= ns.get(0);
		double dist= Double.MAX_VALUE;
		for (Node n : ns) {
			if (n != null) {
				double nDist = Util.distance(n.x(), n.y(), p.getX(), p.getY());
				if (dist > nDist) {
					dist= nDist;
					closest= n;
				}
			}
		}
		return closest;
	}

	/** Return the number of rows in this grid. */
	private int rows() {
		return rs.size();
	}

	/** Return the number of columns in this grid. */
	private int cols() {
		return rs.get(0).size();
	}

	@Override
	public Iterator<Node> iterator() {
		return new MapIterator();
	}

	/** An instance enumerates all Nodes in this NodeProximityGrid. */
	private class MapIterator implements Iterator<Node> {

		/* The current index whose Nodes are being iterated. */
		private int ri, rj;

		/* The current iterator getting Nodes. */
		private Iterator<Node> iter;

		/** Constructor: a MapIterator starting at rs[0][0]. */
		public MapIterator() {
			ri= 0;
			rj= 0;
			iter= rs.get(0).get(0).iterator();
			ensureTotalEnumeration();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Node next() {
			Node n = iter.next();
			ensureTotalEnumeration();
			return n;
		}

		/** Iff iter still has more elements, do nothing. Otherwise, cycle to
		 * the next List's iterator until all Nodes have been enumerated. */
		private void ensureTotalEnumeration() {
			while (!iter.hasNext() && ri < rs.size()) {
				++rj;
				if (rj < cols()) {
					iter= rs.get(ri).get(rj).iterator();
				} else {
					++ri;
					if (ri < rows()) {
						rj= 0;
						iter = rs.get(ri).get(rj).iterator();
					}
				}
			}
		}
	}
}
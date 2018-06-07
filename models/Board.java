package models;

import generation.DelaunayTriangulation;
import generation.UEdge;
import util.Paths;
import util.Util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Random;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;

/** A Board represents the physical layout of a game: it tracks Nodes,
 * Edges, and size of the map. Boards are randomly generated from a seed. */
public class Board {

	/* The seed given to a RNG to generate this Board. */
	private final long seed;

	/* The "Earth" Node. The spaceship starts and must return here. */
	private Node earth;

	/* The target Node that must be reached during the rescue stage. */
	private Node target;

	/* The distance of the Node furthest from the target. */
	private double furthestNodeDistance;

	/* The minimum traveled distance from Earth to the target. */
	private int distanceToTarget;

	/* The set of all Edges on this Board. */
	private Set<Edge> edges;

	/* The NodeProximityTree of all Nodes on this Board. */
	private NodeProximityGrid nodes;

	/* The dimensions of this Board, which is a rectangle. */
	private int width;
	private int height;

	/* The total sum of the edge weights on this Board. */
	private int sumEdges;

	/** Constructor: a rectangular Board generated via RNG with seed s. There
	 * are many Board generation parameters here explained in the Builder. */
	private Board(int w, int h, long s, int minNodes, int maxNodes, int minGems,
		int maxGems) {
		width= w;
		height= h;
		seed= s;
		Random r= new Random(s);
		int np= r.nextInt(maxNodes - minNodes + 1) + minNodes;
		DelaunayTriangulation dt= new DelaunayTriangulation(np, r, w, h);

		// convert Points to Nodes, mapping each Point to its corresponding Node
		HashMap<Point, Node> pToN = new HashMap<Point, Node>();
		nodes= new NodeProximityGrid(0, 0, w, h);
		Queue<String> names= planetNames(r); // shuffled list of planet names
		int id= 0; // id of each planet
		int targetId= r.nextInt(dt.getVertices().size() - 1) + 1;

		for (Point p : dt.getVertices()) {
			Node n= new Node.NodeBuilder().pos(p.x, p.y).name(names.peek()).id(id)
				.gems(gems(r, minGems, maxGems)).build();
			if (id == 0) {
				n.name= Util.EARTH_NAME;
				n.gems= 0;
				earth= n;
			} else if (id == targetId) {
				n.name= Util.CRASHED_PLANET_NAME;
				n.gems= 0;
				target= n;
			} else {
				names.remove();
			}
			++id;
			nodes.addNode(n);
			pToN.put(p, n);
		}

		// add the edges
		edges= new HashSet<Edge>();
		for (UEdge ue : dt.getEdges()) {
			Node n1= pToN.get(ue.p1());
			Node n2= pToN.get(ue.p2());
			Edge e= new Edge(n1, n2);
			n1.addExit(e);
			n2.addExit(e);
			edges.add(e);
		}

		// remove an arbitrary amount of edges, while keeping connectivity
		trimEdges(r);

		// set the furthest distance (needed for getPing)
		double maxDistance= 0;
		for (Node n : nodes) {
			double nodeDistance= absoluteDistanceToTarget(n);
			if (nodeDistance > maxDistance)
				maxDistance= nodeDistance;
		}
		furthestNodeDistance = maxDistance;
		distanceToTarget = Paths.pathWeight(Paths.minPath(earth, target));

		sumEdges= sum();
	}

	/** Return the sum of the distances on all edges. */
	private int sum() {
		int sum= 0;
		for (Edge e : edges) {
			sum= sum + e.length;
		}
		return sum;
	}

	/** Return the total sum of the edges on this Board. */
	public int sumEdges() {
		return sumEdges;
	}

	/** Trim the current edge set, removing edges chosen by RNG r.
	 * The graph will remain connected. */
	private void trimEdges(Random r) {
		class NEPair {
			Node n;
			Edge e;

			public NEPair(Node node, Edge edge) {
				n= node;
				e= edge;
			}
		}
		/* Visited Nodes */
		Set<Node> visited= new HashSet<Node>();

		/* Nodes to visit and the Edge used to reach them */
		Deque<NEPair> stack= new LinkedList<NEPair>();

		/* Edges that could be removed */
		ArrayList<Edge> candidates = new ArrayList<Edge>();

		/* Edges that must be kept to maintain connectivity */
		Set<Edge> keep= new HashSet<Edge>();
		stack.push(new NEPair(earth, null));
		while (!stack.isEmpty()) {
			NEPair p= stack.pop();
			if (visited.add(p.n)) {
				keep.add(p.e);
				for (Edge e : p.n.exits())
					stack.push(new NEPair(e.getOther(p.n), e));
			} else if (!keep.contains(p.e)) {
				candidates.add(p.e);
			}
		}

		// randomly trim some candidate edges
		int iterations= r.nextInt(candidates.size());
		for (int count= 0; count < iterations; ++count) {
			int end= candidates.size() - 1;
			int index= r.nextInt(candidates.size());
			Edge trim= candidates.get(index);
			candidates.set(index, candidates.get(end));
			candidates.remove(end);
			trim.firstExit().removeExit(trim);
			trim.secondExit().removeExit(trim);
			edges.remove(trim);
		}
	}

	/* Location of files for board generation */
	private static final String BOARD_GENERATION_DIRECTORY = Util.DIRECTORY
		+ "/data/board_generation";

	/** Return the planet names listed in planets.txt, shuffled to a
	 * random order using RNG r.
	 * 
	 * Precondition: planets.txt is in BOARD_GENERATION_DIRECTORY. */
	private static Queue<String> planetNames(Random r) {
		File f= new File(BOARD_GENERATION_DIRECTORY + "/planets.txt");
		BufferedReader reader;
		try {
			reader= new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			throw new UncheckedIOException("Cannot find planets.txt", e);
		}
		LinkedList<String> names= new LinkedList<String>();
		try {
			String line;
			while ((line= reader.readLine()) != null) {
				// Strip non-ascii or null characters out of string
				line= line.replaceAll("[\uFEFF-\uFFFF \u0000]", "");
				names.add(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading planets.txt", e);
		}
		Collections.shuffle(names, r);
		return names;
	}

	/** Return a random amount of gems set by the constraints min and max.
	 * The number tends to favor lower values. */
	private static int gems(Random r, int min, int max) {
		double weight= r.nextDouble();
		weight *= weight; // quadratic distribution; mean weight is 1/3
		return (int)(weight * (max - min + 1)) + min;
	}

	/** Return the seed used to generate this Board.*/
	public long seed() {
		return seed;
	}

	/** Return the signal strength from the crashed spaceship's at node n. This is
	 * inversely correlated with the distance between n and the target planet.
	 * 
	 * The returned value d satisfies 0 <= d <= 1. If d = 1, n is the target
	 * node. If d = 0, n is the node furthest from the target node. */
	public double signal(Node n) {
		return 1.0 - absoluteDistanceToTarget(n) / furthestNodeDistance;
	}

	/** Return the absolute distance from n to the target. */
	private double absoluteDistanceToTarget(Node n) {
		return Util.distance(n.x(), n.y(), target.x(), target.y());
	}

	/** Return an Iterable containing all the Nodes in this board.
	 * Do NOT modify this Iterable or its elements in any way.*/
	public Iterable<Node> nodes() {
		return nodes;
	}

	/** Return the closest Node to the given Point, or null if
	 * there are no Nodes. */
	public Node closestNode(Point2D p) {
		return nodes.closestNode(p);
	}

	/** Return the Node with ID id in this board if it exists,
	 * null otherwise. */
	public Node getNode(int id) {
		for (Node n : nodes) {
			if (n.id() == id)
				return n;
		}

		return null;
	}

	/** Return the starting Earth Node. */
	public Node earth() {
		return earth;
	}

	/** Return the unique target Node for which the spaceship is searching. */
	public Node target() {
		return target;
	}

	/** Return an unmodifiable Set of Edges in this board. */
	public Set<Edge> edges() {
		return Collections.unmodifiableSet(edges);
	}

	/** Return the distance from the target Node to its furthest Node. */
	public double furthestNodeDistance() {
		return furthestNodeDistance;
	}

	/** Return the width of this Board. */
	public int width() {
		return width;
	}

	/** Return the height of this Board. */
	public int height() {
		return height;
	}

	/** Return the min traveled distance between the target and Earth. */
	public int distanceToTarget() {
		return distanceToTarget;
	}

	/** An instance builds a Board with the appropriate parameters.
	 * It will refuse to build if a parameter is unset. */
	public static class BoardBuilder {

		/* The parameters needed to build a Board */
		private Integer width, height;
		private Long seed;
		private Integer minNodes, maxNodes;
		private Integer minGems, maxGems;

		/** Set the dimensions (width x height) of this  Board. */
		public BoardBuilder size(int width, int height) {
			this.width= width;
			this.height= height;
			return this;
		}

		/** Set the seed of the RNG used to generate this Board. */
		public BoardBuilder seed(long seed) {
			this.seed= seed;
			return this;
		}

		/** Set the min and max number of Nodes on this Board. */
		public BoardBuilder nodeBounds(int min, int max) {
			minNodes= min;
			maxNodes= max;
			return this;
		}

		/** Set the min and max number of gems per Node on this Board. */
		public BoardBuilder gemBounds(int min, int max) {
			minGems= min;
			maxGems= max;
			return this;
		}

		/** Build this Board.
		 * Precondition: all appropriate parameters have been set. */
		public Board build() {
			if (Util.anyNull(width, height, seed, minNodes, maxNodes, minGems,
				maxGems))
				throw new IllegalStateException("unset BoardBuilder params");

			return new Board(width, height, seed, minNodes, maxNodes, minGems,
				maxGems);
		}
	}
}

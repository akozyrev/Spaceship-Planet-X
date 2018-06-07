package models;

import java.util.*;

import util.Util;

/** A Node (vertex) represents a Planet. Each Node maintains:
 * 1. a set of edges that exit it,
 * 2. a modifier that affects the ship's speed upon visiting it,
 * 3. the amount of gems on the planet and the rate at which they are lost.
 */
public class Node implements BoardElement {
	String name; // Name of this planet 
	private final int id; // Unique identifier for this planet
	private int x, y; // Logical x, y coordinates of this Node

	int gems; // Current amount of gems on this planet; always >= 0
	private HashSet<Edge> exits; // Edges leaving this Node

	/** Constructor: a Node named name with ID id with no edges
	 * leaving it, the given x and y coordinates, speed modifier sm, initial
	 * amount of gems g, and loss rate lr. */
	private Node(int x, int y, String name, int id, int g) {
		this.x= x;
		this.y= y;
		this.name= name;
		this.id= id;
		gems= g;
		exits= new HashSet<Edge>();
	}

	/**
	 * Return an immutable set of edges leaving this node.
	 */
	public Set<Edge> exits() {
		return Collections.unmodifiableSet(exits);
	}
	
	/** Return an immutable set of edges leaving this node. */
	public Set<Edge> getExits() {
		return exits();
	}

	/** Return this Node's unique ID. */
	public int id() {
		return id;
	}

	/** Return a map of neighboring Nodes to the lengths of the Edges
	 * connecting them to this Node. */
	public HashMap<Node, Integer> neighbors() {
		HashMap<Node, Integer> neighbors= new HashMap<>();
		for (Edge e : exits) {
			neighbors.put(e.getOther(this), e.length);
		}
		return neighbors;
	}

	/** Add e to this Node's set of exits. */
	void addExit(Edge e) {
		exits.add(e);
	}

	/** Remove e from this Node's set of exits. */
	void removeExit(Edge e) {
		exits.remove(e);
	}

	/** Return true iff r is connected to this Node. */
	public boolean isExit(Edge r) {
		return exits.contains(r);
	}

	/** Return false if other.equals(this). Otherwise, return true iff
	 * there is an edge connecting this planet and other. */
	public boolean isConnectedTo(Node other) {
		if (other.equals(this))
			return false;

		for (Edge r : exits) {
			if (r.isExit(other))
				return true;
		}
		return false;
	}

	/** Return the edge that this Node shares with Node n, or null
	 * if they are not connected. */
	public Edge getEdge(Node n) {
		for (Edge r : exits) {
			if (r.getOther(this).equals(n))
				return r;
		}
		return null;
	}

	/** Return the current amount of gems on this planet. */
	public int gems() {
		return gems;
	}

	/** Return the current number of gems on this planet and set
	 * the number of gems to 0. */
	int takeGems() {
		int ret= gems;
		gems= 0;
		return ret;
	}

	/** Return true iff ob and this point to the same Node, or
	 * if ob is a Node with the same ID as this Node.
	 * Precondition: all Nodes have unique IDs. */
	@Override
	public boolean equals(Object ob) {
		if (ob == this)
			return true;
		if (ob == null || getClass() != ob.getClass())
			return false;
		return id == ((Node) ob).id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	/** Return a String containing the name and coordinates of this Node. */
	@Override
	public String toString() {
		return String.format("%s: (%s, %s)", name, x, y);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}

	/** An instance builds a Node with the appropriate parameters.
	 * It will refuse to build if a parameter is unset. */
	static class NodeBuilder {

		/* The parameters needed to build a Node */
		private Integer x, y;
		private String name;
		private Integer id;
		private Integer gems;

		/** Set the position (x, y) of this Node and return the node.*/
		public NodeBuilder pos(int x, int y) {
			this.x= x;
			this.y= y;
			return this;
		}

		/** Set the name of this Node to name and return the node. */
		public NodeBuilder name(String name) {
			this.name= name;
			return this;
		}

		/** Set the id to id, a unique identifier for this Node and
		 * return the node. */
		public NodeBuilder id(int id) {
			this.id= id;
			return this;
		}

		/** Set the initial number of gems on this Node to gems and
		 * return this Node. */
		public NodeBuilder gems(int gems) {
			this.gems= gems;
			return this;
		}

		/** Build this Node.
		 * Precondition: all appropriate parameters have been set. */
		public Node build() {
			if (Util.anyNull(x, y, name, id, gems))
				throw new IllegalStateException("unset NodeBuilder params");

			return new Node(x, y, name, id, gems);
		}
	}
}

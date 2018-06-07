package models;

import java.awt.geom.Point2D;
import java.util.Set;

/** An instance can access a Space Gems game's state. */
public interface Model {

	/** Return the seed used to generate this game. */
	public long seed();

	/** Return a Set of all Nodes in this game. */
	public Set<Node> nodes();

	/** Return a Set of all Edges in this game. */
	public Set<Edge> edges();

	/** Return the maximum separation between Nodes in the x-direction. */
	public int width();

	/** Return the maximum separation between Nodes in the y-direction. */
	public int height();

	/** Return the location of Earth, the starting Node. */
	public Node earth();

	/** Return the location of PlanetX, the destination of the rescue stage. */
	public Node planetX();

	/** Return the closest Node to the given Point, or null if
	 * there are no Nodes on the Board. */
	public Node closestNode(Point2D p);

	/** Return the Node that the ship is currently on or the Node
	 * from which the ship has just departed. */
	public Node shipNode();

	/** Return the current location of the ship in this game. */
	public Point2D shipLocation();

	/** Return the current stage of this Space Gems game. */
	public Phase phase();

	/** Return the remaining amount of fuel. */
	public int fuelRemaining();

	/** Return the total distance traveled since the rescue stage started. */
	public int fuelUsed();

	/** Return the current amount of gems collected. */
	public int gems();

	/** Return the current score of this game. */
	public int score();

	/** Return true iff the search phase ended successfully. */
	public boolean searchSucceeded();

	/** Return true iff the rescue phase ended successfully. */
	public boolean rescueSucceeded();

	/** An instance describes the current phase of the model. */
	public static enum Phase {
		SEARCH, RESCUE, NONE
	};
}

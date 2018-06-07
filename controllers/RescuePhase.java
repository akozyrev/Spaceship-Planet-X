package controllers;

import models.Node;
import java.util.Set;

/** Return to Earth on time while collecting as many gems as possible.
 * The rescued spaceship has information on the entire galaxy.
 * Gems on a planet are collected automatically when the planet is move to.
 * 
 * N.B.: There are many other methods in other classes that you will also
 * probably want to use, such as those in Node.
 * 
 * An instance provides all the necessary methods to move through the galaxy,
 * collect speed upgrades, and reach Earth.
 */
public interface RescuePhase {
	/** Return the Node on which the ship is. */
	public Node currentNode();

	/** Return node Earth Node. You MUST return at this Node to succeed. */
	public Node earth();

	/** Return the set of all Nodes in the graph. */
	public Set<Node> nodes();

	/** Move the Ship to Node n.
	 * An exception occurs if the ship's current planet is not adjacent to n. */
	public void moveTo(Node n);

	/** Return the remaining amount of distance that your ship can travel.
	 * Your solution must end before this becomes negative. */
	public int fuelRemaining();
}

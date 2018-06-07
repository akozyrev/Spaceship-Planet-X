package controllers;

/** An instance contains the methods that must be implemented
 * in order to solve the game. */
public interface Spaceship {

	/** The spaceship is on the location given by parameter state.
	 * Move the spaceship to Planet X and then return, while the spaceship
	 * is on Planet X. This completes the first phase of the mission.<br><br>
	 * 
	 * If the spaceship continues to move after reaching Planet X, rather than
	 * returning, it will not count. A return from this procedure while
	 * not on Planet X count as a failure.<br><br>
	 *
	 * There is no limit to how many steps you can take, but the score is
	 * directly related to how long it takes you to find Planet X.<br><br>
	 *
	 * At every step, you know only the current planet's ID, the IDs of
	 * neighboring planets, and the strength of the signal from Planet X
	 * at each planet.<br><br>
	 *
	 * In this rescuePhase,<br>
	 * (1) In order to get information about the current state, use
	 * functions currentID(), neighbors(), and signal().<br><br>
	 *
	 * (2) Use method onPlanetX() to know if your ship is on Planet X.<br><br>
	 *
	 * (3) Use method moveTo(int id) to move to a neighboring planet
	 * with the given ID. Doing this will change state to reflect the
	 * ship's new position. */
	public void search(SearchPhase state);

	/** The spaceship is on the location given by state. Get back to Earth
	 * without running out of fuel and return while on Earth. Your ship can
	 * determine how much fuel it has left via method fuelRemaining(), and how
	 * much fuel is needed to travel on an edge via Edge's fuelNeeded().<br><br>
	 * 
	 * Each Planet has some gems. Moving to a Planet automatically
	 * collects any gems it carries, which increase your score. your
	 * objective is to return to earth with as many gems as possible.<br><br>
	 * 
	 * You now have access to the entire underlying graph, which can be
	 * accessed through parameter state. currentNode() and earth() return
	 * Node objects= of interest, and nodes() returns a collection of
	 * all nodes on the graph.<br><br>
	 *
	 * Note: Use moveTo() to move to a destination node adjacent to
	 * your ship's current node. */
	public void rescue(RescuePhase state);
}
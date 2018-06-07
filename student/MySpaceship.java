package student;

import controllers.Spaceship;
import models.Edge;
import models.Node;
import models.NodeStatus;

import controllers.SearchPhase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import controllers.RescuePhase;

/** An instance implements the methods needed to complete the mission. */
public class MySpaceship implements Spaceship {

    long startTime= System.nanoTime(); // start time of rescue phase

    /** The spaceship is on the location given by parameter state.
     * Move the spaceship to Planet X and then return (with the spaceship is on
     * Planet X). This completes the first phase of the mission.
     * 
     * If the spaceship continues to move after reaching Planet X, rather than
     * returning, it will not count. If you return from this procedure while
     * not on Planet X, it will count as a failure.
     *
     * There is no limit to how many steps you can take, but your score is
     * directly related to how long it takes you to find Planet X.
     *
     * At every step, you know only the current planet's ID, the IDs of
     * neighboring planets, and the strength of the signal from Planet X at
     * each planet.
     *
     * In this rescuePhase,
     * (1) In order to get information about the current state, use functions
     * currentID(), neighbors(), and signal().
     *
     * (2) Use method onPlanetX() to know if you are on Planet X.
     *
     * (3) Use method moveTo(int id) to move to a neighboring planet with the
     * given ID. Doing this will change state to reflect your new position.
     */
    @Override
    public void search(SearchPhase state) {
	// TODO: Find the missing spaceship
	HashSet<Integer> visited = new HashSet<Integer>();
	if (!state.onPlanetX()) {
//	    dfsWalk(state, visited);
	    optimizedWalk2(state, visited);
	} else {
	    return;
	}
    }

    /* [Non-optimized search]
     * Visit every node reachable along paths of unvisited nodes from
     * the node given by state, call it node N.
     * If Planet X is reached, return with the ship on Planet X. 
     * If Planet X is not found, return with ship on planet N.
     * 
     * Precondition: Node N is unvisited.
     */
    public void dfsWalk(SearchPhase state, HashSet<Integer> visited) {
	int N = state.currentID();
	visited.add(N);
	if (state.onPlanetX()) return;
        
	for (NodeStatus ns : state.neighbors()) {
	    if (!visited.contains(ns.id())) {
		state.moveTo(ns.id());
		visited.add(state.currentID());
		if (state.onPlanetX()) return;
		dfsWalk(state, visited);
		if (state.onPlanetX())  return;
		state.moveTo(N);
	    }
	} 
	return;
    }
    
    /* [Optimized search]
     * Visit nodes with highest signal, reachable along paths of unvisited nodes
     * from the node N, given by state.
     * If Planet X is reached, return with the ship on Planet X.
     * If Planet X is not found, return with ship on planet N.
     * 
     * Precondition: Node N is unvisited.
     */
    public void optimizedWalk2(SearchPhase state, HashSet<Integer> visited) {
	int N = state.currentID();
	visited.add(N);
	if (state.onPlanetX()) return;
        NodeStatus[] neighbors= state.neighbors();
        Arrays.sort(neighbors, (b, c) -> b.compareTo(c));
        
	for (NodeStatus ns : neighbors) {
	    if (!visited.contains(ns.id())) {
		state.moveTo(ns.id());
		visited.add(state.currentID());
		if (state.onPlanetX()) return;
		dfsWalk(state, visited);
		if (state.onPlanetX())  return;
		state.moveTo(N);
	    }
	} 
	return;
    }
    
    /** The spaceship is on the location given by state. Get back to Earth
     * without running out of fuel and return while on Earth. Your ship can
     * determine how much fuel it has left via method fuelRemaining().
     * 
     * In addition, each Planet has some gems. Passing over a Planet will
     * automatically collect any gems it carries, which will increase your
     * score; your objective is to return to earth successfully with as many
     * gems as possible.
     * 
     * You now have access to the entire underlying graph, which can be accessed
     * through parameter state. Functions currentNode() and earth() return Node
     * objects of interest, and nodes() returns a collection of all nodes on the
     * graph.
     *
     * Note: Use moveTo() to move to a destination node adjacent to your current
     * node. */
    @Override
    public void rescue(RescuePhase state) {
	// TODO: Complete the rescue mission and collect gems
	if (state.currentNode() != state.earth()) {
	    shortestPath(state);
	} else {
	    return;
	}
    }

    /* Move the spaceship back to Earth along the shortest path
     * from Planet X. Return with spaceship on Earth.
     */
    public void shortestPath(RescuePhase state) {
	if (state.currentNode() != state.earth()) {
	    List<Node> path = Paths.minPath(state.currentNode(), state.earth());
	    for (Node n : path) {
		if (n != state.currentNode()) {
		    state.moveTo(n);
		}
	    }
	} else {
	    return;
	}
    }
}

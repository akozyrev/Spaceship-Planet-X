package models;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import controllers.SearchPhase;
import controllers.RescuePhase;

import static models.Model.Phase.*;

/** An instance maintains the current state of a Planet X game.*/
public class PlanetXModel implements Model, Controllable {

	private Phase phase; // The current phase of the model
	private final Board board; // The Board associated with this instance 

	private Node shipNode; // The Node that the ship is on or has last visited.
	private Point2D.Double shipLocation; // The current location of the ship.
	private Edge shipEdge; // The Edge on which the ship is traveling. null if still.
	private double lerpT; // linear interpolation parameter (shipNode to next)
	private int edgeDist; // current distance traveled on this edge

	private int fuelRemaining; // Fuel left for rescue. < 0 => failed solution.
	private int fuelUsed; // Fuel used by the ship
	private static final int BASE_SPEED = 100; // Base speed of ship (per second)

	private int gems; // The current amount of gems that the ship holds; >= 0
	private int score; // The cumulative score

	private String failMessage; // Iff failed, contains message; else null
	private boolean abort; // True if a game has aborted
	private boolean searchSuccessful; // True if rescue ended successfully
	private boolean rescueSuccessful; // True if return ended successfully

	/** Constructor: a new game with Board b. */
	public PlanetXModel(Board b) {
		phase= NONE;
		board= b;

		shipNode= board.earth();
		shipLocation= new Point2D.Double(shipNode.x(), shipNode.y());
		shipEdge= null;
		lerpT= 0d;

		fuelUsed= 0;
		fuelRemaining= 0;

		gems= 0;
		score= 0;

		failMessage= null;
		abort= false;
		searchSuccessful= false;
		rescueSuccessful= false;
	}

	@Override
	public int width() {
		return board.width();
	}

	@Override
	public int height() {
		return board.height();
	}

	@Override
	public long seed() {
		return board.seed();
	}

	@Override
	public Set<Node> nodes() {
		HashSet<Node> ns = new HashSet<>();
		for (Node n : board.nodes())
			ns.add(n);
		return ns;
	}

	@Override
	public Set<Edge> edges() {
		return board.edges();
	}

	@Override
	public Node closestNode(Point2D p) {
		return board.closestNode(p);
	}

	@Override
	public Node shipNode() {
		return shipNode;
	}

	@Override
	public Point2D shipLocation() {
		return shipLocation;
	}

	@Override
	public Phase phase() {
		return phase;
	}

	@Override
	public int fuelRemaining() {
		return fuelRemaining - edgeDist;
	}

	@Override
	public int fuelUsed() {
		return fuelUsed + edgeDist;
	}

	@Override
	public int score() {
		if (phase == SEARCH) {
			int tmp = score - edgeDist;
			return tmp > 0 ? tmp : 0;
		} else {
			return score;
		}
	}

	@Override
	public synchronized void update(int tick) throws SolutionFailedException {
		if (failMessage != null) {
			throw new SolutionFailedException(failMessage);
		}

		if (shipEdge != null) {
			Node shipNext= shipEdge.getOther(shipNode);
			double travelDist= BASE_SPEED * (tick / 1e3);
			lerpT += travelDist / shipEdge.length;
			if (lerpT > 1d) {
				shipArrive();
				notifyAll();
			} else {
				edgeDist= (int) (lerpT * shipEdge.length + 0.5d);
				shipLocation.x = (1 - lerpT) * shipNode.x() + lerpT * shipNext.x();
				shipLocation.y = (1 - lerpT) * shipNode.y() + lerpT * shipNext.y();
			}

			if (phase == RESCUE && fuelRemaining() < 0) {
				failMessage= "ran out of fuel and can no longer travel.";
				score= 0;
				throw new SolutionFailedException(failMessage);
			}
		}
	}

	/** Make the ship arrive to its next destination.
	 * Precondition: the ship is moving between two Nodes. */
	private void shipArrive() {
		shipNode= shipEdge.getOther(shipNode);
		shipLocation.x= shipNode.x();
		shipLocation.y= shipNode.y();
		lerpT= 0d;
		edgeDist= 0;
		fuelUsed += shipEdge.length;
		if (phase == SEARCH) {
			score -= shipEdge.length;
			if (score < 0)
				score= 0;
		}
		else if (phase == RESCUE)
			fuelRemaining -= shipEdge.length;
		shipEdge= null;
	}

	@Override
	public int currentID() {
		return shipNode.id();
	}

	@Override
	public double signal() {
		return board.signal(shipNode);
	}

	@Override
	public NodeStatus[] neighbors() {
		Set<Node> nodes= shipNode.neighbors().keySet();
		NodeStatus[] ns= new NodeStatus[nodes.size()];
		int i= 0;
		for (Node n : nodes) {
			ns[i]= new NodeStatus(n.id(), n.name(), board.signal(n));
			++i;
		}
		return ns;
	}

	@Override
	public boolean onPlanetX() {
		return shipNode == board.target();
	}

	@Override
	public Node currentNode() {
		return shipNode;
	}

	@Override
	public Node earth() {
		return board.earth();
	}

	@Override
	public Node planetX() {
		return board.target();
	}

	/** When called, blocks until the ship has moved from shipNode to n. */
	private synchronized void waitUntilMoved(Node n) {
		shipEdge= shipNode.getEdge(n);
		shipEdge.visit();
		while (shipEdge != null) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public synchronized void moveTo(int id) {
		if (abort)
			throw new AbortException();
		if (failMessage != null)
			waitUntilAbort();

		for (Node n : shipNode.neighbors().keySet()) {
			if (n.id() == id) {
				waitUntilMoved(n);
				return;
			}
		}
		failMessage= "tried to call moveTo to a non-adjacent ID.";
		waitUntilAbort();
	}

	@Override
	public synchronized void moveTo(Node n) {
		if (abort)
			throw new AbortException();
		if (failMessage != null)
			waitUntilAbort();

		if (!shipNode.isConnectedTo(n)) {
			failMessage= "tried to call moveTo to a non-adjacent Node.";
			waitUntilAbort();
		}

		waitUntilMoved(n);

		int g= n.takeGems();
		gems += g;
		score += g;
	}

	@Override
	public int gems() {
		return gems;
	}

	@Override
	public SearchPhase beginSearchPhase() {
		phase= SEARCH;
		score= board.distanceToTarget() * 2;

		return new SearchPhase() {
			@Override
			public int currentID() {
				return PlanetXModel.this.currentID();
			}

			@Override
			public double signal() {
				return PlanetXModel.this.signal();
			}

			@Override
			public NodeStatus[] neighbors() {
				return PlanetXModel.this.neighbors();
			}

			@Override
			public boolean onPlanetX() {
				return PlanetXModel.this.onPlanetX();
			}

			@Override
			public void moveTo(int id) {
				PlanetXModel.this.moveTo(id);
			}

		};
	}

	@Override
	public RescuePhase beginRescuePhase() {
		phase= RESCUE;
		fuelRemaining= board.sumEdges() / 2 + board.distanceToTarget();

		return new RescuePhase() {
			@Override
			public Node currentNode() {
				return PlanetXModel.this.currentNode();
			}

			@Override
			public Node earth() {
				return PlanetXModel.this.earth();
			}

			@Override
			public Set<Node> nodes() {
				return PlanetXModel.this.nodes();
			}

			@Override
			public void moveTo(Node n) {
				PlanetXModel.this.moveTo(n);
			}

			@Override
			public int fuelRemaining() {
				return PlanetXModel.this.fuelRemaining();
			}
		};
	}

	@Override
	public boolean endSearchPhase() {
		if (phase != SEARCH)
			throw new IllegalStateException(
				"error: not in rescue stage; can't end rescue stage");

		phase= NONE;
		searchSuccessful= shipNode == board.target();
		if (!searchSuccessful)
			score= 0;
		return searchSuccessful;
	}

	@Override
	public boolean endRescuePhase() {
		if (phase != RESCUE)
			throw new IllegalStateException(
				"error: not in return stage; can't end return stage");

		phase= NONE;
		rescueSuccessful = shipNode == board.earth();
		if (!rescueSuccessful)
			score= 0;
		return rescueSuccessful;
	}

	@Override
	public boolean searchSucceeded() {
		return searchSuccessful;
	}

	@Override
	public boolean rescueSucceeded() {
		return rescueSuccessful;
	}

	@Override
	public void setShipLocation(Node n) {
		shipNode= n;
	}

	/** Block until the game is aborted, then throws an AbortException. */
	private synchronized void waitUntilAbort() throws AbortException {
		while (!abort) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		throw new AbortException();
	}

	@Override
	public synchronized void abort() {
		abort= true;

		// If the ship was moving, forcibly stop it
		if (shipEdge != null) {
			shipArrive();
			notifyAll();
		}
	}
}

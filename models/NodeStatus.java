package models;

/** An instance contains the ID and name of a Node and the signal
 * strength at this Node. Used in SearchPhase in lieu of Nodes. */
public class NodeStatus implements Comparable<NodeStatus> {

	private final int id; // The ID of this Node.
	private final String name; // The name of this Node.
	private final double signal; // The signal strength at this Node.

	/** Constructor: an instance with ID nodeId, name nodeName,
	 *  and signal signalStrength. */
	NodeStatus(int nodeId, String nodeName, double signalStrength) {
		id= nodeId;
		name= nodeName;
		signal= signalStrength;
	}

	/** Return the ID of this NodeStatus. */
	public int id() {
		return id;
	}

	/** Return the name of the Node that corresponds to this NodeStatus. */
	public String name() {
		return name;
	}

	/** Return the signal strength at this NodeStatus. */
	public double signal() {
		return signal;
	}

	/** Return an int n, where if this's ping is less than, equal to, greater
	 * than other's ping; then n < 0, n = 0, n > 0, respectively.
	 * 
	 * N.B. pings are inversely proportional to distance! If a ping is LOWER,
	 * then what does this mean about its position relative to the target? */
	@Override
	public int compareTo(NodeStatus other) {
		return Double.compare(signal, other.signal);
	}

	/** Return true iff ob and this point to the same NodeStatus, or if
	 * ob is a NodeStatus with the same ID as this. */
	@Override
	public boolean equals(Object ob) {
		if (ob == this)
			return true;
		if (ob == null || getClass() != ob.getClass())
			return false;
		return id == ((NodeStatus) ob).id;
	}

	@Override
	public int hashCode() {
		return id;
	}
}

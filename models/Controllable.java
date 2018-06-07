package models;

import controllers.SearchPhase;
import controllers.RescuePhase;

/** An instance is controllable by some Controller. */
public interface Controllable extends SearchPhase, RescuePhase {

    /** Start the search phase and return an instance that
     *  purely implements SearchPhase. */
    public SearchPhase beginSearchPhase();

    /** Start the rescue phase and returns an instance that
     *  purely implements ReturnPhase. */
    public RescuePhase beginRescuePhase();

    /** End the search phase, returning true iff it was successful 
     * and false otherwise.
     * Precondition: the game is in the search phase. */
    public boolean endSearchPhase();

    /** End the rescue phase, returning true iff it was successful and
     * false otherwise. Precondition: the game is in the return phase. */
    public boolean endRescuePhase();

    /** Advance the simulation by one tick.
     * Throw SolutionFailedException if the update causes a failure. */
    public void update(int tick) throws SolutionFailedException;

    /** Abort the current game. Any attempts to change the state (i.e. by
     * moveTo) will result in an exception. This allows the game to instantly
     * end a solution, rather than stepping through the entire solution. */
    public void abort();

    /** Set the ship's current location to n. */
    public void setShipLocation(Node n);

    /** An instance indicates that the game has aborted. */
    @SuppressWarnings("serial")
    public static class AbortException extends RuntimeException {}

    /** An instance contains a message detailing how a solution failed.  */
    @SuppressWarnings("serial")
    public static class SolutionFailedException extends Exception {
        public SolutionFailedException(String msg) {
            super(msg);
        }
    }
}

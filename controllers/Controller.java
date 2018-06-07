package controllers;

/** An instance controls a Game and can access its status. */
public interface Controller {

    public static final int TICKTIME= 10; // time per update, in milliseconds

    /** Start the current game. If a game has already started, this will print
     * an error message and return. */
    public void start();

    /** Reset the current game to its initial state. */
    public void reset();
    
    /** Return true iff the search stage ended successfully. */
    public boolean searchSucceeded();
    
    /** Return true iff the rescue stage ended successfully. */
    public boolean rescueSucceeded();
    
    /** Update the model by one tick. */
    public void update();

    /** Create (but don't start) a new game with the long value of str
     * as a seed, or a random seed of str is not a valid long. */
    public void newGame(String str);

    /** Create (but don't start) a new game with seed s. */
    public void newGame(long s);
}

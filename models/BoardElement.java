package models;

/** An instance represents a viewable entity attached to a board. */
public interface BoardElement {

	/*** Return the name of this BoardElement when drawn on the board. */
	public String name();

	/** Return the x coordinate of this BoardElement. */
	public int x();

	/** Return the y coordinate of this BoardElement. */
	public int y();
}

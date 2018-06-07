package gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** An instance represents a Drawable Ship somewhere on a Graph.*/
public class Ship extends Circle {

	/* The diameter of this Ship when drawn. */
	private static final int DIAMETER= 12;

	/* The color of this Ship when drawn. */
	private static final Color SHIP_COLOR= new Color(240, 246, 255);

	/** Constructor: a Ship starting at Point p in area area and 
	 * bounds bounds with speed speed, whose name's size is determined
	 * via fm. */
	public Ship(Point2D p, Rectangle2D area, Rectangle2D bounds, int speed,
		FontMetrics fm) {
		super("You", p, area, bounds, DIAMETER, SHIP_COLOR, fm);
	}

	@Override
	public void draw(Graphics2D g) {
		updateDrawnLocation();
		super.draw(g);
	}
}

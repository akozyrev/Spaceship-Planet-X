package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/** An instance represents a drawable line connecting two Planets. */
public class Line implements Drawable {

	/* Default thickness of drawn lines. */
	public static final int LINE_THICKNESS = 1;

	/* The color to display for a line based on how many times it has been
	 * visited. */
	public static final Color[] COLORS = { new Color(160, 160, 160),
		new Color(0, 255, 0), new Color(255, 255, 0), new Color(255, 0, 0) };

	/* The particular color of this line. */
	private Color color;

	/* The two endpoints of this Line, in no particular order. */
	private Circle p1, p2;

	/** Constructor: an edge from p1 to p2. */
	public Line(Circle p1, Circle p2) {
		this.p1= p1;
		this.p2= p2;
		color= COLORS[0];
	}

	/** Change this Line's color based on its number of visits.
	 * Precondition: visits >= 0. */
	public void setVisits(int visits) {
		int maxIndex= COLORS.length - 1;
		color= COLORS[visits <= maxIndex ? visits : maxIndex];
	}

	@Override public void draw(Graphics2D g) {
		Stroke s= g.getStroke();
		Color c= g.getColor();

		g.setStroke(new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0));
		g.setColor(color);

		g.drawLine(p1.drawnX(), p1.drawnY(), p2.drawnX(), p2.drawnY());

		g.setStroke(s);
		g.setColor(c);
	}
}

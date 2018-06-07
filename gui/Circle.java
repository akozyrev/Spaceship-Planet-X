package gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** An instance represents a Circle that can be drawn using a Graphics2D
 * instance. It is drawn within a certain rectangle, with its drawn size and
 * position changing dynamically based on its initial size and position. */
public class Circle implements Drawable {

	/* Number of pixels between the top of the circle and the bottom of the
	 * name. */
	private static final int NAME_Y_PADDING= 3;

	/* The color used to draw this Circle's name. */
	private static final Color NAME_COLOR= new Color(255, 255, 255);

	/* The color used to draw this Circle's border. */
	private static final Color BORDER_COLOR= Color.GRAY;

	/* The amount of extra space on each side of the bounding box, used to
	 * ensure this Circle is fully bounded. */
	private static final int BOUND_PADDING= 3;

	/* The dimensions of this Circle's name. */
	private int nameWidth;
	private int nameHeight;

	/* The area in which this Circle is drawn. */
	private Rectangle2D bounds;

	/* The "actual" area that is being drawn. */
	private Rectangle2D area;

	/* The "actual" location of this Circle represents. */
	private Point2D location;

	/* The circle used to draw this Circle. */
	private Ellipse2D.Double circle;

	/* The name of this planet drawn above it. */
	private String name;

	/* The color of this Circle. */
	private Color color;

	/** Constructor: a Circle with the given name centered at ctr within the
	 * given area, drawn in the given bounds with initial diameter d and the
	 * given color. Its name's size will be measured using fm. */
	public Circle(String name, Point2D ctr, Rectangle2D area,
		Rectangle2D bounds, double d, Color color, FontMetrics fm) {
		this.name= name;
		location= ctr;
		this.area= area;
		this.bounds= bounds;
		circle= new Ellipse2D.Double(0, 0, d, d);
		setBounds(bounds);
		setArea(area);
		this.color= color;
		nameWidth= fm.stringWidth(name);
		nameHeight= fm.getHeight();
	}

	/** Set the bounding Rectangle in which this Circle is drawn to r. */
	public void setBounds(Rectangle2D r) {
		bounds= r;
		updateDrawnLocation();
	}

	/*** Set the area being drawn to r. */
	public void setArea(Rectangle2D r) {
		circle.width= circle.width;
		circle.height= circle.width;
		area= r;
		updateDrawnLocation();
	}

	/** Update the drawn circle's location based on the current true 
	 * location, bounds, and area. */
	protected void updateDrawnLocation() {
		circle.x= drawnX() - (int) ((circle.width + 1) / 2.0);
		circle.y= drawnY() - (int) ((circle.height + 1) / 2.0);
	}

	/** Graphical x-value for this circle. */
	public int drawnX() {
		return (int) ((location.getX() - area.getX()) * bounds.getWidth()
			/ area.getWidth());

	}

	/** Graphical y-value for this circle. */
	public int drawnY() {
		return (int) ((location.getY() - area.getY()) * bounds.getHeight()
			/ area.getHeight());
	}

	/** Return the x-coordinate of the center of this Circle. */
	public int x() {
		return (int) (location.getX() + 0.5);
	}

	/** Return the y-coordinate of the center of this Circle. */
	public int y() {
		return (int) (location.getY() + 0.5);
	}

	/** Return the radius of this Circle. */
	public double radius() {
		return circle.width / 2;
	}

	/** Return the "true location" of this Circle. */
	public Point2D location() {
		return location;
	}

	/** Set the "true location" of this Circle to v. */
	public void setLocation(Point2D p) {
		location= p;
		updateDrawnLocation();
	}

	/** Return the graphical bounding box of this Circle. */
	public Rectangle bounds() {
		return new Rectangle(
			(int) Math.min(circle.x,
				circle.getCenterX() - nameWidth / 2 - BOUND_PADDING),
			(int) (circle.y - nameHeight - NAME_Y_PADDING - BOUND_PADDING),
			(int) (Math.max(circle.width, nameWidth + 2 * BOUND_PADDING) + 0.5),
			(int) (circle.height + nameHeight + NAME_Y_PADDING
				+ 2 * BOUND_PADDING));
	}
	
	/** Return the color of this Circle. */
	public Color color() {
		return color;
	}
	
	/** Return the name of this Circle. */
	public String name() {
		return name;
	}

	@Override public void draw(Graphics2D g) {
		Color c= g.getColor();
		g.setColor(color);
		g.fill(circle);
		g.setColor(BORDER_COLOR);
		g.draw(circle);

		// draw the Circle's name
		int x= (int) (circle.getCenterX() - (nameWidth + 1) / 2.0);
		int y= (int) (circle.getCenterY() - (circle.height + 1) / 2.0
			+ -NAME_Y_PADDING);

		g.setColor(NAME_COLOR);
		g.drawString(name, x, y);

		g.setColor(c);
	}
}

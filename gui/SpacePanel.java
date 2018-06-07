package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import models.Edge;
import models.Model;
import models.Node;
import util.Util;

/** An instance is a JPanel where the game's graph is drawn. */
@SuppressWarnings("serial")
public class SpacePanel extends JPanel {

	/* The font used to render all text in this panel. */
	private static final Font FONT= new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	/* The background of this panel. */
	private Image backgroundImage;

	/* The current Model being viewed by this SpacePanel. */
	private Model model;

	/* The area of the Model drawn by this SpacePanel at a zoom of 1. */
	private Rectangle2D.Double baseArea;

	/* The current area of the Model drawn by this SpacePanel. */
	private Rectangle2D.Double area;

	/* The current zoom level of the camera. zoom 1: default. zoom > 0. */
	private int zoom;

	/* The Ship traveling around on this Board. */
	private Ship ship;

	/* A map of all Nodes to the Planets on this SpacePanel. */
	private ConcurrentMap<Node, Planet> nToP;

	/* A map of all Edges to the Lines on this SpacePanel. */
	private ConcurrentMap<Edge, Line> eToL;

	/* The direction in which the camera is moving. */
	private Direction cameraDir;

	/* The distance that the camera moves per frame, if it is moving. */
	private static final int CAMERA_SPEED= 50;

	/* True iff the camera follows the ship. */
	private boolean followShip;

	/**  Constructor: an instance with dimensions (width, height). */
	public SpacePanel(int width, int height) {
		try {
			backgroundImage= ImageIO
				.read(new File(Util.DIRECTORY + "/data/images/horsehead.jpg"));
		} catch (IOException e) {
			System.err.println("Failed to set background image; "
				+ "defaulting to a black background");
			setBackground(Color.BLACK);
		}
		setBorder(new LineBorder(Color.LIGHT_GRAY));
		setPreferredSize(new Dimension(width, height));
		setLayout(null);
		setFont(FONT);
		setDoubleBuffered(true);

		nToP= new ConcurrentHashMap<>();
		eToL= new ConcurrentHashMap<>();
		addMouseListener(clickListener);

		followShip= false;
		zoom= 1;
	}

	/** Initialize this SpacePanel to display Model m.
	 * The Ship will be placed on the starting Node. */
	public void init(Model m) {
		// clear the old Board stuff, if necessary
		nToP.clear();
		eToL.clear();

		model= m;
		cameraDir= Direction.NONE;
		FontMetrics fm= getFontMetrics(FONT);

		Rectangle2D bounds= getBounds();
		double padding = Math.min(m.width(), m.height()) * 0.02;
		double w= m.width() + 2 * padding;
		double h= m.height() + 2 * padding;
		baseArea= new Rectangle2D.Double(-padding, -padding, w, h);
		area= new Rectangle2D.Double(-padding, -padding, w, h);

		// add the Nodes
		Random r= new Random(m.seed());
		for (Node n : m.nodes()) {
			Planet p= Planet.make(n.name(), new Point(n.x(), n.y()), area, bounds,
				fm, r);
			nToP.put(n, p);
		}

		// add the Edges
		for (Edge e : m.edges()) {
			Line l= new Line(nToP.get(e.firstExit()), nToP.get(e.secondExit()));
			eToL.put(e, l);
		}

		// add the Ship
		ship= new Ship(m.shipLocation(), area, bounds, SidePanel.INITIAL_SPEED,
			fm);

		// Add a listener for resize events
		addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				changeBounds();
			}

			@Override
			public void componentMoved(ComponentEvent e) {}

			@Override
			public void componentShown(ComponentEvent e) {}

			@Override
			public void componentHidden(ComponentEvent e) {}
		});

		setZoom(zoom);
		repaint();
	}
	
	/** Return the Planet corresponding to Node n. */
	public Planet getPlanet(Node n) {
		return nToP.get(n);
	}

	/** Update the displayed Drawables on this SpacePanel. */
	public void update() {
		if (followShip) {
			Point2D p= ship.location();
			area.x= p.getX() - area.getWidth() / 2.0;
			area.y= p.getY() - area.getHeight() / 2.0;
			updateArea();
		} else {
			switch (cameraDir) {
			case UP:
				area.y -= CAMERA_SPEED;
				updateArea();
				break;
			case RIGHT:
				area.x += CAMERA_SPEED;
				updateArea();
				break;
			case DOWN:
				area.y += CAMERA_SPEED;
				updateArea();
				break;
			case LEFT:
				area.x -= CAMERA_SPEED;
				updateArea();
				break;
			case NONE:
			}
		}
		repaint();
	}

	/** If follows is true, this SpacePanel's view follows the ship.
	 * Otherwise, the view remains fixed. */
	public void setFollowShip(boolean follows) {
		followShip= follows;
	}

	/** Adjust the position of all Drawables on this SpacePanel, to be used
	 * if this SpacePanel's bounds have changed. */
	private void changeBounds() {
		Rectangle2D bounds = getBounds();
		for (Planet p : nToP.values())
			p.setBounds(bounds);
		ship.setBounds(bounds);
		repaint();
	}

	/** Set the current zoom of the camera to the given level. */
	public void setZoom(int z) {
		double w= area.width;
		double h= area.height;
		area.width= baseArea.width / z;
		area.height= baseArea.height / z;
		area.x += (w - area.width) / 2;
		area.y += (h - area.height) / 2;
		updateArea();
		zoom = z;
	}

	/** Adjust all Drawable positions based on this panel's drawn area. */
	private void updateArea() {
		for (Planet p : nToP.values())
			p.setArea(area);
		ship.setArea(area);
	}

	/** An instance represents a direction (or lack thereof) in a 2D plane. */
	private static enum Direction {
		UP, RIGHT, DOWN, LEFT, NONE
	};

	/* When attached to a component with focus, allows the camera to move. */
	private KeyListener keyListener= new KeyListener() {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case VK_UP:
			case VK_W:
			case VK_K:
				cameraDir= Direction.UP;
				break;
			case VK_RIGHT:
			case VK_D:
			case VK_L:
				cameraDir= Direction.RIGHT;
				break;
			case VK_DOWN:
			case VK_S:
			case VK_J:
				cameraDir= Direction.DOWN;
				break;
			case VK_LEFT:
			case VK_A:
			case VK_H:
				cameraDir= Direction.LEFT;
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
			case VK_UP:
			case VK_W:
			case VK_K:
				if (cameraDir == Direction.UP)
					cameraDir= Direction.NONE;
				break;
			case VK_RIGHT:
			case VK_D:
			case VK_L:
				if (cameraDir == Direction.RIGHT)
					cameraDir= Direction.NONE;
				break;
			case VK_DOWN:
			case VK_S:
			case VK_J:
				if (cameraDir == Direction.DOWN)
					cameraDir= Direction.NONE;
				break;
			case VK_LEFT:
			case VK_A:
			case VK_H:
				if (cameraDir == Direction.LEFT)
					cameraDir = Direction.NONE;
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {}
	};

	public KeyListener spacePanelCameraMover() {
		return keyListener;
	}

	/* Function that should be called every time a Node is clicked. */
	private Consumer<Node> whenClicked;

	/* Used to find Nodes that are being clicked */
	private MouseListener clickListener= new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent arg0) {}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mousePressed(MouseEvent arg0) {}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			Point clicked= arg0.getPoint();
			Rectangle2D bounds= getBounds();
			// translate drawn area to "actual" area
			clicked.x= (int) (clicked.x * area.width / bounds.getWidth() + area.x
				+ 0.5);
			clicked.y= (int) (clicked.y * area.height / bounds.getHeight() + area.y
				+ 0.5);
			Node n= model.closestNode(clicked);

			// see if the Node was actually clicked
			if (n != null && (nToP.get(n).radius() * 3) >= Util.distance(n.x(),
				n.y(), clicked.x, clicked.y)) {
				whenClicked.accept(n);
			} else {
				whenClicked.accept(null);
			}
		}
	};

	/** Set this SpacePanel to call fun every time a Node is clicked.
	 * Calling this more than once will overwrite the previous function. */
	public void callWhenClicked(Consumer<Node> fun) {
		whenClicked= fun;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d= (Graphics2D) g;
		g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);

		for (Planet p : nToP.values())
			p.draw(g2d);
		for (ConcurrentMap.Entry<Edge, Line> el : eToL.entrySet()) {
			Line l= el.getValue();
			l.setVisits(el.getKey().getVisits());
			l.draw(g2d);
		}
		if (ship != null)
			ship.draw(g2d);
	}
}

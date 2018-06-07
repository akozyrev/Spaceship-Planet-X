package gui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/** An instance is a menu bar which allows a user to control the game,
 * e.g. by starting it or resetting it. */
@SuppressWarnings("serial")
public class TopMenu extends JMenuBar {
	/* The menu item to start the game. */
	private JMenuItem start;

	/* The menu item to reset the game. */
	private JMenuItem reset;

	/* The menu item to generate a new map. */
	private JMenuItem newMap;

	/** Constructor: an instance with new start, reset, and new map options. */
	public TopMenu() {
		JMenu fileMenu= new JMenu("File");
		start= new JMenuItem("Start");
		reset= new JMenuItem("Reset");
		newMap= new JMenuItem("New Map");

		fileMenu.add(start);
		fileMenu.add(reset);
		fileMenu.add(newMap);
		add(fileMenu);
	}

	/** Return the start menu item. */
	public JMenuItem getStartItem() {
		return start;
	}

	/** Return the reset menu item. */
	public JMenuItem getResetItem() {
		return reset;
	}

	/** Return the new map menu item. */
	public JMenuItem getNewMapItem() {
		return newMap;
	}
}

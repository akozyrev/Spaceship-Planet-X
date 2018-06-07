package gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

/** An instance is a JPanel that displays various stats and has sliders to
 * control the view of the GUI. */
@SuppressWarnings("serial")
public class SidePanel extends JPanel {

	/* Values for the speed slider. */
	public static final int MINIMUM_SPEED= 1;
	public static final int MAXIMUM_SPEED= 100;
	public static final int INITIAL_SPEED= MINIMUM_SPEED;

	/* Values for the zoom slider. */
	public static final int MINIMUM_ZOOM= 1;
	public static final int MAXIMUM_ZOOM= 10;
	public static final int INITIAL_ZOOM= MINIMUM_ZOOM;

	/* The font used to display stats on this Panel. */
	private static final Font LABEL_FONT= new Font("SansSerif", Font.BOLD, 14);
	private static final Font STAT_FONT= new Font("SansSerif", Font.PLAIN, 16);

	/* The background color of this Panel. */
	private static final Color BACKCOLOR= Color.BLACK;

	/* The default foreground colors. */
	private static final Color STATCOLOR= Color.LIGHT_GRAY;
	private static final Color FORECOLOR= Color.WHITE;

	/* Stats currently on the SidePanel; no null Stats. */
	private HashMap<StatName, Stat> stats;

	/*** An instance is a stat that can be displayed on a SidePanel. */
	public static enum StatName {
		SEED, SEARCH, RESCUE, PREVIOUS_NAME, GEMS, SEARCH_SCORE, RESCUE_SCORE,
		SCORE, FUEL_USED, FUEL_LEFT, CLICKED_NAME, CLICKED_GEMS, MESSAGE0, MESSAGE1,
		MESSAGE2, MESSAGE3;
		private final static int MESSAGES = 4;
	}

	/* Subpanels on this SidePanel. */
	private JPanel ctrlPanel;
	private JPanel clickedPanel;

	/* The sliders on panel. */
	private JSlider speedSlider;
	private JSlider zoomSlider;

	/* The check boxes on panel. */
	private JCheckBox followShipBox;
	private JCheckBox pauseBox;
	private JCheckBox pauseOnRescueBox;

	/** Constructor: a side panel of dimension (width, height). */
	public SidePanel(int width, int height) {
		super();

		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(BACKCOLOR);
		setPreferredSize(new Dimension(width, height));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		stats= new HashMap<>();

		ctrlPanel= new JPanel();
		addText(ctrlPanel, "Simulation speed");
		ctrlPanel.setBackground(BACKCOLOR);
		speedSlider= makeSlider(MINIMUM_SPEED, MAXIMUM_SPEED, INITIAL_SPEED);
		ctrlPanel.add(speedSlider);
		ctrlPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		add(ctrlPanel);

		addText(ctrlPanel, "Zoom");
		zoomSlider= makeSlider(MINIMUM_ZOOM, MAXIMUM_ZOOM, INITIAL_ZOOM);
		ctrlPanel.add(zoomSlider);

		followShipBox= new JCheckBox("Camera follows ship", false);
		followShipBox.setBackground(BACKCOLOR);
		followShipBox.setForeground(FORECOLOR);
		followShipBox.setFocusable(false);
		ctrlPanel.add(followShipBox);

		pauseBox= new JCheckBox("Pause", false);
		pauseBox.setBackground(BACKCOLOR);
		pauseBox.setForeground(FORECOLOR);
		pauseBox.setFocusable(false);
		pauseBox.setSelected(true);
		ctrlPanel.add(pauseBox);

		pauseOnRescueBox= new JCheckBox("Pause at rescue phase", false);
		pauseOnRescueBox.setBackground(BACKCOLOR);
		pauseOnRescueBox.setForeground(FORECOLOR);
		pauseOnRescueBox.setFocusable(false);
		ctrlPanel.add(pauseOnRescueBox);

		ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.PAGE_AXIS));
		add(ctrlPanel);
		
		addText(this, " ");
		addStat(this, StatName.SEED, "Seed: ", "");
		updateStat(StatName.SEED, null, FORECOLOR);
		addText(this, " ");
		addText(this, "LOG");
		for (int i= 0; i < StatName.MESSAGES; ++i) {
			addStat(this, StatName.valueOf("MESSAGE" + i), "", "");
		}
		addText(this, " ");
		addText(this, "CLICKED PLANET");
		clickedPanel= new JPanel();
		clickedPanel.setBackground(Color.BLACK);
		clickedPanel.setBorder(new EmptyBorder(3, 5, 3, 5));
		clickedPanel.setLayout(new BoxLayout(clickedPanel, BoxLayout.PAGE_AXIS));
		addStat(clickedPanel, StatName.CLICKED_NAME, "Name: ", "N/A");
		addStat(clickedPanel, StatName.CLICKED_GEMS, "Gems: ", "N/A");
		add(clickedPanel);
		addText(this, " ");
		addText(this, "MISSION CONTROL");
		addStat(this, StatName.SEARCH, "", "");
		addStat(this, StatName.RESCUE, "", "");
		addStat(this, StatName.PREVIOUS_NAME, "Previous planet: ", "N/A");
		addStat(this, StatName.FUEL_LEFT, "Fuel remaining: ", "N/A");
		updateStat(StatName.FUEL_LEFT, null, Color.GREEN);
		addText(this, " ");
		addText(this, "SCORE");
		addStat(this, StatName.FUEL_USED, "Fuel used for search: ", "N/A");
		updateStat(StatName.FUEL_USED, null, Color.YELLOW);
		addStat(this, StatName.SEARCH_SCORE, "Search score: ", "N/A");
		updateStat(StatName.SEARCH_SCORE, null, Color.YELLOW);
		addStat(this, StatName.GEMS, "Gems: ", "N/A");
		updateStat(StatName.GEMS, null, Color.CYAN);
		addStat(this, StatName.RESCUE_SCORE, "Rescue score: ", "N/A");
		updateStat(StatName.RESCUE_SCORE, null, Color.CYAN);
		addStat(this, StatName.SCORE, "Total score: ", "N/A");
		updateStat(StatName.SCORE, null, Color.GREEN);
	}

	/** Initialize this sidePanel to display seed seed. */
	public void init(long seed) {
		clearMessages();
		updateStat(StatName.SEED, Long.toString(seed), null);
		updateStat(StatName.MESSAGE0, "To start mission: File -> Start", Color.RED);
		updateStat(StatName.SEARCH, "", STATCOLOR);
		updateStat(StatName.RESCUE, "", STATCOLOR);
		updateStat(StatName.PREVIOUS_NAME, "N/A", STATCOLOR);
		updateStat(StatName.FUEL_USED, "N/A", Color.YELLOW);
		updateStat(StatName.SEARCH_SCORE, "N/A", Color.YELLOW);
		updateStat(StatName.GEMS, "N/A", Color.CYAN);
		updateStat(StatName.RESCUE_SCORE, "N/A", Color.CYAN);
		updateStat(StatName.SCORE, "N/A", Color.GREEN);
		updateStat(StatName.FUEL_LEFT, "N/A", Color.GREEN);
		resetClicked();
		pauseBox.setSelected(true);
		repaint();
	}
	
	/** Clear all messages. */
	public void clearMessages() {
		for (int i= 0; i < StatName.MESSAGES; ++i) {
			updateStat(StatName.valueOf("MESSAGE" + i), "", STATCOLOR);
		}
	}
	
	/** Add a message to the current list of messages.*/
	public void addMessage(String s, Color c) {
		for (int i= StatName.MESSAGES - 1; i > 0; --i) {
			Stat stat = stats.get(StatName.valueOf("MESSAGE" + (i-1)));
			updateStat(StatName.valueOf("MESSAGE" + i),
				stat.value, stat.label.getForeground());
		}
		updateStat(StatName.MESSAGE0, s, c);
	}
	
	/** Reset the text and color of the clicked planet panel. */
	public void resetClicked() {
		clickedPanel.setBackground(BACKCOLOR);
		updateStat(StatName.CLICKED_NAME, "N/A", STATCOLOR);
		updateStat(StatName.CLICKED_GEMS, "N/A", STATCOLOR);
	}
	
	/** Set the name of the clicked planet for p. The panel's background
	 * will be set to p's color. The text in the clicked panel will
	 * automatically update to black or white to improve visibility. */
	public void setClicked(Planet p) {
		Color c= p.color();
		clickedPanel.setBackground(c);
		if (0.299d*c.getRed() + 0.587d*c.getGreen() + 0.114d*c.getBlue() > 127d) {
			updateStat(StatName.CLICKED_NAME, p.name(), Color.BLACK);
			updateStat(StatName.CLICKED_GEMS, null, Color.BLACK);
		} else {
			updateStat(StatName.CLICKED_NAME, p.name(), Color.WHITE);
			updateStat(StatName.CLICKED_GEMS, null, Color.WHITE);
		}
	}
	
	/** Set the gems of the clicked planet to the given argument. */
	public void setClickedGems(String gems) {
		updateStat(StatName.CLICKED_GEMS, gems, null);
	}

	/** Append a new label into Container c with text t.
	 * This label cannot be updated later. */
	public static void addText(Container c, String t) {
		JLabel label= new JLabel(t);
		label.setFont(LABEL_FONT);
		label.setForeground(FORECOLOR);
		c.add(label);
	}

	/** Return a new horizontal Slider with the given parameters. */
	private JSlider makeSlider(int min, int max, int init) {
		JSlider slider= new JSlider(JSlider.HORIZONTAL, min, max, init);
		slider.setBackground(BACKCOLOR);
		int range= max - min;
		slider.setMajorTickSpacing(range / 10);
		slider.setMinorTickSpacing(range / 5);
		slider.setPaintTicks(true);
		slider.setPaintLabels(false);
		slider.setFocusable(false);
		return slider;
	}

	/** Add listener to this SidePanel's speed slider. */
	public void addSpeedSliderListener(ChangeListener listener) {
		speedSlider.addChangeListener(listener);
	}

	/** Add listener to this SidePanel's zoom slider. */
	public void addZoomSliderListener(ChangeListener listener) {
		zoomSlider.addChangeListener(listener);
	}

	/** Add listener to this SidePanel's follow ship check box. */
	public void addFollowShipListener(ItemListener listener) {
		followShipBox.addItemListener(listener);
	}

	/**  Add listener to this SidePanel's pause check box. */
	public void addPauseListener(ItemListener listener) {
		pauseBox.addItemListener(listener);
	}

	/** Set this SidePanel's pause check box to checked or unchecked. */
	public void setPauseBox(boolean checked) {
		pauseBox.setSelected(checked);
	}

	/** Add listener to this SidePanel's pause on return check box. */
	public void addPauseOnReturnListener(ItemListener listener) {
		pauseOnRescueBox.addItemListener(listener);
	}

	/** Append a statistic to display on c. The number 
	 * displayed can be updated later using the specified StatName.
	 * Precondition: no stat for sn exists. */
	public void addStat(Container c, StatName sn, String name, String value) {
		if (stats.containsKey(sn)) {
			throw new IllegalArgumentException(sn + " already exists!");
		}
		Stat stat= new Stat(name, value);
		stats.put(sn, stat);
		c.add(stat.label);
	}

	/** Update existing statistic sn to display string s and color c.
	 * If s is null, don't change the value.
	 * If c is null, don't change the color.
	 * Precondition: sn already corresponds to a stat. */
	public void updateStat(StatName sn, String s, Color c) {
		Stat stat = stats.get(sn);
		if (stat == null)
			throw new IllegalArgumentException("Uninitialized stat " + sn);
		if (s != null)
			stat.setValue(s);
		if (c != null)
			stat.setColor(c);
	}

	/** An instance is a statistic displayed in this SidePanel. */
	private static class Stat {

		private String name; // the first block of text displayed for this stat 
		private String value; // the second block of text displayed for this stat
		private JLabel label; // the JLabel used to display this stat

		/** Constructor: a stat with name n and value v. */
		public Stat(String n, String v) {
			name= n;
			value= v;
			label= new JLabel(name + value);
			label.setFont(STAT_FONT);
			label.setForeground(STATCOLOR);
		}

		/** Change this stat's value to v. */
		public void setValue(String v) {
			value= v;
			label.setText(name + value);
		}

		/** Change this stat's color to c. */
		public void setColor(Color c) {
			label.setForeground(c);
		}
	}
}

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import controllers.Controller;

import static gui.SidePanel.StatName.*;
import models.Model;
import models.Model.Phase;
import models.Node;

/**
 * An instance is a graphical representation of a Planet X game.
 */
@SuppressWarnings("serial")
public class GUI extends JFrame {

	/* Define buffers to base initial interface drawing on */
	public static final int X_BUFFER= 100;
	public static final int Y_BUFFER= 50;

	/* Minimum size of the drawing board screen */
	public static final int DRAWING_BOARD_WIDTH_MIN= 400;
	public static final int DRAWING_BOARD_HEIGHT_MIN= 400;

	/* Dynamic based on the screen size the user has */
	public static final int DRAWING_BOARD_WIDTH;
	public static final int DRAWING_BOARD_HEIGHT;

	/* Two panels aside from the drawing board (fixed dimensions) */
	public static final int UPDATE_PANEL_HEIGHT= 100;
	public static final int SIDE_PANEL_WIDTH= 300;

	/* How long to wait for an old renderer to terminate */
	private static final int RESET_TIMEOUT= 3;
	private static final TimeUnit RESET_TIMEOUT_UNITS= TimeUnit.SECONDS;

	/* Set the (width, height) based on user's screen size */
	static {
		Dimension s= Toolkit.getDefaultToolkit().getScreenSize();
		DRAWING_BOARD_WIDTH= s.width - SIDE_PANEL_WIDTH - 2 * X_BUFFER;
		DRAWING_BOARD_HEIGHT= (int) (s.height * 0.8) - UPDATE_PANEL_HEIGHT
			- 2 * Y_BUFFER;
	}

	/* Various panels of the GUI */
	private SpacePanel spacePanel;
	private SidePanel sidePanel;
	private TopMenu menuBar;

	/* The model that this GUI is displaying. */
	private Model model;

	/* The controller of this game */
	private Controller ctrlr;

	/* True iff the model has entered the rescue phase. */
	private boolean rescuePhase;

	/* True iff the GUI should pause when ending the search stage. */
	private boolean pauseSearchEnds;

	/* Simulation speed factor; 1 = normal speed, 2 = 2x speed, etc. */
	private int simSpeed;

	/* iff true, a Renderer will continue to run */
	private boolean running;

	/* iff true, a Renderer will not update the model */
	private boolean paused;

	/* The current renderer for this GUI. */
	private Renderer renderer;

	/* The currently selected Node. */
	private Node clicked;

	/** Constructor: a paused GUI that displays an empty SpacePanel.*/
	public GUI() {
		super("Planet X");
		setMinimumSize(new Dimension(SIDE_PANEL_WIDTH + DRAWING_BOARD_WIDTH_MIN,
			UPDATE_PANEL_HEIGHT + DRAWING_BOARD_HEIGHT_MIN));
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationByPlatform(true);

		spacePanel= new SpacePanel(DRAWING_BOARD_WIDTH, DRAWING_BOARD_HEIGHT);
		sidePanel= new SidePanel(SIDE_PANEL_WIDTH, DRAWING_BOARD_HEIGHT);
		menuBar= new TopMenu();
		getContentPane().add(spacePanel, BorderLayout.CENTER);
		getContentPane().add(sidePanel, BorderLayout.WEST);
		setJMenuBar(menuBar);
		addKeyListener(spacePanel.spacePanelCameraMover());
		spacePanel.callWhenClicked(nodeClicked);

		simSpeed= SidePanel.INITIAL_SPEED;

		// connect listeners
		sidePanel.addSpeedSliderListener(
			e -> simSpeed= ((JSlider) e.getSource()).getValue());
		sidePanel.addFollowShipListener(e -> spacePanel
			.setFollowShip(e.getStateChange() == ItemEvent.SELECTED));
		sidePanel.addZoomSliderListener(
			e -> spacePanel.setZoom(((JSlider) e.getSource()).getValue()));
		sidePanel.addPauseListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
				pause();
			else
				unpause();
		});
		sidePanel.addPauseOnReturnListener(
			e -> pauseSearchEnds = e.getStateChange() == ItemEvent.SELECTED);

		pack();
		validate();
		repaint();
		setVisible(true);
	}

	/** An instance animates a Planet X game. */
	private class Renderer extends SwingWorker<Void, Void> {
		@Override
		protected Void doInBackground() {
			running= true;
			long prevTime= System.nanoTime();
			long deltaTime= 0;
			while (running) {
				if (!paused) {
					long time= System.nanoTime();
					deltaTime += time - prevTime;
					prevTime= time;

					while (deltaTime >= (Controller.TICKTIME * 1e6)) {
						deltaTime -= (Controller.TICKTIME * 1e6);
						for (int i= 0; i < simSpeed; ++i)
							ctrlr.update();
					}
				} else {
					prevTime= System.nanoTime();
				}

				publish((Void) null);
			}

			render();
			return null;
		}

		@Override
		protected void process(List<Void> chunks) {
			if (running) render();
		}
	};

	/** Render the current state of the game. */
	private void render() {
		spacePanel.update();
		updateStats();
		updateClickedStats();
	}

	/* The function to run when a Node is clicked. */
	private Consumer<Node> nodeClicked = n -> {
		clicked= n;
		if (n != null) {
			sidePanel.setClicked(spacePanel.getPlanet(n));
		} else {
			sidePanel.resetClicked();
		}
		updateClickedStats();
	};

	/** Update the displayed stats for the clicked Node. */
	private void updateClickedStats() {
		if (clicked != null && rescuePhase) {
			sidePanel.setClickedGems(Integer.toString(clicked.gems()));
		}
		sidePanel.repaint();
	}

	/** Update the current stats of the game. */
	private void updateStats() {
		sidePanel.updateStat(PREVIOUS_NAME, model.shipNode().name(), null);
		sidePanel.updateStat(SCORE, Integer.toString(model.score()), null);
		if (rescuePhase) {
			String gems= Integer.toString(model.gems());
			sidePanel.updateStat(GEMS, gems, null);
			sidePanel.updateStat(RESCUE_SCORE, gems, null);
			int fuel= model.fuelRemaining();
			sidePanel.updateStat(FUEL_LEFT, Integer.toString(fuel),
				fuel > 0 ? Color.GREEN : Color.RED);
		} else {
			sidePanel.updateStat(FUEL_USED, Integer.toString(model.fuelUsed()), null);
			sidePanel.updateStat(SEARCH_SCORE, Integer.toString(model.score()), null);
		}
	}

	/** Initialize this GUI to display the game with m and c,
	 * overwriting any previous GUI. */
	public void init(Controller c, Model m) {
		if (!m.nodes().contains(clicked))
			clicked= null;

		model= m;
		ctrlr= c;

		running= false;
		paused= true;
		rescuePhase= false;

		spacePanel.init(m);
		sidePanel.init(m.seed());

		if (renderer != null) {
			running= false;
			try {
				renderer.get(RESET_TIMEOUT, RESET_TIMEOUT_UNITS);
			} catch (TimeoutException e) {
				System.err.println("error: old GUI not responding!\n"
					+ "You may want to close this program.");

			} catch (Exception e) {}
		}
		renderer= new Renderer();
		renderer.execute();
	}

	/** Pause this GUI, preventing it from updating to the game's state. */
	public void pause() {
		paused= true;
		sidePanel.setPauseBox(true);
	}

	/** Unpause this GUI, allowing it to update and reflect the game's state.
	 */
	public void unpause() {
		paused= false;
		sidePanel.setPauseBox(false);
	}

	/** Signal that stage s has begun. */
	public void beginStage(Phase s) {
		switch (s) {
		case SEARCH:
			sidePanel.clearMessages();
			sidePanel.updateStat(SEARCH, "Search phase underway...", Color.RED);
			unpause();
			break;

		case RESCUE:
			rescuePhase= true;
			sidePanel.updateStat(RESCUE, "Rescue phase underway...", Color.RED);
			break;

		case NONE:
		}
	}

	/** Signal that phase s has ended. */
	public void endPhase(Phase s) {
		switch (s) {
		case SEARCH:
			if (model.searchSucceeded()) {
				sidePanel.updateStat(SEARCH, "Search phase complete!", Color.GREEN);
			} else {
				sidePanel.updateStat(SEARCH, "Search phase failed!", Color.RED);
				sidePanel.updateStat(SEARCH_SCORE, "0", Color.RED);
				sidePanel.updateStat(SCORE, "0", Color.RED);
			}
			if (pauseSearchEnds) pause();
			break;

		case RESCUE:
			if (model.rescueSucceeded()) {
				sidePanel.updateStat(RESCUE, "Rescue phase complete!", Color.GREEN);
			} else {
				sidePanel.updateStat(RESCUE, "Rescue phase failed!", Color.RED);
				sidePanel.updateStat(RESCUE_SCORE, "0", Color.RED);
				sidePanel.updateStat(SCORE, "0", Color.RED);
				pause();
			}
			break;

		default:
		}
	}

	/** Add listener to the top menu's "Start" item. */
	public void addStartListener(ActionListener listener) {
		menuBar.getStartItem().addActionListener(listener);
	}
	
	/** Enable or disable the "Start" item. */
	public void setStartEnabled(boolean enabled) {
		menuBar.getStartItem().setEnabled(enabled);
	}

	/** Add listener to the top menu's "Reset" item. */
	public void addResetListener(ActionListener listener) {
		menuBar.getResetItem().addActionListener(listener);
	}

	/** Add an listener to the top menu's "New Map" item. */
	public void addNewMapListener(ActionListener listener) {
		menuBar.getNewMapItem().addActionListener(listener);
	}
	
	/** Print error message s in the SidePanel. */
	public void errprint(String s) {
		sidePanel.addMessage(s, Color.RED);
	}
	
	/*** Print regular message s in the SidePanel. */
	public void outprint(String s) {
		sidePanel.addMessage(s, Color.GREEN);
	}
}

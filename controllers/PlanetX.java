package controllers;

import java.util.Random;
import java.util.function.Supplier;

import models.Board;
import models.Controllable.AbortException;
import models.Controllable.SolutionFailedException;
import models.PlanetXModel;
import static models.Model.Phase.*;
import views.*;
import student.MySpaceship;

/** An instance runs the game, linking the state to the user interface. */
public class PlanetX implements Controller {

	/* Tunable map generation parameters. */
	public static final int MIN_NODES= 5;
	public static final int MAX_NODES= 750;
	public static final int MIN_GEMS= 0;
	public static final int MAX_GEMS= 5000;
	public static final int WIDTH= 4096;
	public static final int HEIGHT= 4096;

	protected long seed; // The seed used to generate this game.
	protected Spaceship spaceship; // The solution implementing this game. 

	protected boolean started; // True iff this game has started.
	protected boolean failed; // True iff this game's solution failed.

	protected PlanetXModel model; // The controllable model for this game.
	protected View view; // The view for this game.

	private static final Random RNG= new Random(); // used for random seed generation.

	/* A Spaceship supplier used to get new Spaceships (e.g. for restarting). */
	private static Supplier<Spaceship> ships;

	/* Separate thread used to prevent the model from blocking the view */
	protected ModelThread thread;

	/** Constructor: a game with seed s, spaceship sp, and View v. */
	public PlanetX(long s, Spaceship sp, View v) {
		view= v;
		ships= () -> {
			try {
				return sp.getClass().newInstance();
			} catch (Exception e) {
				System.err.println("fatal error: failed to create new Spaceship");
				System.exit(1);
			}
			return null;
		};

		init(s, sp);
	}

	/** Initialize the game with seed s and spaceship sp. If this game has already
	 * been initialized, this overwrites the previous initialization. */
	protected void init(long s, Spaceship sp) {
		// stop the old thread, if it exists
		if (thread != null)	thread.kill();

		started= false;
		failed= false;

		seed= s;
		spaceship= sp;
		Board b= new Board.BoardBuilder().size(WIDTH, HEIGHT).seed(s)
			.nodeBounds(MIN_NODES, MAX_NODES).gemBounds(MIN_GEMS, MAX_GEMS).build();
		model= new PlanetXModel(b);
		thread= new ModelThread();
		view.init(this, model);
	}

	@Override public void newGame(String str) {
		if (str == null) return;
		try {
			init(Long.valueOf(str), ships.get());
		} catch (NumberFormatException ex) {
			init(RNG.nextLong(), ships.get());
		}
	}

	@Override public void newGame(long s) {
		init(s, ships.get());
	}

	@Override public void reset() {
		init(seed, ships.get());
	}

	@Override public void start() {
		if (started) {
			view.errprintln("Game has already started");
			return;
		}
		started= true;
		thread.start();
	}
	
	/** Take the appropriate actions when a solution fails. */
	private void fail(SolutionFailedException e) {
		failed= true;
		view.endStage(model.phase());
		view.errprintln("Solution failed with reason: " + e.getMessage());
		view.endGame(model.score());
	}

	@Override public synchronized void update() {
		try {
			model.update(TICKTIME);
		} catch (SolutionFailedException e) {
			if (!failed) {
				fail(e);
			}
		}
	}

	/**  An instance runs a model in a separate thread.
	 * It can be killed by calling kill(). */
	protected class ModelThread extends Thread {
		/** Run through the game until it finishes, fails, or is aborted. */
		@Override public void run() {
			try {
				search();
				rescue();
				view.endGame(model.score());
			} catch (SolutionFailedException e) {
				fail(e);
			} catch (AbortException e) {}
		}

		/** Kill this model thread by aborting the underlying model. */
		public void kill() {
			model.abort();
		}
	}

	/** Run ship's search method.
	 * Throw a SolutionFailedException if the search  fails. */
	protected void search() throws SolutionFailedException {
		view.beginStage(SEARCH);
		spaceship.search(model.beginSearchPhase());
		boolean success= model.endSearchPhase();
		view.endStage(SEARCH);
		if (success) return;

		throw new SolutionFailedException(
			"Your solution to search() returned at the wrong location.");
	}

	/** Run ship's rescue() method.
	 * Throw a SolutionFailedException if the rescue fails. */
	protected void rescue() throws SolutionFailedException {
		view.beginStage(RESCUE);
		spaceship.rescue(model.beginRescuePhase());
		boolean success= model.endRescuePhase();
		view.endStage(RESCUE);
		if (success) return;

		throw new SolutionFailedException(
			"Your solution to rescue() returned at the wrong location.");
	}

	@Override public boolean searchSucceeded() {
		return model.searchSucceeded();
	}

	@Override public boolean rescueSucceeded() {
		return model.rescueSucceeded();
	}

	/** Run PlanetX. Without any options, this defaults to an instance
	 * with a random seed using a GUI view.
	 * 
	 * -s, --seed=SEED  Run this game using the seed SEED 
	 * -g, --gui        Use the GUI (graphical user interface) view 
	 * -c, --cli        Use the CLI (command-line interface) view
	 * -b, --benchmark  Use a benchmark view, which will give statistics
	 *                  of your solution when run on multiple seeds
	 * -q, --quiet      Use a quiet view, which outputs nothing. */
	public static void main(String[] argv) {
		// parse arguments
		View view= null;
		Long seed= null;
		for (int i= 0; i < argv.length; ++i) {
			try {
				if (argv[i].equals("-g") || argv[i].equals("--gui")) {
					if (view != null) {
						System.err.println(
							"Error: cannot specify more than " + "one view option");
						return;
					} else {
						view = new GUIView();
					}
				} else if (argv[i].equals("-c") || argv[i].equals("--cli")) {
					if (view != null) {
						System.err.println(
							"Error: cannot specify more than " + "one view option");
						return;
					} else {
						view= new CLIView();
					}
				} else if (argv[i].equals("-q") || argv[i].equals("--quiet")) {
					if (view != null) {
						System.err.println(
							"Error: cannot specify more than " + "one view option");
						return;
					} else {
						view= new QuietView();
					}
				} else if (argv[i].equals("-b") || argv[i].equals("--benchmark")) {
					if (view != null) {
						System.err.println(
							"Error: cannot specify more than " + "one view option");
						return;
					} else {
						view= new BenchmarkView();
					}
				} else if (argv[i].length() > 7
					&& argv[i].substring(0, 7).equals("--seed=")) {
					seed = Long.parseLong(argv[i].substring(7));
				} else if (argv[i].equals("-s")) {
					if (i + 1 < argv.length) {
						++i;
						seed= Long.parseLong(argv[i]);
					} else {
						System.err.println("Error: no seed specified.");
						return;
					}
				} else {
					System.err.println("Error: invalid argument \"" + argv[i] + '"');
					return;
				}
			} catch (NumberFormatException e) {
				System.err.println("Invalid seed \"" + argv[i] + '"');
				return;
			}
		}
		if (seed == null)
			seed = RNG.nextLong(); // avoid burning RNG; only generate if needed

		// begin the game with the appropriate parameters
		if (view == null) view = new GUIView();
		new PlanetX(seed, new MySpaceship(), view);
	}
}

package views;

import java.util.Random;

import controllers.Controller;
import models.Model;
import models.Model.Phase;

/** An instance is a view of the Space Gems Game that immediately starts N 
 * games and outputs statistics about the games.*/
public class BenchmarkView implements View {

	public static final int N= 150; // try 150 trials
	private static final Random R= new Random(42); // choose consistent seeds

	private ViewThread thread; // thread for current game
	private Controller ctrlr; // controller for this game
	private Model model;
	private int i= 0; // current run number
	private int[] scores= new int[N]; // scores for each run
	private double[] times= new double[N]; // times for each run, in seconds
	private long start; // time when starting the current run

	@Override
	public void init(Controller c, Model m) {
		ctrlr= c;
		model= m;
		thread= new ViewThread(c, m);
		thread.run();
	}

	@Override
	public void beginStage(Phase s) {
		if (s == Phase.SEARCH)
			start= System.nanoTime();
	}

	@Override
	public void endStage(Phase s) {}

	@Override
	public void endGame(int score) {
		thread.running= false;
		times[i]= toSeconds(System.nanoTime() - start);
		scores[i]= score;
		outprintln("Finished run " + i + " with seed " + model.seed() + ", score "
			+ score + ", and time " + times[i]);
		++i;
		if (i >= N) {
			outprintln("");
			scoreStats();
			outprintln("");
			timeStats();
		} else {
			ctrlr.newGame(Long.toString(R.nextLong()));
		}
	}

	/** Print the mean, std. deviation, min, and max of the scores. */
	public void scoreStats() {
		double mean = 0d;
		int min= Integer.MAX_VALUE;
		int max= Integer.MIN_VALUE;
		for (int i : scores) {
			mean += i;
			if (min > i)
				min= i;
			if (max < i)
				max= i;
		}
		mean /= scores.length;

		double variance= 0d;
		for (int i : scores) {
			variance += (i - mean) * (i - mean);
		}
		double sd= Math.sqrt(variance / (scores.length - 1));

		outprintln("Distribution of scores:");
		outprintln("  Mean: " + mean);
		outprintln("  Standard deviation: " + sd);
		outprintln("  Min: " + min);
		outprintln("  Max: " + max);
	}

	/** Convert time ns in nanoseconds to seconds. */
	private static double toSeconds(long ns) {
		return ns / 1e9;
	}

	/** Print the mean, std. deviation, min, and max of the times. */
	public void timeStats() {
		double mean= 0d;
		double min= Long.MAX_VALUE;
		double max= Long.MIN_VALUE;
		for (double d : times) {
			mean += d;
			if (min > d)
				min= d;
			if (max < d)
				max= d;
		}
		mean /= times.length;

		double variance = 0d;
		for (double d : times) {
			variance += (d - mean) * (d - mean);
		}
		double sd = Math.sqrt(variance / (times.length - 1));

		outprintln("Distribution of times:");
		outprintln("  Mean: " + mean);
		outprintln("  Standard deviation: " + sd);
		outprintln("  Min: " + min);
		if (max >= 10d) {
			errprintln("  Max: " + max);
			errprintln("\nWARNING: At least one of your runs exceeded 15 seconds.");
		} else {
			outprintln("  Max: " + max);
		}
	}

	private class ViewThread extends Thread {
		/* The controller and model of the current game. */
		private Controller ctrlr;

		/* True iff the current game is still running. */
		private boolean running;

		public ViewThread(Controller c, Model m) {
			ctrlr= c;
		}

		/** Run a single game, then prompts the user for further action. */
		@Override
		public void run() {
			running= true;
			ctrlr.start();
			while (running) {
				ctrlr.update();
			}
		}
	}
}

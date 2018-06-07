package views;

import models.Model;
import static models.Model.Phase;

import controllers.Controller;

/**  An instance is minimum requirements of a view of the Space Gem game. */
public interface View {

	/** Initialize the view with the given Controller and Model. If the view
	 * has already been initialized, this will simply overwrite the previous
	 * initialization. */
	public void init(Controller c, Model m);

	/** Begin a particular stage. The view will autonomously update and display
	 * the model until told to stop updating this particular stage. */
	public void beginStage(Phase s);

	/** Signal the end of a particular stage; the view will stop updating,
	 * leaving a static displayed state.
	 * Precondition: this stage has begun. */
	public void endStage(Phase s);

	/** Signal that the game has ended with score score. */
	public void endGame(int score);

	/** Print s as a regular message. */
	public default void outprint(String s) {
		System.out.print(s);
		System.out.flush();
	}

	/** Print s as an error message. */
	public default void errprint(String s) {
		System.err.print(s);
		System.err.flush();
	}

	/** Print s terminated by a newline as a regular message. */
	public default void outprintln(String s) {
		outprint(s + '\n');
	}

	/** Print s terminated by a newline as an error message. */
	public default void errprintln(String s) {
		errprint(s + '\n');
	}
}

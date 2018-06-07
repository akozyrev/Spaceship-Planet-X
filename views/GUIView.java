package views;

import javax.swing.JOptionPane;

import controllers.Controller;
import gui.GUI;
import models.Model;
import models.Model.Phase;

/** An instance represents a graphical view. */
public class GUIView implements View {

	private GUI gui; // The GUI rendering this game
	private Controller ctrlr; // The controller of this game's model

	/** Constructor: a GUIView with a blank GUI. */
	public GUIView() {
		gui = new GUI();
		addTopMenuListeners();
	}

	/** Add listeners to the top menu to relay user input to the presenter. */
	private void addTopMenuListeners() {
		gui.addStartListener(e -> ctrlr.start());
		gui.addResetListener(e -> ctrlr.reset());
		gui.addNewMapListener(e -> ctrlr.newGame(JOptionPane.showInputDialog(
			"Enter either a valid seed, or anything else to get a random seed.")));
	}

	@Override
	public void init(Controller c, Model m) {
		ctrlr= c;
		gui.setStartEnabled(true);
		gui.init(c, m);
		outprintln("Seed: " + m.seed());
	}

	@Override
	public void beginStage(Phase s) {
		gui.beginStage(s);
		gui.setStartEnabled(false);
	}

	@Override
	public void endStage(Phase s) {
		gui.endPhase(s);
		switch (s) {
		case SEARCH:
			if (ctrlr.searchSucceeded())
				outprintln("Search phase ended successfully!");
			break;

		case RESCUE:
			if (ctrlr.rescueSucceeded())
				outprintln("Rescue phase ended successfully!");
			break;

		default:
		}
	}

	@Override
	public void endGame(int score) {
		gui.pause();
		if (ctrlr.rescueSucceeded())
			outprintln("Score: " + score);
		else
			errprintln("Score: " + score);
	}
	
	@Override
	public void errprint(String s) {
		View.super.errprint(s);
		gui.errprint(s);
	}
	
	@Override
	public void outprint(String s) {
		View.super.outprint(s);
		gui.outprint(s);
	}
}

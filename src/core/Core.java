package core;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Core {
	
	public static final boolean debug = true;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final double startTime = System.nanoTime();
	    try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // Sets the look and feel of the UI
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	    Parser parser = new Parser(); // Constructs the parser object
		new RouteFinderGUI(parser).setVisible(true); // Creates the GUI passing the parser as an argument
		final double duration = System.nanoTime() - startTime;
		System.out.println("Run Time: " + duration/1000000000 + " seconds");
	}
	
	public static void debug(String message){
		if(debug){
			System.out.println(message);
		}
	}
	
	public static void debug(StringBuilder message){
		if(debug){
			System.out.println(message);
		}
	}
	
	public static void debug(int message){
		if(debug){
			System.out.println(message);
		}
	}

}

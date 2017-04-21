package core;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * The central class with the core methods for initialising other classes.
 * @author Jacob Pennington
 *
 */
public class Core { // The central class with the core methods for initialising other classes
	
	public static final boolean debug = false; // A simple flag to turn debug messages on and off

	/**
	 * The root method all others are called from.
	 * <p>
	 * When the program runs, this method is the first to be called. 
	 * It creates the parser object
	 * and the GUI.
	 * @param args Command line arguments when the program is run from the command line.
	 */
	public static void main(String[] args) {
		final double startTime = System.nanoTime();
	    try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName()); // Sets the look and feel of the UI
		} catch (ClassNotFoundException 
				| InstantiationException 
				| IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	    Parser parser = new Parser(); // Constructs the parser object
		new RouteFinderGUI(parser).setVisible(true); // Creates the GUI passing the parser as an argument
		final double duration = System.nanoTime() - startTime;
		System.out.println("Run Time: " +
		duration/1000000000 + " seconds");
	}
	
	/**
	 * Prints the passed message to the console/output file.
	 * @param message The message to print
	 */
	public static void debug(String message){
		if(debug){
			System.out.println(message);
		}
	}
	
	/**
	 * Prints the passed message to the console/output file.
	 * @param message The message to print
	 */
	public static void debug(StringBuilder message){
		if(debug){
			System.out.println(message);
		}
	}
	
	/**
	 * Prints the passed message to the console/output file.
	 * @param message The message to print
	 */
	public static void debug(int message){
		if(debug){
			System.out.println(message);
		}
	}
}

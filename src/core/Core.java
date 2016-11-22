package core;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Core {
	
	public static final boolean debug = true;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final double startTime = System.nanoTime();
	    try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Parser parser = new Parser();
		new RouteFinderGUI(parser).setVisible(true);
		final double duration = System.nanoTime() - startTime;
		System.out.println("Run Time: " + duration/1000000000 + " seconds");
	}
	
	public static void debug(String message){
		if(debug){
			System.out.println(message);
		}
	}

}

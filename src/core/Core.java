package core;

public class Core {
	
	public static final boolean debug = true;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final double startTime = System.nanoTime();
		new Parser();
		final double duration = System.nanoTime() - startTime;
		System.out.println("Run Time: " + duration/1000000000 + " seconds");
	}
	
	public static void debug(String message){
		if(debug){
			System.out.println(message);
		}
	}

}

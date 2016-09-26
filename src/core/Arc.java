package core;

public class Arc {
	
	private Vertex start;
	private Vertex end;
	public String id;
	private int weight;
	
	public Arc(Vertex start, Vertex end, String id){
		this.start = start;
		this.end = end;
		this.id = id;
	}
}

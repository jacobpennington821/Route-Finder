package core;

import java.util.HashMap;

public class Graph {
	
	private HashMap<String,Vertex> vertexMap = new HashMap<String,Vertex>();
	private HashMap<String,Arc> arcMap = new HashMap<String,Arc>();
	
	public Graph(HashMap<String,Vertex> vertexMap, HashMap<String,Arc> arcMap){
		this.vertexMap = vertexMap;
		this.arcMap = arcMap;
	}

}

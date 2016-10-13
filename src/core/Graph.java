package core;

import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
	
	public static final int EARTHDIAMETER = 12742;
	
	private HashMap<String,Vertex> vertexMap = new HashMap<String,Vertex>();
	private HashMap<String,Arc> arcMap = new HashMap<String,Arc>();
	
	public Graph(HashMap<String,Vertex> vertexMap, HashMap<String,Arc> arcMap){
		this.vertexMap = vertexMap;
		this.arcMap = arcMap;
		this.calculateWeights();
		for(String key : vertexMap.keySet()){
			System.out.println("Id: " + vertexMap.get(key).getId());
			System.out.println("Lat: " + vertexMap.get(key).getLat() + ", Lon: " + vertexMap.get(key).getLon());
			for(int i = 0; i < vertexMap.get(key).arcList.size(); i++){
				System.out.println("  " + vertexMap.get(key).arcList.get(i) + " - " + arcMap.get(vertexMap.get(key).arcList.get(i)).getWeight());
			}
		}
		this.shortestRoute("33071154", "2062355426");

	}
	
	public Graph shortestRoute(String source, String destination){
		ArrayList<String> unsettledVertexes = new ArrayList<String>();
		ArrayList<String> settledVertexes = new ArrayList<String>();
		vertexMap.get(source).setDistanceFromSource(0);
		for(int i = 0; i < vertexMap.get(source).arcList.size(); i++){
			if(source.equals(arcMap.get(vertexMap.get(source).arcList.get(i)).getStart())){
				vertexMap.get(arcMap.get(vertexMap.get(source).arcList.get(i)).getEnd()).setDistanceFromSource(arcMap.get(vertexMap.get(source).arcList.get(i)).getWeight());
				unsettledVertexes.add(arcMap.get(vertexMap.get(source).arcList.get(i)).getEnd());
			}
			else{
				if(source.equals(arcMap.get(vertexMap.get(source).arcList.get(i)).getEnd())){
					vertexMap.get(arcMap.get(vertexMap.get(source).arcList.get(i)).getStart()).setDistanceFromSource(arcMap.get(vertexMap.get(source).arcList.get(i)).getWeight());
					unsettledVertexes.add(arcMap.get(vertexMap.get(source).arcList.get(i)).getStart());
				}
				else{
					System.out.println("Node is not end or start of arc: " + source + " is not: " + arcMap.get(vertexMap.get(source).arcList.get(i)).getStart() + ", or: " + arcMap.get(vertexMap.get(source).arcList.get(i)).getEnd());
				}
			}
			//vertexMap.get(source).arcList.get(i)
		}
		for(int jeff = 0; jeff < unsettledVertexes.size(); jeff++){
			System.out.println(unsettledVertexes.get(jeff));
		}
		
		// Need two maps, visited and unvisited
		return null;
	}
	
	private void calculateWeights(){
		for(String key : arcMap.keySet()){
			Arc tempArc = arcMap.get(key);
			double lat1 = Math.toRadians(vertexMap.get(tempArc.getStart()).getLat());
			double lon1 = Math.toRadians(vertexMap.get(tempArc.getStart()).getLon());
			double lat2 = Math.toRadians(vertexMap.get(tempArc.getEnd()).getLat());
			double lon2 = Math.toRadians(vertexMap.get(tempArc.getEnd()).getLon());
			double temp = Math.pow(Math.sin((lat2 - lat1) / 2), 2) + (Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin((lon2 - lon1) / 2), 2));
			double distance = EARTHDIAMETER * Math.asin(Math.sqrt(temp));
			arcMap.get(key).setWeight(distance);
			System.out.println("Dist: " + distance);
			//arcMap.get(key).start
			//arcMap.get(key).setWeight(0);
		}
	}

}

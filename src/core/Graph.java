package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Graph {
	
	public static final int EARTHDIAMETER = 12742;
	public static final double KM_IN_MILE = 1.61;
	
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
		this.shortestRoute("2476991925", "2062355456");

	}
	
	public Graph shortestRoute(String source, String destination){
		System.out.println("-------------------------------- BEGIN DIJKSTRA'S ----------------------------");
		Set<String> unsettledVertexes = new HashSet<String>();
		Set<String> settledVertexes = new HashSet<String>();
		vertexMap.get(source).setDistanceFromSource(0);
		vertexMap.get(source).setPreviousVertex(null);
		unsettledVertexes.add(source);
		while(unsettledVertexes.size() > 0){
			String workingVertex = getVertexWithLowestDistance(unsettledVertexes);
			if(workingVertex.equals(destination)){
				String previousVertex = vertexMap.get(workingVertex).getPreviousVertex();
				System.out.println("ROUTE FOUND:");
				while(previousVertex != null){
					System.out.println("  " + vertexMap.get(previousVertex).getPreviousVertex());
					previousVertex = vertexMap.get(previousVertex).getPreviousVertex();
				}
				return null;
			}
			System.out.println("Using Vertex: " + workingVertex);
			unsettledVertexes.remove(workingVertex);
			settledVertexes.add(workingVertex);
			for(int i = 0; i < vertexMap.get(workingVertex).arcList.size(); i++){ // Explore all arcs connected to the vertex
				String arc = vertexMap.get(workingVertex).arcList.get(i);
				System.out.println("Exploring Arc: " + arc);
				System.out.println("  Start: " + arcMap.get(arc).getStart());
				System.out.println("  End: " + arcMap.get(arc).getEnd());
				if(workingVertex.equals(arcMap.get(arc).getStart()) && !settledVertexes.contains(arcMap.get(arc).getEnd())){
					double workingDistance = arcMap.get(arc).getWeight() + vertexMap.get(workingVertex).getDistanceFromSource();
					if(workingDistance < vertexMap.get(arcMap.get(arc).getEnd()).getDistanceFromSource()){
						System.out.println("  Modifying End Vertex - Distance From Source = " + workingDistance);
						vertexMap.get(arcMap.get(arc).getEnd()).setDistanceFromSource(workingDistance); // Assign working values to vertexes
						vertexMap.get(arcMap.get(arc).getEnd()).setPreviousVertex(workingVertex);
						unsettledVertexes.add(arcMap.get(arc).getEnd());
					}else{
						System.out.println("  Leaving End Vertex - Distance From Source = " + vertexMap.get(arcMap.get(arc).getEnd()).getDistanceFromSource() + ", Rejected Distance = " + workingDistance);
					}
				}
				else{
					if(workingVertex.equals(arcMap.get(arc).getEnd()) && !settledVertexes.contains(arcMap.get(arc).getStart())){
						double workingDistance = arcMap.get(arc).getWeight() + vertexMap.get(workingVertex).getDistanceFromSource();
						if(workingDistance < vertexMap.get(arcMap.get(arc).getStart()).getDistanceFromSource()){
							System.out.println("  Modifying Start Vertex - Distance From Source = " + workingDistance);
							vertexMap.get(arcMap.get(arc).getStart()).setDistanceFromSource(workingDistance);
							unsettledVertexes.add(arcMap.get(arc).getStart());
							vertexMap.get(arcMap.get(arc).getStart()).setPreviousVertex(workingVertex);
						}else{
							System.out.println("  Leaving Start Vertex - Distance From Source = " + vertexMap.get(arcMap.get(arc).getStart()).getDistanceFromSource() + ", Rejected Distance = " + workingDistance);
						}
					}
					else{
						System.out.println("Dead End: " + workingVertex);
					}
				}
				//vertexMap.get(source).arcList.get(i)
			}
		}
		for(String vertexId : unsettledVertexes){
			System.out.println(vertexId);
		}
		
		// Need two maps, visited and unvisited
		return null;
	}
	
	private String getVertexWithLowestDistance(Set<String> set){
		Double minimum = null;
		String minimumVertex = null;
		for(String vertexId : set){
			Vertex tempVertex = vertexMap.get(vertexId);
			if(minimum == null){
				minimum = tempVertex.getDistanceFromSource();
				minimumVertex = tempVertex.getId();
			}else{
				if(tempVertex.getDistanceFromSource() < minimum){
					minimum = tempVertex.getDistanceFromSource();
					minimumVertex = tempVertex.getId();
				}
			}
			
		}
		return minimumVertex;
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
			for(String tag : tempArc.tagList.keySet()){
				switch(tag){
					case "highway":
						switch(tempArc.tagList.get("highway")){
							case "primary":
								
						}
						break;
					case "maxspeed":
						String maxspeedString = tempArc.tagList.get("maxspeed").trim();
						double maxspeed = 0;
						if(maxspeedString.length() >= 4){
							if(maxspeedString.endsWith("mph")){
								if(maxspeedString.contains(" ")){
									if(maxspeedString.length() == 5){ //TODO Deal with "60mph" structure - check for space
										maxspeed = KM_IN_MILE * Integer.parseInt(maxspeedString.substring(0,1));
									}
									else{
										maxspeed = KM_IN_MILE * Integer.parseInt(maxspeedString.substring(0,2));
									}
								}
								else{
									maxspeed = KM_IN_MILE * Integer.parseInt(maxspeedString.substring(0,maxspeedString.length() - 3));
								}
							}
							else{
								System.out.println("Malformed maxspeed tag");
							}
						}else{
							if(maxspeedString.length() > 0){
								maxspeed = Integer.parseInt(maxspeedString);
							}
							else{
								System.out.println("Malformed maxspeed tag");
							}
						}
						System.out.println("  Maxspeed = " + maxspeedString);
						System.out.println("    Maxspeed = " + maxspeed);
						break;
					default:
						break;
				}
			}
		}
		
	}

}

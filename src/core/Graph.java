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
		this.calculateNonTraversableArcs();
		for(String key : vertexMap.keySet()){
			Core.debug("Id: " + vertexMap.get(key).getId());
			Core.debug("Lat: " + vertexMap.get(key).getLat() + ", Lon: " + vertexMap.get(key).getLon());
			for(int i = 0; i < vertexMap.get(key).arcList.size(); i++){
				Core.debug("  " + vertexMap.get(key).arcList.get(i) + " - " + arcMap.get(vertexMap.get(key).arcList.get(i)).getWeight());
			}
		}
		this.shortestRoute("33071014", "33071061");

	}
	
	public Graph shortestRoute(String source, String destination){
		Core.debug("-------------------------------- BEGIN DIJKSTRA'S ----------------------------");
		Set<String> unsettledVertexes = new HashSet<String>();
		Set<String> settledVertexes = new HashSet<String>();
		vertexMap.get(source).setWeightedDistanceFromSource(0);
		vertexMap.get(source).setDistanceFromSource(0);
		vertexMap.get(source).setPreviousVertex(null);
		unsettledVertexes.add(source);
		while(unsettledVertexes.size() > 0){
			String workingVertex = getVertexWithLowestWeightedDistance(unsettledVertexes);
			if(workingVertex.equals(destination)){
				String previousVertex = vertexMap.get(workingVertex).getPreviousVertex();
				Core.debug("ROUTE FOUND:");
				while(previousVertex != null){
					Core.debug("  " + vertexMap.get(previousVertex).getPreviousVertex());
					previousVertex = vertexMap.get(previousVertex).getPreviousVertex();
				}
				return null;
			}
			Core.debug("Using Vertex: " + workingVertex);
			unsettledVertexes.remove(workingVertex);
			settledVertexes.add(workingVertex);
			for(int i = 0; i < vertexMap.get(workingVertex).arcList.size(); i++){ // Explore all arcs connected to the vertex
				String arc = vertexMap.get(workingVertex).arcList.get(i);
				Core.debug("Exploring Arc: " + arc);
				Core.debug("  Start: " + arcMap.get(arc).getStart());
				Core.debug("  End: " + arcMap.get(arc).getEnd());
				if(workingVertex.equals(arcMap.get(arc).getStart()) && !settledVertexes.contains(arcMap.get(arc).getEnd())){
					if(arcMap.get(arc).getOneWay() == -1){
						Core.debug("Ignoring Arc, Reverse One Way Road");
					}else{
						double workingDistance = arcMap.get(arc).getWeight() + vertexMap.get(workingVertex).getDistanceFromSource();
						double workingWeightedDistance = arcMap.get(arc).getWeightedDistance() + vertexMap.get(workingVertex).getWeightedDistanceFromSource();
						if(workingWeightedDistance < vertexMap.get(arcMap.get(arc).getEnd()).getWeightedDistanceFromSource()){
							Core.debug("  Modifying End Vertex - Distance From Source = " + workingDistance);
							Core.debug("  Modifying End Vertex - Weighted Distance From Source = " + workingWeightedDistance);
							vertexMap.get(arcMap.get(arc).getEnd()).setDistanceFromSource(workingDistance);
							vertexMap.get(arcMap.get(arc).getEnd()).setWeightedDistanceFromSource(workingWeightedDistance);// Assign working values to vertexes
							vertexMap.get(arcMap.get(arc).getEnd()).setPreviousVertex(workingVertex);
							unsettledVertexes.add(arcMap.get(arc).getEnd());
						}else{
							Core.debug("  Leaving End Vertex - Distance From Source = " + vertexMap.get(arcMap.get(arc).getEnd()).getDistanceFromSource() + ", Rejected Distance = " + workingDistance);
							Core.debug("  Leaving End Vertex - Weighted Distance From Source = " + vertexMap.get(arcMap.get(arc).getEnd()).getWeightedDistanceFromSource() + ", Rejected Weighted Distance = " + workingWeightedDistance);

						}
					}
				}
				else{
					if(workingVertex.equals(arcMap.get(arc).getEnd()) && !settledVertexes.contains(arcMap.get(arc).getStart())){
						if(arcMap.get(arc).getOneWay() == 1){
							Core.debug("Ignoring Arc, One Way Road");
						}else{
							
							double workingDistance = arcMap.get(arc).getWeight() + vertexMap.get(workingVertex).getDistanceFromSource();
							double workingWeightedDistance = arcMap.get(arc).getWeightedDistance() + vertexMap.get(workingVertex).getWeightedDistanceFromSource();
							if(workingWeightedDistance < vertexMap.get(arcMap.get(arc).getStart()).getWeightedDistanceFromSource()){
								Core.debug("  Modifying Start Vertex - Distance From Source = " + workingDistance);
								Core.debug("  Modifying Start Vertex - Weighted Distance From Source = " + workingWeightedDistance);
								vertexMap.get(arcMap.get(arc).getStart()).setDistanceFromSource(workingDistance);
								vertexMap.get(arcMap.get(arc).getStart()).setWeightedDistanceFromSource(workingWeightedDistance);
								unsettledVertexes.add(arcMap.get(arc).getStart());
								vertexMap.get(arcMap.get(arc).getStart()).setPreviousVertex(workingVertex);
							}else{
								Core.debug("  Leaving Start Vertex - Distance From Source = " + vertexMap.get(arcMap.get(arc).getStart()).getDistanceFromSource() + ", Rejected Distance = " + workingDistance);
								Core.debug("  Leaving Start Vertex - Weighted Distance From Source = " + vertexMap.get(arcMap.get(arc).getStart()).getWeightedDistanceFromSource() + ", Rejected Weighted Distance = " + workingWeightedDistance);
	
							}
						}
					}
					else{
						Core.debug("Dead End: " + workingVertex);
					}
				}
				//vertexMap.get(source).arcList.get(i)
			}
		}
		for(String vertexId : unsettledVertexes){
			Core.debug(vertexId);
		}
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
	
	private String getVertexWithLowestWeightedDistance(Set<String> set){
		Double minimum = null;
		String minimumVertex = null;
		for(String vertexId : set){
			Vertex tempVertex = vertexMap.get(vertexId);
			if(minimum == null){
				minimum = tempVertex.getWeightedDistanceFromSource();
				minimumVertex = tempVertex.getId();
			}else{
				if(tempVertex.getWeightedDistanceFromSource() < minimum){
					minimum = tempVertex.getWeightedDistanceFromSource();
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
			Core.debug("Dist: " + distance);
			Core.debug("Way Id: " + tempArc.id);
			Core.debug("  Estimated Max Speed: " + getMaxSpeed(tempArc) + " kph");
			double weightedDistance = distance/(getMaxSpeed(tempArc)/100);
			Core.debug("  Weighted Distance: " + weightedDistance);
			tempArc.setWeightedDistance(weightedDistance);
			// divide by the speed over 100
		}
	}
	
	private double getMaxSpeed(Arc arc){
		double maxspeed = 0;
		for(String tag : arc.tagList.keySet()){
			switch(tag){
				case "maxspeed":
					String maxspeedString = arc.tagList.get("maxspeed").trim();
					if(maxspeedString.length() >= 4){
						if(maxspeedString.endsWith("mph")){
							if(maxspeedString.contains(" ")){
								if(maxspeedString.length() == 5){
									maxspeed = KM_IN_MILE * Integer.parseInt(maxspeedString.substring(0,1));
									Core.debug("Parsing " + maxspeedString + " to " + maxspeed);
									return maxspeed;
								}
								else{
									maxspeed = KM_IN_MILE * Integer.parseInt(maxspeedString.substring(0,2));
									Core.debug("Parsing " + maxspeedString + " to " + maxspeed);
									return maxspeed;
								}
							}
							else{
								maxspeed = KM_IN_MILE * Integer.parseInt(maxspeedString.substring(0,maxspeedString.length() - 3));
								Core.debug("Parsing " + maxspeedString + " to " + maxspeed);
								return maxspeed;
							}
						}
						else{
							Core.debug("Malformed maxspeed tag");
						}
					}else{
						if(maxspeedString.length() > 0){
							maxspeed = Integer.parseInt(maxspeedString);
							Core.debug("Parsing " + maxspeedString + " to " + maxspeed);
							return maxspeed;
						}
						else{
							Core.debug("Malformed maxspeed tag");
						}
					}
					Core.debug("  Maxspeed = " + maxspeedString);
					Core.debug("    Maxspeed = " + maxspeed);
					break;
				case "highway":
					Core.debug(arc.tagList.get("highway"));
					switch(arc.tagList.get("highway").trim()){
						case "motorway":
							maxspeed = KM_IN_MILE * 70;
							Core.debug("Motorway, assuming: " + maxspeed);
							return maxspeed;
						case "trunk":
							maxspeed = KM_IN_MILE * 70;
							Core.debug("Trunk, assuming: " + maxspeed);
							return maxspeed;
						case "primary":
							maxspeed = KM_IN_MILE * 60;
							Core.debug("Primary, assuming: " + maxspeed);
							return maxspeed;
						case "secondary":
							maxspeed = KM_IN_MILE * 50;
							Core.debug("Secondary, assuming: " + maxspeed);
							return maxspeed;
						case "tertiary":
							maxspeed = KM_IN_MILE * 40;
							Core.debug("Tertiary, assuming: " + maxspeed);
							return maxspeed;
						case "unclassified":
							maxspeed = KM_IN_MILE * 30;
							Core.debug("Unclassified, assuming: " + maxspeed);
							return maxspeed;
						case "residential":
							maxspeed = KM_IN_MILE * 30;
							Core.debug("Residential, assuming: " + maxspeed);
							return maxspeed;
						case "service":
							maxspeed = KM_IN_MILE * 10;
							Core.debug("Service, assuming: " + maxspeed);
							return maxspeed;
						case "track":
							maxspeed = KM_IN_MILE * 5;
							Core.debug("Track, assuming: " + maxspeed);
							return maxspeed;
						case "motorway_link":
							maxspeed = KM_IN_MILE * 65;
							Core.debug("Motorway Link, assuming: " + maxspeed);
							return maxspeed;
						case "trunk_link":
							maxspeed = KM_IN_MILE * 65;
							Core.debug("Trunk Link, assuming: " + maxspeed);
							return maxspeed;
						case "primary_link":
							maxspeed = KM_IN_MILE * 55;
							Core.debug("Primary Link, assuming: " + maxspeed);
							return maxspeed;
						case "secondary_link":
							maxspeed = KM_IN_MILE * 45;
							Core.debug("Secondary Link, assuming: " + maxspeed);
							return maxspeed;
						case "tertiary_link":
							maxspeed = KM_IN_MILE * 35;
							Core.debug("Tertiary Link, assuming: " + maxspeed);
							return maxspeed;
						default:
							maxspeed = KM_IN_MILE * 30;
							Core.debug("Unknown, assuming: " + maxspeed);
							return maxspeed;
					}
					default:
						break;
			}
		}
		maxspeed = KM_IN_MILE * 30;
		Core.debug("No tag info, assuming: " + maxspeed);
		return maxspeed;
	}
	
	private void calculateNonTraversableArcs(){
		for(String key : arcMap.keySet()){
			Arc tempArc = arcMap.get(key);
			for(String tag : tempArc.tagList.keySet()){
				if(tag.equals("oneway")){
					switch(tempArc.tagList.get(tag)){
						case "yes":
							tempArc.setOneWay(1);
							Core.debug("One way");
							break;
						case "-1":
							tempArc.setOneWay(-1);
							Core.debug("Reverse One Way");
							break;
						default:
							break;
					}
				}
				if(tag.equals("junction")){
					switch(tempArc.tagList.get(tag)){
						case "roundabout":
							tempArc.setOneWay(1);
							break;
					}
				}
			}
		}
	}
}


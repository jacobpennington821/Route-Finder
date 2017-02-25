package core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Graph {
	
	public static final int EARTHDIAMETER = 12742;
	public static final double KM_IN_MILE = 1.61;
	public static final double MAXSPEEDDAMPENING = 0.75;
	
	private HashMap<String,Vertex> vertexMap = new HashMap<String,Vertex>(); // A hashmap that stores all vertexes using their id as a reference
	private HashMap<String,Arc> arcMap = new HashMap<String,Arc>(); // Same as above but with arcs
	
	private String lastEnteredSource;
	private String lastEnteredDestination;
	
	public Graph(HashMap<String,Vertex> vertexMap, HashMap<String,Arc> arcMap){
		this.vertexMap = vertexMap;
		this.arcMap = arcMap;
		this.calculateWeights(); // Calculates all arc weights on the graph
		this.calculateNonTraversableArcs(); // Calculates whether any arcs on the graph are one way and if so in which direction

	}
	
	public Graph shortestRoute(String source, String destination){ // Calculates the shortest route from one vertex to another using dijkstra's algorithm - https://en.wikipedia.org/wiki/Dijkstra's_algorithm
		lastEnteredSource = source;
		lastEnteredDestination = destination;
		Core.debug("-------------------------------- BEGIN DIJKSTRA'S ----------------------------");
		Set<String> unsettledVertexes = new HashSet<String>(); // A hashset of all vertexes that have working distances from the source but are not confirmed and settled
		Set<String> settledVertexes = new HashSet<String>(); // A hashset of all vertexes that are settled and have distances from the source
		vertexMap.get(source).setWeightedDistanceFromSource(0); // Starts by setting all distance values for the source node to 0
		vertexMap.get(source).setDistanceFromSource(0);
		vertexMap.get(source).setPreviousVertex(null); // Assigns the previous vertex of the source to null
		unsettledVertexes.add(source); // The source node is added to the list of unsettled vertexes
		while(unsettledVertexes.size() > 0){ // Loop until no vertexes are unsettled
			String workingVertex = getVertexWithLowestWeightedDistance(unsettledVertexes); // The current working vertex is assigned to the vertex in the unsettled vertex hashset with the shortest distance from any currently settled vertex
			if(workingVertex.equals(destination)){
				String previousVertex = vertexMap.get(workingVertex).getPreviousVertex();
				Core.debug("  " + workingVertex);
				Core.debug("ROUTE FOUND:");
				while(previousVertex != null){
					Core.debug("  " + vertexMap.get(previousVertex).getPreviousVertex() + " - " + getArcConnectingTwoVertexes(previousVertex, workingVertex).tagList.get("ref") + ", " + getArcConnectingTwoVertexes(previousVertex, workingVertex).tagList.get("name"));
					workingVertex = previousVertex;
					previousVertex = vertexMap.get(previousVertex).getPreviousVertex();
				}
				return null;
			}
			Core.debug("Using Vertex: " + workingVertex);
			unsettledVertexes.remove(workingVertex); // Transfers the current vertex from the unsettled hashset to the settled hashset
			settledVertexes.add(workingVertex);
			for(int i = 0; i < vertexMap.get(workingVertex).arcList.size(); i++){ // Explore all arcs connected to the vertex
				String arc = vertexMap.get(workingVertex).arcList.get(i); // Temporarily stores the current arc being used
				Core.debug("Exploring Arc: " + arc);
				Core.debug("  Start: " + arcMap.get(arc).getStart());
				Core.debug("  End: " + arcMap.get(arc).getEnd());
				if(workingVertex.equals(arcMap.get(arc).getStart()) && !settledVertexes.contains(arcMap.get(arc).getEnd())){ // Checking for the orientation of the arc being used: either start to end or end to start
					// Section for dealing with arcs from start to end
					if(arcMap.get(arc).getOneWay() == -1){ // Discards arc if the arc has the "oneway" tag set to -1 - can't be travelled down from start to end
						Core.debug("Ignoring Arc, Reverse One Way Road");
					}else{
						double workingDistance = arcMap.get(arc).getWeight() + vertexMap.get(workingVertex).getDistanceFromSource(); // Sets the working distance to the current vertex's distance from the source + the weight of the arc
						double workingWeightedDistance = arcMap.get(arc).getWeightedDistance() + vertexMap.get(workingVertex).getWeightedDistanceFromSource(); // Same as above but using weighted distance instead
						if(workingWeightedDistance < vertexMap.get(arcMap.get(arc).getEnd()).getWeightedDistanceFromSource()){ // If the working weighted distance is less than the target vertexes current weighted distance
							Core.debug("  Modifying End Vertex - Distance From Source = " + workingDistance);
							Core.debug("  Modifying End Vertex - Weighted Distance From Source = " + workingWeightedDistance);
							vertexMap.get(arcMap.get(arc).getEnd()).setDistanceFromSource(workingDistance);
							vertexMap.get(arcMap.get(arc).getEnd()).setWeightedDistanceFromSource(workingWeightedDistance);// Assign working distances to target vertex
							vertexMap.get(arcMap.get(arc).getEnd()).setPreviousVertex(workingVertex); // Assigns the current vertex as the target vertex's previous vertex
							unsettledVertexes.add(arcMap.get(arc).getEnd()); // If the target vertex is not present in the unsettled vertexes hashset then it is added - hashset doesn't accept duplicates
						}else{ // Ignore target vertex if the route to it is longer than its current distance from source
							Core.debug("  Leaving End Vertex - Distance From Source = " + vertexMap.get(arcMap.get(arc).getEnd()).getDistanceFromSource() + ", Rejected Distance = " + workingDistance);
							Core.debug("  Leaving End Vertex - Weighted Distance From Source = " + vertexMap.get(arcMap.get(arc).getEnd()).getWeightedDistanceFromSource() + ", Rejected Weighted Distance = " + workingWeightedDistance);
						}
					}
				}
				else{
					if(workingVertex.equals(arcMap.get(arc).getEnd()) && !settledVertexes.contains(arcMap.get(arc).getStart())){ // Checking for the orientation of the arc
						// Section for dealing with arcs from end to start
						if(arcMap.get(arc).getOneWay() == 1){ // Discards arc if it can't be travelled down from end to start
							Core.debug("Ignoring Arc, One Way Road");
						}else{
							double workingDistance = arcMap.get(arc).getWeight() + vertexMap.get(workingVertex).getDistanceFromSource(); // Sets the working distance to the current vertex's distance from the source + the weight of the arc
							double workingWeightedDistance = arcMap.get(arc).getWeightedDistance() + vertexMap.get(workingVertex).getWeightedDistanceFromSource(); // Same as above but using weighted distance instead
							if(workingWeightedDistance < vertexMap.get(arcMap.get(arc).getStart()).getWeightedDistanceFromSource()){ // If the working weighted distance is less that the target vertexes current weighted distance
								Core.debug("  Modifying Start Vertex - Distance From Source = " + workingDistance);
								Core.debug("  Modifying Start Vertex - Weighted Distance From Source = " + workingWeightedDistance);
								vertexMap.get(arcMap.get(arc).getStart()).setDistanceFromSource(workingDistance); 
								vertexMap.get(arcMap.get(arc).getStart()).setWeightedDistanceFromSource(workingWeightedDistance); // Assigns the working distance to the target vertex
								vertexMap.get(arcMap.get(arc).getStart()).setPreviousVertex(workingVertex); // Assigns the current vertex as the target vertex's previous vertex
								unsettledVertexes.add(arcMap.get(arc).getStart()); // If the target vertex is not present in the unsettled vertexes hashset then it is added - hashset doesn't accept duplicates
							}else{ // Ignore target vertex if the route to its longer than its current distance from source
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
	
	private String getVertexWithLowestDistance(Set<String> set){ // Returns the vertex with the lowest distance from source in a hashset
		Double minimum = null; // Using the double wrapper class so that it can be assigned a null value for when first initialised
		String minimumVertex = null;
		for(String vertexId : set){ // Iterates through every vertex id in the hashset
			Vertex tempVertex = vertexMap.get(vertexId); // Stores the vertex currently used in a temporary variable
			if(minimum == null){ // If this is the first vertex checked then the minimum will always be this vertex's distance from source
				minimum = tempVertex.getDistanceFromSource();
				minimumVertex = tempVertex.getId();
			}else{
				if(tempVertex.getDistanceFromSource() < minimum){ // Checks if the value of the currently held vertex distance from source is lower than the minimum
					minimum = tempVertex.getDistanceFromSource(); // If it is, assigns that value to the minimum
					minimumVertex = tempVertex.getId(); // Stores the vertex id of the currently lowest distance from source
				}
			}
			
		}
		return minimumVertex;
	}
	
	private String getVertexWithLowestWeightedDistance(Set<String> set){ // As above but using weighted distances instead of distance
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
	
	private void calculateWeights(){ // Assigns distances and times to each arc in km and hours respectively
		double lat1,lon1,lat2,lon2,temp,distance,weightedDistance;
		for(String key : arcMap.keySet()){ // Iterates through every arc in the arc hashmap
			Arc tempArc = arcMap.get(key);
			lat1 = Math.toRadians(vertexMap.get(tempArc.getStart()).getLat());
			lon1 = Math.toRadians(vertexMap.get(tempArc.getStart()).getLon());
			lat2 = Math.toRadians(vertexMap.get(tempArc.getEnd()).getLat());
			lon2 = Math.toRadians(vertexMap.get(tempArc.getEnd()).getLon());
			temp = Math.pow(Math.sin((lat2 - lat1) / 2), 2) + (Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin((lon2 - lon1) / 2), 2)); // A temporary value for use in the distance calculation
			distance = EARTHDIAMETER * Math.asin(Math.sqrt(temp)); // Using the haversine formula to calculate the length of arcs using latitudes and longitudes - https://en.wikipedia.org/wiki/Haversine_formula 
			tempArc.setWeight(distance); // Assigns unweighted distance to the arc being used
			Core.debug("Way Id: " + tempArc.id);
			Core.debug("  Dist: " + distance);
			//Core.debug("  Estimated Max Speed: " + getMaxSpeed(tempArc) + " kph"); 
			weightedDistance = distance/(getMaxSpeed(tempArc)); // Calculates weighted distance which is equal to time in hours of road by dividing distance by the average speed
			//Core.debug("  Weighted Distance: " + weightedDistance); 
			tempArc.setWeightedDistance(weightedDistance); // Assigns the weighted distance to the arc being used 
		}
	}
	
	private double getMaxSpeed(Arc arc){ // Returns the max speed in km/hr of a give arc
		double maxspeed = 0;
		for(String tag : arc.tagList.keySet()){ // Iterates through every tag in the arc's taglist
			switch(tag){ // Switch is in order of priority
				case "maxspeed": // First looks for if the arc has a "maxspeed" tag
					String maxspeedString = arc.tagList.get("maxspeed").trim(); // Stores the value of the "maxspeed" tag
					if(maxspeedString.length() >= 4){ // If maxspeed is not just a number
						if(maxspeedString.endsWith("mph")){ // If maxspeed is measured in mph
							if(maxspeedString.contains(" ")){ // Differentiates between "30 mph" and "30mph"
								maxspeed = KM_IN_MILE * Integer.parseInt(maxspeedString.substring(0,maxspeedString.indexOf(" "))); // Extracts values such as 5 from "5 mph" and then converts them to km/hr
								//Core.debug("Parsing " + maxspeedString + " to " + maxspeed);
								return maxspeed * MAXSPEEDDAMPENING;
							}
							else{
								maxspeed = KM_IN_MILE * Integer.parseInt(maxspeedString.substring(0,maxspeedString.length() - 3)); // Extracts values such as 5 from "5mph" and then converts them to km/hr
								//Core.debug("Parsing " + maxspeedString + " to " + maxspeed);
								return maxspeed * MAXSPEEDDAMPENING;
							}
						}
						else{
							Core.debug("Malformed maxspeed tag");
						}
					}else{ // If string does not contain "mph" then it is measured in km/hr
						if(maxspeedString.length() > 0){ // Ensuring string is not empty
							maxspeed = Integer.parseInt(maxspeedString); // No substring needed as the string should just be a number - No conversion required either as value is already in km/hr
							//Core.debug("Parsing " + maxspeedString + " to " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						}
						else{
							Core.debug("Malformed maxspeed tag");
						}
					}
					//Core.debug("  Maxspeed = " + maxspeedString);
					//Core.debug("    Maxspeed = " + maxspeed);
					break;
				case "highway": // If maxspeed tag is not present then the "highway" tag will be used instead
					//Core.debug(arc.tagList.get("highway"));
					switch(arc.tagList.get("highway").trim()){ // Switches based on "highway" value
						case "motorway":
							maxspeed = KM_IN_MILE * 70; // If the road is a motorway the speed limit in the UK is 70 mph
							//Core.debug("Motorway, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "trunk":
							maxspeed = KM_IN_MILE * 70; // Roads tagged with "trunk" are usually dual carriageway so 70 mph is assumed
							//Core.debug("Trunk, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "primary": // Roads tagged with primary tend to be A roads and similar so 60 mph is assumed
							maxspeed = KM_IN_MILE * 60;
							//Core.debug("Primary, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "secondary": // Roads tagged with secondary tend to be roads slightly smaller than an A road and so 50 mph is assumed as a safe halfway point
							maxspeed = KM_IN_MILE * 50;
							//Core.debug("Secondary, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "tertiary": // Roads tagged with tertiary tend to be small and narrow, so while the speed limit may be 60 mph it is not achievable, therefore 40 mph is assumed
							maxspeed = KM_IN_MILE * 40;
							//Core.debug("Tertiary, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "unclassified": // Roads tagged with "unclassified" may be farm tracks or similar so 30 mph is assumed
							maxspeed = KM_IN_MILE * 30;
							//Core.debug("Unclassified, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "residential": // Roads that are residential are usually 30 mph speed limits
							maxspeed = KM_IN_MILE * 30;
							//Core.debug("Residential, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "service": // Service roads are usually private roads and very poorly maintained so 10 mph is assumed
							maxspeed = KM_IN_MILE * 10;
							//Core.debug("Service, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "track": // Tracks are farm tracks or worse so only 5 mph is assumed
							maxspeed = KM_IN_MILE * 5;
							//Core.debug("Track, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "motorway_link": // Motorway_link roads are usually slip roads or similar so 65 mph is assumed
							maxspeed = KM_IN_MILE * 65;
							//Core.debug("Motorway Link, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "trunk_link": // Trunk_link are roads such as slip ways onto dual carriageways so 65 mph assumed
							maxspeed = KM_IN_MILE * 65;
							//Core.debug("Trunk Link, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "primary_link": // Primary_link roads are smaller than primary roads but larger than secondary so 55 mph is assumed
							maxspeed = KM_IN_MILE * 55;
							//Core.debug("Primary Link, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "secondary_link": // Seccondary_link roads are smaller than secondary but larger than tertiary so 45 mph is assumed
							maxspeed = KM_IN_MILE * 45;
							//Core.debug("Secondary Link, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						case "tertiary_link": // Tertiary_link roads are smaller than tertiary but larger than tracks or service roads so 35 mph is assumed
							maxspeed = KM_IN_MILE * 35;
							//Core.debug("Tertiary Link, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
						default: // If highway tag is malformed then 30 mph is assumed
							maxspeed = KM_IN_MILE * 30; 
							//Core.debug("Unknown, assuming: " + maxspeed);
							return maxspeed * MAXSPEEDDAMPENING;
					}
					default:
						break;
			}
		}
		maxspeed = KM_IN_MILE * 30; // If no tags are present at all then 30 mph is assumed
		Core.debug("No tag info, assuming: " + maxspeed);
		return maxspeed * MAXSPEEDDAMPENING;
	}
	
	private void calculateNonTraversableArcs(){
		for(String key : arcMap.keySet()){ // Iterates through every arc in the arc hashmap
			Arc tempArc = arcMap.get(key); // Stores the currently used arc temporarily for manipulating
			for(String tag : tempArc.tagList.keySet()){ // Iterates through every tag in the current arc
				if(tag.equals("oneway")){ // Check for if the "oneway" tag is present
					switch(tempArc.tagList.get(tag)){ // Gets the value of the "oneway" tag
						case "yes":
							tempArc.setOneWay(1); // Assign the road to be one way
							//Core.debug("One way");
							break;
						case "-1":
							tempArc.setOneWay(-1); // Assign the road to be one way in reverse
							//Core.debug("Reverse One Way");
							break;
						default: // If oneway does not equal yes or -1 then the road is not one way
							break;
					}
				}
				if(tag.equals("junction")){ // Check for presence of "junction" tag
					switch(tempArc.tagList.get(tag)){
						case "roundabout":
							tempArc.setOneWay(1); // Roundabouts imply the "oneway" tag
							break;
					}
				}
				if(tag.equals("highway")){
					switch(tempArc.tagList.get(tag)){
					case "motorway":
						tempArc.setOneWay(1); // Motorways also imply the "oneway" tag
						break;
					}
				}
			}
		}
	}
	
	private Arc getArcConnectingTwoVertexes(String vertexId1, String vertexId2){ // Finds the id of an arc connecting two vertexes together
		Vertex vertex1 = vertexMap.get(vertexId1);
		Vertex vertex2 = vertexMap.get(vertexId2);
		for(int i = 0; i < vertex1.arcList.size(); i++){ // Iterates through every arc connected to the first vertex
			if(arcMap.get(vertex1.arcList.get(i)).getStart().equals(vertexId2) || arcMap.get(vertex1.arcList.get(i)).getEnd().equals(vertexId2)){ // Checks if the current arc also has the second vertex as a member
				return arcMap.get(vertex1.arcList.get(i)); // If so return that arc
			}
		}
		return null;
	}
	
	public String convertGraphToDirections(){
		Core.debug(getExitOnRoundabout("1687317961","1687317956"));
		Core.debug(getExitOnRoundabout("33071014", "2062355376"));
		StringBuilder output = new StringBuilder();
		String workingVertex = lastEnteredDestination;
		String previousVertex = vertexMap.get(workingVertex).getPreviousVertex();
		ArrayList<String> reverseRoute = new ArrayList<String>();
		while(previousVertex != null){
			reverseRoute.add(workingVertex);
			workingVertex = previousVertex;
			previousVertex = vertexMap.get(previousVertex).getPreviousVertex();
		}
		reverseRoute.add(workingVertex);
		Arc currentArc;
		String currentRoadRef = "!novalue";
		String currentRoadName = "!novalue";
		boolean inRoundabout = false;
		String roundaboutEntry = "";
		double previousDirectionDistance = 0;
		for(int i = reverseRoute.size() - 1; i >= 0; i--){
			if(i != 0){
				currentArc = getArcConnectingTwoVertexes(reverseRoute.get(i), reverseRoute.get(i-1));
				if(!inRoundabout){
					if(currentArc.tagList.get("junction") == null){
						if(currentArc.tagList.get("ref") == null || currentRoadRef == null){
							if(currentRoadRef != currentArc.tagList.get("ref")){
								if(i != reverseRoute.size() - 1){
									output.append("After " + round((vertexMap.get(reverseRoute.get(i)).getDistanceFromSource() - previousDirectionDistance),2) + " km, ");
									previousDirectionDistance = vertexMap.get(reverseRoute.get(i)).getDistanceFromSource();
									output.append("turn " + calculateDirection(reverseRoute.get(i + 1), reverseRoute.get(i), reverseRoute.get(i - 1)) + " onto ");
								}else{
									output.append("Travel " + getBearingString(reverseRoute.get(i),reverseRoute.get(i-1)) + " along ");
								}
								currentRoadRef = currentArc.tagList.get("ref");
								currentRoadName = currentArc.tagList.get("name");
								if(currentRoadRef == null){
									if(currentRoadName == null){
										Core.debug("Unnamed Road");
										output.append("Unnamed Road\n");
									} else {
										Core.debug(currentRoadName);
										output.append(currentRoadName + "\n");
									}
								} else {
									Core.debug(currentRoadRef);
									output.append(currentRoadRef + "\n");
								}
							} else {
								if(currentArc.tagList.get("name") == null || currentRoadName == null){
									if(currentRoadName != currentArc.tagList.get("name")){
										if(i != reverseRoute.size() - 1){
											output.append("After " + round((vertexMap.get(reverseRoute.get(i)).getDistanceFromSource() - previousDirectionDistance),2) + " km, ");
											previousDirectionDistance = vertexMap.get(reverseRoute.get(i)).getDistanceFromSource();
											output.append("turn " + calculateDirection(reverseRoute.get(i + 1), reverseRoute.get(i), reverseRoute.get(i - 1)) + " onto ");
										}else{
											output.append("Travel " + getBearingString(reverseRoute.get(i),reverseRoute.get(i-1)) + " along ");
										}
										currentRoadName = currentArc.tagList.get("name");
										if(currentRoadName == null){
											Core.debug("Unnamed Road");
											output.append("Unnamed Road\n");
										} else {
											Core.debug(currentRoadName);
											output.append(currentRoadName + "\n");
										}
									}
								} else {
									if(!currentRoadName.equals(currentArc.tagList.get("name"))){
										if(i != reverseRoute.size() - 1){
											output.append("After " + round((vertexMap.get(reverseRoute.get(i)).getDistanceFromSource() - previousDirectionDistance),2) + " km, ");
											previousDirectionDistance = vertexMap.get(reverseRoute.get(i)).getDistanceFromSource();
											output.append("turn " + calculateDirection(reverseRoute.get(i + 1), reverseRoute.get(i), reverseRoute.get(i - 1)) + " onto ");
										}else{
											output.append("Travel " + getBearingString(reverseRoute.get(i),reverseRoute.get(i-1)) + " along ");
										}
										currentRoadName = currentArc.tagList.get("name");
										Core.debug(currentRoadName);
										output.append(currentRoadName + "\n");
									}
								}
							}
						} else {
							if(!currentRoadRef.equals(currentArc.tagList.get("ref"))){
								if(i != reverseRoute.size() - 1){
									output.append("After " + round((vertexMap.get(reverseRoute.get(i)).getDistanceFromSource() - previousDirectionDistance),2) + " km, ");
									previousDirectionDistance = vertexMap.get(reverseRoute.get(i)).getDistanceFromSource();
									output.append("turn " + calculateDirection(reverseRoute.get(i + 1), reverseRoute.get(i), reverseRoute.get(i - 1)) + " onto ");
								}else{
									output.append("Travel " + getBearingString(reverseRoute.get(i),reverseRoute.get(i-1)) + " along ");
								}
								currentRoadRef = currentArc.tagList.get("ref");
								currentRoadName = currentArc.tagList.get("name");
								Core.debug(currentRoadRef);
								output.append(currentRoadRef + "\n");
							}
						}
					} else {
						if(currentArc.tagList.get("junction").equals("roundabout")){
							inRoundabout = true;
							roundaboutEntry = reverseRoute.get(i-1);
						}
					}
				} else {
					if(currentArc.tagList.get("junction") == null){
						inRoundabout = false;
						output.append("After " + round((vertexMap.get(reverseRoute.get(i)).getDistanceFromSource() - previousDirectionDistance),2) + " km, ");
						previousDirectionDistance = vertexMap.get(reverseRoute.get(i)).getDistanceFromSource();
						output.append("take the " + formatNumberToPlace(getExitOnRoundabout(roundaboutEntry,reverseRoute.get(i-1))) + " exit on the roundabout, onto ");
						currentRoadRef = currentArc.tagList.get("ref");
						currentRoadName = currentArc.tagList.get("name");
						if(currentRoadRef == null){
							if(currentRoadName == null){
								output.append("Unnamed Road\n");
							} else{
								output.append(currentRoadName + "\n");
							}
						} else {
							output.append(currentRoadRef + "\n");
						}
					}
				}
				//Core.debug(reverseRoute.get(i) + " - " + reverseRoute.get(i-1) + " --> " + currentArc.tagList.get("ref") + ", " + currentArc.tagList.get("name"));
			}
			//Core.debug(reverseRoute.get(i));
		}
		output.append("Time = " + vertexMap.get(reverseRoute.get(0)).getWeightedDistanceFromSource());
		return output.toString();
	}
	
	private double calculateBearing(String vertexId1, String vertexId2){
		Vertex vertex1 = vertexMap.get(vertexId1);
		Vertex vertex2 = vertexMap.get(vertexId2);
		double lat1 = Math.toRadians(vertex1.getLat());
		double lon1 = Math.toRadians(vertex1.getLon());
		double lat2 = Math.toRadians(vertex2.getLat());
		double lon2 = Math.toRadians(vertex2.getLon());
		double temp1 = Math.cos(lat2) * Math.sin(lon2 - lon1);
		double temp2 = (Math.cos(lat1) * Math.sin(lat2)) - (Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
		double bearing = Math.atan2(temp1, temp2);
		if(bearing < 0){
			bearing += 2 * Math.PI;
		}
		return bearing;
	}
	
	private String getBearingString(String vertexId1, String vertexId2){
		double bearing = calculateBearing(vertexId1, vertexId2);
		if(((7 * Math.PI) / 4 <= bearing && bearing <= 2 * Math.PI )|| (bearing <= 0 && bearing < (Math.PI / 4))){
			return "north";
		}
		if(Math.PI / 4 <= bearing && bearing < (3 * Math.PI) / 4){
			return "east";
		}
		if((3 * Math.PI) / 4 <= bearing && bearing < (5 * Math.PI) / 4){
			return "south";
		}
		if((5 * Math.PI) / 4 <= bearing && bearing < (7 * Math.PI) / 4){
			return "west";
		}
		return "invalid";
	}
	
	private String calculateDirection(String vertexId1, String vertexId2, String vertexId3){
		double directionValue = calculateBearing(vertexId1,vertexId2) - calculateBearing(vertexId2,vertexId3);
		if((-Math.PI < directionValue && directionValue < 0) || (Math.PI < directionValue && directionValue < 2 * Math.PI)){
			return "right";
		}
		if((0 < directionValue && directionValue < Math.PI) || (-2 * Math.PI < directionValue && directionValue < -Math.PI)){
			return "left";
		}
		return "Invalid";
	}

	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();
	
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	private int getExitOnRoundabout(String startingVertex, String exitVertex){
		Core.debug("Start: " + startingVertex + ", End: " + exitVertex);
		String currentVertex = startingVertex;
		boolean found = false;
		int exits = 0;
		String nextVertex = "";
		do{
			for(int i = 0; i < vertexMap.get(currentVertex).arcList.size(); i++){
				String currentArc = vertexMap.get(currentVertex).arcList.get(i);
				Core.debug("Inspecting: " + currentArc + ", on " + currentVertex);
				if(arcMap.get(currentArc).tagList.get("junction") != null){
					if(!arcMap.get(currentArc).tagList.get("junction").equals("roundabout")){
						if(arcMap.get(currentArc).getStart().equals(currentVertex) && arcMap.get(currentArc).getOneWay() != -1){
							Core.debug(currentArc + " is an exit");
							exits++;
							if(arcMap.get(currentArc).getEnd().equals(exitVertex)){
								return exits;
							}
						}
						if(arcMap.get(currentArc).getEnd().equals(currentVertex) && arcMap.get(currentArc).getOneWay() != 1){
							Core.debug(currentArc + " is an exit");
							exits++;
							if(arcMap.get(currentArc).getStart().equals(exitVertex)){
								return exits;
							}
						}
					}else{
						if(arcMap.get(currentArc).getStart().equals(currentVertex)){
							nextVertex = arcMap.get(currentArc).getEnd();
							Core.debug("Discovered next vertex: " + nextVertex);
						}						
						
					}
				}else{
					if(arcMap.get(currentArc).getStart().equals(currentVertex) && arcMap.get(currentArc).getOneWay() != -1){
						Core.debug(currentArc + " is an exit");
						exits++;
						if(arcMap.get(currentArc).getEnd().equals(exitVertex)){
							return exits;
						}
					}
					if(arcMap.get(currentArc).getEnd().equals(currentVertex) && arcMap.get(currentArc).getOneWay() != 1){
						Core.debug(currentArc + " is an exit");
						exits++;
						if(arcMap.get(currentArc).getStart().equals(exitVertex)){
							return exits;
						}
					}
				}
			}
			currentVertex = nextVertex;
		}while(!currentVertex.equals(startingVertex) || !found);
		return exits;
	}
	
	private String formatNumberToPlace(int number){
		switch(number){
		case 1:
			return number + "st";
		case 2:
			return number + "nd";
		case 3:
			return number + "rd";
		default:
			return number + "th";
		}
	}
}

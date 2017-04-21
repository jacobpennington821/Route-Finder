package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The class for storing vertexes and arcs in a weighted graph.
 * <p>
 * Stores vertexes and arcs as well as containing methods for manipulating the graph such as
 * shortest route calculations and length calculations.
 * @author Jacob Pennington
 *
 */
public class Graph {
	
	public static final int EARTHDIAMETER = 12742;
	public static final double KM_IN_MILE = 1.61;
	public static final double MAXSPEEDDAMPENING = 0.75;
	
	private HashMap<String,Vertex> vertexMap = new HashMap<String,Vertex>(); // A hashmap that stores all vertexes using their id as a reference
	private HashMap<String,Arc> arcMap = new HashMap<String,Arc>(); // Same as above but with arcs
	HashMap<String,Vertex> vertexMapMirror;
	
	private String lastEnteredDestination;
	
	public double calculatedRouteDistance = 0;
	public double calculatedRouteTime = 0;
	
	/**
	 * Constructs the graph object then calculates all weights and
	 * marks non-traversable arcs.
	 * @param vertexMap - The hashmap of vertex objects to use.
	 * @param arcMap - The hashmap of arc objects to use.
	 */
	public Graph(HashMap<String,Vertex> vertexMap, HashMap<String,Arc> arcMap){
		this.vertexMap = vertexMap;
		this.arcMap = arcMap;
		this.calculateWeights(); // Calculates all arc weights on the graph
		this.calculateNonTraversableArcs(); // Calculates whether any arcs on the graph are one way and if so in which direction
		vertexMapMirror = cloneVertexMap(vertexMap);
	}
	
	/**
	 * Calculates the quickest route from one vertex to another using Dijkstra's algorithm
	 * <p>
	 * Calculates the shortest route using distances weighted by the maximum speed of the road to give time. 
	 * https://en.wikipedia.org/wiki/Dijkstra's_algorithm
	 * @param source - The ID of the source node in the vertex hashmap
	 * @param destination - The ID of the destination node in the vertex hashmap
	 */
	public void quickestRoute(String source, String destination){ // Calculates the quickest route from one vertex to another using dijkstra's algorithm - https://en.wikipedia.org/wiki/Dijkstra's_algorithm
		System.out.println("Memory Usage Before Clone: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		vertexMapMirror = cloneVertexMap(vertexMap);
		System.out.println("Memory Usage After Clone: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		lastEnteredDestination = destination;
		Core.debug("-------------------------------- BEGIN DIJKSTRA'S ----------------------------");
		Core.debug("Going from " + source + " to " + destination);
		Set<String> unsettledVertexes = new HashSet<String>(); // A hashset of all vertexes that have working distances from the source but are not confirmed and settled
		Set<String> settledVertexes = new HashSet<String>(); // A hashset of all vertexes that are settled and have distances from the source
		vertexMapMirror.get(source).setWeightedDistanceFromSource(0); // Starts by setting all distance values for the source node to 0
		vertexMapMirror.get(source).setDistanceFromSource(0);
		vertexMapMirror.get(source).setPreviousVertex(null); // Assigns the previous vertex of the source to null
		unsettledVertexes.add(source); // The source node is added to the list of unsettled vertexes
		while(unsettledVertexes.size() > 0){ // Loop until no vertexes are unsettled
			String workingVertex = getVertexWithLowestWeightedDistance(unsettledVertexes); // The current working vertex is assigned to the vertex in the unsettled vertex hashset with the shortest distance from any currently settled vertex
			if(workingVertex.equals(destination)){ // If the current working vertex is the destination then the route has been found
				String previousVertex = vertexMapMirror.get(workingVertex).getPreviousVertex(); // Gets the previous vertex before the destination
				Core.debug("  " + workingVertex);
				Core.debug("ROUTE FOUND:");
				while(previousVertex != null){ // Iterates back through each vertex in the route to print out the route taken
					Core.debug("  " + vertexMapMirror.get(previousVertex).getPreviousVertex() + " - " + getArcConnectingTwoVertexes(previousVertex, workingVertex).tagList.get("ref") + ", " + getArcConnectingTwoVertexes(previousVertex, workingVertex).tagList.get("name"));
					workingVertex = previousVertex;
					previousVertex = vertexMapMirror.get(previousVertex).getPreviousVertex(); // Gets the next vertex in the route
				}
				return; // Finishes the method
			}
			Core.debug("Using Vertex: " + workingVertex);
			unsettledVertexes.remove(workingVertex); // Transfers the current vertex from the unsettled hashset to the settled hashset
			settledVertexes.add(workingVertex);
			for(int i = 0; i < vertexMapMirror.get(workingVertex).arcList.size(); i++){ // Explore all arcs connected to the vertex
				String arc = vertexMapMirror.get(workingVertex).arcList.get(i); // Temporarily stores the current arc being used
				Core.debug("Exploring Arc: " + arc);
				Core.debug("  Start: " + arcMap.get(arc).getStart());
				Core.debug("  End: " + arcMap.get(arc).getEnd());
				if(workingVertex.equals(arcMap.get(arc).getStart()) && !settledVertexes.contains(arcMap.get(arc).getEnd())){ // Checking for the orientation of the arc being used: either start to end or end to start
					// Section for dealing with arcs from start to end
					if(arcMap.get(arc).getOneWay() == -1){ // Discards arc if the arc has the "oneway" tag set to -1 - can't be travelled down from start to end
						Core.debug("Ignoring Arc, Reverse One Way Road");
					}else{
						double workingDistance = arcMap.get(arc).getWeight() + vertexMapMirror.get(workingVertex).getDistanceFromSource(); // Sets the working distance to the current vertex's distance from the source + the weight of the arc
						double workingWeightedDistance = arcMap.get(arc).getWeightedDistance() + vertexMapMirror.get(workingVertex).getWeightedDistanceFromSource(); // Same as above but using weighted distance instead
						if(workingWeightedDistance < vertexMapMirror.get(arcMap.get(arc).getEnd()).getWeightedDistanceFromSource()){ // If the working weighted distance is less than the target vertexes current weighted distance
							Core.debug("  Modifying End Vertex - Distance From Source = " + workingDistance);
							Core.debug("  Modifying End Vertex - Weighted Distance From Source = " + workingWeightedDistance);
							vertexMapMirror.get(arcMap.get(arc).getEnd()).setDistanceFromSource(workingDistance);
							vertexMapMirror.get(arcMap.get(arc).getEnd()).setWeightedDistanceFromSource(workingWeightedDistance);// Assign working distances to target vertex
							vertexMapMirror.get(arcMap.get(arc).getEnd()).setPreviousVertex(workingVertex); // Assigns the current vertex as the target vertex's previous vertex
							unsettledVertexes.add(arcMap.get(arc).getEnd()); // If the target vertex is not present in the unsettled vertexes hashset then it is added - hashset doesn't accept duplicates
						}else{ // Ignore target vertex if the route to it is longer than its current distance from source
							Core.debug("  Leaving End Vertex - Distance From Source = " + vertexMapMirror.get(arcMap.get(arc).getEnd()).getDistanceFromSource() + ", Rejected Distance = " + workingDistance);
							Core.debug("  Leaving End Vertex - Weighted Distance From Source = " + vertexMapMirror.get(arcMap.get(arc).getEnd()).getWeightedDistanceFromSource() + ", Rejected Weighted Distance = " + workingWeightedDistance);
						}
					}
				}
				else{
					if(workingVertex.equals(arcMap.get(arc).getEnd()) && !settledVertexes.contains(arcMap.get(arc).getStart())){ // Checking for the orientation of the arc
						// Section for dealing with arcs from end to start
						if(arcMap.get(arc).getOneWay() == 1){ // Discards arc if it can't be travelled down from end to start
							Core.debug("Ignoring Arc, One Way Road");
						}else{
							double workingDistance = arcMap.get(arc).getWeight() + vertexMapMirror.get(workingVertex).getDistanceFromSource(); // Sets the working distance to the current vertex's distance from the source + the weight of the arc
							double workingWeightedDistance = arcMap.get(arc).getWeightedDistance() + vertexMapMirror.get(workingVertex).getWeightedDistanceFromSource(); // Same as above but using weighted distance instead
							if(workingWeightedDistance < vertexMapMirror.get(arcMap.get(arc).getStart()).getWeightedDistanceFromSource()){ // If the working weighted distance is less that the target vertexes current weighted distance
								Core.debug("  Modifying Start Vertex - Distance From Source = " + workingDistance);
								Core.debug("  Modifying Start Vertex - Weighted Distance From Source = " + workingWeightedDistance);
								vertexMapMirror.get(arcMap.get(arc).getStart()).setDistanceFromSource(workingDistance); 
								vertexMapMirror.get(arcMap.get(arc).getStart()).setWeightedDistanceFromSource(workingWeightedDistance); // Assigns the working distance to the target vertex
								vertexMapMirror.get(arcMap.get(arc).getStart()).setPreviousVertex(workingVertex); // Assigns the current vertex as the target vertex's previous vertex
								unsettledVertexes.add(arcMap.get(arc).getStart()); // If the target vertex is not present in the unsettled vertexes hashset then it is added - hashset doesn't accept duplicates
							}else{ // Ignore target vertex if the route to its longer than its current distance from source
								Core.debug("  Leaving Start Vertex - Distance From Source = " + vertexMapMirror.get(arcMap.get(arc).getStart()).getDistanceFromSource() + ", Rejected Distance = " + workingDistance);
								Core.debug("  Leaving Start Vertex - Weighted Distance From Source = " + vertexMapMirror.get(arcMap.get(arc).getStart()).getWeightedDistanceFromSource() + ", Rejected Weighted Distance = " + workingWeightedDistance);
							}
						}
					}
					else{
						Core.debug("Dead End: " + workingVertex);
					}
				}
				//vertexMapMirror.get(source).arcList.get(i)
			}
		}
		for(String vertexId : unsettledVertexes){
			Core.debug(vertexId);
		}
		return;
	}
	
	/**
	 * Returns the vertex ID in the given hashset with the lowest value for distanceFromSource.
	 * @param set - The hashset of vertex IDs to scan.
	 * @return The vertex ID in the hashset with the lowest value for distanceFromSource.
	 */
	@SuppressWarnings("unused")
	private String getVertexWithLowestDistance(Set<String> set){ // Returns the vertex with the lowest distance from source in a hashset
		Double minimum = null; // Using the double wrapper class so that it can be assigned a null value for when first initialised
		String minimumVertex = null;
		for(String vertexId : set){ // Iterates through every vertex id in the hashset
			Vertex tempVertex = vertexMapMirror.get(vertexId); // Stores the vertex currently used in a temporary variable
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
	
	/**
	 * Returns the vertex ID in the given hashset with the lowest value for weightedDistanceFromSource.
	 * @param set - The hashset of vertex IDs to scan.
	 * @return The vertex ID in the hashset with the lowest value for weightedDistanceFromSource.
	 */
	private String getVertexWithLowestWeightedDistance(Set<String> set){ // As above but using weighted distances instead of distance
		Double minimum = null;
		String minimumVertex = null;
		for(String vertexId : set){
			Vertex tempVertex = vertexMapMirror.get(vertexId);
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
	
	/**
	 * Iterates through the arc hashmap and calculates both distance and weighted distance for each arc. Then assigns the values to each arc.
	 */
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
			//Core.debug("Way Id: " + tempArc.id);
			//Core.debug("  Dist: " + distance);
			//Core.debug("  Estimated Max Speed: " + getMaxSpeed(tempArc) + " kph"); 
			weightedDistance = distance/(getAverageSpeed(tempArc)); // Calculates weighted distance which is equal to time in hours of road by dividing distance by the average speed
			//Core.debug("  Weighted Distance: " + weightedDistance); 
			tempArc.setWeightedDistance(weightedDistance); // Assigns the weighted distance to the arc being used 
		}
	}
	
	/**
	 * Returns the the average speed of an arc based on its tag information.
	 * @param arc - The arc to use.
	 * @return The average speed of the arc passed.
	 */
	private double getAverageSpeed(Arc arc){ // Returns the max speed in km/hr of a give arc
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
	
	/**
	 * Iterates through the arc hashmap and marks any one way streets accordingly.
	 */
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
	
	/**
	 * Returns the Arc object that has both vertex IDs as its start and end.
	 * @param vertexId1 - The first vertex ID to use.
	 * @param vertexId2 - The second vertex ID to use.
	 * @return The Arc object that connects the two vertexes, else returns null.
	 */
	private Arc getArcConnectingTwoVertexes(String vertexId1, String vertexId2){ // Finds the id of an arc connecting two vertexes together
		Vertex vertex1 = vertexMapMirror.get(vertexId1);
		for(int i = 0; i < vertex1.arcList.size(); i++){ // Iterates through every arc connected to the first vertex
			if(arcMap.get(vertex1.arcList.get(i)).getStart().equals(vertexId2) || arcMap.get(vertex1.arcList.get(i)).getEnd().equals(vertexId2)){ // Checks if the current arc also has the second vertex as a member
				return arcMap.get(vertex1.arcList.get(i)); // If so return that arc
			}
		}
		return null;
	}
	
	/**
	 * Converts the vertexMapMirror and arcMapMirror into directions that can be easily followed.
	 * @return A string of directions.
	 */
	public String convertGraphToDirections(){ // Converts the current vertexMap to directions
		StringBuilder output = new StringBuilder(); // The overall output stringbuilder that is added to with each direction
		String workingVertex = lastEnteredDestination;
		String previousVertex = vertexMapMirror.get(workingVertex).getPreviousVertex();
		ArrayList<String> reverseRoute = new ArrayList<String>(); // The route stored in reverse order
		while(previousVertex != null){
			reverseRoute.add(workingVertex);
			workingVertex = previousVertex;
			previousVertex = vertexMapMirror.get(previousVertex).getPreviousVertex(); // Working backwards along the route to find each vertex used
		}
		reverseRoute.add(workingVertex); // Adds the first vertex to the route
		Arc currentArc;
		String currentRoadRef = "!novalue"; // !novalue acts as null in this situation as null is an acceptable value
		String currentRoadName = "!novalue"; // These two variables store the values of ref and name tags of the current road
		boolean inRoundabout = false; // True if the current vertex is in a roundabout
		boolean compassDirection = false; // A marker for if the direction is a "Travel east" direction
		String roundaboutEntry = ""; // Stores the value of the first vertex's id in a roundabout
		double previousDirectionDistance = 0;
		int directionCounter = 1;
		for(int i = reverseRoute.size() - 1; i >= 0; i--){ // Iterates through every vertex in the route
			compassDirection = false; // Resets the marker for if the direction is a "Travel north" direction on each new vertex
			if(i != 0){ // Ensures no arc is retrieved using only one vertex - can't do i - 1 if i == 0
				currentArc = getArcConnectingTwoVertexes(reverseRoute.get(i), reverseRoute.get(i-1));
				if(!inRoundabout){ // Checks the vertex isnt in a roundabout
					if(currentArc.tagList.get("junction") == null){ // Checks the vertex isn't the first in a roundabout
						if(!Utilities.checkStringsAreEqual(currentRoadRef, currentArc.tagList.get("ref"))){ // If the ref is of a new road it means a junction has been found
							if(i != reverseRoute.size() - 1){// Ensures this is not the first direction
								if(vertexMapMirror.get(reverseRoute.get(i)).arcList.size() > 2){ // Checks if the vertex actually has more than two arcs coming off it - only two means there isnt any choice of where to turn - no direction required
									output.append(directionCounter + ") After " + Utilities.round((vertexMapMirror.get(reverseRoute.get(i)).getDistanceFromSource() - previousDirectionDistance),2) + " km, "); // Adds "After x km," to the string, x is calculated from the last time a direction is called
									previousDirectionDistance = vertexMapMirror.get(reverseRoute.get(i)).getDistanceFromSource(); // Assigns the distance from the source of the current vertex to previousDirectionDistance so that the next direction can measure the distance since the previous direction
									output.append("turn " + calculateDirection(reverseRoute.get(i + 1), reverseRoute.get(i), reverseRoute.get(i - 1)) + " onto "); // Adds the turn direction to the output using the previous, current and next vertexes
									directionCounter++;
								}
							}else{ // Triggered if the instruction is the first instruction
								output.append(directionCounter + ") Travel " + getBearingString(reverseRoute.get(i),reverseRoute.get(i-1)) + " along "); // Adds the first direction format of "Travel north along "
								compassDirection = true; // A marker to ensure that the road name adding section is executed even though the direction is not a turning
								directionCounter++;
							}
							currentRoadRef = currentArc.tagList.get("ref"); // Stores the current road ref and name for use in later comparisons
							currentRoadName = currentArc.tagList.get("name"); // ^
							if(vertexMapMirror.get(reverseRoute.get(i)).arcList.size() > 2 || compassDirection){ // Only triggers if the turning can have more than one option - one option is not a turning
								if (currentRoadRef == null) {
									if (currentRoadName == null) {
										Core.debug("Unnamed Road");
										output.append("Unnamed Road\n"); // If neither ref or name have values then the road is unnamed
									} else {
										Core.debug(currentRoadName);
										output.append(currentRoadName + "\n"); // Uses name if it has a value and ref doesnt
									}
								} else {
									Core.debug(currentRoadRef);
									output.append(currentRoadRef + "\n"); // Uses ref if it has a value
								}
							}
						} else {
							if (currentRoadRef == currentArc.tagList.get("ref")) { // If the refs are equal but they're both null then use names instead, otherwise its the same road
								if (!Utilities.checkStringsAreEqual(currentRoadName, currentArc.tagList.get("name"))) { // If ref is the same compare name instead
									if (i != reverseRoute.size() - 1) { // Ensures this isnt the first direction
										if (vertexMapMirror.get(reverseRoute.get(i)).arcList.size() > 2) { // Check for more than two arcs - reduces false positives
											output.append(directionCounter 
													+ ") After "
													+ Utilities.round((vertexMapMirror.get(reverseRoute.get(i))
															.getDistanceFromSource() - previousDirectionDistance), 2)
													+ " km, ");
											previousDirectionDistance = vertexMapMirror.get(reverseRoute.get(i))
													.getDistanceFromSource();
											output.append("turn " + calculateDirection(reverseRoute.get(i + 1),
													reverseRoute.get(i), reverseRoute.get(i - 1)) + " onto ");
											directionCounter++;
										}
									} else {
										output.append(directionCounter + ") Travel "
												+ getBearingString(reverseRoute.get(i), reverseRoute.get(i - 1))
												+ " along ");
										compassDirection = true;
										directionCounter++;
									}
									currentRoadName = currentArc.tagList.get("name");
									if (vertexMapMirror.get(reverseRoute.get(i)).arcList.size() > 2
											|| compassDirection) {
										if (currentRoadName == null) { // Only needs to check for a name as this code block is only executed if ref is already null
											Core.debug("Unnamed Road");
											output.append("Unnamed Road\n");
										} else {
											Core.debug(currentRoadName);
											output.append(currentRoadName + "\n");
										}
									}
								} 
							}
						}
					} else { // Triggered if the vertex has a value for the "junction" tag
						if(Utilities.checkStringsAreEqual(currentArc.tagList.get("junction"), "roundabout")){
							inRoundabout = true; // Ensures no directions such as "turn left" are triggered until the roundabout is cleared
							roundaboutEntry = reverseRoute.get(i-1); // Roundabout entry is the first vertex which lies on the roundabout
						}
					}
				} else { // Triggered when the marker for being in a roundabout is true
					if(currentArc.tagList.get("junction") == null){ // Checks if the current arc is no longer in the roundabout - otherwise do nothing and go to the next arc
						inRoundabout = false; // Flags the function is no longer in a roundabout and normal directions can continue
						output.append(directionCounter + ") After " + Utilities.round((vertexMapMirror.get(reverseRoute.get(i)).getDistanceFromSource() - previousDirectionDistance),2) + " km, "); // Calculates the distance since the last direction
						previousDirectionDistance = vertexMapMirror.get(reverseRoute.get(i)).getDistanceFromSource(); // Assigns the current vertex's distance from source as the direction distance
						output.append("take the " + Utilities.formatNumberToPlace(getExitOnRoundabout(roundaboutEntry, reverseRoute.get(i-1))) + " exit on the roundabout, onto "); // Works out the exit on the roundabout by using the first vertex on the roundabout and the first vertex after that isnt
						currentRoadRef = currentArc.tagList.get("ref");
						currentRoadName = currentArc.tagList.get("name");
						directionCounter++;
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
		calculatedRouteDistance = vertexMapMirror.get(reverseRoute.get(0)).getDistanceFromSource();
		calculatedRouteTime = vertexMapMirror.get(reverseRoute.get(0)).getWeightedDistanceFromSource();
		output.append("You will have arrived at your destination\n");
		return output.toString().replaceAll("&apos;", "'");
	}
	/**
	 * Calculates the angle between two vertexes relative to north.
	 * @param vertexId1 - The first vertex ID to use.
	 * @param vertexId2 - The second vertex ID to use.
	 * @return The angle between two vertexes in radians.
	 */
	private double calculateBearing(String vertexId1, String vertexId2){ // Calculates the angle between two vertexes relative to north
		Vertex vertex1 = vertexMapMirror.get(vertexId1);
		Vertex vertex2 = vertexMapMirror.get(vertexId2);
		double lat1 = Math.toRadians(vertex1.getLat());
		double lon1 = Math.toRadians(vertex1.getLon());
		double lat2 = Math.toRadians(vertex2.getLat());
		double lon2 = Math.toRadians(vertex2.getLon());
		double temp1 = Math.cos(lat2) * Math.sin(lon2 - lon1);
		double temp2 = (Math.cos(lat1) * Math.sin(lat2)) - (Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
		double bearing = Math.atan2(temp1, temp2); // http://www.movable-type.co.uk/scripts/latlong.html#bearing
		if(bearing < 0){
			bearing += 2 * Math.PI; // Ensures no negative values are created, which would break other parts of the system, by adding 2 pi as a circle contains 2 pi radians
		}
		return bearing;
	}
	
	/**
	 * Gets a string of the bearing between two vertexes.
	 * @param vertexId1 - The first vertex ID to use.
	 * @param vertexId2 - The second vertex ID to use.
	 * @return A string of the direction between two vertexes, north, south, east, west or invalid.
	 */
	private String getBearingString(String vertexId1, String vertexId2){ // Returns a string based bearing between two vertexes
		double bearing = calculateBearing(vertexId1, vertexId2);
		if(((7 * Math.PI) / 4 <= bearing && bearing <= 2 * Math.PI ) || (bearing >= 0 && bearing < (Math.PI / 4))){ // Northern quadrant contains from 7/4 pi to 0 and 0 to 1/4 pi
			return "north";
		}
		if(Math.PI / 4 <= bearing && bearing < (3 * Math.PI) / 4){ // Eastern quadrant contains 1/4 pi to 3/4 pi
			return "east";
		}
		if((3 * Math.PI) / 4 <= bearing && bearing < (5 * Math.PI) / 4){ // Southern quadrant contains 3/4 pi to 5/4 pi
			return "south";
		}
		if((5 * Math.PI) / 4 <= bearing && bearing < (7 * Math.PI) / 4){ // Western quadrant contains 5/4 pi to 7/4 pi
			return "west";
		}
		return "invalid";
	}
	
	/**
	 * Calculates the direction change between three vertexes from vertex 1 to 2 to 3.
	 * @param vertexId1 - The first vertex ID to use.
	 * @param vertexId2 - The second vertex ID to use.
	 * @param vertexId3 - The third vertex ID to use.
	 * @return A string of either right, left or invalid.
	 */
	private String calculateDirection(String vertexId1, String vertexId2, String vertexId3){
		double directionValue = calculateBearing(vertexId1,vertexId2) - calculateBearing(vertexId2,vertexId3); // Gets the difference between the two bearings
		if((-Math.PI < directionValue && directionValue < 0) || (Math.PI < directionValue && directionValue < 2 * Math.PI)){
			return "right";
		}
		if((0 < directionValue && directionValue < Math.PI) || (-2 * Math.PI < directionValue && directionValue < -Math.PI)){
			return "left";
		}
		return "Invalid"; // If value is out of range, the bearings supplied are incorrect
	}


	/**
	 * Gets the number of valid exits that occur travelling around a roundabout before the given exit is reached.
	 * @param startingVertex - The ID of the first vertex to use on the roundabout.
	 * @param exitVertex - The ID of the exit vertex, not on the roundabout.
	 * @return An integer of the number of valid exits that occur before reaching the given exit.
	 */
	private int getExitOnRoundabout(String startingVertex, String exitVertex){ // Returns the number of exits that occur before a given vertex is reached on a roundabout
		Core.debug("Start: " + startingVertex + ", End: " + exitVertex);
		String currentVertex = startingVertex;
		int exits = 0;
		String nextVertex = "";
		do{
			for(int i = 0; i < vertexMapMirror.get(currentVertex).arcList.size(); i++){ // Iterates through every arc on the vertex
				String currentArc = vertexMapMirror.get(currentVertex).arcList.get(i);
				Core.debug("Inspecting: " + currentArc + ", on " + currentVertex);
				if(!Utilities.checkStringsAreEqual(arcMap.get(currentArc).tagList.get("junction"), "roundabout")){ // Checks if the arc is not on the roundabout - means its either an exit or not traversable
					if(arcMap.get(currentArc).getStart().equals(currentVertex) && arcMap.get(currentArc).getOneWay() != -1){ // True if arc is traversable - therefore an exit
						Core.debug(currentArc + " is an exit");
						exits++;
						if(arcMap.get(currentArc).getEnd().equals(exitVertex)){ // If the end of the exit is the destination then the function is complete
							return exits;
						}
					}
					if(arcMap.get(currentArc).getEnd().equals(currentVertex) && arcMap.get(currentArc).getOneWay() != 1){
						Core.debug(currentArc + " is an exit");
						exits++;
						if(arcMap.get(currentArc).getStart().equals(exitVertex)){ // Same as previous block but for inverted one way arcs
							return exits;
						}
					}
				}else{ // If arc is on roundabout check for the arc that leads to the next vertex to check
					if(arcMap.get(currentArc).getStart().equals(currentVertex)){ // Vertexes are stored in order of way round a roundabout so there is only one arc with a start and end round the correct way - checks for this
						nextVertex = arcMap.get(currentArc).getEnd(); // Assigns the next vertex to go to
						Core.debug("Discovered next vertex: " + nextVertex);
					}								
				}
			}
			currentVertex = nextVertex;
		}while(!currentVertex.equals(startingVertex)); // Base case of if the roundabout has been traversed all the way around
		return exits;
	}
	

	/**
	 * Creates a new vertex hashmap identical to the given one, except cleansed of any distance values.
	 * @param vertexMapToClone - The vertex map to clone.
	 * @return A new vertex hashmap cleansed of any distance values.
	 */
	private HashMap<String,Vertex> cloneVertexMap(HashMap<String,Vertex> vertexMapToClone){
		final double startTime = System.nanoTime(); // Gets the start time of the operation in nanoseconds
		HashMap<String,Vertex> clone = new HashMap<String,Vertex>(); // Creates a new hashmap to store the clones into
		for(String key : vertexMapToClone.keySet()){ // Iterates through every vertex in the vertexMap
			clone.put(key, vertexMapToClone.get(key).clone()); // Puts each cloned vertex into the new vertex map
		}
		final double duration = System.nanoTime() - startTime; // Gets the time taken for the operation to complete in nanoseconds
		Core.debug("Time To Clone: " + duration/1000000000 + " seconds");
		return clone; // Returns the new, cloned, hashmap
	}
	
}

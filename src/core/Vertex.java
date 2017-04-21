package core;

import java.util.ArrayList;

/**
 * A class for storing information to do with nodes/vertexes on the weighted graph.
 * @author Jacob Pennington
 *
 */
public class Vertex {

	public ArrayList<String> arcList; // A list of all the arcs that this vertex is a member of
	private String id; // The id of the vertex
	private double lat; // The latitude of the vertex
	private double lon; // The longitude of the vertex
	private double distanceFromSource = Double.MAX_VALUE; // Distance from source is set by default as high as possible to ensure that Dijkstra's algorithm still recognises it as a number but does not use it in a route
	private double weightedDistanceFromSource = Double.MAX_VALUE; // Same as above but with weighted distance
	private String previousVertex; // The id of the previous vertex in the shortest route from one vertex to another

	/**
	 * A constructor for creating a vertex without a list of connected arcs.
	 * @param id - The ID of the vertex.
	 * @param lat - The latitude of the point.
	 * @param lon - The longitude of the point.
	 */
	public Vertex(String id,double lat, double lon){
		this.setId(id);
		this.setLat(lat);
		this.setLon(lon);
		this.arcList = new ArrayList<String>(); // Initialises the arraylist 
	}
	
	/**
	 * A constructor for creating a vertex with a list of connected arcs.
	 * @param id - The ID of the vertex.
	 * @param lat - The latitude of the point.
	 * @param lon - The longitude of the point.
	 * @param arcList - A list of IDs of all ways connected to the arc.
	 */
	public Vertex(String id, double lat, double lon, ArrayList<String> arcList){
		this.setId(id);
		this.setLat(lat);
		this.setLon(lon);
		this.arcList = arcList;
	}
	
	/**
	 * Outputs the information about the vertex to the console/output file.
	 */
	public void printInfo(){
		System.out.println("ID: " + getId() + ", lat: " + getLat() + ", lon: " + getLon());
	}
	
	/**
	 * Clones the vertex, omitting the distance fields.
	 */
	@Override
	public Vertex clone(){
		return new Vertex(this.id,this.lat,this.lon,this.arcList);
	}
	
	///////////////////////// GETTERS AND SETTERS ////////////////////////////////
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getDistanceFromSource() {
		return distanceFromSource;
	}

	public void setDistanceFromSource(double distanceFromSource) {
		if(distanceFromSource < 0){ // Ensures that the distance cannot be lower than 0
			return;
		}
		this.distanceFromSource = distanceFromSource;
	}
	
	public double getWeightedDistanceFromSource() {
		return weightedDistanceFromSource;
	}

	public void setWeightedDistanceFromSource(double weightedDistanceFromSource) {
		if(weightedDistanceFromSource < 0){ // Ensures that the distance cannot be lower than 0
			return;
		}
		this.weightedDistanceFromSource = weightedDistanceFromSource;
	}

	public String getPreviousVertex() {
		return previousVertex;
	}

	public void setPreviousVertex(String previousVertex) {
		this.previousVertex = previousVertex;
	}

}
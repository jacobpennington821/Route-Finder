package core;

import java.util.ArrayList;

public class Vertex {

	public ArrayList<String> arcList; // A list of all the arcs that this vertex is a member of
	private String id; // The id of the vertex
	private double lat; // The latitude of the vertex
	private double lon; // The longitude of the vertex
	private double distanceFromSource = Double.MAX_VALUE; // Distance from source is set by default as high as possible to ensure that Dijkstra's algorithm still recognises it as a number but does not use it in a route
	private double weightedDistanceFromSource = Double.MAX_VALUE; // Same as above but with weighted distance
	private String previousVertex; // The id of the previous vertex in the shortest route from one vertex to another

	
	public Vertex(String id,double lat, double lon){
		this.setId(id);
		this.setLat(lat);
		this.setLon(lon);
		this.arcList = new ArrayList<String>(); // Initialises the arraylist 
	}
	
	public void printInfo(){
		System.out.println("ID: " + getId() + ", lat: " + getLat() + ", lon: " + getLon());
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
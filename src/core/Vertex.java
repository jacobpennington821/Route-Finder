package core;

import java.util.ArrayList;

public class Vertex {

	public ArrayList<String> arcList;
	private String id;
	private double lat;
	private double lon;
	private double distanceFromSource = Double.MAX_VALUE;
	private String previousVertex;
	
	public Vertex(String id){
		this.setId(id);
		this.arcList = new ArrayList<String>();
	}
	
	public Vertex(String id,double lat, double lon){
		this.setId(id);
		this.setLat(lat);
		this.setLon(lon);
		this.arcList = new ArrayList<String>();
	}
	
	public void printInfo(){
		System.out.println("ID: " + getId() + ", lat: " + getLat() + ", lon: " + getLon());
	}

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
		if(distanceFromSource < 0){
			return;
		}
		this.distanceFromSource = distanceFromSource;
	}

	public String getPreviousVertex() {
		return previousVertex;
	}

	public void setPreviousVertex(String previousVertex) {
		this.previousVertex = previousVertex;
	}

}
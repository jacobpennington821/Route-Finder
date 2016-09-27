package core;

import java.util.ArrayList;

public class Vertex {

	public ArrayList<Arc> arcList;
	private String id;
	private double lat;
	private double lon;
	
	public Vertex(String id){
		this.setId(id);
		this.arcList = new ArrayList<Arc>();
	}
	
	public Vertex(String id,double lat, double lon){
		this.setId(id);
		this.setLat(lat);
		this.setLon(lon);
		this.arcList = new ArrayList<Arc>();
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

}
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
		this.lat = lat;
		this.lon = lon;
		this.arcList = new ArrayList<Arc>();
	}
	
	public void printInfo(){
		System.out.println("ID: " + getId() + ", lat: " + lat + ", lon: " + lon);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
package core;

import java.util.ArrayList;

public class Vertex {

	private ArrayList<Arc> arcList;
	private String id;
	private double lat;
	private double lon;
	
	public Vertex(String id){
		this.id = id;
		this.arcList = new ArrayList<Arc>();
	}
	
	public Vertex(String id,double lat, double lon){
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.arcList = new ArrayList<Arc>();
	}
	
	public void printInfo(){
		System.out.println("ID: " + id + ", lat: " + lat + ", lon: " + lon);
	}

}
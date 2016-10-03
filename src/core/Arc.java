package core;

import java.util.HashMap;

public class Arc {
	
	private String start;
	private String end;
	public String id;
	private float weight;
	public HashMap<String,String> tagList = new HashMap<String, String>();

	
	public Arc(String start, String end, String id, HashMap<String,String> tagList){
		this.start = start;
		this.end = end;
		this.id = id;
		this.tagList = tagList;
		this.calculateWeight();
	}
	
	private float calculateWeight(){
		//d = 2r * arcsin(sqrt(sin2((lat2inrad - lat1inrad) /2) + cos(lat1inrad) * cos(lat2inrad) * sin2((long2inrad - long1rad) /2)));
		return 1;
	}
}

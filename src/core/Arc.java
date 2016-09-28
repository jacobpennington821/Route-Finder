package core;

import java.util.HashMap;

public class Arc {
	
	private String start;
	private String end;
	public String id;
	private int weight;
	public HashMap<String,String> tagList = new HashMap<String, String>();

	
	public Arc(String start, String end, String id, HashMap<String,String> tagList){
		this.start = start;
		this.end = end;
		this.id = id;
		this.tagList = tagList;
	}
}

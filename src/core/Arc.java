package core;

import java.util.HashMap;

public class Arc {
	
	private String start;
	private String end;
	public String id;
	public double weight;
	public double weightedDistance;
	public HashMap<String,String> tagList = new HashMap<String, String>();
	private int oneWay = 0;

	
	public Arc(String start, String end, String id, HashMap<String,String> tagList){
		this.setStart(start);
		this.setEnd(end);
		this.id = id;
		this.tagList = tagList;
	}
		
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}
	
	public void setWeightedDistance(double weightedDistance){
		this.weightedDistance = weightedDistance;
	}
	
	public double getWeightedDistance(){
		return weightedDistance;
	}

	public int getOneWay() {
		return oneWay;
	}

	public void setOneWay(int oneWay) {
		this.oneWay = oneWay;
	}
}

package core;

import java.util.HashMap;

/**
 * The class for storing information about arcs connecting two vertices.
 * @author Jacob Pennington
 *
 */
public class Arc {
	
	private String start; // A string that stores the id of the node at the start of the arc
	private String end; // Same as above but with the end
	public String id; // The id of the arc
	public double weight; // The length of the arc with no weighting
	public double weightedDistance; // The length of the arc weighted by maximum speed
	public HashMap<String,String> tagList = new HashMap<String, String>(); // A hashmap of all the tags attributed to the arc
	private int oneWay = 0; // If the arc is one way - 1 means one way from start to end, -1 means one way from end to start, 0 means not one way

	/**
	 * Constructs an arc with the given information.
	 * @param start - The ID of the vertex the arc starts on.
	 * @param end - The ID of the vertex the arc ends on.
	 * @param id - The ID of the arc.
	 * @param tagList - A hashmap of all the tags the arc has.
	 */
	public Arc(String start, String end, String id, HashMap<String,String> tagList){
		this.setStart(start);
		this.setEnd(end);
		this.id = id;
		this.tagList = tagList;
	}
	
	////////////////////// GETTERS AND SETTERS //////////////////////////////////
	
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

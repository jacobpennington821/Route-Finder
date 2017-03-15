package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

// The Parser is the section of program dedicated to converting OSM XML files to a Graph Datatype that can then be manipulated

public class Parser {
	
	URL path = getClass().getResource("defaultMap.osm"); // File location to load

	File xmlFile;
	int nodeCounter = 0;
	int wayCounter = 0;
	HashMap<String,Vertex> vertexMap = new HashMap<String,Vertex>();
	HashMap<String,Arc> arcMap = new HashMap<String,Arc>();
	boolean inWay = false;
	TempWay tempWay;
	Graph map;
	
	private class TempWay{ // TempWay is a temporary class for use in my parser as it stores data to do with arcs.
		String id;
		public TempWay(String id){
			this.id = id;
		}
		public ArrayList<String> nodeList = new ArrayList<String>(); // List of all nodes in the arc
		public HashMap<String,String> tagList = new HashMap<String, String>(); // List of all tags and their values
	}
	
	public Parser(){
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream("output.log")); // Ensures that all console output is redirected to a text file for reviewing
			System.setOut(out);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			xmlFile = new File(path.toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		this.map = this.parseXMLtoGraph(xmlFile); // Immediately parses the xml file once the class is initialised

	}
	
	public Graph parseXMLtoGraph(File XMLfile){
		Graph map = null;
		try (BufferedReader br = new BufferedReader(new FileReader(xmlFile))){
			String line;
			while ((line = br.readLine()) != null){ // Loops through every line in the XML file
				parseLine(line); // Individually parses each line
				//Core.debug(line);
			}

			map = new Graph(vertexMap, arcMap); // Constructs a new graph using the extracted information
			return map; // Returns the graph datatype 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Core.debug("File Not Found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null; // Will return null if any errors occur
	}
	
	public void parseLine(String line){ // Individually works on one line and extracts useful information
		if(line.length() > 8){ // Discount any lines like </way>
			if(!inWay){ // Ensures the line being parsed is not one inside a <way> block
				if(line.substring(3, 7).equals("node")){ // Checks if the line being parsed is a <node> line
					String id = line.substring(12, line.indexOf("\"", 12)); // Extracts the id of the node - always in the same place - index 12 to the first set of double quotes
					int latLocationInString = line.indexOf("lat=\"") + 5; // Finds the index of lat=" in the string
					int lonLocationInString = line.indexOf("lon=\"") + 5; // Finds the index of lon=" in the string
					double lat = Double.parseDouble(line.substring(latLocationInString,latLocationInString + line.substring(latLocationInString).indexOf("\""))); // Finds the latitude of the node in the string and then parses it to a double
					double lon = Double.parseDouble(line.substring(lonLocationInString,lonLocationInString + line.substring(lonLocationInString).indexOf("\""))); // Same as above but with longitude
					nodeCounter++;
					vertexMap.put(id,new Vertex(id,lat,lon)); // Add a new vertex to the hashmap of vertexes using its id as a reference, also constructs a vertex using the data extracted
					//Core.debug("node" + id);
					//Core.debug("  " + Double.toString(lat));
					//Core.debug("  " + Double.toString(lon));
					
				}else{
					if(line.substring(3,7).equals("way ")){ // Checks if the line being parsed is a <way> line
						inWay = true; // Sets the flag to make sure the next line parsed knows its inside a <way> block
						wayCounter++;
						String tempId = line.substring(11, 11 + line.substring(11).indexOf("\"")); // Extracts the id of the way - always in the same place - index 11 to the first set of double quotes
						tempWay = new TempWay(tempId); // Creates a temporary way datatype held outside the function to edit when the contents of the way are parsed in the next lines
						//Core.debug(tempId);
						//Core.debug("way" + wayCounter);
					}
					//Core.debug(line.substring(3, 7));
				}
			}else{
				if(line.substring(5,7).equals("nd")){ // If inside a <way> block and on a <nd> line
					//Core.debug("node line");
					tempWay.nodeList.add(line.substring(13, 13 + line.substring(13).indexOf('"'))); // Adds the node reference to the list of nodes stored in the temporary way
				}else{
					if(line.substring(5,7).equals("ta")){ // If inside a <way> block and on a <tag> line
						//Core.debug("tag line");
						int keyIndex = line.indexOf("k=\"") + 3; // Find the location in the string of k=" which indicates the start of the tag key
						String key = line.substring(keyIndex,keyIndex + line.substring(keyIndex).indexOf('"')); // Extracts the tag key from the keyIndex to the next set of double quotes
						int valueIndex = line.indexOf("v=\"") + 3; // Find the location in the string of v=" which indicates the start of the tag value
						String value = line.substring(valueIndex,valueIndex + line.substring(valueIndex).indexOf('"')); // Extracts the tag value from the valueIndex to the next set of double quotes
						//Core.debug("  " + key + ": " + value);
						tempWay.tagList.put(key, value); // Add the key value pair to the hashmap of tags
					}else{
						inWay = false; // If in a <way> block and encounters a line not recognised - assumed to be quitting out of the <way> block - updates boolean flag accordingly
						for(int i = 0; i < tempWay.nodeList.size() - 1; i++){ // For every node inside the temporary way create a new Arc between each individual pair of nodes
							String start = tempWay.nodeList.get(i);
							String end = tempWay.nodeList.get(i + 1);
							String id = tempWay.id + "-" + i; // Assigns an id to the broken down road 1234 gets broken down to 1234-0, 1234-1 etc
							Arc tempArc = new Arc(start,end,id,tempWay.tagList); // Creates a new arc using the data extracted
							arcMap.put(id, tempArc); // Adds the new arc to the hashmap of arcs
							vertexMap.get(start).arcList.add(id); // Updates the vertexes that are contained in each arc, giving them the ids of all arcs they are included in
							vertexMap.get(end).arcList.add(id);
							//Core.debug(start + ", " + end + ", " + id);
						}
						// Construct Arcs
						parseLine(line); // Calls itself with the new perspective that it is not in a <way> block, and so makes sure no important information such as a new <way> block or a <node> was missed
					}
				}
			}
		}
	}
}

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

public class Parser {
	
	URL path = getClass().getResource("wiltshireRoadsNoRelations.osm");

	File xmlFile;
	int nodeCounter = 0;
	int wayCounter = 0;
	//ArrayList<Vertex> vertexMap = new ArrayList<Vertex>();
	HashMap<String,Vertex> vertexMap = new HashMap<String,Vertex>();
	HashMap<String,Arc> arcMap = new HashMap<String,Arc>();
	ArrayList<Arc> arcTest = new ArrayList<Arc>();
	boolean inWay = false;
	TempWay tempWay;
	//ArrayList<String> nodeList = new ArrayList<String>();
	
	private class TempWay{
		String id;
		public TempWay(String id){
			this.id = id;
		}
		public ArrayList<String> nodeList = new ArrayList<String>();
		public HashMap<String,String> tagList = new HashMap<String, String>();
	}
	
	public Parser(){
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream("output.log"));
			System.setOut(out);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			xmlFile = new File(path.toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

	}
	
	public Graph parseXMLtoGraph(File XMLfile){
		try (BufferedReader br = new BufferedReader(new FileReader(xmlFile))){
			String line;
			while ((line = br.readLine()) != null){
				parseLine(line);
				//Core.debug(line);
			}

		Graph map = new Graph(vertexMap, arcMap);
		return map;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Core.debug("File Not Found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void parseLine(String line){
		if(line.length() > 8){ // Discount any lines like </way>
			if(!inWay){
				if(line.substring(3, 7).equals("node")){
					String id = line.substring(12, line.indexOf("\"", 12));
					int latLocationInString = line.indexOf("lat=\"") + 5;
					int lonLocationInString = line.indexOf("lon=\"") + 5;
					double lat = Double.parseDouble(line.substring(latLocationInString,latLocationInString + line.substring(latLocationInString).indexOf("\"")));
					double lon = Double.parseDouble(line.substring(lonLocationInString,lonLocationInString + line.substring(lonLocationInString).indexOf("\"")));
					nodeCounter++;
					vertexMap.put(id,new Vertex(id,lat,lon));
					//Core.debug("node" + line.substring(12, line.indexOf('"', 12)));
					//Core.debug(lat);
					//Core.debug(lon);
					
				}else{
					if(line.substring(3,7).equals("way ")){
						inWay = true;
						wayCounter++;
						String tempId = line.substring(11, 11 + line.substring(11).indexOf("\""));
						tempWay = new TempWay(tempId);
						Core.debug(tempId);
						//find next lines
						Core.debug("way" + wayCounter);
					}
					//Core.debug(line.substring(3, 7));
				}
			}else{
				if(line.substring(5,7).equals("nd")){
					Core.debug("node line");
					tempWay.nodeList.add(line.substring(13, 13 + line.substring(13).indexOf('"')));
				}else{
					if(line.substring(5,7).equals("ta")){
						Core.debug("tag line");
						int keyIndex = line.indexOf("k=\"") + 3;
						String key = line.substring(keyIndex,keyIndex + line.substring(keyIndex).indexOf('"'));
						int valueIndex = line.indexOf("v=\"") + 3;
						String value = line.substring(valueIndex,valueIndex + line.substring(valueIndex).indexOf('"'));
						Core.debug("  " + key + ": " + value);
						tempWay.tagList.put(key, value);
					}else{
						inWay = false;
						for(int i = 0; i < tempWay.nodeList.size() - 1; i++){
							String start = tempWay.nodeList.get(i);
							String end = tempWay.nodeList.get(i + 1);
							String id = tempWay.id + "-" + i;
							Arc tempArc = new Arc(start,end,id,tempWay.tagList);
							arcMap.put(id, tempArc);
							vertexMap.get(start).arcList.add(id);
							vertexMap.get(end).arcList.add(id);
							Core.debug(start + ", " + end + ", " + id);
							/*for(int it = 0; it < vertexMap.get(start).arcList.size(); it++){
								Core.debug("Start arcs: " + vertexMap.get(start).arcList.get(it));
								
							}
							for(int it = 0; it < vertexMap.get(end).arcList.size(); it++){
								Core.debug("End arcs: " + vertexMap.get(end).arcList.get(it));
								
							}*/
						}
						// Construct Arcs
						parseLine(line);
					}
				}
			}
		}
	}
}

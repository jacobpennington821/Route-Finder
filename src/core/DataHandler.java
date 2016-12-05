package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class DataHandler {
	
	private String origin;
	private String destination;
	private Parser parser;
	
	public DataHandler(String origin, String destination, Parser parser){
		this.origin = origin;
		this.destination = destination;
		this.parser = parser;
		this.convertInputToNodeId(this.origin);
		this.convertInputToNodeId(this.destination);

	}
	
	public DataHandler(Parser parser){
		this.parser = parser;
	}
	
	public String convertInputToNodeId(String name){
		String nodeId = "";
		if(isANumber(name)){
			nodeId = name.trim();
			if(parser.vertexMap.get(nodeId) != null){
				return nodeId;
			}
			return "!presence";
		}
		String urlString = "http://nominatim.openstreetmap.org/search?q=" + name.replace(' ', '+') + "&format=xml&addressdetails=1";
		URL website;
		try {
			website = new URL(urlString);
			URLConnection connection = website.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder xmlResponse = new StringBuilder();
			String outputLine;
			while((outputLine = reader.readLine()) != null){
				xmlResponse.append(outputLine);
			}
			Core.debug(xmlResponse);
			
			if(xmlResponse.indexOf("osm_type") != -1){
				nodeId = null;
				int osm_typeIndex = xmlResponse.indexOf("osm_type") + xmlResponse.substring(xmlResponse.indexOf("osm_type")).indexOf("'") + 1;
				String osm_type = xmlResponse.substring(osm_typeIndex, osm_typeIndex + xmlResponse.substring(osm_typeIndex).indexOf("'"));
				int osm_idIndex = xmlResponse.indexOf("osm_id") + 8;
				if(osm_type.equals("way")){
					String wayId = xmlResponse.substring(osm_idIndex, osm_idIndex + xmlResponse.substring(osm_idIndex).indexOf("'"));
					nodeId = convertWayIdToNodeId(wayId);
					Core.debug("Way ID: " + wayId);
					Core.debug("Node ID: " + nodeId);
					if(parser.vertexMap.get(nodeId) != null){
						return nodeId;
					}
					return "!presence";
					
				}else{
					if(osm_type.equals("node")){
						nodeId = xmlResponse.substring(osm_idIndex, osm_idIndex + xmlResponse.substring(osm_idIndex).indexOf("'"));
						if(parser.vertexMap.get(nodeId) != null){
							return nodeId;
						}
						return "!presence";
					}else{
						Core.debug("Unsupported osm_type: " + osm_type + " - returning null");
						return null;
					}
				}
			}
			
			
		}catch(java.net.UnknownHostException e){
			Core.debug("No Internet Connection");
			return "!internet";
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Core.debug(urlString);
		return null;
	}
	
	private String convertWayIdToNodeId(String wayId){
		String parsedWayId = wayId + "-0";
		Core.debug("Way Id to convert: " + parsedWayId);
		HashMap<String,Arc> arcMap = parser.arcMap;
		Arc arcToRetrieve = arcMap.get(parsedWayId);
		String bestGuessNodeId = arcToRetrieve.getStart();
		return bestGuessNodeId;
	}
	
	public boolean isANumber(String str)
	{
	    for (char c : str.toCharArray())
	    {
	        if (!Character.isDigit(c)) return false;
	    }
	    return true;
	}

}


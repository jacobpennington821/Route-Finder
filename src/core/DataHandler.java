package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * The class for dealing with converting input from the UI into IDs that the core program can use.
 * @author Jacob Pennington
 *
 */
public class DataHandler { // The data handler receives input from the GUI and performs operations such as lookups
	
	private String origin;
	private String destination;
	private Parser parser;
	
	/**
	 * Constructor for creating the DataHandler
	 * @param parser - The parser object to use, contains all map data to use.
	 */
	public DataHandler(Parser parser){ // Single argument constructor for simplicity
		this.parser = parser; // Parser contains information about the map loaded, therefore parser has to be initialised before using the data handler
	}
	
	/**
	 * Converts an input into a node ID.
	 * @param input - The input string.
	 * @return A String of a node ID, or an error in the form "!presence" or "!internet"
	 */
	public String convertInputToNodeId(String input){ // Takes an input in the form of a string such as "3 Smith Road Wiltshire"
		String nodeId = "";
		if(isANumber(input)){ // If the string entered is just a number - assume it is a node id
			nodeId = input.trim();
			if(parser.vertexMap.get(nodeId) != null){ // Checks to see if node id is present on the currently loaded map
				return nodeId;
			}
			return "!presence";
		}
		String urlString = "http://nominatim.openstreetmap.org/search?q=" + input.replace(' ', '+') + "&format=xml&addressdetails=1"; // Nominatim is an online lookup which can convert addresses to osm ids
		URL website;
		try {
			website = new URL(urlString);
			URLConnection connection = website.openConnection(); // Opens the connection to the webpage
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); // Reads the returned XML file
			StringBuilder xmlResponse = new StringBuilder();
			String outputLine;
			while((outputLine = reader.readLine()) != null){ // For every line in the XML document
				xmlResponse.append(outputLine); // Adds each line in the XML document to one large string for analysing
			}
			Core.debug(xmlResponse);
			if(xmlResponse.indexOf("osm_type") != -1){ // Ensures the tag "osm_type" is present
				nodeId = null;
				int osm_typeIndex = xmlResponse.indexOf("osm_type") + xmlResponse.substring(xmlResponse.indexOf("osm_type")).indexOf("'") + 1; // Finds the index in the string of the value of the "osm_type" tag
				String osm_type = xmlResponse.substring(osm_typeIndex, osm_typeIndex + xmlResponse.substring(osm_typeIndex).indexOf("'")); // Extracts the value of the "osm_type" tag
				int osm_idIndex = xmlResponse.indexOf("osm_id") + 8; // Finds the index in the string of the value of the "osm_id" tag
				if(osm_type.equals("way")){ // If the id is a way then it needs to be converted to a node id before it can be used
					String wayId = xmlResponse.substring(osm_idIndex, osm_idIndex + xmlResponse.substring(osm_idIndex).indexOf("'")); // Extracts the value of the "osm_id" tag
					nodeId = convertWayIdToNodeId(wayId); // Calculates a best guess node id from the way id
					Core.debug("Way ID: " + wayId);
					Core.debug("Node ID: " + nodeId);
					if(parser.vertexMap.get(nodeId) != null){ // Checks to see whether the node id is present on the currently loaded map
						return nodeId;
					}
					return "!presence";
				}else{
					if(osm_type.equals("node")){ // If the id is a node id
						nodeId = xmlResponse.substring(osm_idIndex, osm_idIndex + xmlResponse.substring(osm_idIndex).indexOf("'")); // Extracts the node id from the "osm_id" tag
						if(parser.vertexMap.get(nodeId) != null){ // Checks to see whether the node id is present on the currently loaded map
							return nodeId;
						}
						return "!presence";
					}else{
						Core.debug("Unsupported osm_type: " + osm_type + " - returning null");
						return null;
					}
				}
			}
		}catch(java.net.UnknownHostException e){ // Triggered when the program cannot connect to the openstreetmap website
			Core.debug("No Internet Connection");
			return "!internet";
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Core.debug(urlString);
		return null;
	}
	
	/**
	 * Converts a Way ID to a node ID usable by the shortest route methods.
	 * @param wayId - A way ID on the current map.
	 * @return A vertex/node ID on the given way.
	 */
	private String convertWayIdToNodeId(String wayId){ // Calculates a best guess of a node id from a way id
		String parsedWayId = wayId + "-0"; // Assumes that the node id is placed on the first part of the way parsed by the parser
		Core.debug("Way Id to convert: " + parsedWayId);
		HashMap<String,Arc> arcMap = parser.arcMap; // Stores the hashmap of all arcs temporarily
		Arc arcToRetrieve = arcMap.get(parsedWayId); // Gets the arc by the way id
		String bestGuessNodeId;
		if(arcToRetrieve != null){ // Ensures that the arc is present on the arc hashmap
			bestGuessNodeId = arcToRetrieve.getEnd(); // Gets the last node in the arc
		}
		else{
			bestGuessNodeId = null; // Just returns null if the arc is not present
		}
		return bestGuessNodeId;
	}
	
	/**
	 * A simple utility method to return whether a String is a number.
	 * @param str - The string to check.
	 * @return True if the string is all numbers, false if not.
	 */
	public boolean isANumber(String str){ // Checks through a string to check if it is a number
	    for (char c : str.toCharArray()) // Iterates through every character in a string
	    {
	        if (!Character.isDigit(c)){ // If one of the characters is not a digit
	        	return false; // The string is not all numeric
	        }
	    }
	    return true; // The string is a number
	}

}


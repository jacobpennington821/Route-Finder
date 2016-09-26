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

public class Parser {
	
	URL path = getClass().getResource("wiltshireRoadsNoRelations.osm");

	File xmlFile;
	int nodeCounter = 0;
	int wayCounter = 0;
	ArrayList<Vertex> test = new ArrayList<Vertex>();
	ArrayList<Arc> arcTest = new ArrayList<Arc>();
	boolean inWay = false;
	String tempId;
	
	public Parser(){
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream("output.txt"));
			//System.setOut(out);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			xmlFile = new File(path.toURI());
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try (BufferedReader br = new BufferedReader(new FileReader(xmlFile))){
			String line;
			while ((line = br.readLine()) != null){
				parse(line);
				//System.out.println(line);
			}
			for(int i = 0; i < test.size(); i++){
				test.get(i).printInfo();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("File Not Found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void parse(String line){
		
		if(line.length() > 8){
			if(!inWay){
				if(line.substring(3, 7).equals("node")){
					String id = line.substring(12, line.indexOf("\"", 12));
					int latLocationInString = line.indexOf("lat=\"") + 5;
					int lonLocationInString = line.indexOf("lon=\"") + 5;
					double lat = Double.parseDouble(line.substring(latLocationInString,latLocationInString + line.substring(latLocationInString).indexOf("\"")));
					double lon = Double.parseDouble(line.substring(lonLocationInString,lonLocationInString + line.substring(lonLocationInString).indexOf("\"")));
					nodeCounter++;
					test.add(new Vertex(id,lat,lon));
					//System.out.println("node" + line.substring(12, line.indexOf('"', 12)));
					//System.out.println(lat);
					//System.out.println(lon);
					
				}else{
					if(line.substring(3,7).equals("way ")){
						inWay = true;
						wayCounter++;
						tempId = line.substring(11,11+line.substring(11).indexOf("\""));
						System.out.println(tempId);
						//find next lines
						System.out.println("way" + wayCounter);
					}
					//System.out.println(line.substring(3, 7));
				}
			}else{
				if(line.substring(5,7).equals("nd")){
					
				}
			}
		}
	}
	
}

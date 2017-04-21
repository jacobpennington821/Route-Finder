package core;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;

import javax.swing.*;

/**
 * The class for creating and displaying the UI
 * @author Jacob Pennington
 *
 */
@SuppressWarnings("serial")
public class RouteFinderGUI extends JFrame implements ActionListener, ItemListener{
	
	private Parser parser;                   // The parser with map data
	private JTextField destinationInputField;// The destination input field
	private JTextField originInputField;     // The origin input field
	private JTextField viaInputField;		 // The via input field
	private JTabbedPane tabPane;             // The tabbed pane at the top of the GUI
	DataHandler dataHandler;                 // The DataHandler for converting inputs
	private JTextArea outputBox;             // The output box on the second tab
	private JLabel distanceLabel;			 // The distance label on the second tab
	private JLabel timeLabel;				 // The time label on the second tab
	boolean travelVia = false;				 // Flag for multiple destinations
	private double distanceOfRoute = 0;		 // Distance of the route
	private double timeOfRoute = 0;			 // Time of the route
	private KeyListener textBoxListener;	 // KeyListener to deal with pressing enter

	/**
	 * The constructor for creating the GUI and displaying any errors that may occur at startup.
	 * @param parser - The parser object which contains map data.
	 */
	public RouteFinderGUI(Parser parser){ // GUI requires the parser being created before being constructed itself - byproduct of single threading means parser needs to parse map before showing UI
		this.parser = parser;
		this.dataHandler = new DataHandler(parser); // Datahandler deals with manipulating inputs to return values that can be used in the core program
		this.setTitle("Route Finder");
		textBoxListener = new KeyListener() {
				
			@Override
			public void keyTyped(KeyEvent e) {			
			}
				
			@Override
			public void keyReleased(KeyEvent e) {				
			}
				
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					callRouteCalculations(); // Calls the calculations when enter is pressed
				}
			}
		};
		initComponents(); // Creates UI components
		this.pack(); // Compresses the window down to make sure there's no extra blank space to the bottom or side of the window
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if(!parser.filePresent){ // Checks if the file is present
			showWarning("There is no map file at " + parser.path + " \nTherefore the program will not function.", "File Not Present");
			System.exit(ERROR);
		}
	}
	
	/**
	 * Creates the GUI components.
	 */
	private void initComponents(){
		tabPane = new JTabbedPane(); // Creates the main tabs
		tabPane.addTab("Input", makeInputPanel());
		tabPane.addTab("Text Display", makeDirectionPanel());
		this.add(tabPane); // Adds the tabbed pane to the JFrame
		this.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e){
				originInputField.requestFocusInWindow(); // Ensures that when the window is loaded the first text entry field has the focus
			}
		});
	}

	/**
	 * Creates the "Input" tab and its contents.
	 * @return The input tab in the form of a JComponent.
	 */
	private JComponent makeInputPanel(){
		JPanel panel = new JPanel(); // Creates a panel to add everything to
		GroupLayout layout = new GroupLayout(panel); 
		panel.setLayout(layout); // Assigns the layout format of the panel as GroupLayout
        panel.setPreferredSize(new Dimension(500, 400)); // 500 x 400 pixels
        JLabel originInputLabel = new JLabel("Input Origin");
        originInputField = new JTextField(20); // Creates a text field of 20 columns
        JLabel destinationInputLabel = new JLabel("Input Destination");
        destinationInputField = new JTextField(20);
        destinationInputField.addKeyListener(textBoxListener);
        JCheckBox viaCheckbox = new JCheckBox("Travel Via");
        viaCheckbox.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event){
        		if(viaCheckbox.isSelected()){
        			travelVia = true;
        		}else{
        			if(!viaCheckbox.isSelected()){
        				travelVia = false;
        			}
        		}
        	}
        });
        viaInputField = new JTextField(20);
        viaInputField.addKeyListener(textBoxListener);
        JButton calculateButton = new JButton("Calculate Route");
        calculateButton.addActionListener(new ActionListener() { // Creates the method for what to do when the "calculate" button is pressed
        	public void actionPerformed(ActionEvent event){
        		callRouteCalculations();
        	}
        });
        layout.setAutoCreateGaps(true); // Makes sure everything is spaced nicely
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup() // Horizontal group deals with the layout of things in order from left to right (x axis)
        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                		.addGroup(layout.createSequentialGroup()
                				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                		.addComponent(originInputLabel)
                                        .addComponent(destinationInputLabel)
                                        .addComponent(viaCheckbox)
                                        )
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                		.addComponent(originInputField)
                                        .addComponent(destinationInputField)
                                        .addComponent(viaInputField)
                                        )
                				)
                		.addComponent(calculateButton)
        				)


        	);
        layout.setVerticalGroup(layout.createSequentialGroup() // Vertical group deals with the layout of things from top to bottom (y axis)
                		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                				.addComponent(originInputLabel)
                				.addComponent(originInputField)
                				)
                		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                				.addComponent(destinationInputLabel)
                				.addComponent(destinationInputField)
                				)
                		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                				.addComponent(viaCheckbox)
                				.addComponent(viaInputField)
                				)
                		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                				.addComponent(calculateButton)
                				)
        		);
		return panel;
	}
	
	/**
//	 * Creates the Output tab and its components.
	 * @return The output tab in JComponent form.
	 */
	private JComponent makeDirectionPanel(){ // Creates the output panel
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout()); // Assigns the layout to GridBagLayout
		GridBagConstraints c = new GridBagConstraints();
		outputBox = new JTextArea();
		outputBox.setEditable(false);
		JScrollPane scroll = new JScrollPane(outputBox); // Makes the output box scrollable
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridheight = 2;
		c.gridwidth = 4;
		panel.add(scroll, c); // Adds the box to the UI with the same constraints as above
		distanceLabel = new JLabel("Distance: ");
		c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(distanceLabel, c);
		timeLabel = new JLabel("Time: ");
		c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(timeLabel, c);
		JButton printButton = new JButton("Print"); // Creates the print button
		printButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){ // Event that is triggered when the print button is pressed
				try {
					outputBox.print(); // Calls the Java method to print the text contained in the box
				} catch (PrinterException e) {
					e.printStackTrace();
				}
			}
		});
		c = new GridBagConstraints(); // Resets the constraints to prevent overlapping variables
		c.gridheight = 1;
		c.gridx = 2;
		c.gridy = 2;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		panel.add(printButton, c); // Adds the print button to the UI with the constraints above
		JButton copyToClipboardButton = new JButton("Copy To Clipboard"); // Creates the copy to clipboard button
		copyToClipboardButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){ // Event that is triggered when the copy to clipboard button is pressed
				String text = outputBox.getText(); // Retrives the text in the box
				StringSelection stringSelection = new StringSelection(text); 
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // Gets the system clipboard
				clipboard.setContents(stringSelection, null); // Adds the string to the clipboard
			}
		});
		c = new GridBagConstraints(); // Resets the constraints again
		c.gridheight = 1;
		c.gridx = 3;
		c.gridy = 2;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		panel.add(copyToClipboardButton, c); // Adds the coppy to clipboard button to the UI
		//panel.setPreferredSize(new Dimension(500,400));
		return panel;
	}
	
	/**
	 * The method for validating any input, calling the route calculation methods and handling any errors.
	 */
	private void callRouteCalculations(){
		outputBox.setText(""); // Clears the output box
		Core.debug("Calculate Route");
		if(originInputField.getText().equals("") || destinationInputField.getText().equals("")){ // Checks if text present in both fields
			showWarning("Please complete all fields.", "Incomplete Fields");
		}else{
			String originResponse = dataHandler.convertInputToNodeId(originInputField.getText()); // Sends the origin text to the data handler for processing
			if(originResponse == "!internet"){
				showWarning("No Internet Connection, please use hardcoded node Ids", "No Internet Connection");
				return;
			}
			if(originResponse == "!presence" || originResponse == null){
				showWarning("Origin not present on current map. Please refine your search", "Node Not Present");
				return;
			}
			
			String destinationResponse = dataHandler.convertInputToNodeId(destinationInputField.getText()); // Sends the destination text to the data handler
			if(destinationResponse == "!internet"){
				showWarning("No Internet Connection, please use hardcoded node Ids", "No Internet Connection");
				return;
			}
			if(destinationResponse == "!presence" || destinationResponse == null){
				showWarning("Destination not present on current map. Please refine your search", "Node Not Present");
				return;
			}
			
			if(travelVia){
				String viaResponse = dataHandler.convertInputToNodeId(viaInputField.getText()); // Sends the destination text to the data handler
				if(viaResponse == "!internet"){
					showWarning("No Internet Connection, please use hardcoded node Ids", "No Internet Connection");
					return;
				}
				if(viaResponse == "!presence" || viaResponse == null){
					showWarning("Travel Via destination not present on current map. Please refine your search", "Node Not Present");
					return;
				}
				Core.debug(originResponse + " to " + viaResponse);
				parser.map.quickestRoute(originResponse, viaResponse); // Calls the first route calculation
				tabPane.setSelectedIndex(1); // Moves the ui to the output tab
				outputBox.append(parser.map.convertGraphToDirections()); // Gets the directions of the first calculation and adds it to the UI
				outputBox.append("\n"); // Adds a new line before the next directions
				distanceOfRoute = parser.map.calculatedRouteDistance; // Sets the distance to the first route's distance
				timeOfRoute = parser.map.calculatedRouteTime; // Sets the time to the first route's time
				Core.debug(viaResponse + " to " + destinationResponse);
				parser.map.quickestRoute(viaResponse, destinationResponse); // Calls the second route calculation
				outputBox.append(parser.map.convertGraphToDirections()); // Adds the second set of directions to the UI
				distanceOfRoute += parser.map.calculatedRouteDistance; // Adds the distance of the second route to the first route
				timeOfRoute += parser.map.calculatedRouteTime; // Adds the time of the second route to the first route

			}else{
				parser.map.quickestRoute(originResponse, destinationResponse); // Calls the shortest route algorithm
				tabPane.setSelectedIndex(1); // Changes the tab to the output tab
				outputBox.append(parser.map.convertGraphToDirections()); // Adds the directions to the output tab
				distanceOfRoute = parser.map.calculatedRouteDistance;
				timeOfRoute = parser.map.calculatedRouteTime;
			}
			distanceLabel.setText("Distance: " + Utilities.round(distanceOfRoute,2) + " km");
			if(timeOfRoute < 1){ // If the time of the route is less than 1 hour
				if(timeOfRoute < (0.017)){ // If the time of the route is less than 1 minute
					timeLabel.setText("Time: " + Double.toString(Utilities.round((timeOfRoute*60)*60, 0)).replaceAll("\\.0", "") + " seconds");
					// Seconds scale
				} else {
					timeLabel.setText("Time: " + Double.toString(Utilities.round(timeOfRoute*60, 0)).replaceAll("\\.0", "") + " minutes");
					// Minutes scale
				}
			}else{
				timeLabel.setText("Time: " 
				+ Double.toString(Math.floor(timeOfRoute)).replaceAll("\\.0", "") // Number of hours rounded down
				+ " hour(s), "
				+ Double.toString((Utilities.round((timeOfRoute - Math.floor(timeOfRoute)) * 60,0))).replaceAll("\\.0", "") // Number of minutes to 0 decimal places
				+ " minute(s)");
				// Hours and minutes scale
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {		
	}
	
	/**
	 * A simple method to create a warning with specified content.
	 * @param content - The text within the box.
	 * @param title - The text on the header of the box.
	 */
	public void showWarning(String content, String title){ // Creates a warning message with the passed text
		JOptionPane.showMessageDialog(this, content, title, JOptionPane.WARNING_MESSAGE);
	}
}

package core;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;

@SuppressWarnings("serial")
public class RouteFinderGUI extends JFrame implements ActionListener, ItemListener{
	
	private Parser parser;                   //->
	private JTextField destinationInputField;//-->
	private JTextField originInputField;     //---> Variables and fields that need to be accessed from multiple functions
	private JTabbedPane tabPane;             //---> 
	DataHandler dataHandler;                 //-->
	private JTextArea outputBox;             //->
	boolean travelVia = false;

	
	public RouteFinderGUI(Parser parser){ // GUI requires the parser being created before being constructed itself - byproduct of single threading means parser needs to parse map before showing UI
		this.parser = parser;
		this.dataHandler = new DataHandler(parser); // Datahandler deals with manipulating inputs to return values that can be used in the core program
		this.setTitle("Route Finder");
		initComponents(); // Creates UI components
		this.pack(); // Compresses the window down to make sure there's no extra blank space to the bottom or side of the window
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void initComponents(){
		tabPane = new JTabbedPane(); // Creates the main tabs
		tabPane.addTab("Input", makeInputPanel());
		tabPane.addTab("Text Display", makeDirectionPanel());
		//tabPane.addTab("Map Display", makeMapPanel());
		this.add(tabPane); // Adds the tabbed pane to the JFrame
		this.setJMenuBar(constructMenuBar());
		this.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e){
				originInputField.requestFocusInWindow(); // Ensures that when the window is loaded the first text entry field has the focus
			}
		});
	}

	private JComponent makeInputPanel(){
		JPanel panel = new JPanel(); // Creates a panel to add everything to
		GroupLayout layout = new GroupLayout(panel); 
		panel.setLayout(layout); // Assigns the layout format of the panel as GroupLayout
        panel.setPreferredSize(new Dimension(500, 400)); // 500 x 400 pixels
        JLabel originInputLabel = new JLabel("Input Origin");
        originInputField = new JTextField(20); // Creates a text field of 20 columns
        JLabel destinationInputLabel = new JLabel("Input Destination");
        destinationInputField = new JTextField(20);
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
        JLabel viaInputLabel = new JLabel("Travel Via");
        JTextField viaInputField = new JTextField(20);
        JButton calculateButton = new JButton("Calculate Route");
        calculateButton.addActionListener(new ActionListener() { // Creates the method for what to do when the "calculate" button is pressed
        	public void actionPerformed(ActionEvent event){
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
        				parser.map.shortestRoute(originResponse, viaResponse);
        				tabPane.setSelectedIndex(1);
        				outputBox.append(parser.map.convertGraphToDirections());
        				Core.debug(viaResponse + " to " + destinationResponse);
        				parser.map.shortestRoute(viaResponse, destinationResponse);
        				outputBox.append(parser.map.convertGraphToDirections());
    				}else{
        				parser.map.shortestRoute(originResponse, destinationResponse); // Calls the shortest route algorithm
        				tabPane.setSelectedIndex(1); // Changes the tab to the output tab
        				outputBox.append(parser.map.convertGraphToDirections()); // Adds the directions to the output tab
    				}

    			}
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
		c.gridwidth = 2;
		panel.add(scroll, c); // Adds the box to the UI with the same constraints as above
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
		c.gridx = 0;
		c.gridy = 2;
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
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.NONE;
		panel.add(copyToClipboardButton, c); // Adds the coppy to clipboard button to the UI
		//panel.setPreferredSize(new Dimension(500,400));
		return panel;
	}
	
	private JComponent makeMapPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,1));
		panel.setPreferredSize(new Dimension(500,400));
		return panel;
	}
	
	private JMenuBar constructMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem run = new JMenuItem("Run");
		run.addActionListener(this);
		fileMenu.add(run);
		menuBar.add(fileMenu);
		return menuBar;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		JMenuItem source = (JMenuItem)(event.getSource());
		if(source.getText() == "Run"){
			Core.debug("Run Clicked");
			parser.parseXMLtoGraph(parser.xmlFile);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void showWarning(String content, String title){ // Creates a warning message with the passed text
		JOptionPane.showMessageDialog(this, content, title, JOptionPane.WARNING_MESSAGE);
	}
}

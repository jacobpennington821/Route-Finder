package core;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;

@SuppressWarnings("serial")
public class RouteFinderGUI extends JFrame implements ActionListener, ItemListener{
	
	private Parser parser;
	private JTextField destinationInputField;
	private JTextField originInputField;
	
	public RouteFinderGUI(Parser parser){
		this.parser = parser;
		this.setTitle("Route Finder");
		initComponents();
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void initComponents(){
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Input", makeInputPanel());
		tabPane.addTab("Text Display", makeDirectionPanel());
		tabPane.addTab("Map Display", makeMapPanel());
		this.add(tabPane);
		this.setJMenuBar(constructMenuBar());
	}

	private JComponent makeInputPanel(){
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
        panel.setPreferredSize(new Dimension(500, 400));
        JLabel originInputLabel = new JLabel("Input Origin");
        originInputField = new JTextField(20);
        JLabel destinationInputLabel = new JLabel("Input Destination");
        destinationInputField = new JTextField(20);
        JButton calculateButton = new JButton("Calculate Route");
        calculateButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event){
    			Core.debug("Calculate Route");
    			if(originInputField.getText().equals("") || destinationInputField.getText().equals("")){
    				showWarning();
    			}else{
    				DataHandler dataHandler = new DataHandler(originInputField.getText(),destinationInputField.getText(), parser);
    			}
        	}
        });
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup()
        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                		.addGroup(layout.createSequentialGroup()
                				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                		.addComponent(originInputLabel)
                                        .addComponent(destinationInputLabel)
                                        )
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                		.addComponent(originInputField)
                                        .addComponent(destinationInputField)
                                        )
                				)
                		.addComponent(calculateButton)
        				)


        	);
        layout.setVerticalGroup(layout.createSequentialGroup()
                		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                				.addComponent(originInputLabel)
                				.addComponent(originInputField)
                				)
                		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                				.addComponent(destinationInputLabel)
                				.addComponent(destinationInputField)
                				)
                		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                				.addComponent(calculateButton)
                				)
        		);
		return panel;
	}
	
	private JComponent makeDirectionPanel(){
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Directions");
		panel.add(label);
		panel.setLayout(new GridLayout(1,1));
		panel.setPreferredSize(new Dimension(500,400));
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
	
	public void showWarning(){
		JOptionPane.showMessageDialog(this, "Please complete all fields.", "Incomplete Field", JOptionPane.WARNING_MESSAGE);
	}
}

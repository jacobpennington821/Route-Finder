package core;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

@SuppressWarnings("serial")
public class RouteFinderGUI extends JFrame implements ActionListener, ItemListener{
	
	private Parser parser;
	
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
		panel.setLayout(new GridLayout(1,1));
        panel.setPreferredSize(new Dimension(500, 400));
        JLabel destinationInputLabel = new JLabel("Input Destination");
        JTextField destinationInputField = new JTextField(20);
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
}

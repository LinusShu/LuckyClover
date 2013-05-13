package com.eBingo.view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.eBingo.EBingoModel;

public class EBingoView extends JPanel implements ViewInterface{
	private final String DEFAULT_CONFIG = "choose LuckyClover config file";
	private final String DEFAULT_BLOCK = "choose LuckyClover block file";
	
	private JFileChooser fc = new JFileChooser();
	private JLabel configFileLabel = new JLabel("Lucky Clover Configuration");
	private JTextField configFileField = new JTextField(20);
	private JButton configFileButton = new JButton("..");
	
	private JLabel blockFileLabel = new JLabel("Blocks Configuration");
	private JTextField blockFileField = new JTextField(20);
	private JButton blockFileButton = new JButton("..");
	
	private JLabel dbNameLabel = new JLabel("Database Name");
	private JTextField dbNameField = new JTextField(20);
	private JButton dbNameButton = new JButton("Reset");
	
	private JButton runButton = new JButton("Generate!");
	
	private EBingoModel model;
	
	private GridBagLayout layout;
	private GridBagConstraints gbc;
	
	public EBingoView(EBingoModel model) {
		super();
		this.model = model;
		this.layoutView();
		this.registerControllers();
		this.model.AddView(this);
	}
	
	private void layoutView() {
		this.fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.fc.setFileFilter(new XMLFileFilter());
		this.configFileField.setText(this.DEFAULT_CONFIG);
		this.configFileField.setEditable(false);
		this.configFileField.setBackground(Color.LIGHT_GRAY);
		this.blockFileField.setText(this.DEFAULT_BLOCK);
		this.blockFileField.setEditable(false);
		this.blockFileField.setBackground(Color.LIGHT_GRAY);
		
		this.layout = new GridBagLayout();
		this.gbc = new GridBagConstraints();
		
		this.setLayout(layout);
		this.gbc.insets = new Insets(10,10,0,0);
		
		// Laying out the config file selection row
		this.gbc.gridx = 0;
		this.gbc.gridy = 1;
		this.gbc.anchor = GridBagConstraints.EAST;
		this.add(configFileLabel, gbc);
		this.gbc.gridx = 1;
		this.gbc.gridy = 1;
		this.gbc.anchor = GridBagConstraints.WEST;
		this.add(configFileField, gbc);
		this.gbc.gridx = 2;
		this.gbc.gridy = 1;
		this.add(configFileButton, gbc);
		
		// Laying out the block file selection row
		this.gbc.gridx = 0;
		this.gbc.gridy = 2;
		this.gbc.anchor = GridBagConstraints.EAST;
		this.add(blockFileLabel, gbc);
		this.gbc.gridx = 1;
		this.gbc.gridy = 2;
		this.gbc.anchor = GridBagConstraints.WEST;
		this.add(blockFileField, gbc);
		this.gbc.gridx = 2;
		this.gbc.gridy = 2;
		this.add(blockFileButton, gbc);
		
		// Laying out the DB name row
		this.gbc.gridx = 0;
		this.gbc.gridy = 3;
		this.gbc.anchor = GridBagConstraints.EAST;
		this.add(dbNameLabel, gbc);
		this.gbc.gridx = 1;
		this.gbc.gridy = 3;
		this.gbc.anchor = GridBagConstraints.WEST;
		this.add(dbNameField, gbc);
		this.gbc.gridx = 2;
		this.gbc.gridy = 3;
		this.gbc.anchor = GridBagConstraints.WEST;
		this.add(dbNameButton, gbc);
		
		// Laying out the "Generate!" button
		this.gbc.gridx = 0;
		this.gbc.gridy = 4;
		this.gbc.gridwidth = 3;
		this.gbc.fill = GridBagConstraints.HORIZONTAL;
		this.gbc.anchor = GridBagConstraints.CENTER;
		this.gbc.insets.set(40, 0, 0, 0);
		this.add(runButton, gbc);
	}
	
	private void registerControllers() {
		// ActionListener for select config file button
		this.configFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				File f = new File("config/");
				fc.setCurrentDirectory(f);
				
				int result = fc.showOpenDialog(EBingoView.this);
				
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (file.getName().endsWith(".xml")) {
						EBingoView.this.model.setConfigFile(file);
					} else {
						JOptionPane.showMessageDialog(EBingoView.this, 
								"Please choose an XML file type",
								"Bad File Type",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		// ActionListener for select block file button
		this.blockFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				File f = new File("config/");
				fc.setCurrentDirectory(f);
				
				int result = fc.showOpenDialog(EBingoView.this);
				
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (file.getName().endsWith(".xml")) {
						EBingoView.this.model.setBlockFile(file);
					} else {
						JOptionPane.showMessageDialog(EBingoView.this, 
								"Please choose an XML file type",
								"Bad File Type",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		// FocusListener for the DB name text field
		this.dbNameField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				dbNameField.selectAll();
			}
			
			@Override
			public void focusLost(FocusEvent arg0) {
				EBingoView.this.model.setDBName(dbNameField.getText().trim());
			}
		});
		
		// ActionListener for the DB name button
		this.dbNameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				EBingoView.this.model.setDBName();
			}
		});
		
		// ActionListener for the "Generate!" button
		this.runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (EBingoView.this.model.isSetupValid()) {
					EBingoView.this.model.launch();
				} else {
					JOptionPane.showMessageDialog(EBingoView.this,
							"Setup is not valid!",
							"Bad Setup",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
	
	class XMLFileFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
		}
		
		public String getDescription() {
			return ".xml files";
		}
	}
	
	@Override
	public void updateView() {
		if (!this.model.isRunning()) {
			File configFile = this.model.getConfigFile();
			if (configFile != null) 
				this.configFileField.setText(configFile.getName());
			else 
				this.configFileField.setText(this.DEFAULT_CONFIG);
			
			File blockFile = this.model.getBlockFile();
			if (blockFile != null) 
				this.blockFileField.setText(blockFile.getName());
			else 
				this.blockFileField.setText(this.DEFAULT_BLOCK);
			
			this.dbNameField.setText(this.model.getDBName());
		}
		
		if (this.model.isRunning()) {
			this.runButton.setEnabled(false);
			this.configFileButton.setEnabled(false);
			this.blockFileButton.setEnabled(false);
			this.dbNameButton.setEnabled(false);
			
			this.configFileField.setEnabled(false);
			this.blockFileField.setEnabled(false);
			this.dbNameField.setEnabled(false);
		} else {
			this.runButton.setEnabled(true);
			this.configFileButton.setEnabled(true);
			this.blockFileButton.setEnabled(true);
			this.dbNameButton.setEnabled(true);
			
			this.configFileField.setEnabled(true);
			this.blockFileField.setEnabled(true);
			this.dbNameField.setEnabled(true);
		}
		
	}
	
}

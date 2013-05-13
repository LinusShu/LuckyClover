package com.eBingo.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.eBingo.EBingoModel;

public class EBingoProcessView extends JPanel implements ViewInterface {
	
	private JProgressBar pb = new JProgressBar();
	private JLabel logLabel = new JLabel("Output Log Location");
	private JLabel logValueLabel = new JLabel("N/A");
	
	private JLabel totalPlayedLabel = new JLabel("0");
	private JLabel currPlayLabel = new JLabel("0");
	
	private JLabel processLabel = new JLabel("Generation Results...");
	private JButton cancelButton = new JButton("X");
	private JButton pauseButton = new JButton("||");
	
	private EBingoModel model;
	
	private GridBagLayout layout;
	private GridBagConstraints gbc;
	
	public EBingoProcessView(EBingoModel model) {
		super();
		this.model = model;
		this.layoutView();
		this.registerControllers();
		this.model.AddView(this);
	}
	
	public void layoutView() {
		this.layout = new GridBagLayout();
		this.gbc = new GridBagConstraints();
		
		Font f = new Font("Arial", Font.BOLD, 18);
		
		this.pauseButton.setVisible(false);
		this.cancelButton.setVisible(false);
		this.processLabel.setFont(f);
		this.logValueLabel.setForeground(Color.GRAY);
		
		this.setLayout(layout);
		// Laying out the process status label
		this.gbc.insets = new Insets(20,10,10,10);
		
		this.gbc.gridx = 0;
		this.gbc.gridy = 0;
		this.gbc.gridwidth = 4;
		this.gbc.anchor = GridBagConstraints.CENTER;
		this.add(processLabel, gbc);
		
		// Laying out the output log row
		this.gbc.insets = new Insets(5,2,5,2);
		
		JPanel jp = new JPanel();
		jp.setLayout(new GridBagLayout());
		
		this.gbc.gridx = 0;
		this.gbc.gridy = 0;
		this.gbc.gridwidth = 4;
		this.gbc.weightx = 4;
		this.gbc.anchor = GridBagConstraints.CENTER;
		jp.add(logLabel, gbc);
		
		this.gbc.gridx = 0;
		this.gbc.gridy = 1;
		this.gbc.gridwidth = 4;
		this.gbc.weightx = 4;
		this.gbc.anchor = GridBagConstraints.CENTER;
		jp.add(logValueLabel, gbc);
		
		this.gbc.gridx = 0;
		this.gbc.gridy = 1;
		this.gbc.gridwidth = 4;
		this.gbc.anchor = GridBagConstraints.CENTER;
		this.add(jp, gbc);
		
		// Laying out the played card row
		this.gbc.insets = new Insets(10,10,10,10);
		
		JPanel jp2 = new JPanel();
		jp2.setLayout(new GridBagLayout());
		
		this.gbc.gridx = 1;
		this.gbc.gridy = 0;
		this.gbc.gridwidth = 1;
		this.gbc.anchor = GridBagConstraints.WEST;
		jp2.add(currPlayLabel, gbc);
		
		this.gbc.gridx = 2;
		this.gbc.gridy = 0;
		this.gbc.gridwidth = 2;
		this.gbc.weightx = 2;
		this.gbc.anchor = GridBagConstraints.WEST;
		jp2.add(totalPlayedLabel, gbc);
		
		this.gbc.gridx = 0;
		this.gbc.gridy = 2;
		this.gbc.gridwidth = 4;
		this.gbc.anchor = GridBagConstraints.CENTER;
		this.add(jp2, gbc);
		
		// Laying out the progress bar
		this.gbc.insets = new Insets(10,10,10,10);
		
		this.gbc.gridx = 0;
		this.gbc.gridy = 3;
		this.gbc.gridwidth = 4;
		this.gbc.fill = GridBagConstraints.HORIZONTAL;
		this.gbc.anchor = GridBagConstraints.CENTER;
		this.add(pb, gbc);
		
		// Laying out the cancel & pause buttons
		this.gbc.insets = new Insets(10,10,20,10);
		
		this.gbc.gridx = 0;
		this.gbc.gridy = 4;
		this.gbc.gridwidth = 2;
		this.gbc.weightx = 2;
		this.gbc.anchor = GridBagConstraints.EAST;
		this.gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(cancelButton, gbc);
		
		this.gbc.gridx = 2;
		this.gbc.gridy = 4;
		this.gbc.weightx = 2;
		this.gbc.gridwidth = 2;
		this.gbc.fill = GridBagConstraints.HORIZONTAL;
		this.gbc.anchor = GridBagConstraints.WEST;
		this.add(pauseButton, gbc);
	}
	
	public void registerControllers() {
	
		// ActionListener for the cancel button
		this.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				EBingoProcessView.this.model.cancel();
			}
		});
		
		// ActionListener for the pause button
		this.pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (pauseButton.getText().compareTo("||") == 0)
					EBingoProcessView.this.model.pause();
				else
					EBingoProcessView.this.model.resume();
			}
		});
	}
	
	@Override
	public void updateView() {
		if (this.model.isRunning()) {
			this.cancelButton.setVisible(true);
			this.pauseButton.setVisible(true);
			if (this.model.isPaused())
				this.processLabel.setText("Generating Results...Paused");
			else
				this.processLabel.setText("Generating Results...Running");
			
		} else {
			this.logValueLabel.setText(this.model.getLogFilePath());
			this.cancelButton.setVisible(false);
			this.pauseButton.setVisible(false);
			
			if (this.model.isError()) 
				this.processLabel.setText("Error...Check Error Log");
			else
				this.processLabel.setText("Ready");
		}
		
		if (this.model.isPaused()) 
			this.pauseButton.setText(">>");
		else
			this.pauseButton.setText("||");
		
		this.currPlayLabel.setText("Generated: " + Integer.toString(this.model.getCurrPlayed()));
		this.totalPlayedLabel.setText("Total Cards: " + Integer.toString(this.model.getTotalPlays()));
		
		this.pb.setMaximum(this.model.getTotalPlays());
		this.pb.setValue(this.model.getCurrPlayed());
		this.pb.repaint();
	}

}

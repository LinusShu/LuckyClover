package com.eBingo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import com.eBingo.view.EBingoProcessView;
import com.eBingo.view.EBingoView;


//Reads a Comma Separated Value file and creates summary data in another CSV file.

public class EBingoMain{
	
 
	public static void main(String[] arg) {
		
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension dim = tk.getScreenSize();
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(800, 600);
		f.setLocation((int)(dim.width * 0.5 - f.getSize().width * 0.5),
				(int)(dim.height * 0.5 - f.getSize().height * 0.5));
		f.setVisible(true);
		f.setTitle("LuckyClover Results Generator");
		f.getContentPane().setBackground(Color.WHITE);
		f.getContentPane().setLayout(new BorderLayout());
		
		EBingoModel model = EBingoModel.getInstance();
		EBingoView view = new EBingoView(model);
		EBingoProcessView pview = new EBingoProcessView(model);
		
		f.getContentPane().add(view, BorderLayout.CENTER);
		f.getContentPane().add(pview, BorderLayout.SOUTH);
		f.getContentPane().validate();
	
	}
}
    



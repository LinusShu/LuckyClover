package com.luckyclover;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import com.luckyclover.view.LuckyCloverProcessView;
import com.luckyclover.view.LuckyCloverView;


//Reads a Comma Separated Value file and creates summary data in another CSV file.

public class LuckyCloverMain{
	
 
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
		
		LuckyCloverModel model = LuckyCloverModel.getInstance();
		LuckyCloverView view = new LuckyCloverView(model);
		LuckyCloverProcessView pview = new LuckyCloverProcessView(model);
		
		f.getContentPane().add(view, BorderLayout.CENTER);
		f.getContentPane().add(pview, BorderLayout.SOUTH);
		f.getContentPane().validate();
	
	}
}
    



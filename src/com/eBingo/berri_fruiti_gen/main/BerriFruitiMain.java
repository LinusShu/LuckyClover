package com.eBingo.berri_fruiti_gen.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import com.eBingo.berri_fruiti_gen.cards.Card;
import com.eBingo.berri_fruiti_gen.cards.CardFactory;
import com.eBingo.berri_fruiti_gen.cards.Fruits;
import com.eBingo.berri_fruiti_gen.cards.Tab;

public class BerriFruitiMain {
	
	public static void main(String[] arg) throws Exception {
		FileWriter fStream = new FileWriter("log.csv");
		BufferedWriter out = new BufferedWriter(fStream);
		CardFactory f = new CardFactory(50000);
		List<Card> cards = f.getCards();
		
		try {
			for(Card c : cards) {
				out.write("Card: (prize = " + c.getPrize() +  ")\n");
				for (Tab t : c.getTabs()) {
					out.write("Tab: ");
					for (Fruits fruit : t.getSymbols()) {
						out.write(fruit.toString() + " ");
					}
				
				}
				//System.out.println(" ");
				out.write("\n");
			}
		} catch (Exception e) {
			out.close();
		}
		out.close();
		System.out.println("Log created!");
	}
}

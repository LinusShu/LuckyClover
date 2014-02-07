package com.eBingo.berri_fruiti_gen.cards;

import java.util.ArrayList;
import java.util.List;

public class Tab {
	private List<Fruits> symbols;
	
	public Tab(List<Fruits> c) {
		if (c.size() > 3) 
			System.err.println("Error: Only 3 symbols are allawed on 1 tab!");
		symbols = new ArrayList<Fruits>(3);
		
		this.symbols = c;
	}
	
	/**
	 * Method used to get an array representation of the symbol combination on the pull-tab. 
	 * 
	 * @return  Array of Fruits enums.
	 */
	public List<Fruits> getSymbols() {
		return this.symbols;
	}
	
	public void setSymbol(int index, Fruits f) {
		symbols.set(index, f);
	}

}

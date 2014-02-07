package com.eBingo.berri_fruiti_gen.cards;

import java.util.List;

public class Card {
	private List<Tab> tabs;
	private float prize;
	
	public Card(List<Tab> t, float p) {
		this.prize = p;
		this.tabs = t;
	}
	
	public List<Tab> getTabs() {
		return tabs;
	}
	
	public float getPrize() {
		return prize;
	}
}

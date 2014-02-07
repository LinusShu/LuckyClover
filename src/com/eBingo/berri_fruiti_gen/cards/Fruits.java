package com.eBingo.berri_fruiti_gen.cards;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Fruits {
	CHERRY, 
	BANANA,
	PEAR,
	BLUEBERRY,
	APPLE;

	private static final Map<Integer, Fruits> table = new HashMap<Integer, Fruits>();
	
	static {
		int ordinal = 0;
		for (Fruits f: EnumSet.allOf(Fruits.class)) {
			table.put(ordinal, f);
			ordinal += 1;
		}
	}
	
	public static Fruits toFruit(int ordinal) {
		return table.get(ordinal);
	}
}

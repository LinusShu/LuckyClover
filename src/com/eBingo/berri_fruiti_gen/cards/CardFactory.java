package com.eBingo.berri_fruiti_gen.cards;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardFactory {
	private static int ONE_CHERRY = 14480;
	private static int APPLES = 200;
	private static int BLUEBERRIES = 25;
	private static int PEARS = 20;
	private static int BANANAS = 15;
	private static int CHERRIES = 10;
	private static int NO_WIN = 35250;
	
	private List<Card> cards;
	private float totalwin;
	private int[] win_counts;
	private Random ran;
	private byte[] nums;

	public CardFactory(int cardcount) {
		cards = new ArrayList<Card>(cardcount);
		int totalwin = 0;
		ran = new Random();
		
		//initialize win counts array
		win_counts = new int[6];
		for (int i=0; i<6; i++) {
			win_counts[i] = 0;
		}
		
		
		//initialize nums array
		nums = new byte[50000];
		for (int i=0; i<nums.length; i++) {
			nums[i] = 1;
		}
		
		//create the cards
		for (int i=0; i<cardcount; i++) {
			createCards();
		}
	}
	
	/**
	 * @return the list of the card bought
	 */
	public List<Card> getCards() {
		return cards;
	}
	
	/**
	 * @return the total payout on all cards.
	 */
	public float getTotalWin() {
		return totalwin;
	}
	
	/**
	 * This is the main method used to create a card. It will create/assemble a card 
	 * according to the probabilities of winning.
	 */
	public void createCards() {
		Card newCard = null;
		List<Tab> tabs = new ArrayList<Tab>(5);
		//randomly generated number used to determine the prize.
		int chance = 0;
		//randomly generated number used to determine the position of the winning tab on the card. 
		final int pos = ran.nextInt(5);
		//randomly generated number used to determine the chance for near misses.
		final int nearmiss = ran.nextInt(100)+1; 
		boolean goAgain = true;
		
		//initialize Tabs list
		for(int i=0; i<5; i++) 
			tabs.add(null);
		
		while (goAgain) {
			//generate a new chance
			chance = ran.nextInt(50000); 
			//if chance generated has not been generated before
			if (nums[chance] != 0) {
				nums[chance] = 0;
				//3 Cherries
				if (chance < 4 && win_counts[0] < 5) {
					tabs.set(pos, createWinTab(0));
					//TODO: Randomly generate Near Misses
					for (int i=0; i<tabs.size(); i++) {
						if (tabs.get(i) == null) {
							tabs.set(i, new Tab (new Fruits[] {Fruits.BANANA, Fruits.BLUEBERRY, Fruits.BANANA}));
						}
					}
					goAgain = false;
					cards.add(new Card(tabs, 500));
					totalwin += 500;
					win_counts[0] += 1;
					
				//3 BANANAS
				} else if (chance >= 4 && chance < 34 && win_counts[1] < 30) {
					tabs.set(pos, createWinTab(1));
					
					for (int i=0; i<tabs.size(); i++) {
						if (tabs.get(i) == null) {
							tabs.set(i, new Tab (new Fruits[] {Fruits.BANANA, Fruits.BLUEBERRY, Fruits.BANANA}));
						}
					}
					
					cards.add(new Card(tabs, 100));
					goAgain = false;
					totalwin += 100;
					win_counts[1] += 1;
					
				//3 PEARS
				} else if (chance >= 34 && chance < 54 && win_counts[2] < 20) {
					tabs.set(pos, createWinTab(2));
					
					//TODO: Randomly generate Near Misses
					for (int i=0; i<tabs.size(); i++) {
						if (tabs.get(i) == null) {
							tabs.set(i, new Tab (new Fruits[] {Fruits.BANANA, Fruits.BLUEBERRY, Fruits.BANANA}));
						}
					}
					
					cards.add(new Card(tabs, 50));
					goAgain = false;
					totalwin += 50;
					win_counts[2] += 1;
				
				//3 BLUEBERRIES
				} else if (chance >= 54 && chance < 104 && win_counts[3] < 50) {
					tabs.set(pos, createWinTab(3));
					
					//TODO: Randomly generate Near Misses
					for (int i=0; i<tabs.size(); i++) {
						if (tabs.get(i) == null) {
							tabs.set(i, new Tab (new Fruits[] {Fruits.BANANA, Fruits.BLUEBERRY, Fruits.BANANA}));
						}
					}
					
					cards.add(new Card(tabs, 5));
					goAgain = false;
					totalwin += 5;
					win_counts[3] += 1;
					
				//3 APPLES
				} else if (chance >= 104 && chance < 604 && win_counts[4] < 500) {
					tabs.set(pos, createWinTab(4));
					
					//TODO: Randomly generate Near Misses
					for (int i=0; i<tabs.size(); i++) {
						if (tabs.get(i) == null) {
							tabs.set(i, new Tab (new Fruits[] {Fruits.BANANA, Fruits.BLUEBERRY, Fruits.BANANA}));
						}
					}
					
					cards.add(new Card(tabs, 2.5f));
					goAgain = false;
					totalwin += 2.5f;
					win_counts[4] += 1;
					
				//CHERRY and two others of the same
				} else if (chance >= 604 && chance < 9604 && win_counts[5] < 9000) {
					tabs.set(pos, createWinTab(5));
					
					//TODO: Randomly generate Near Misses
					for (int i=0; i<tabs.size(); i++) {
						if (tabs.get(i) == null) {
							tabs.set(i, new Tab (new Fruits[] {Fruits.BANANA, Fruits.BLUEBERRY, Fruits.BANANA}));
						}
					}
					
					cards.add(new Card(tabs, 1.25f));
					goAgain = false;
					totalwin += 1.25f;
					win_counts[5] += 1;
						
				//TODO: Test else clause, suppose to generate losing tabs only
				} else {
					for (int i=0; i<5; i++) {
						tabs.set(i, new Tab (new Fruits[] {Fruits.BANANA, Fruits.BANANA, Fruits.PEAR}));
					}
					goAgain = false;
					cards.add(new Card(tabs, 0));
				}
			}
		}
		
	}
	
	/**
	 * This method will create 1 of the 5 winning tabs.
	 * 
	 * @param type  value=[0,5]
	 * @return		the generated winning tab
	 */
	private Tab createWinTab(int type) {
		Tab t = null;
		//used to determine which 2 other fruits to use in the "1 cherry and 2 others the same" win.
		int n = ran.nextInt(4)+1; 

		switch (type) {
			//3 Cherries
			case 0: 
				t = new Tab(new Fruits[] {Fruits.CHERRY, Fruits.CHERRY, Fruits.CHERRY});
				break;
			//3 Bananas
			case 1:
				t = new Tab(new Fruits[] {Fruits.BANANA, Fruits.BANANA, Fruits.BANANA});
				break;
			//3 Pears
			case 2:
				t = new Tab(new Fruits[] {Fruits.PEAR, Fruits.PEAR, Fruits.PEAR});
				break;
			case 3:
			//3 Blueberries
				t = new Tab(new Fruits[] {Fruits.BLUEBERRY, Fruits.BLUEBERRY, Fruits.BLUEBERRY});
				break;
			//3 Apples
			case 4:
				t = new Tab(new Fruits[] {Fruits.APPLE, Fruits.APPLE, Fruits.APPLE});
				break;
			//1 Cherry & 2 others of the same
			case 5:
				t = new Tab(new Fruits[] {null, null, null});
				//assign 1 cherry to a random slot on the tab
				t.setSymbol(ran.nextInt(3), Fruits.CHERRY);
				//assign 2 others of the same to the remaining slots on the tab
				for (int i=0; i<t.getSymbols().length; i++) {
					if (t.getSymbols()[i] == null)
						t.setSymbol(i, Fruits.toFruit(n));
				}
			default:
				break;
				
		}
	
		
		return t;
	}
}

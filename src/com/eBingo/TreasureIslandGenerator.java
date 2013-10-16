package com.eBingo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.eBingo.EBingoModel.Card;

public class TreasureIslandGenerator {
	private static enum Rates {
		CARD1_ONE_LINE_RATE(97), CARD1_FOUR_CORNERS_RATE(2), CARD1_X_RATE(1),
		CARD2_ONE_LINE_RATE(78), CARD2_FOUR_CORNERS_RATE(20), CARD2_X_RATE(2);
		
		private int rate;
		private Rates(int rate) {
			this.rate = rate;
		}
		
		private int getValue() {
			return rate;
		}
	};
	
	private final int HITODDS = 20;
	
	private int CARD1_ONE_LINE = 10500;
	private int CARD1_FOUR_CORNERS = 250;
	private int CARD1_X = 80;
	private int CARD2_ONE_LINE = 500;
	private int CARD2_FOUR_CORNERS = 125;
	private int CARD2_X = 15;
	private int DUAL_WINS = 295;
	
	Random ran = new Random();
	
	EBingoModel model = null;
	long id = 0;
	Map<List<Integer>, List<Card>> play = new HashMap<List<Integer>, List<Card>>(50000);
	List<Integer> called = new ArrayList<Integer>(25);
	List<Card> cards = new ArrayList<Card>(2);
	
	Card tmpCard = null;
	List<Integer> tmpArray = null;
	List<Integer> tmpArray2 = null;
	List<Integer> tmpArray3 = null;
	List<Integer> tmpArray4 = null;
	List<Integer> tmpArray5 = null;
	List<Integer> tmpCalled = null;
	
	private List<Integer> array1_15 = new ArrayList<Integer>(15);
	private List<Integer> array16_30 = new ArrayList<Integer>(15);
	private List<Integer> array31_45 = new ArrayList<Integer>(15);
	private List<Integer> array46_60 = new ArrayList<Integer>(15);
	private List<Integer> array61_75 = new ArrayList<Integer>(15);
	private List<Integer> array1_75 = new ArrayList<Integer>(75);
	
	public TreasureIslandGenerator(EBingoModel m) {
		this.model = m;
		
		// Initialize the Lists
		for (int i = 0; i < 15; i++) {
			array1_15.add(i + 1);
			array16_30.add(i + 16);
			array31_45.add(i + 31);
			array46_60.add(i + 46);
			array61_75.add(i + 61);
		}
					
		for (int i = 0; i < 75; i++) 
			array1_75.add(i + 1);
		
	}
	
	/**
	 * The main method used to generate called numbers and cards
	 */
	public void generatePlays() {
		boolean valid = false;
		// Generate dual wins
		while (this.DUAL_WINS != 0) {
			generateDualWins();
			this.DUAL_WINS --;
		}
		
		// Generate Card #1 X wins
		while (this.CARD1_X != 0) {
			generateCard1XWin();
			while (! valid)
				valid = generateCard2NoWin();
			
		}
		
		// Generate Card #1 4 corners wins
		while (this.CARD1_FOUR_CORNERS != 0) {
			
		}
		
		// Generate Card #1 1 line wins 
		while (this.CARD1_ONE_LINE != 0) {
			
		}
		
		// Generate Card #2 X wins
		while (this.CARD2_X != 0) {
			
		}
		
		// Generate Card #2 4 corners wins
		while (this.CARD2_FOUR_CORNERS != 0) {
			
		}
		
		// Generate Card #2 1 line wins
		while (this.CARD2_ONE_LINE != 0) {
			
		}
		
	}
	
	private void generateDualWins() {
		int num = this.ran.nextInt(100) + 1;
		num = 10;
		
		// Generate card 1 line win if the rate is within range
		if (num <= Rates.CARD1_X_RATE.getValue()) {
			generateCard1XWin();
		
		// Generate card 1 corner win if the rate is within range
		} else if (num <= Rates.CARD1_FOUR_CORNERS_RATE.getValue()) {
			generateCard1CornerWin();	
			
		// Generate card 1 Letter X win if the rate is within range
		} else {
			generateCard1LineWin();	
		}
		
		generateCard2();
	}
	
	private void generateCard2() {
		//TODO remove here
		int num = 50;//this.ran.nextInt(100) + 1;
		boolean valid = false;
		
		while (!valid) {
		
			if (num <= Rates.CARD2_ONE_LINE_RATE.getValue())
				valid = generateCard2LineWin();
			else if (num > Rates.CARD2_ONE_LINE_RATE.getValue() && 
					num <= (100 - Rates.CARD2_FOUR_CORNERS_RATE.getValue()))
				generateCard2CornerWin();
			else 
				generateCard2XWin();
		}
	}
	
	private void generateCard1LineWin() {
		int pattern_id = ran.nextInt(12) + 1;
		resetCards(1);
		
		tmpCard.setID(++ this.id);
		tmpCard.setWinName("one_line_anyway");
		tmpCard.setWin(true);
		tmpCard.setCreditsWon(2);
		
		// Shuffle all the numbers for card columns
		Collections.shuffle(array1_15);
		Collections.shuffle(array16_30);
		Collections.shuffle(array31_45);
		Collections.shuffle(array46_60);
		Collections.shuffle(array61_75);
		
		// Keep a copy of the shuffled arrays
		this.tmpArray = new ArrayList<Integer>(array1_15);
		this.tmpArray2 = new ArrayList<Integer>(array16_30);
		this.tmpArray3 = new ArrayList<Integer>(array31_45);
		this.tmpArray4 = new ArrayList<Integer>(array46_60);
		this.tmpArray5 = new ArrayList<Integer>(array61_75);
		
		switch (pattern_id) {
		// 1st vertical column
		case 1:
			for (int i = 0; i < 5; i++) {
				this.called.add(this.tmpArray.get(0));
				tmpArray.remove(0);
			}
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(0, called.get(0));
			tmpCard.setNumberOnCard(5, called.get(1));
			tmpCard.setNumberOnCard(10, called.get(2));
			tmpCard.setNumberOnCard(14, called.get(3));
			tmpCard.setNumberOnCard(19, called.get(4));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
			
		// 2nd vertical column
		case 2:
			for (int i = 0; i < 5; i++) {
				this.called.add(this.tmpArray2.get(0));
				tmpArray2.remove(0);
			}
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(1, called.get(0));
			tmpCard.setNumberOnCard(6, called.get(1));
			tmpCard.setNumberOnCard(11, called.get(2));
			tmpCard.setNumberOnCard(15, called.get(3));
			tmpCard.setNumberOnCard(20, called.get(4));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		
		// 3rd vertical column
		case 3:
			for (int i = 0; i < 4; i++) {
				this.called.add(this.tmpArray3.get(0));
				tmpArray3.remove(0);
			}
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(2, called.get(0));
			tmpCard.setNumberOnCard(7, called.get(1));
			tmpCard.setNumberOnCard(16, called.get(2));
			tmpCard.setNumberOnCard(21, called.get(3));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		case 4:
			for (int i = 0; i < 5; i++) {
				this.called.add(this.tmpArray4.get(0));
				tmpArray4.remove(0);
			}
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(3, called.get(0));
			tmpCard.setNumberOnCard(8, called.get(1));
			tmpCard.setNumberOnCard(12, called.get(2));
			tmpCard.setNumberOnCard(17, called.get(3));
			tmpCard.setNumberOnCard(22, called.get(4));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		case 5:
			for (int i = 0; i < 5; i++) {
				this.called.add(this.tmpArray5.get(0));
				tmpArray5.remove(0);
			}
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(4, called.get(0));
			tmpCard.setNumberOnCard(9, called.get(1));
			tmpCard.setNumberOnCard(13, called.get(2));
			tmpCard.setNumberOnCard(18, called.get(3));
			tmpCard.setNumberOnCard(23, called.get(4));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		case 6:
			this.called.add(this.tmpArray.get(0));
			tmpArray.remove(0);
			this.called.add(this.tmpArray2.get(0));
			tmpArray2.remove(0);
			this.called.add(this.tmpArray3.get(0));
			tmpArray3.remove(0);
			this.called.add(this.tmpArray4.get(0));
			tmpArray4.remove(0);
			this.called.add(this.tmpArray5.get(0));
			tmpArray5.remove(0);
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(0, called.get(0));
			tmpCard.setNumberOnCard(1, called.get(1));
			tmpCard.setNumberOnCard(2, called.get(2));
			tmpCard.setNumberOnCard(3, called.get(3));
			tmpCard.setNumberOnCard(4, called.get(4));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		case 7:
			this.called.add(this.tmpArray.get(0));
			tmpArray.remove(0);
			this.called.add(this.tmpArray2.get(0));
			tmpArray2.remove(0);
			this.called.add(this.tmpArray3.get(0));
			tmpArray3.remove(0);
			this.called.add(this.tmpArray4.get(0));
			tmpArray4.remove(0);
			this.called.add(this.tmpArray5.get(0));
			tmpArray5.remove(0);
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(5, called.get(0));
			tmpCard.setNumberOnCard(6, called.get(1));
			tmpCard.setNumberOnCard(7, called.get(2));
			tmpCard.setNumberOnCard(8, called.get(3));
			tmpCard.setNumberOnCard(9, called.get(4));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		case 8:
			this.called.add(this.tmpArray.get(0));
			tmpArray.remove(0);
			this.called.add(this.tmpArray2.get(0));
			tmpArray2.remove(0);
			this.called.add(this.tmpArray4.get(0));
			tmpArray4.remove(0);
			this.called.add(this.tmpArray5.get(0));
			tmpArray5.remove(0);
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(10, called.get(0));
			tmpCard.setNumberOnCard(11, called.get(1));
			tmpCard.setNumberOnCard(12, called.get(2));
			tmpCard.setNumberOnCard(13, called.get(3));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		case 9:
			this.called.add(this.tmpArray.get(0));
			tmpArray.remove(0);
			this.called.add(this.tmpArray2.get(0));
			tmpArray2.remove(0);
			this.called.add(this.tmpArray3.get(0));
			tmpArray3.remove(0);
			this.called.add(this.tmpArray4.get(0));
			tmpArray4.remove(0);
			this.called.add(this.tmpArray5.get(0));
			tmpArray5.remove(0);
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(14, called.get(0));
			tmpCard.setNumberOnCard(15, called.get(1));
			tmpCard.setNumberOnCard(16, called.get(2));
			tmpCard.setNumberOnCard(17, called.get(3));
			tmpCard.setNumberOnCard(18, called.get(4));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		case 10:
			this.called.add(this.tmpArray.get(0));
			tmpArray.remove(0);
			this.called.add(this.tmpArray2.get(0));
			tmpArray2.remove(0);
			this.called.add(this.tmpArray3.get(0));
			tmpArray3.remove(0);
			this.called.add(this.tmpArray4.get(0));
			tmpArray4.remove(0);
			this.called.add(this.tmpArray5.get(0));
			tmpArray5.remove(0);
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(19, called.get(0));
			tmpCard.setNumberOnCard(20, called.get(1));
			tmpCard.setNumberOnCard(21, called.get(2));
			tmpCard.setNumberOnCard(22, called.get(3));
			tmpCard.setNumberOnCard(23, called.get(4));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		case 11:
			this.called.add(this.tmpArray.get(0));
			tmpArray.remove(0);
			this.called.add(this.tmpArray2.get(0));
			tmpArray2.remove(0);
			this.called.add(this.tmpArray4.get(0));
			tmpArray4.remove(0);
			this.called.add(this.tmpArray5.get(0));
			tmpArray5.remove(0);
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(0, called.get(0));
			tmpCard.setNumberOnCard(6, called.get(1));
			tmpCard.setNumberOnCard(17, called.get(2));
			tmpCard.setNumberOnCard(23, called.get(3));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			
			break;
		case 12:
			this.called.add(this.tmpArray.get(0));
			tmpArray.remove(0);
			this.called.add(this.tmpArray2.get(0));
			tmpArray2.remove(0);
			this.called.add(this.tmpArray4.get(0));
			tmpArray4.remove(0);
			this.called.add(this.tmpArray5.get(0));
			tmpArray5.remove(0);
			
			// Add the called numbers onto the card
			tmpCard.setNumberOnCard(19, called.get(0));
			tmpCard.setNumberOnCard(15, called.get(1));
			tmpCard.setNumberOnCard(8, called.get(2));
			tmpCard.setNumberOnCard(4, called.get(3));
			
			generateCard1RestLineWin(tmpCard);
			Collections.shuffle(this.called);
			break;
		}
		
		this.cards.add(tmpCard);
		this.CARD1_ONE_LINE --;
	}
	
	private void generateCard1CornerWin() {
	}
	
	private void generateCard1XWin() {
		resetCards(1);
		
		tmpCard.setID(++ this.id);
		tmpCard.setWinName("letter_x");
		tmpCard.setWin(true);
		tmpCard.setCreditsWon(80);
		
		// Shuffle all the numbers for card columns
		Collections.shuffle(array1_15);
		Collections.shuffle(array16_30);
		Collections.shuffle(array31_45);
		Collections.shuffle(array46_60);
		Collections.shuffle(array61_75);
		
		// Keep a copy of the shuffled arrays
		this.tmpArray = new ArrayList<Integer>(array1_15);
		this.tmpArray2 = new ArrayList<Integer>(array16_30);
		this.tmpArray3 = new ArrayList<Integer>(array31_45);
		this.tmpArray4 = new ArrayList<Integer>(array46_60);
		this.tmpArray5 = new ArrayList<Integer>(array61_75);
		
		// Adding hits to card and called
		// 1st column
		tmpCard.setNumberOnCard(0, tmpArray.get(0));
		called.add(tmpArray.get(0));
		tmpArray.remove(0);
		
		tmpCard.setNumberOnCard(19, tmpArray.get(0));
		called.add(tmpArray.get(0));
		tmpArray.remove(0);
		
		// 2nd column
		tmpCard.setNumberOnCard(6, tmpArray2.get(0));
		called.add(tmpArray2.get(0));
		tmpArray2.remove(0);
		
		tmpCard.setNumberOnCard(15, tmpArray2.get(0));
		called.add(tmpArray2.get(0));
		tmpArray2.remove(0);
		
		// 4th column
		tmpCard.setNumberOnCard(8, tmpArray4.get(0));
		called.add(tmpArray4.get(0));
		tmpArray4.remove(0);
		
		tmpCard.setNumberOnCard(17, tmpArray4.get(0));
		called.add(tmpArray4.get(0));
		tmpArray4.remove(0);
		
		// 5th column
		tmpCard.setNumberOnCard(4, tmpArray5.get(0));
		called.add(tmpArray5.get(0));
		tmpArray5.remove(0);
		
		tmpCard.setNumberOnCard(23, tmpArray5.get(0));
		called.add(tmpArray5.get(0));
		tmpArray5.remove(0);
		
		generateCard1RestWin();
		
		this.cards.add(tmpCard);
		Collections.shuffle(called);
		this.CARD1_X --;
	}
	
	private void generateCard1NoWin() {
		
	}
	
	private boolean generateCard2LineWin() {
		int pattern_id =0;
		boolean valid = false; 
		List<Integer> hits = new ArrayList<Integer>(5);
		resetCards(2);
		
		tmpCard.setID(++ this.id);
		tmpCard.setWinName("one_line_anyway");
		tmpCard.setWin(true);
		tmpCard.setCreditsWon(4);
		
		while (!valid) {
			pattern_id = ran.nextInt(12) + 1;
			this.tmpCalled = new ArrayList<Integer>(this.called);
			Collections.sort(tmpCalled);
			
			switch (pattern_id) {
			// 1st vertical column
			case 1:
				// Find if the called numbers have 5 numbers within the range of the first column of the card
				for (int i = 0; i < 5; i++) {
					int num = findNumInRange(tmpCalled, 1, 15);
					if (num != -1) 
						hits.add(num);
				    else 
						break;
				}
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(0, hits.get(0));
					tmpCard.setNumberOnCard(5, hits.get(1));
					tmpCard.setNumberOnCard(10, hits.get(2));
					tmpCard.setNumberOnCard(14, hits.get(3));
					tmpCard.setNumberOnCard(19, hits.get(4));
				
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			// 2nd vertical column
			case 2:
				// Find if the called numbers have 5 numbers within the range of the first column of the card
				for (int i = 0; i < 5; i++) {
					int num = findNumInRange(tmpCalled, 16, 30);
					if (num != -1) 
						hits.add(num);
					else 
						break;	
				}
				
				// If can not find 5 numbers, try another pattern
				if (hits.size() == 5) {
				// If the pattern can be formed
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(1, hits.get(0));
					tmpCard.setNumberOnCard(6, hits.get(1));
					tmpCard.setNumberOnCard(11, hits.get(2));
					tmpCard.setNumberOnCard(15, hits.get(3));
					tmpCard.setNumberOnCard(20, hits.get(4));
				
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
			
			// 3rd vertical column
			case 3:
				// Find if the called numbers have 4 numbers within the range of the first column of the card
				for (int i = 0; i < 4; i++) {
					int num = findNumInRange(tmpCalled, 31, 45);
					if (num != -1) 
						hits.add(num);
					else 
						break;
				}
				
				// If the pattern can be formed
				if (hits.size() == 4) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(2, hits.get(0));
					tmpCard.setNumberOnCard(7, hits.get(1));
					tmpCard.setNumberOnCard(16, hits.get(2));
					tmpCard.setNumberOnCard(21, hits.get(3));
				
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			case 4:
				// Find if the called numbers have 5 numbers within the range of the first column of the card
				for (int i = 0; i < 5; i++) {
					int num = findNumInRange(tmpCalled, 46, 60);
					if (num != -1) 
						hits.add(num);
					else 
						break;	
				}
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(3, hits.get(0));
					tmpCard.setNumberOnCard(8, hits.get(1));
					tmpCard.setNumberOnCard(12, hits.get(2));
					tmpCard.setNumberOnCard(17, hits.get(3));
					tmpCard.setNumberOnCard(22, hits.get(4));
				
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			case 5:
				// Find if the called numbers have 5 numbers within the range of the first column of the card
				for (int i = 0; i < 5; i++) {
					int num = findNumInRange(tmpCalled, 61, 75);
					if (num != -1) 
						hits.add(num);
					else 
						break;	
				}
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(4, hits.get(0));
					tmpCard.setNumberOnCard(9, hits.get(1));
					tmpCard.setNumberOnCard(13, hits.get(2));
					tmpCard.setNumberOnCard(18, hits.get(3));
					tmpCard.setNumberOnCard(23, hits.get(4));
				
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			case 6:
				hits = findHorizontalNums(5);
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(0, hits.get(0));
					tmpCard.setNumberOnCard(1, hits.get(1));
					tmpCard.setNumberOnCard(2, hits.get(2));
					tmpCard.setNumberOnCard(3, hits.get(3));
					tmpCard.setNumberOnCard(4, hits.get(4));
					
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			case 7:
				hits = findHorizontalNums(5);
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(5, hits.get(0));
					tmpCard.setNumberOnCard(6, hits.get(1));
					tmpCard.setNumberOnCard(7, hits.get(2));
					tmpCard.setNumberOnCard(8, hits.get(3));
					tmpCard.setNumberOnCard(9, hits.get(4));
					
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			case 8:
				hits = findHorizontalNums(4);
				
				// If the pattern can be formed
				if (hits.size() == 4) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(10, hits.get(0));
					tmpCard.setNumberOnCard(11, hits.get(1));
					tmpCard.setNumberOnCard(12, hits.get(2));
					tmpCard.setNumberOnCard(13, hits.get(3));
					
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			case 9:
				hits = findHorizontalNums(5);
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(14, hits.get(0));
					tmpCard.setNumberOnCard(15, hits.get(1));
					tmpCard.setNumberOnCard(16, hits.get(2));
					tmpCard.setNumberOnCard(17, hits.get(3));
					tmpCard.setNumberOnCard(18, hits.get(4));
					
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			case 10:
				hits = findHorizontalNums(5);
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(19, hits.get(0));
					tmpCard.setNumberOnCard(20, hits.get(1));
					tmpCard.setNumberOnCard(21, hits.get(2));
					tmpCard.setNumberOnCard(22, hits.get(3));
					tmpCard.setNumberOnCard(23, hits.get(4));
					
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			case 11:
				hits = findHorizontalNums(4);
				
				// If the pattern can be formed
				if (hits.size() == 4) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(0, hits.get(0));
					tmpCard.setNumberOnCard(6, hits.get(1));
					tmpCard.setNumberOnCard(17, hits.get(2));
					tmpCard.setNumberOnCard(23, hits.get(3));
					
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			case 12:
				hits = findHorizontalNums(4);
				
				// If the pattern can be formed
				if (hits.size() == 4) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(19, hits.get(0));
					tmpCard.setNumberOnCard(15, hits.get(1));
					tmpCard.setNumberOnCard(8, hits.get(2));
					tmpCard.setNumberOnCard(4, hits.get(3));
					
					valid = generateCard2RestLineWin(tmpCard);
				}
				
				hits.clear();
				break;
				
			}
			
		}
		
		this.cards.add(tmpCard);
		this.CARD2_ONE_LINE --;
		this.play.put(this.called, this.cards);
		this.cards = new ArrayList<Card>(2);
		this.called = new ArrayList<Integer>(25);
		return valid;
	}
	
	private void generateCard2CornerWin() {
		this.play.put(this.called, this.cards);
	}
	
	private void generateCard2XWin() {
		this.play.put(this.called, this.cards);
	}
	  
	private boolean generateCard2NoWin() {
		int max_corner_hits_allowed = 3;
		int[] max_column_hits_allowed = {4, 4, 3, 4, 4};
		int[] max_row_hits_allowed = {4, 4, 3, 4, 4};
		boolean valid = true;
		
		this.tmpCalled = new ArrayList<Integer>(this.called);
		Collections.sort(tmpCalled);
		
		// Generate first column
		for (int i = 0; i < 20; i += 5) {	
			int row = 0;
			// If the cell is going to be a hit
			if (ran.nextInt(100) + 1 <= this.HITODDS) {
				int num = findNumInRange(this.tmpCalled, 1, 15);
				
				// If the top or bottom left corner is going to be a hit 
				if ((i == 0 || i == 19) && max_corner_hits_allowed > 0 && num != -1
						&& max_column_hits_allowed[0] > 0 && max_row_hits_allowed[row] > 0) { 
					max_corner_hits_allowed --;
					max_column_hits_allowed[0] --;
					max_row_hits_allowed[row] --;
					tmpCard.setNumberOnCard(i, num);
				// All the other cells in first column
				} else if ((i != 0 || i != 19) && num != -1 
						&& max_column_hits_allowed[0] > 0 && max_row_hits_allowed[row] > 0) {
					max_column_hits_allowed[0] --;
					max_row_hits_allowed[row] --;
					tmpCard.setNumberOnCard(i, num);
				// If can't find any number to be a hit, find a number from the uncalled array
				} else if (tmpArray.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
					this.tmpArray.remove(0);
				// If the card can't be filled
				} else {
					valid = false;
					return valid;
				}
				
			// If the cell is not going to be a hits 
			} else if (tmpArray.size() > 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
				this.tmpArray.remove(0);
			// If the card can't be filled
			} else {
				valid = false;
				return valid;
			}
			// fix card index to avoid generating number for the middle free cell
			if (i == 10)
				i --;
			
			row++;
		}
		
		
		// Generate 2nd column;
		for (int i = 1; i < 21; i += 5) {
			int row = 0;
			// If there is no number on the card at this cell
			if (tmpCard.getNumberOnCard(i) == 0) {
			
				// If the cell is going to be a hit
				if (ran.nextInt(100) + 1 <= this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 16, 30);
					// If the hit can be added to card
					if (num != -1 && max_column_hits_allowed[1] > 0 && max_row_hits_allowed[row] > 0) {
						max_column_hits_allowed[1] --;
						max_row_hits_allowed[row] --;
						tmpCard.setNumberOnCard(i, num);
					// If can't find any number to be a hit, find a number from the uncalled array
					} else if (tmpArray2.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
						this.tmpArray2.remove(0);
					// If the card can't be filled
					} else {
						valid = false;
						return valid;
					}
				// If it is not going to be a hit
				} else if (tmpArray2.size() > 0){
					tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
					this.tmpArray2.remove(0);
				// If the card can't be filled
				} else {
					valid = false;
					return valid;
				}
			}
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i --;
			
			row ++;
		}
		
		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			int row = 0;
			// Do not add numbers for the center cell
			if (i != 12) {
				// If there is no number on the card at this cell
				if (tmpCard.getNumberOnCard(i) == 0) {
				
					// If the cell is going to be a hit
					if (ran.nextInt(100) + 1 <= this.HITODDS) {
						int num = findNumInRange(this.tmpCalled, 31, 45);
						// If the hit can be added to card
						if (num != -1 && max_column_hits_allowed[2] > 0 && max_row_hits_allowed[row] > 0) {
							max_column_hits_allowed[2] --;
							max_row_hits_allowed[row] --;
							tmpCard.setNumberOnCard(i, num);
						// If no numbers are hit, find a number from the uncalled array
						} else if (tmpArray3.size() > 0){
							tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
							this.tmpArray3.remove(0);
						// If the card can't be filled
						} else {
							valid = false;
							return valid;
						}
					// If it is not going to be a hit
					} else if (tmpArray3.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
						this.tmpArray3.remove(0);
					// If the card can't be filled
					} else {
						valid = false;
						return valid;
					}
				} 
			} else {
				i --;
			}
			row ++;
		}
		
		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			int row = 0;
			// If there is no number on the card at this cell
			if (tmpCard.getNumberOnCard(i) == 0) {
				
				// If the cell is going to be a hit
				if (ran.nextInt(100) + 1 <= this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 46, 60);
					// If the hit can be added to the card
					if (num != -1 && max_column_hits_allowed[3] > 0 && max_row_hits_allowed[row] > 0) {
						max_column_hits_allowed[3] --;
						max_row_hits_allowed[row] --;
						tmpCard.setNumberOnCard(i, num);
					// If can't find any number to be a hit, find a number from the uncalled array
					} else if (tmpArray4.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
						this.tmpArray4.remove(0);
					// If the card can't be filled
					} else {
						valid = false;
						return valid;
					}
				// If it is not going to be a hit
				} else if (tmpArray4.size() > 0){
					tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
					this.tmpArray4.remove(0);
				// If the card can't be filled
				} else {
					valid = false;
					return valid;
				}
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i --;
			
			row ++;
		}
		
		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			int row = 0;
			// If there is no number on the card at this cell
			if (tmpCard.getNumberOnCard(i) == 0) {
				
				// If the cell is going to be a hit
				if (ran.nextInt(100) + 1 <= this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 61, 75);

					// If top or bottom right corner is going to be a hit
					if ((i == 4 || i == 23) && max_corner_hits_allowed > 0 && num != -1
							&& max_column_hits_allowed[4] > 0 && max_row_hits_allowed[row] > 0) {
						max_column_hits_allowed[4] --;
						max_row_hits_allowed[row] --;
						max_corner_hits_allowed --;
						tmpCard.setNumberOnCard(i, num);
					// If any other cell in the last column is going to be a hit 
					} else if ((i != 4 || i != 23) && num != -1
							&& max_column_hits_allowed[4] > 0 && max_row_hits_allowed[row] > 0){
						max_column_hits_allowed[4] --;
						max_row_hits_allowed[row] --;
						tmpCard.setNumberOnCard(i, num); 
					// If no numbers are hit, find a number from the uncalled array
					} else if (tmpArray5.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
						this.tmpArray5.remove(0);
					} else {
						valid = false;
						return valid;
					}
				// If it is not going to be a hit
				} else if (tmpArray5.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
					this.tmpArray5.remove(0);
				// If the card can't be filled
				} else {
					valid = false;
					return false;
				}
			}
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i --;
			
			row ++;
		}
		
		return valid;
		
	}
	
	private void generateCard1RestLineWin(Card card) {
		int max_corner_hits_allowed = 3; 
		
		// Check the number of hits on corners
		if (card.getNumberOnCard(0) != 0) 
			max_corner_hits_allowed --;
		if (card.getNumberOnCard(4) != 0) 
			max_corner_hits_allowed --;
		if (card.getNumberOnCard(19) != 0) 
			max_corner_hits_allowed --;
		if (card.getNumberOnCard(23) != 0) 
			max_corner_hits_allowed --;
		
		// Generate first column
		for (int i = 0; i < 20; i += 5) {
			// If not hits
			if (card.getNumberOnCard(i) == 0) {
				card.setNumberOnCard(i, this.tmpArray.get(0));
				
				// Also randomly add this number to the numbers called
				if (ran.nextInt(100) + 1 <= this.HITODDS) {
					// If the top or bottom left corner is going to be a hit 
					if (i == 0 || i == 19 && max_corner_hits_allowed > 0) { 
						this.called.add(this.tmpArray.get(0));
						max_corner_hits_allowed --;
					// All the other cells in first column
					} else if (i != 0 || i != 19) {
						this.called.add(this.tmpArray.get(0));
					}
				}
				this.tmpArray.remove(0);
			}
			// fix card index to avoid generating number for the middle free cell
			if (i == 10)
				i --;
		}
		
		// Generate 2nd column;
		for (int i = 1; i < 21; i += 5) {
			// If not hits
			if (card.getNumberOnCard(i) == 0) {
				card.setNumberOnCard(i, this.tmpArray2.get(0));
			
				// Also randomly add this number to the numbers called
				if (ran.nextInt(100) + 1 <= this.HITODDS)
					this.called.add(this.tmpArray2.get(0));
				this.tmpArray2.remove(0);
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i --; 
		}
		
		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			// Do not add numbers for the middle cell
			if (i != 12) {
				// If not hits
				if (card.getNumberOnCard(i) == 0) {
					card.setNumberOnCard(i, this.tmpArray3.get(0));
				
					// Also randomly add this number to the numbers called
					if (ran.nextInt(100) + 1 <= this.HITODDS)
						this.called.add(this.tmpArray3.get(0));
					this.tmpArray3.remove(0);
				} 
			} else {
				i --;
			}
		}
		
		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			// If not hits
			if (card.getNumberOnCard(i) == 0) {
				card.setNumberOnCard(i, this.tmpArray4.get(0));
			
				// Also randomly add this number to the numbers called
				if (ran.nextInt(100) + 1 <= this.HITODDS)
					this.called.add(this.tmpArray4.get(0));
				this.tmpArray4.remove(0);
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i --;
		}
		
		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			// If not hits
			if (card.getNumberOnCard(i) == 0) {
				card.setNumberOnCard(i, this.tmpArray5.get(0));
				
				// Also randomly add this number to the numbers called
				if (ran.nextInt(100) + 1 <= this.HITODDS) {
					// If top or bottom right corner is going to be a hit
					if (i == 4 || i == 23 && max_corner_hits_allowed > 0) {
						this.called.add(this.tmpArray5.get(0));
						max_corner_hits_allowed --;
					} else if (i != 4 || i != 23){
						this.called.add(this.tmpArray5.get(0));
					}	
				}
				this.tmpArray5.remove(0);
			}
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i --;
		}
		
		generateRestCalled();
		
	}
	
	private void generateCard1RestWin() {
		// Generate first column
		for (int i = 0; i < 20; i += 5) {
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
				
				// Also randomly add this number to the numbers called
				if (ran.nextInt(100) + 1 <= this.HITODDS) 
					this.called.add(this.tmpArray.get(0));
				this.tmpArray.remove(0);
			}
			// fix card index to avoid generating number for the middle free cell
			if (i == 10)
				i --;
		}
		
		// Generate 2nd column;
		for (int i = 1; i < 21; i += 5) {
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
			
				// Also randomly add this number to the numbers called
				if (ran.nextInt(100) + 1 <= this.HITODDS)
					this.called.add(this.tmpArray2.get(0));
				this.tmpArray2.remove(0);
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i --; 
		}
		
		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			// Do not add numbers for the middle cell
			if (i != 12) {
				// If not hits
				if (tmpCard.getNumberOnCard(i) == 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
				
					// Also randomly add this number to the numbers called
					if (ran.nextInt(100) + 1 <= this.HITODDS)
						this.called.add(this.tmpArray3.get(0));
					this.tmpArray3.remove(0);
				} 
			} else {
				i --;
			}
		}
		
		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
			
				// Also randomly add this number to the numbers called
				if (ran.nextInt(100) + 1 <= this.HITODDS)
					this.called.add(this.tmpArray4.get(0));
				this.tmpArray4.remove(0);
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i --;
		}
		
		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
				
				// Also randomly add this number to the numbers called
				if (ran.nextInt(100) + 1 <= this.HITODDS) 
					this.called.add(this.tmpArray5.get(0));
				this.tmpArray5.remove(0);
			}
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i --;
		}
		
		generateRestCalled();
	}
	
	private boolean generateCard2RestLineWin(Card card) {
		int max_corner_hits_allowed = 3; 
		boolean valid = true;
		
		// Check the number of hits on corners
		if (card.getNumberOnCard(0) != 0) 
			max_corner_hits_allowed --;
		if (card.getNumberOnCard(4) != 0) 
			max_corner_hits_allowed --;
		if (card.getNumberOnCard(19) != 0) 
			max_corner_hits_allowed --;
		if (card.getNumberOnCard(23) != 0) 
			max_corner_hits_allowed --;
		
		// Generate first column
		for (int i = 0; i < 20; i += 5) {
			// If there is no number on the card at this cell
			if (card.getNumberOnCard(i) == 0) {
				
				// If the cell is going to be a hit
				if (ran.nextInt(100) + 1 <= this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 1, 15);

					// If the top or bottom left corner is going to be a hit 
					if ((i == 0 || i == 19) && max_corner_hits_allowed > 0 && num != -1) { 
						max_corner_hits_allowed --;
						card.setNumberOnCard(i, num);
					// All the other cells in first column
					} else if ((i != 0 || i != 19) && num != -1) {
						card.setNumberOnCard(i, num);
					// If no numbers are hit, find a number from the uncalled array
					} else if (tmpArray.size() > 0) {
						card.setNumberOnCard(i, this.tmpArray.get(0));
						this.tmpArray.remove(0);
					// If the card can't be filled
					} else {
						valid = false;
						return valid;
					}
				// If it is not going to be a hit
				} else if (tmpArray.size() > 0) {
					card.setNumberOnCard(i, this.tmpArray.get(0));
					this.tmpArray.remove(0);
				// If the card can't be filled
				} else {
					valid = false;
					return valid;
				}
			} 
			// fix card index to avoid generating number for the middle free cell
			if (i == 10)
				i --;
		}
		
		// Generate 2nd column;
		for (int i = 1; i < 21; i += 5) {
			// If there is no number on the card at this cell
			if (card.getNumberOnCard(i) == 0) {
			
				// If the cell is going to be a hit
				if (ran.nextInt(100) + 1 <= this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 16, 30);
					if (num != -1) {
						card.setNumberOnCard(i, num);
					// If no numbers are hit, find a number from the uncalled array
					} else if (tmpArray2.size() > 0) {
						card.setNumberOnCard(i, this.tmpArray2.get(0));
						this.tmpArray2.remove(0);
					// If the card can't be filled
					} else {
						valid = false;
						return valid;
					}
				// If it is not going to be a hit
				} else if (tmpArray2.size() > 0){
					card.setNumberOnCard(i, this.tmpArray2.get(0));
					this.tmpArray2.remove(0);
				// If the card can't be filled
				} else { 
					valid = false;
					return valid;
			}
		}
			
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i --; 
		}
		
		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			// Do not add numbers for the center cell
			if (i != 12) {
				// If there is no number on the card at this cell
				if (card.getNumberOnCard(i) == 0) {
				
					// If the cell is going to be a hit
					if (ran.nextInt(100) + 1 <= this.HITODDS) {
						int num = findNumInRange(this.tmpCalled, 31, 45);
						if (num != -1) {
							card.setNumberOnCard(i, num);
						// If no numbers are hit, find a number from the uncalled array
						} else if (tmpArray3.size() > 0){
							card.setNumberOnCard(i, this.tmpArray3.get(0));
							this.tmpArray3.remove(0);
						// If the card can't be filled
						} else {
							valid = false;
							return valid;
						}
					// If it is not going to be a hit
					} else if (tmpArray3.size() > 0) {
						card.setNumberOnCard(i, this.tmpArray3.get(0));
						this.tmpArray3.remove(0);
					// If the card can't be filled
					} else {
						valid = false;
						return valid;
					}
				} 
			} else {
				i --;
			}
		}
		
		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			// If there is no number on the card at this cell
			if (card.getNumberOnCard(i) == 0) {
				
				// If the cell is going to be a hit
				if (ran.nextInt(100) + 1 <= this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 46, 60);
					if (num != -1) {
						card.setNumberOnCard(i, num);
					// If no numbers are hit, find a number from the uncalled array
					} else if (tmpArray4.size() > 0) {
						card.setNumberOnCard(i, this.tmpArray4.get(0));
						this.tmpArray4.remove(0);
					// If the card can't be filled
					} else {
						valid = false;
						return valid;
					}
				// If it is not going to be a hit
				} else if (tmpArray4.size() > 0){
					card.setNumberOnCard(i, this.tmpArray4.get(0));
					this.tmpArray4.remove(0);
				// If the card can't be filled
				} else {
					valid = false;
					return valid;
				}
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i --;
		}
		
		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			// If there is no number on the card at this cell
			if (card.getNumberOnCard(i) == 0) {
				
				// If the cell is going to be a hit
				if (ran.nextInt(100) + 1 <= this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 61, 75);

					// If top or bottom right corner is going to be a hit
					if ((i == 4 || i == 23) && max_corner_hits_allowed > 0 && num != -1) {
						card.setNumberOnCard(i, num);
						max_corner_hits_allowed --;
					// If any other cell in the last column is going to be a hit 
					} else if ((i != 4 || i != 23) && num != -1){
						card.setNumberOnCard(i, num); 
					// If no numbers are hit, find a number from the uncalled array
					} else if (tmpArray5.size() > 0) {
						card.setNumberOnCard(i, this.tmpArray5.get(0));
						this.tmpArray5.remove(0);
					// If the card can't be filled
					} else {
						valid = false;
						return valid;
					}
				// If it is not going to be a hit
				} else if (tmpArray5.size() > 0) {
					card.setNumberOnCard(i, this.tmpArray5.get(0));
					this.tmpArray5.remove(0);
				// If the card can't be filled
				} else {
					valid = false;
					return valid;
				}
			}
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i --;
		}
		
		return valid;
	}
	
	private void generateRestCalled() {
		// Generate the rest of numbers called
		Collections.shuffle(array1_75);
		List<Integer> tmp = new ArrayList<Integer>(array1_75);
		int size = 25 - called.size();
		
		// Remove the number on card 1 from the 75 numbers
		for (int i = 0; i < this.tmpCard.getNumbersOnCard().length; i++)
			tmp.remove(Integer.valueOf(tmpCard.getNumberOnCard(i)));
		
		// Fill the numbers called array
		for (int i = 0; i < size; i++) {
				this.called.add(tmp.get(0));
				removeFromTmpArrays(tmp.get(0));
				tmp.remove(0);
		}
	}
	
	private void removeFromTmpArrays(int num) {
		if (num < 16)
			tmpArray.remove(Integer.valueOf(num));
		else if (num >= 16 && num < 30)
			tmpArray2.remove(Integer.valueOf(num));
		else if (num >= 30 && num < 46)
			tmpArray3.remove(Integer.valueOf(num));
		else if (num >= 46 && num < 60)
			tmpArray4.remove(Integer.valueOf(num));
		else
			tmpArray5.remove(Integer.valueOf(num));
	}
	
	private int findNumInRange(List<Integer> list, int low, int high) {
		int max = list.size();
		int min = 0;
		Integer num = 0;
		int result = 0;
		
		while (result == 0) {
			int index = (max + min) / 2 ;
			
			if (max != min) 
				num = list.get(index);
			
			// Can't find it
			if (max <= min) {
				result = -1;
			// Too small
			} else if (num < low) {
				min = index + 1;
			// Too large
			} else if (num > high) {
				max = index - 1;
			// Found it!
			} else {
				result = num;
				list.remove(num);
			}
		}
		
		return result;
	}
	
	private List<Integer> findHorizontalNums(int length) {
		List<Integer> nums = new ArrayList<Integer>(length);
		int num = 0; 
		
		// find number under "B"
		num = findNumInRange(tmpCalled, 1, 15);
		if (num != -1)
			nums.add(num);
		
		// find number under "I"
		num = findNumInRange(tmpCalled, 16, 30);
		if (num != -1)
			nums.add(num);
		
		// find number under "N" (if not middle row)
		if (length != 4) {
			num = findNumInRange(tmpCalled, 31, 45);
			if (num != -1)
				nums.add(num);
		}
		
		// find number under "G"
		num = findNumInRange(tmpCalled, 46, 60);
		if (num != -1)
			nums.add(num);
		
		// find number under "O"
		num = findNumInRange(tmpCalled, 61, 75);
		if (num != -1)
			nums.add(num);
		
		return nums;
	}
	
	private void resetCards(int cardID) {
		// Reset the two cards
		this.tmpCard = model.new Card();
		
		if (cardID == 1)
			tmpCard.setTIID((short)1);
		else 
			tmpCard.setTIID((short)2);
	}
	
}

package com.eBingo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.eBingo.EBingoModel.Card;

public class TreasureIslandGenerator {
	private static enum Rates {
		CARD1_ONE_LINE_RATE(98), CARD1_FOUR_CORNERS_RATE(1), CARD1_X_RATE(1), 
		CARD2_ONE_LINE_RATE(93), CARD2_FOUR_CORNERS_RATE(6), CARD2_X_RATE(1);

		private int rate;

		private Rates(int rate) {
			this.rate = rate;
		}

		private int getValue() {
			return rate;
		}
	};

	private final int HITODDS = 20;

	private int CARD1_ONE_LINE = 23000;
	private int CARD1_FOUR_CORNERS = 250;
	private int CARD1_X = 13;
	private int CARD2_ONE_LINE = 800;
	private int CARD2_FOUR_CORNERS = 60;
	private int CARD2_X = 3;
	private int DUAL_WINS = 295;
	private int NO_WINS = 25874;

	Random ran = new Random();

	EBingoModel model = null;
	long id = 0;
	Map<List<Integer>, List<Card>> play = new HashMap<List<Integer>, List<Card>>(
			50000);
	List<Integer> called = new ArrayList<Integer>(25);
	List<Card> cards = new ArrayList<Card>(2);

	Card tmpCard = null;
	List<Integer> tmpArray = null;
	List<Integer> tmpArray2 = null;
	List<Integer> tmpArray3 = null;
	List<Integer> tmpArray4 = null;
	List<Integer> tmpArray5 = null;
	List<Integer> tmpCalled = null;
	
	List<Integer> ArrayBackup = null;
	List<Integer> Array2Backup = null;
	List<Integer> Array3Backup = null;
	List<Integer> Array4Backup = null;
	List<Integer> Array5Backup = null;

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
		while (this.DUAL_WINS > 0) {
			generateDualWins();
			this.DUAL_WINS--;
		}

		// Generate Card #1 X wins
		while (this.CARD1_X > 0) {
			while (!valid) {
				generateCard1XWin();
				valid = generateCard2NoWin();
				if (!valid)
					this.id -= 2;
			}
			valid = false;
			this.CARD1_X--;
			this.NO_WINS--;
		}

		// Generate Card #1 4 corners wins
		while (this.CARD1_FOUR_CORNERS > 0) {
			while (!valid) {
				generateCard1CornerWin();
				valid = generateCard2NoWin();
				if (!valid)
					this.id -= 2;
			}
			valid = false;
			this.CARD1_FOUR_CORNERS--;
			this.NO_WINS--;
		}

		// Generate Card #1 1 line wins
		while (this.CARD1_ONE_LINE > 0) {
			while (!valid) {
				generateCard1LineWin();
				valid = generateCard2NoWin();
				if (!valid)
					this.id -= 2;
			}
			valid = false;
			this.CARD1_ONE_LINE--;
			this.NO_WINS--;
		}

		// Generate Card #2 X wins
		while (this.CARD2_X > 0) {
			while (!valid) {
				generateCard1NoWin();
				valid = generateCard2XWin();
				if (!valid)
					this.id -= 2;
			}
			valid = false;
			this.NO_WINS--;
			this.CARD2_X--;
		}

		// Generate Card #2 4 corners wins
		while (this.CARD2_FOUR_CORNERS > 0) {
			while(!valid) {
				generateCard1NoWin();
				valid = generateCard2CornerWin();
				if (!valid)
					this.id -= 2;
			}
			valid = false;
			this.NO_WINS--;
			this.CARD2_FOUR_CORNERS--;
		}

		// Generate Card #2 1 line wins
		while (this.CARD2_ONE_LINE > 0) {
			while(!valid) {
				generateCard1NoWin();
				valid = generateCard2LineWin();
				if (!valid)
					this.id -= 2;
			}
			valid = false;
			this.NO_WINS--;
			this.CARD2_ONE_LINE--;
		}
		
		// Generate no wins
		while (this.NO_WINS > 0) {
			while (!valid) {
				generateCard1NoWin();
				valid = generateCard2NoWin();
				if (!valid)
					this.id -= 2;
			}
			valid = false;
			this.NO_WINS -= 2;
		}
	}

	private void generateDualWins() {
		int rate = Rates.CARD1_ONE_LINE_RATE.getValue();
		boolean c2valid = false;
		boolean c1valid = false;
		int wintype = 0;

		while (!c2valid) {
			
			while (!c1valid) {
				int num = ran.nextInt(100) + 1;
				// Generate card 1 line win
				if (num <= rate && this.CARD1_ONE_LINE > 0) {
					generateCard1LineWin();
					wintype = 1;
					c1valid = true;
				// Generate card 1 corner win
				} else if (num > rate && num <= (rate + Rates.CARD1_FOUR_CORNERS_RATE.getValue())
						&& this.CARD1_FOUR_CORNERS > 0) {
					generateCard1CornerWin();
					wintype = 2;
					c1valid = true;
				// Generate card 1 Letter X win 
				} else if (this.CARD1_X > 0) {
					generateCard1XWin();
					wintype = 3;
					c1valid = true;
				}
				
				// Decrement card id if card 1 is not valid
				if (!c1valid)
					this.id--;
			}
			
			c2valid = generateCard2();
			
			if (!c2valid) {
				this.id--;
			} else {
				// Decrement win counts for card 1 if card 2 is valid
				switch (wintype) {
				case 1:
					this.CARD1_ONE_LINE--;
					break;
				case 2:
					this.CARD1_FOUR_CORNERS--;
					break;
				case 3:
					this.CARD1_X--;
					break;
				default:
					break;
				}
			}
		}
	}

	private boolean generateCard2() {
		int num = this.ran.nextInt(100) + 1;
		int rate = Rates.CARD2_ONE_LINE_RATE.getValue();
		boolean valid = false;
		
		// Try to create a card 2 line win
		if (num <= rate && this.CARD2_ONE_LINE > 0) {
			if (generateCard2LineWin()) {
				this.CARD2_ONE_LINE--;
				valid = true;
			}
			return valid;
		// Try to create a card 2 corner win		
		} else if (num > rate && num <= (rate + Rates.CARD2_FOUR_CORNERS_RATE.getValue())
				&& this.CARD2_FOUR_CORNERS > 0) {
			if (generateCard2CornerWin()) {
				this.CARD2_FOUR_CORNERS--;
				valid = true;
			}
			return valid;
		// Try to create a card 2 X win
		} else if (this.CARD2_X > 0) {
			if (generateCard2XWin()) {
				this.CARD2_X--;
				valid = true;
			}
			return valid;
		// If no more card 2 win patterns of certain kind can be formed
		} else {
			return valid;
		}
	}

	private void generateCard1LineWin() {
		int pattern_id = ran.nextInt(12) + 1;
		resetCards(1);
		initializeTmpArrays();
		
		tmpCard.setID(++this.id);
		//tmpCard.setWinName("one_line_anyway");
		//tmpCard.setWin(true);
		//tmpCard.setCreditsWon(1);

		switch (pattern_id) {
		// 1st column
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
			
			generateCard1RestLineWin();
			break;

		// 2nd column
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
			
			generateCard1RestLineWin();
			break;

		// 3rd column
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
			
			generateCard1RestLineWin();
			break;

		// 4th column
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
			
			generateCard1RestLineWin();
			break;

		// 5th column
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
			
			generateCard1RestLineWin();
			break;

		// 1st row
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
			
			generateCard1RestLineWin();
			break;

		// 2nd row
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
			
			generateCard1RestLineWin();
			break;

		// 3rd row
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
			
			generateCard1RestLineWin();
			break;

		// 4th row
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
			
			generateCard1RestLineWin();
			break;

		// 5th row
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
			
			generateCard1RestLineWin();
			break;

		// left-to-right diagonal
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
			
			generateCard1RestLineWin();
			break;

		// right-to-left diagonal
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
	
			generateCard1RestLineWin();
			break;
		}
		
		generateRestCalled();
		Collections.shuffle(this.called);
		this.cards.add(tmpCard);
	}

	private void generateCard1CornerWin() {
		resetCards(1);
		initializeTmpArrays();
		
		tmpCard.setID(++this.id);
		//tmpCard.setWinName("4_corners");
		//tmpCard.setWin(true);
		//tmpCard.setCreditsWon(20);

		// Get 4 called numbers
		this.called.add(this.tmpArray.get(0));
		tmpArray.remove(0);
		this.called.add(this.tmpArray.get(0));
		tmpArray.remove(0);
		this.called.add(this.tmpArray5.get(0));
		tmpArray5.remove(0);
		this.called.add(this.tmpArray5.get(0));
		tmpArray5.remove(0);

		// Add the called numbers onto the card
		tmpCard.setNumberOnCard(0, called.get(0));
		tmpCard.setNumberOnCard(19, called.get(1));
		tmpCard.setNumberOnCard(4, called.get(2));
		tmpCard.setNumberOnCard(23, called.get(3));

		// Generate the rest of the card1
		generateCard1RestCornerWin();
		generateRestCalled();
		Collections.shuffle(this.called);
		this.cards.add(tmpCard);
	}

	private void generateCard1XWin() {
		resetCards(1);
		initializeTmpArrays();
		
		tmpCard.setID(++this.id);
		//tmpCard.setWinName("letter_x");
		//tmpCard.setWin(true);
		//tmpCard.setCreditsWon(500);
		
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

		generateCard1RestXWin();
		generateRestCalled();
		Collections.shuffle(called);
		this.cards.add(tmpCard);
	}

	private void generateCard1NoWin() {
		int max_corner_hits_allowed = 3;
		int max_inner_corner_hits_allowed = 3;
		int[] max_column_hits_allowed = {4, 4, 3, 4, 4};
		int[] max_row_hits_allowed = {4, 4, 3, 4, 4};
		int row = 0;
		
		resetCards(1);
		initializeTmpArrays();
		
		tmpCard.setID(++ this.id);
		
		// Generate first column
		for (int i = 0; i < 20; i += 5) {	
			int chance = ran.nextInt(100);
			
			// Always add numbers from the tmpArray to the card
			tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
			// If the cell is going to be a hit
			if (chance < this.HITODDS && this.called.size() <= 25 
					&& max_column_hits_allowed[0] > 0 && max_row_hits_allowed[row] > 0) {
				// If the top or bottom left corner is going to be a hit 
				if ((i == 0 || i == 19) && max_corner_hits_allowed > 0) { 
					max_corner_hits_allowed --;
					max_column_hits_allowed[0] --;
					max_row_hits_allowed[row] --;
					this.called.add(tmpArray.get(0));
				// All the other cells in first column
				} else if (i != 0 && i != 19) {
					max_column_hits_allowed[0] --;
					max_row_hits_allowed[row] --;
					this.called.add(tmpArray.get(0));
				}
			}
			this.tmpArray.remove(0);
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 10)
				i --;
			
			row++;
		}
		
		// Generate 2nd column;
		row = 0;
		for (int i = 1; i < 21; i += 5) {
			int chance = ran.nextInt(100);
			
			// Always add numbers from the tmpArray to the card	
			tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
			// If the cell is going to be a hit
			if (chance < this.HITODDS && this.called.size() <= 25 
					&& max_column_hits_allowed[1] > 0 && max_row_hits_allowed[row] > 0) {
					// Make sure no diagonal lines wins are possible
					if ((i == 6 || i == 15) &&
							isCornerNotHits(i) && max_inner_corner_hits_allowed > 0) {
						max_column_hits_allowed[1] --;
						max_row_hits_allowed[row] --;
						max_inner_corner_hits_allowed --;
						this.called.add(tmpArray2.get(0));
					} else if (i != 6 && i != 15) { 
						max_column_hits_allowed[1] --;
						max_row_hits_allowed[row] --;
						this.called.add(tmpArray2.get(0));
					}
			}

			this.tmpArray2.remove(0);
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i --;
			
			row ++;
		}
		
		// Generate 3rd column;
		row = 0;
		for (int i = 2; i < 22; i += 5) {
			int chance = ran.nextInt(100);
			
			// Do not add numbers for the center cell
			if (i != 12) {
				// Always add numbers from the tmpArray to the card
				tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
				
				// If the cell is going to be a hit
				if (chance < this.HITODDS && this.called.size() <= 25 
						&& max_column_hits_allowed[2] > 0 && max_row_hits_allowed[row] > 0) {
					max_column_hits_allowed[2] --;
					max_row_hits_allowed[row] --;
					this.called.add(tmpArray3.get(0));
				}
				this.tmpArray3.remove(0);
				
			} else {
				i --;
			}
			row ++;
		}
		
		// Generate 4th column;
		row = 0;
		for (int i = 3; i < 23; i += 5) {
			int chance = ran.nextInt(100);
			
			// Always add numbers from the tmpArray to the card	
			tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
			// If the cell is going to be a hit
			if (chance < this.HITODDS && this.called.size() <= 25 
					&& max_column_hits_allowed[3] > 0 && max_row_hits_allowed[row] > 0) {
				// Make sure no diagonal lines wins are possible
				if ((i == 8 || i == 17) 
						&& isCornerNotHits(i) && max_inner_corner_hits_allowed > 0) {
					max_column_hits_allowed[3] --;
					max_row_hits_allowed[row] --;
					max_inner_corner_hits_allowed --;
					this.called.add(tmpArray4.get(0));
				} else if (i != 8 && i != 17) { 
					max_column_hits_allowed[3] --;
					max_row_hits_allowed[row] --;
					this.called.add(tmpArray4.get(0));
				}
			}

		    this.tmpArray4.remove(0);
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i --;
			
			row ++;
		}
		
		// Generate 5th column;
		row = 0;
		for (int i = 4; i < 24; i += 5) {
			int chance = ran.nextInt(100);
			
			// Always add numbers from the tmpArray to the card
			tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
			// If the cell is going to be a hit
			if (chance < this.HITODDS && this.called.size() <= 25
					&& max_column_hits_allowed[4] > 0 && max_row_hits_allowed[row] > 0) {
				// If top or bottom right corner is going to be a hit
				if ((i == 4 || i == 23) && max_corner_hits_allowed > 0) {
					max_column_hits_allowed[4] --;
					max_row_hits_allowed[row] --;
					max_corner_hits_allowed --;
					this.called.add(tmpArray5.get(0));
				// If any other cell in the last column is going to be a hit 
				} else if (i != 4 && i != 23) {
					max_column_hits_allowed[4] --;
					max_row_hits_allowed[row] --;
					this.called.add(tmpArray5.get(0));
				}
			}

			this.tmpArray5.remove(0);
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i --;
			row ++;
		}
		
		this.generateRestCalled();
		Collections.shuffle(called);
		this.cards.add(tmpCard);
	}
	
	private boolean generateCard2LineWin() {
		int pattern_id = 1;
		boolean valid = false; 
		List<Integer> hits = new ArrayList<Integer>(5);
		List<Integer> pids = new ArrayList<Integer>(12);
		resetCards(2);
		
		tmpCard.setID(++ this.id);
		//tmpCard.setWinName("one_line_anyway");
		//tmpCard.setWin(true);
		//tmpCard.setCreditsWon(2);
		
		for (int i = 0; i < 12; i++)
			pids.add(i + 1);
		Collections.shuffle(pids);
		
		// Keep trying to fill card 2 until tried all 12 line win patterns
		while (!valid && pids.size() > 0) {
			pattern_id = pids.get(0);
			
			this.tmpCalled = new ArrayList<Integer>(this.called);
			Collections.sort(tmpCalled);
			
			switch (pattern_id) {
			
			// 1st column
			case 1:
				// Find if the called numbers have 5 numbers within the range of the first column of the card
				for (int i = 0; i < 5; i++) {
					int num = findNumInRange(tmpCalled, 1, 15);
					if (num > 0) {
						hits.add(num);
						tmpCalled.remove(Integer.valueOf(num));
					} else { 
						break;
					}
				}
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(0, hits.get(0));
					tmpCard.setNumberOnCard(5, hits.get(1));
					tmpCard.setNumberOnCard(10, hits.get(2));
					tmpCard.setNumberOnCard(14, hits.get(3));
					tmpCard.setNumberOnCard(19, hits.get(4));
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
					
				// If pattern can't be formed, add the found numbers back in	
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
				
			// 2nd column
			case 2:
				// Find if the called numbers have 5 numbers within the range of the first column of the card
				for (int i = 0; i < 5; i++) {
					int num = findNumInRange(tmpCalled, 16, 30);
					if (num > 0) {
						hits.add(num);
						tmpCalled.remove(Integer.valueOf(num));
					} else {
						break;
					}
				}
				
				// If can not find 5 numbers, try another pattern
				if (hits.size() == 5) {	
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(1, hits.get(0));
					tmpCard.setNumberOnCard(6, hits.get(1));
					tmpCard.setNumberOnCard(11, hits.get(2));
					tmpCard.setNumberOnCard(15, hits.get(3));
					tmpCard.setNumberOnCard(20, hits.get(4));
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
							
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
			// 3rd column
			case 3:
				// Find if the called numbers have 4 numbers within the range of the first column of the card
				for (int i = 0; i < 4; i++) {
					int num = findNumInRange(tmpCalled, 31, 45);
					if (num > 0) {
						hits.add(num);
						this.tmpCalled.remove(Integer.valueOf(num));
					} else { 
						break;
					}
				}
				
				// If the pattern can be formed
				if (hits.size() == 4) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(2, hits.get(0));
					tmpCard.setNumberOnCard(7, hits.get(1));
					tmpCard.setNumberOnCard(16, hits.get(2));
					tmpCard.setNumberOnCard(21, hits.get(3));
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
					
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
				// 4th column
			case 4:
				// Find if the called numbers have 5 numbers within the range of the first column of the card
				for (int i = 0; i < 5; i++) {
					int num = findNumInRange(tmpCalled, 46, 60);
					if (num > 0) {
						hits.add(num);
						this.tmpCalled.remove(Integer.valueOf(num));
					} else { 
						break;
					}
				}
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(3, hits.get(0));
					tmpCard.setNumberOnCard(8, hits.get(1));
					tmpCard.setNumberOnCard(12, hits.get(2));
					tmpCard.setNumberOnCard(17, hits.get(3));
					tmpCard.setNumberOnCard(22, hits.get(4));
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
				
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
				// 5th column
			case 5:
				// Find if the called numbers have 5 numbers within the range of the first column of the card
				for (int i = 0; i < 5; i++) {
					int num = findNumInRange(tmpCalled, 61, 75);
					if (num > 0) {
						hits.add(num);
						this.tmpCalled.remove(Integer.valueOf(num));
					} else { 
						break;
					}
				}
				
				// If the pattern can be formed
				if (hits.size() == 5) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(4, hits.get(0));
					tmpCard.setNumberOnCard(9, hits.get(1));
					tmpCard.setNumberOnCard(13, hits.get(2));
					tmpCard.setNumberOnCard(18, hits.get(3));
					tmpCard.setNumberOnCard(23, hits.get(4));
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
				
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
			// 1st row
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
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
				
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
			// 2nd row
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
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
				
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
			// 3rd row
			case 8:
				hits = findHorizontalNums(4);
				
				// If the pattern can be formed
				if (hits.size() == 4) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(10, hits.get(0));
					tmpCard.setNumberOnCard(11, hits.get(1));
					tmpCard.setNumberOnCard(12, hits.get(2));
					tmpCard.setNumberOnCard(13, hits.get(3));
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
				
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
			// 4th row	
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
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
				
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
			// 5th row	
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
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
				
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
			// diagonal win left to right
			case 11:
				hits = findHorizontalNums(4);
				
				// If the pattern can be formed
				if (hits.size() == 4) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(0, hits.get(0));
					tmpCard.setNumberOnCard(6, hits.get(1));
					tmpCard.setNumberOnCard(17, hits.get(2));
					tmpCard.setNumberOnCard(23, hits.get(3));
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
				
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;
			
			// diagonal win right to left	
			case 12:
				hits = findHorizontalNums(4);
				
				// If the pattern can be formed
				if (hits.size() == 4) {
					// Add the called numbers onto the card
					tmpCard.setNumberOnCard(19, hits.get(0));
					tmpCard.setNumberOnCard(15, hits.get(1));
					tmpCard.setNumberOnCard(8, hits.get(2));
					tmpCard.setNumberOnCard(4, hits.get(3));
					
					// If generating rest of line win failed
					if (!generateCard2RestLineWin()) {
						tmpCard.resetNumbersOnCard();
						resetTmpArrays();
					} else {
						valid = true;
					}
					
				} else {
					for (int j = 0; j < hits.size(); j++) {
						this.tmpCalled.add(hits.get(j));
					}
					Collections.sort(this.tmpCalled);
				}
				
				hits.clear();
				pids.remove(0);
				break;	
			}	
		}
		
		if (valid) {
			this.cards.add(tmpCard);
			this.play.put(this.called, this.cards);
		}
		
		pids.clear();
		return valid;
	}

	private boolean generateCard2CornerWin() {
		boolean valid = false;
		
		this.resetCards(2);
		tmpCard.setID(++id);
		//tmpCard.setWinName("4_corners");
		//tmpCard.setWin(true);
		//tmpCard.setCreditsWon(100);
		
		this.tmpCalled = new ArrayList<Integer>(this.called);
		Collections.sort(tmpCalled);
			
		// Set the four corners
		int num = findNumInRange(tmpCalled, 1, 15);
		// If a number in range can't be found for the 1st column
		if (num < 0) {
			return valid;
		} else { 
			tmpCard.setNumberOnCard(0, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		}
		
		num = findNumInRange(tmpCalled, 1, 15);
		// If a number in range can't be found for the 1st column
		if (num < 0) {
			return valid;
		} else { 
			tmpCard.setNumberOnCard(19, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		}
		
		num = findNumInRange(tmpCalled, 61, 75);
		// If a number in range can't be found for the 5th column
		if (num < 0) {
			return valid;
		} else { 
			tmpCard.setNumberOnCard(4, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		}
		
		num = findNumInRange(tmpCalled, 61, 75);
		// If a number in range can't be found for the 5th column
		if (num < 0) {
			return valid;
		} else { 
			tmpCard.setNumberOnCard(23, num);	
			this.tmpCalled.remove(Integer.valueOf(num));
		}
		
		valid = generateCard2RestCornerWin();
		
		// If the pattern can be formed
		if (valid) {
			this.cards.add(tmpCard);
			this.play.put(this.called, this.cards);
		}
		
		return valid;
	}
	
	private boolean generateCard2XWin() {
		boolean valid = false;
		
		this.resetCards(2);
		tmpCard.setID(++id);
		//tmpCard.setWinName("letter_x");
		//tmpCard.setWin(true);
		//tmpCard.setCreditsWon(1000);
		
		this.tmpCalled = new ArrayList<Integer>(this.called);
		Collections.sort(tmpCalled);
			
		// Find numbers in 1st column
		int num = findNumInRange(tmpCalled, 1, 15);
		
		if (num > 0) {
			tmpCard.setNumberOnCard(0, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		} else {
			return valid;
		}
		
		num = findNumInRange(tmpCalled, 1, 15);
		if (num > 0) {
			tmpCard.setNumberOnCard(19, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		} else {
			return valid;
		}
		
		// Find numbers in 2nd column
		num = findNumInRange(tmpCalled, 16, 30);
		if (num > 0) { 
			tmpCard.setNumberOnCard(6, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		} else {
			return valid;
		}
		
		num = findNumInRange(tmpCalled, 16, 30);
		if (num > 0) {
			tmpCard.setNumberOnCard(15, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		} else {
			return valid;
		}
		
		// Find numbers in 4th column
		num = findNumInRange(tmpCalled, 46, 60);
		if (num > 0) {
			tmpCard.setNumberOnCard(8, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		} else {
			return valid;
		}

		num = findNumInRange(tmpCalled, 46, 60);
		if (num > 0) {
			tmpCard.setNumberOnCard(17, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		} else {
			return valid;
		}
		
		// Find numbers in 5th column
		num = findNumInRange(tmpCalled, 61, 75);
		if (num > 0) { 
			tmpCard.setNumberOnCard(4, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		} else {
			return valid;
		}
		
		num = findNumInRange(tmpCalled, 61, 75);
		if (num > 0) {
			tmpCard.setNumberOnCard(23, num);
			this.tmpCalled.remove(Integer.valueOf(num));
		} else {
			return valid;	
		}
		
		valid = generateCard2RestXWin();
		
		// If the pattern can be formed
		if (valid) {
			this.cards.add(tmpCard);
			this.play.put(this.called, this.cards);
		}
		
		return valid;
	}
	  
	private boolean generateCard2NoWin() {
		int max_corner_hits_allowed = 3;
		int max_inner_corner_hits_allowed = 3;
		int[] max_column_hits_allowed = {4, 4, 3, 4, 4};
		int[] max_row_hits_allowed = {4, 4, 3, 4, 4};
		int row = 0;
		
		this.resetCards(2);
		tmpCard.setID(++id);
		
		this.tmpCalled = new ArrayList<Integer>(this.called);
		Collections.sort(tmpCalled);
		
		// Generate first column
		for (int i = 0; i < 20; i += 5) {	
			int chance = ran.nextInt(100);
			
			// If the cell is going to be a hit
			if (chance < this.HITODDS && max_column_hits_allowed[0] > 0 && max_row_hits_allowed[row] > 0) {
				int num = findNumInRange(this.tmpCalled, 1, 15);
				
				// If the top or bottom left corner is going to be a hit 
				if ((i == 0 || i == 19) && max_corner_hits_allowed > 0 && num > 0) { 
					max_corner_hits_allowed --;
					max_column_hits_allowed[0] --;
					max_row_hits_allowed[row] --;
					tmpCard.setNumberOnCard(i, num);
					this.tmpCalled.remove(Integer.valueOf(num));
				// All the other cells in first column
				} else if ((i != 0 && i != 19) && num > 0) {
					max_column_hits_allowed[0] --;
					max_row_hits_allowed[row] --;
					tmpCard.setNumberOnCard(i, num);
					this.tmpCalled.remove(Integer.valueOf(num));
				} else if (tmpArray.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
					this.tmpArray.remove(0);
				} else {
					return false;
				}
				
			// If the cell is not going to be a hits 
			} else if (tmpArray.size() > 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
				this.tmpArray.remove(0);
			// If the card can't be filled
			} else {
				return false;
			}
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 10)
				i --;
			
			row++;
		}
		
		
		// Generate 2nd column;
		row = 0;
		for (int i = 1; i < 21; i += 5) {
			int chance = ran.nextInt(100);	
			
			// If the cell is going to be a hit
			if (chance < this.HITODDS && max_column_hits_allowed[1] > 0 && max_row_hits_allowed[row] > 0) {
				int num = findNumInRange(this.tmpCalled, 16, 30);
				
				// Make sure no diagonal line wins can be formed
				if ((i == 6 || i == 15) && num > 0 
						&& isCornerNotHits(i) && max_inner_corner_hits_allowed > 0) {
					max_column_hits_allowed[1]--;
					max_row_hits_allowed[row]--;
					max_inner_corner_hits_allowed--;
					tmpCard.setNumberOnCard(i, num);
					this.tmpCalled.remove(Integer.valueOf(num));
				} else if ((i != 6 && i != 15) && num > 0) {
					max_column_hits_allowed[1]--;
					max_row_hits_allowed[row]--;
					tmpCard.setNumberOnCard(i, num);
					this.tmpCalled.remove(Integer.valueOf(num));
				} else if (tmpArray2.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
					this.tmpArray2.remove(0);
				} else {
					return false;
				}
				// If it is not going to be a hit
			} else if (tmpArray2.size() > 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
				this.tmpArray2.remove(0);
				// If the card can't be filled
			} else {
				return false;
			}

			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i --;
			
			row ++;
		}
		
		// Generate 3rd column;
		row = 0;
		for (int i = 2; i < 22; i += 5) {
			int chance = ran.nextInt(100);
			
			// Do not add numbers for the center cell
			if (i != 12) {			
				// If the cell is going to be a hit
				if (chance < this.HITODDS && max_column_hits_allowed[2] > 0 && max_row_hits_allowed[row] > 0) {
					int num = findNumInRange(this.tmpCalled, 31, 45);
					// If the hit can be added to card
					if (num > 0) {
						max_column_hits_allowed[2] --;
						max_row_hits_allowed[row] --;
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					} else if (tmpArray3.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
						this.tmpArray3.remove(0);
					} else {
						return false;
					}
					// If it is not going to be a hit
				} else if (tmpArray3.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
					this.tmpArray3.remove(0);
					// If the card can't be filled
				} else {
					return false;
				}
			} else {
				i --;
			}
			row ++;
		}
		
		// Generate 4th column;
		row = 0;
		for (int i = 3; i < 23; i += 5) {
			int chance = ran.nextInt(100);
			
			// If the cell is going to be a hit
			if (chance < this.HITODDS && max_column_hits_allowed[3] > 0 && max_row_hits_allowed[row] > 0) {
				int num = findNumInRange(this.tmpCalled, 46, 60);
				// Make sure no diagonal line wins can be formed
				if ((i == 8 || i == 17) && num > 0 
						&& isCornerNotHits(i) && max_inner_corner_hits_allowed > 0) {
					max_column_hits_allowed[3]--;
					max_row_hits_allowed[row]--;
					max_inner_corner_hits_allowed--;
					tmpCard.setNumberOnCard(i, num);
					this.tmpCalled.remove(Integer.valueOf(num));
				} else if ((i != 8 && i != 17) && num > 0) {
					max_column_hits_allowed[3]--;
					max_row_hits_allowed[row]--;
					tmpCard.setNumberOnCard(i, num);
					this.tmpCalled.remove(Integer.valueOf(num));
				} else if (tmpArray4.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
					this.tmpArray4.remove(0);
				} else {
					return false;
				}
				// If it is not going to be a hit
			} else if (tmpArray4.size() > 0){
				tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
				this.tmpArray4.remove(0);
				// If the card can't be filled
			} else {
				return false;
			} 	

			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i --;

			row ++;
		}
		
		// Generate 5th column;
		row = 0;
		for (int i = 4; i < 24; i += 5) {
			int chance = ran.nextInt(100);
			
			// If the cell is going to be a hit
			if (chance < this.HITODDS && max_column_hits_allowed[4] > 0 && max_row_hits_allowed[row] > 0) {
				int num = findNumInRange(this.tmpCalled, 61, 75);

				// If top or bottom right corner is going to be a hit
				if ((i == 4 || i == 23) && max_corner_hits_allowed > 0 && num > 0) {
					max_column_hits_allowed[4] --;
					max_row_hits_allowed[row] --;
					max_corner_hits_allowed --;
					tmpCard.setNumberOnCard(i, num);
					this.tmpCalled.remove(Integer.valueOf(num));
				// If any other cell in the last column is going to be a hit 
				} else if ((i != 4 && i != 23) && num > 0) {
					max_column_hits_allowed[4] --;
					max_row_hits_allowed[row] --;
					tmpCard.setNumberOnCard(i, num); 
					this.tmpCalled.remove(Integer.valueOf(num));
				} else if (tmpArray5.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
					this.tmpArray5.remove(0);
				} else {
					return false;
				}
				// If it is not going to be a hit
			} else if (tmpArray5.size() > 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
				this.tmpArray5.remove(0);
				// If the card can't be filled
			} else {
				return false;
			}

			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i --;
			row ++;
		}
		
		// If the pattern can be formed
		this.cards.add(tmpCard);
		this.play.put(this.called, this.cards);
		
		return true;
	}
	
	private void generateCard1RestLineWin() {
		int max_corner_hits_allowed = 3; 
		
		// Check the number of hits on corners
		if (tmpCard.getNumberOnCard(0) != 0) 
			max_corner_hits_allowed --;
		if (tmpCard.getNumberOnCard(4) != 0) 
			max_corner_hits_allowed --;
		if (tmpCard.getNumberOnCard(19) != 0) 
			max_corner_hits_allowed --;
		if (tmpCard.getNumberOnCard(23) != 0) 
			max_corner_hits_allowed --;
		
		// Generate first column
		for (int i = 0; i < 20; i += 5) {
			int chance = ran.nextInt(100); 
			
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
				
				// Also randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25) {
					// If the top or bottom left corner is going to be a hit 
					if ((i == 0 || i == 19) && max_corner_hits_allowed > 0) { 
						this.called.add(this.tmpArray.get(0));
						max_corner_hits_allowed --;
					// All the other cells in first column
					} else if (i != 0 && i != 19) {
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
			int chance = ran.nextInt(100);
			
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
			
				// Also randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25)
					this.called.add(this.tmpArray2.get(0));
				this.tmpArray2.remove(0);
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i --; 
		}
		
		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			int chance = ran.nextInt(100);
			
			// Do not add numbers for the middle cell
			if (i != 12) {
				// If not hits
				if (tmpCard.getNumberOnCard(i) == 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
				
					// Also randomly add this number to the numbers called
					if (chance < this.HITODDS && this.called.size() <= 25)
						this.called.add(this.tmpArray3.get(0));
					this.tmpArray3.remove(0);
				} 
			} else {
				i --;
			}
		}
		
		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			int chance = ran.nextInt(100);
			
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
			
				// Also randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25)
					this.called.add(this.tmpArray4.get(0));
				this.tmpArray4.remove(0);
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i --;
		}
		
		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			int chance = ran.nextInt(100);
			
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
				
				// Also randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25) {
					// If top or bottom right corner is going to be a hit
					if ((i == 4 || i == 23) && max_corner_hits_allowed > 0) {
						this.called.add(this.tmpArray5.get(0));
						max_corner_hits_allowed --;
					} else if (i != 4 && i != 23) {
						this.called.add(this.tmpArray5.get(0));
					}	
				}
				this.tmpArray5.remove(0);
			}
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i --;
		}
		
	}
	
	private void generateCard1RestCornerWin() {
		int max_inner_corner_hits_allowed = 3;
		// Generate first column
		for (int i = 0; i < 20; i += 5) {
			int chance = ran.nextInt(100);
			
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray.get(0));

				// Also randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25)
					this.called.add(this.tmpArray.get(0));
				this.tmpArray.remove(0);
			}
			// fix card index to avoid generating number for the middle free cell
			if (i == 10)
				i--;
		}

		// Generate 2nd column;
		for (int i = 1; i < 21; i += 5) {
			int chance = ran.nextInt(100);
			
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));

				// Randomly add this number to the numbers called and make sure no X pattern will be formed
				if (chance < this.HITODDS && this.called.size() <= 25) {
					if ((i == 6 || i == 15) 
							&& isCornerNotHits(i) && max_inner_corner_hits_allowed > 0) {
						this.called.add(this.tmpArray2.get(0));
						max_inner_corner_hits_allowed--;
					} else if (i != 6 && i != 15) {
						this.called.add(this.tmpArray2.get(0));
					}
				}
				this.tmpArray2.remove(0);
			}

			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i--;
		}

		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			int chance = ran.nextInt(100);
			
			// Do not add numbers for the middle cell
			if (i != 12) {
				// If not hits
				if (tmpCard.getNumberOnCard(i) == 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));

					// Also randomly add this number to the numbers called
					if (chance < this.HITODDS && this.called.size() <= 25)
						this.called.add(this.tmpArray3.get(0));
					this.tmpArray3.remove(0);
				}
			} else {
				i--;
			}
		}

		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			int chance = ran.nextInt(100);
			
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));

				// Also randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25) {
					if ((i == 8 || i == 17) 
							&& isCornerNotHits(i) && max_inner_corner_hits_allowed > 0) {
						this.called.add(this.tmpArray4.get(0));
						max_inner_corner_hits_allowed--;
					} else if (i != 8 && i != 17) {
						this.called.add(this.tmpArray4.get(0));
					}
				}
				this.tmpArray4.remove(0);
			}

			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i--;
		}

		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			int chance = ran.nextInt(100);
			
			// If not hits
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));

				// Also randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25)
					this.called.add(this.tmpArray5.get(0));
				this.tmpArray5.remove(0);
			}

			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i--;
		}
	}
	
	private void generateCard1RestXWin() {
		// Generate first column
		for (int i = 0; i < 20; i += 5) {
			int chance = ran.nextInt(100);
			
			// If the cell does not have a number yet
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
				
				// Randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25) 
					this.called.add(this.tmpArray.get(0));
				this.tmpArray.remove(0);
			}
			// fix card index to avoid generating number for the centre free cell
			if (i == 10)
				i --;
		}
		
		// Generate 2nd column;
		for (int i = 1; i < 21; i += 5) {
			int chance = ran.nextInt(100);
			
			// If the cell does not have a number yet
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
			
				// Randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25)
					this.called.add(this.tmpArray2.get(0));
				this.tmpArray2.remove(0);
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i --; 
		}
		
		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			int chance = ran.nextInt(100);
			
			// Do not add numbers for the centre cell
			if (i != 12) {
				// If the cell does not have a number yet
				if (tmpCard.getNumberOnCard(i) == 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
				
					// Randomly add this number to the numbers called
					if (chance < this.HITODDS && this.called.size() <= 25)
						this.called.add(this.tmpArray3.get(0));
					this.tmpArray3.remove(0);
				} 
			} else {
				i --;
			}
		}
		
		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			int chance = ran.nextInt(100);
			
			// If the cell does not have a number yet
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
			
				// Randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25)
					this.called.add(this.tmpArray4.get(0));
				this.tmpArray4.remove(0);
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i --;
		}
		
		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			int chance = ran.nextInt(100);
			
			// If the cell does not have a number yet
			if (tmpCard.getNumberOnCard(i) == 0) {
				tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
				
				// Randomly add this number to the numbers called
				if (chance < this.HITODDS && this.called.size() <= 25) 
					this.called.add(this.tmpArray5.get(0));
				this.tmpArray5.remove(0);
			}
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i --;
		}
	}
	
	private boolean generateCard2RestLineWin() {
		int max_corner_hits_allowed = 3; 
		
		// Check the number of hits on corners
		if (tmpCard.getNumberOnCard(0) != 0) 
			max_corner_hits_allowed --;
		if (tmpCard.getNumberOnCard(4) != 0) 
			max_corner_hits_allowed --;
		if (tmpCard.getNumberOnCard(19) != 0) 
			max_corner_hits_allowed --;
		if (tmpCard.getNumberOnCard(23) != 0) 
			max_corner_hits_allowed --;
		
		// Generate 1st column
		for (int i = 0; i < 20; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on the card at this cell
			if (tmpCard.getNumberOnCard(i) == 0) {
				
				// If the cell is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 1, 15);
					
					// If the top left or bottom left corner cell can be a hit
					if ((i == 0 || i == 19) && num > 0 && max_corner_hits_allowed > 0) {
						max_corner_hits_allowed --;
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If the other cells in the first column can be a hit
					} else if ((i != 0 && i != 19) && num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If can't find a called number within range or can no long have a corner hit
					} else if (tmpArray.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
						this.tmpArray.remove(0);
					// If uncalled number array is empty
					} else {
						return false;
					}
				// If it is not going to be a hit
				} else if (tmpArray.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
					this.tmpArray.remove(0);
				// If the card can't be filled
				} else {
					return false;
				}
			} 
			// fix card index to avoid generating number for the middle free cell
			if (i == 10)
				i --;
		}
		
		// Generate 2nd column;
		for (int i = 1; i < 21; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on the card at this cell
			if (tmpCard.getNumberOnCard(i) == 0) {
			
				// If the cell is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 16, 30);
					
					// If a called number within range can be found
					if (num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If can't find a called number within range
					} else if (tmpArray2.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
						this.tmpArray2.remove(0);
					// If uncalled number array is empty
					} else {
						return false;
					}
				// If it is not going to be a hit
				} else if (tmpArray2.size() > 0){
					tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
					this.tmpArray2.remove(0);
				// If the card can't be filled
				} else { 
					return false;
			}
		}
			// fix card index to avoid generating number for the middle free cell
			if (i == 11)
				i --; 
		}
		
		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			int chance = ran.nextInt(100);
			
			// Do not add numbers for the center cell
			if (i != 12) {
				// If there is no number on the card at this cell
				if (tmpCard.getNumberOnCard(i) == 0) {
					// If the cell is going to be a hit
					if (chance < this.HITODDS) {
						int num = findNumInRange(this.tmpCalled, 31, 45);
						
						// If a called number within range can be found
						if (num > 0) {
							tmpCard.setNumberOnCard(i, num);
							this.tmpCalled.remove(Integer.valueOf(num));
						// If can't find a called number within range
						} else if (tmpArray3.size() > 0) {
							tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
							this.tmpArray3.remove(0);
						// If uncalled number array is empty	
						} else {
							return false;
						}
						
					// If it is not going to be a hit
					} else if (tmpArray3.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
						this.tmpArray3.remove(0);
					// If the card can't be filled
					} else {
						return false;
					}
				} 
			} else {
				i --;
			}
		}
		
		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on the card at this cell
			if (tmpCard.getNumberOnCard(i) == 0) {
				
				// If the cell is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 46, 60);
					
					// If a called number within range can be found
					if (num > 0) { 
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If can't find a called number within range
					} else if (tmpArray4.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
						this.tmpArray4.remove(0);
					// If uncalled number array is empty	
					} else {
						return false;
					}
				// If it is not going to be a hit
				} else if (tmpArray4.size() > 0){
					tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
					this.tmpArray4.remove(0);
				// If the card can't be filled
				} else {
					return false;
				}
			} 	
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 8)
				i --;
		}
		
		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on the card at this cell
			if (tmpCard.getNumberOnCard(i) == 0) {
				
				// If the cell is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(this.tmpCalled, 61, 75);
					
					// If the top right or bottom right corner cell can be a hit
					if ((i == 4 || i == 23) && num > 0 && max_corner_hits_allowed > 0) {
						max_corner_hits_allowed --;
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If the other cells in the 5th column can be a hit
					} else if ((i != 4 && i != 23) && num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If can't find a called number within range or can no long have a corner hit
					} else if (tmpArray5.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
						this.tmpArray5.remove(0);
					// If uncalled number array is empty
					} else {
						return false;
					}
				// If it is not going to be a hit
				} else if (tmpArray5.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
					this.tmpArray5.remove(0);
				// If the card can't be filled
				} else {
					return false;
				}
			}
			
			// fix card index to avoid generating number for the middle free cell
			if (i == 9)
				i --;
		}
		
		return true;
	}
	
	private boolean generateCard2RestCornerWin() {
		int max_inner_corner_hits_allowed = 3;
		
		// Generate first column
		for (int i = 0; i < 20; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on this position
			if (tmpCard.getNumberOnCard(i) == 0) {
				// If it is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(tmpCalled, 1, 15);
					// If found a number in range from the called
					if (num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If can't find a called number within range 
					} else if (tmpArray.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
						this.tmpArray.remove(0);
					// If uncalled number array is empty
					} else {
						return false;
					}
				// If it not going to be a hit
				} else if (tmpArray.size() > 0){
					tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
					this.tmpArray.remove(0);
				// If the card can't be filled
				} else {
					return false;
				}
			}
			// fix card index to avoid generating number for the centre free cell
			if (i == 10)
				i--;
		}

		// Generate 2nd column;
		for (int i = 1; i < 21; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on this position
			if (tmpCard.getNumberOnCard(i) == 0) {
				// If it is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(tmpCalled, 16, 30);
					// Make sure no diagonal line wins can be formed
					if ((i == 6 || i == 15) && num > 0
							&& isCornerNotHits(i) && max_inner_corner_hits_allowed > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
						max_inner_corner_hits_allowed--;
					} else if (i != 6 && i != 15 && num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If can't find a called number within range 
					} else if (tmpArray2.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
						this.tmpArray2.remove(0);
					// If uncalled number array is empty	
					} else {
						return false;
					}
				// If it is not going to be a hit
				} else if (tmpArray2.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
					this.tmpArray2.remove(0);
				// If the card can't be filled
				} else {
					return false;
				}
			}

			// fix card index to avoid generating number for the centre free cell
			if (i == 11)
				i--;
		}

		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			int chance = ran.nextInt(100);
			
			// Do not add numbers for the middle cell
			if (i != 12) {
				// If there is no number on this position
				if (tmpCard.getNumberOnCard(i) == 0) {
					// If it is going to be a hit
					if (chance < this.HITODDS && this.called.size() <= 25) {
						int num = findNumInRange(tmpCalled, 31, 45);
						// If found a number in range from the called
						if (num > 0) {
							tmpCard.setNumberOnCard(i, num);
							this.tmpCalled.remove(Integer.valueOf(num));
						// If can't find a called number within range 
						} else if (tmpArray3.size() > 0) {
							tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
							this.tmpArray3.remove(0);
						// If uncalled number array is empty	
						} else {
							return false;
						}
					// If it not going to be a hit
					} else if (tmpArray3.size() > 0){
						tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
						this.tmpArray3.remove(0);
					// If the card can't be filled
					} else {
						return false;
					}
				}
			// fix card index to avoid generating number for the centre free cell
			} else {
				i--;
			}
		}

		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on this position
			if (tmpCard.getNumberOnCard(i) == 0) {
				// If it is going to be a hit
				if (chance < this.HITODDS && this.called.size() <= 25) {
					int num = findNumInRange(tmpCalled, 46, 60);
					// Make sure no diagonal line wins can be formed
					if ((i == 8 || i == 17) && num > 0
							&& isCornerNotHits(i) && max_inner_corner_hits_allowed > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
						max_inner_corner_hits_allowed--;
					} else if (i != 8 && i != 17 && num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If can't find a called number within range 
					} else if (tmpArray4.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
						this.tmpArray4.remove(0);
					// If uncalled number array is empty
					} else {
						return false;
					}
				
				// If it is not going to be a hit
				} else if (tmpArray4.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
					this.tmpArray4.remove(0);
				// If the card can't be filled
				} else {
					return false;
				}
			}
			// fix card index to avoid generating number for the centre free cell
			if (i == 8)
				i--;
		}

		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on this position
			if (tmpCard.getNumberOnCard(i) == 0) {
				// If it is going to be a hit
				if (chance < this.HITODDS && this.called.size() <= 25) {
					int num = findNumInRange(tmpCalled, 61, 75);
					// If found a number in range from the called
					if (num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// If can't find a called number within range 
					} else if (tmpArray5.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
						this.tmpArray5.remove(0);
					// If uncalled number array is empty
					} else {
						return false;
					}
				// If it not going to be a hit
				} else if (tmpArray5.size() > 0){
					tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
					this.tmpArray5.remove(0);
				// If the card can't be filled
				} else {
					return false;
				}
			}
			// fix card index to avoid generating number for the centre free cell
			if (i == 9)
				i--;
		}
		
		return true;
	}

	private boolean generateCard2RestXWin() {
		// Generate first column
		for (int i = 0; i < 20; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on this position
			if (tmpCard.getNumberOnCard(i) == 0) {
				// If it is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(tmpCalled, 1, 15);
					// If found a number in range from the called
					if (num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// Use uncalled numbers if can't find a number in range from the called
					} else if (tmpArray.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
						this.tmpArray.remove(0);
					// If the card can't be filled
					} else {
						return false;
					}
				// If it not going to be a hit
				} else if (tmpArray.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray.get(0));
					this.tmpArray.remove(0);
				// If the card can't be filled
				} else {
					return false;
				}
			}
			
			// fix card index to avoid generating number for the center free cell
			if (i == 10)
				i--;
		}

		// Generate 2nd column;
		for (int i = 1; i < 21; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on this position
			if (tmpCard.getNumberOnCard(i) == 0) {
				// If it is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(tmpCalled, 16, 30);
					// If found a number in range from the called
					if (num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// Use uncalled numbers if can't find a number in range from the called
					} else if (tmpArray2.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
						this.tmpArray2.remove(0);
					// If the card can't be filled
					} else {
						return false;
					}
				// If it is not going to be a hit
				} else if (tmpArray2.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray2.get(0));
					this.tmpArray2.remove(0);
					// If the card can't be filled
				} else {
					return false;
				}
			}

			// fix card index to avoid generating number for the centre free
			// cell
			if (i == 11)
				i--;
		}

		// Generate 3rd column;
		for (int i = 2; i < 22; i += 5) {
			int chance = ran.nextInt(100);
			
			// Do not add numbers for the middle cell
			if (i != 12) {
				// If there is no number on this position
				if (tmpCard.getNumberOnCard(i) == 0) {
					// If it is going to be a hit
					if (chance < this.HITODDS) {
						int num = findNumInRange(tmpCalled, 31, 45);
						// If found a number in range from the called
						if (num > 0) {
							tmpCard.setNumberOnCard(i, num);
							this.tmpCalled.remove(Integer.valueOf(num));
						// Use uncalled numbers if can't find a number in range from the called
						} else if (tmpArray3.size() > 0) {
							tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
							this.tmpArray3.remove(0);
						// If the card can't be filled
						} else {
							return false;
						}
					// If it not going to be a hit
					} else if (tmpArray3.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray3.get(0));
						this.tmpArray3.remove(0);
						// If the card can't be filled
					} else {
						return false;
					}
				}
				// fix card index to avoid generating number for the centre free
				// cell
			} else {
				i--;
			}
		}

		// Generate 4th column;
		for (int i = 3; i < 23; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on this position
			if (tmpCard.getNumberOnCard(i) == 0) {
				// If it is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(tmpCalled, 46, 60);
					// If found a number in range from the called
					if (num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// Use uncalled numbers if can't find a number in range from the called
					} else if (tmpArray4.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
						this.tmpArray4.remove(0);
					// If the card can't be filled
					} else {
						return false;
					}
					// If it is not going to be a hit
				} else if (tmpArray4.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray4.get(0));
					this.tmpArray4.remove(0);
					// If the card can't be filled
				} else {
					return false;
				}
			}
			// fix card index to avoid generating number for the centre free
			// cell
			if (i == 8)
				i--;
		}

		// Generate 5th column;
		for (int i = 4; i < 24; i += 5) {
			int chance = ran.nextInt(100);
			
			// If there is no number on this position
			if (tmpCard.getNumberOnCard(i) == 0) {
				// If it is going to be a hit
				if (chance < this.HITODDS) {
					int num = findNumInRange(tmpCalled, 61, 75);
					// If found a number in range from the called
					if (num > 0) {
						tmpCard.setNumberOnCard(i, num);
						this.tmpCalled.remove(Integer.valueOf(num));
					// Use uncalled numbers if can't find a number in range from the called
					} else if (tmpArray5.size() > 0) {
						tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
						this.tmpArray5.remove(0);
					// If the card can't be filled
					} else {
						return false;
					}
				// If it not going to be a hit
				} else if (tmpArray5.size() > 0) {
					tmpCard.setNumberOnCard(i, this.tmpArray5.get(0));
					this.tmpArray5.remove(0);
					// If the card can't be filled
				} else {
					return false;
				}
			}
			// fix card index to avoid generating number for the centre free cell
			if (i == 9)
				i--;
		}

		return true;
	}

	private void generateRestCalled() {
		// Generate the rest of numbers called
		Collections.shuffle(array1_75);
		List<Integer> tmp = new ArrayList<Integer>(array1_75);
		int size = 25 - called.size();
		
		if (size <= 0)
			System.out.println("Not suppose to be here");
		
		Set<Integer> calledset = new HashSet<Integer>(this.called);
		if (calledset.size() < this.called.size())
			System.out.println("Not suppose to be here");
		
		// Remove the number on card 1 from the 75 numbers
		for (int i = 0; i < this.tmpCard.getNumbersOnCard().size(); i++)
			tmp.remove(Integer.valueOf(tmpCard.getNumberOnCard(i)));

		// Fill the numbers called array
		for (int i = 0; i < size; i++) {
			this.called.add(tmp.get(0));
			removeFromTmpArrays(tmp.get(0));
			tmp.remove(0);
		}
		
		this.ArrayBackup = new ArrayList<Integer>(this.tmpArray);
		this.Array2Backup = new ArrayList<Integer>(this.tmpArray2);
		this.Array3Backup = new ArrayList<Integer>(this.tmpArray3);
		this.Array4Backup = new ArrayList<Integer>(this.tmpArray4);
		this.Array5Backup = new ArrayList<Integer>(this.tmpArray5);
		
	}

	private void removeFromTmpArrays(int num) {
		if (num < 16)
			tmpArray.remove(Integer.valueOf(num));
		else if (num >= 16 && num < 31)
			tmpArray2.remove(Integer.valueOf(num));
		else if (num >= 31 && num < 46)
			tmpArray3.remove(Integer.valueOf(num));
		else if (num >= 46 && num < 61)
			tmpArray4.remove(Integer.valueOf(num));
		else
			tmpArray5.remove(Integer.valueOf(num));
	}

	/**
	 * Perform a binary search on a sorted int list to find a number within given range;
	 * 
	 * @param list	sorted int list
	 * @param low	lower bound for the number
	 * @param high	upper bound for the number 
	 * @return		-1 or the number 
	 */
	private int findNumInRange(List<Integer> list, int low, int high) {
		int max = list.size() - 1;
		int min = 0;
		int num = 0;
		int result = 0;
		
		if (list.size() == 0) {
			return -1;
		}
		
		while (result == 0) {
			int index = (max + min) / 2;

			num = list.get(index);

			// Can't find it
			if (max < min) {
				result = -1;
			// Too small
			} else if (num < low) {
				min = index + 1;
			// Too large
			} else if (num > high) {
				max = index - 1;
			// Found it
			} else {
				result = num;
			}
		}

		return result;
	}

	private List<Integer> findHorizontalNums(int length) {
		List<Integer> nums = new ArrayList<Integer>(length);
		int num = 0;

		// find number under "B"
		num = findNumInRange(tmpCalled, 1, 15);
		if (num > 0) {
			nums.add(num);
			this.tmpCalled.remove(Integer.valueOf(num));
		}

		// find number under "I"
		num = findNumInRange(tmpCalled, 16, 30);
		if (num > 0) {
			nums.add(num);
			this.tmpCalled.remove(Integer.valueOf(num));
		}

		// find number under "N" (if not middle row)
		if (length != 4) {
			num = findNumInRange(tmpCalled, 31, 45);
			if (num > 0) {
				nums.add(num);
				this.tmpCalled.remove(Integer.valueOf(num));
			}
		}

		// find number under "G"
		num = findNumInRange(tmpCalled, 46, 60);
		if (num > 0) {
			nums.add(num);
			this.tmpCalled.remove(Integer.valueOf(num));
		}

		// find number under "O"
		num = findNumInRange(tmpCalled, 61, 75);
		if (num > 0) {
			nums.add(num);
			this.tmpCalled.remove(Integer.valueOf(num));
		}

		return nums;
	}

	private boolean isCornerNotHits(int index) {
		switch (index) {
		case 6:
			return (!this.called.contains(Integer.valueOf(tmpCard
					.getNumberOnCard(0)))) && (ran.nextInt(100) < 50);
		case 15:
			return (!this.called.contains(Integer.valueOf(tmpCard
					.getNumberOnCard(19)))) && (ran.nextInt(100) < 50);
		case 8:
			return (!this.called.contains(Integer.valueOf(tmpCard
					.getNumberOnCard(19))))
					|| (!this.called.contains(Integer.valueOf(tmpCard
							.getNumberOnCard(15))));
		case 17:
			return (!this.called.contains(Integer.valueOf(tmpCard
					.getNumberOnCard(0))))
					|| (!this.called.contains(Integer.valueOf(tmpCard
							.getNumberOnCard(6))));
		default:
			return false;
		}

	}

	private void initializeTmpArrays() {
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
	}
	
	private void resetTmpArrays() {
		this.tmpArray = new ArrayList<Integer>(this.ArrayBackup);
		this.tmpArray2 = new ArrayList<Integer>(this.Array2Backup);
		this.tmpArray3 = new ArrayList<Integer>(this.Array3Backup);
		this.tmpArray4 = new ArrayList<Integer>(this.Array4Backup);
		this.tmpArray5 = new ArrayList<Integer>(this.Array5Backup);
	}
	
	private void resetCards(int cardID) {
		// Reset the two cards
		this.tmpCard = model.new Card();

		if (cardID == 1) {
			tmpCard.setTIID((short) 1);
			this.called = new ArrayList<Integer>(25);
			this.cards = new ArrayList<Card>(2);
		} else {
			tmpCard.setTIID((short) 2);
		}
	}

}

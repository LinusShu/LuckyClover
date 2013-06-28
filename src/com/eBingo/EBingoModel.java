package com.eBingo;

import java.io.File;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.eBingo.database.Database;
import com.eBingo.view.ViewInterface;

public class EBingoModel {
	private static EBingoModel instance = null;
	
	public static final int MAX_REPEATS = 10000;
	public static final int NUM_ON_CARDS = 24;

	public enum Mode {
		LUCKY_CLOVER, OLD_GLORY, REGULAR
	}
	
	public static String LC_RESULTS_TABLE_NAME = "LuckyCLover_Results";
	public static String LC_PAYTABLE_NAME = "LuckyCLover_Paytable";
	public static String LC_BLOCKS_TABLE_NAME = "LuckyCLover_Blocks";
	public static String LC_BASICINFO_TABLE_NAME = "LuckyClover_BasicInfo";
	
	public static String RB_RESULTS_TABLE_NAME = "RegularBingo_Results";
	public static String RB_PAYTABLE_NAME = "RegularBingo_Paytable";
	public static String RB_BLOCKS_TABLE_NAME = "RegularBingo_Blocks";
	
	public static String PERCENT_LOSERS_TABLE_NAME = "PercentLosers";
	
	private File configFile;
	private File blockFile;
	private String tablesuffix = "";
	
	private EBingoLog log;
	private Simulator simulator;
	
	private boolean running = false;
	private boolean cancelled = false;
	private boolean paused = false;
	private boolean error = false;
	public boolean outputToLog = true;
	
	private int totalplays = 0;
	private int currplayed = 0;
	
	private Block currblock = null;
	private int currblockindex = 0;
	
	private Mode mode = Mode.LUCKY_CLOVER;
	private boolean genPlayResults = false;
	
	private BasicInfoEntry currbie = null;
	private PercentLosersEntry currple = null;
	//TODO add current "Entries" as needed
	
	private ArrayList<ViewInterface> views = new ArrayList<ViewInterface>();
	private ArrayList<String> errorLog = new ArrayList<String>();
	private ArrayList<String> warningLog = new ArrayList<String>();
	private ArrayList<String> errorLog2 = new ArrayList<String>();
	private ArrayList<String> warningLog2 = new ArrayList<String>();
	
	private ArrayList<PaytableEntry> paytable = new ArrayList<PaytableEntry>();
	private ArrayList<Block> blocks = new ArrayList<Block>();
	private List<Integer> hittable = new ArrayList<Integer>();
	
	private EBingoModel() {
		this.log = new EBingoLog("log");
		this.simulator = new Simulator(this);
	}
	
	public static EBingoModel getInstance() {
		if (instance == null)
			instance = new EBingoModel();
		return instance;
	}
	
	public void AddView(ViewInterface view) {
		if (view != null) {
			this.views.add(view);
			view.updateView();
		}
	}
	
	public void UpdateViews() {
		for (ViewInterface v : this.views) {
			v.updateView();
		}
	}
	
	public void setTotalPlays(int value) {
		this.totalplays = value;
	}
	
	public void setCurrBlock(Block currblock) {
		this.currblock = currblock;
	}
	
	public void setBlockIndex(int value) {
		this.currblockindex = value;
	}
	
	public void setConfigFile(File f) {
		this.configFile = f;
		this.UpdateViews();
	}
	
	public void setBlockFile(File f) {
		this.blockFile = f;
		this.UpdateViews();
	}
	
	public void setDBName(String name) {
		Database.setDBName(name);
		this.UpdateViews();
	}
	
	public void setDBName() {
		Database.setDBName(Database.DEFAULT_DB_NAME);
		this.UpdateViews();
	}
	
	public void setTableSuffix() {
		Date date = new Date();
		SimpleDateFormat sft = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
		String formattedDate = sft.format(date);
		this.tablesuffix = formattedDate;
	}

	public void setMode(Mode value) {
		this.mode = value;
		this.UpdateViews();
	}
	
	public Mode getMode() {
		return this.mode;
	}
	
	public void setGenPlayResults(boolean value) {
		this.genPlayResults = value;
		this.UpdateViews();
	}
	
	public void setError() {
		this.error = true;
		this.UpdateViews();
	}
	
	public void incrementHits(int index) {
		this.hittable.set(index, hittable.get(index) + 1);
	}
	
	public String getDBName() {
		return Database.getDBname();
	}
	
	public String getTableSuffix() {
		return this.tablesuffix;
	}
	
	public String getLCResultsDBTableName() {
		return this.buildDBTableName(LC_RESULTS_TABLE_NAME);
	}
	
	public String getLCPaytableDBName() {
		return this.buildDBTableName(LC_PAYTABLE_NAME);
	}
	
	public String getLCBlocksDBname() {
		return this.buildDBTableName(LC_BLOCKS_TABLE_NAME);
	}
	
	public String getLCBasicInfoDBname() {
		return this.buildDBTableName(LC_BASICINFO_TABLE_NAME);
	}
	
	public String getRBResultsDBTableName() {
		return this.buildDBTableName(RB_RESULTS_TABLE_NAME);
	}
	
	public String getRBPaytableDBName() {
		return this.buildDBTableName(RB_PAYTABLE_NAME);
	}
	
	public String getRBBlockDBName() {
		return this.buildDBTableName(RB_BLOCKS_TABLE_NAME);
	}
	
	public String getPercentLosersDBTableName() {
		return this.buildDBTableName(PERCENT_LOSERS_TABLE_NAME);
	}
	
	//TODO add getDBname methods as needed
	
	public File getConfigFile() {
		return this.configFile;
	}
	
	public File getBlockFile() {
		return this.blockFile;
	}
	
	public String getLogFilePath() {
		return this.log.getFilePath();
	}
	
	public int getTotalPlays() {
		return this.totalplays;
	}
	
	public int getCurrPlayed() {
		return this.currplayed;
	}
	
	public Block getCurrBlock() {
		return currblock;
	}

	public int getBlockIndex() {
		return this.currblockindex;
	}
	
	public void start() {
		this.running = true;
		this.paused = false;
		this.UpdateViews();
	}
	
	public void stop() {
		this.running = false;
		this.paused = false;
		this.UpdateViews();
	}
	
	public void pause() {
		this.paused = true;
		this.UpdateViews();
	}
	
	public void cancel() {
		this.cancelled = true;
		this.running = false;
		this.UpdateViews();
	}
	
	public void resume() {
		this.paused = false;
		this.UpdateViews();
	}
	
	public boolean isSetupValid() {
		if (this.configFile != null && this.blockFile != null
				&& this.getDBName().length() > 0)
			return true;
		
		return false;
	}
	
	public boolean isRunning() {
		return this.running;
	}
	
	public boolean isPaused() {
		return this.paused;
	}
	
	public boolean isCancelled() {
		return this.cancelled;
	}
	
	public boolean isError() {
		return this.error;
	}
	
	private String buildDBTableName(String tablename) {
		return tablename + "_" + getTableSuffix();
	}
	
	protected void generateDBTableName() {
		this.setTableSuffix();
		this.log.writeLine("Set Paytable DB Table Name: " + this.getLCPaytableDBName());
		this.log.writeLine("Set Blocks DB Table Name: " + this.getLCBlocksDBname());
		this.log.writeLine("Set Results DB Table Name: " + this.getLCResultsDBTableName());
	}
	
	public void addErrorToLog(String err) {
		this.errorLog.add(err);
	}
	
	public void addWarningToLog(String warn) {
		this.warningLog.add(warn);
	}
	
	public void addErrorToLog2(String err) {
		this.errorLog2.add(err);
	}
	
	public void addWarningToLog2(String warn) {
		this.warningLog2.add(warn);
	}
	
	/**
	 * This method clears EBingoModel variables for each launch
	 */
	private void clearAll() {
		//TODO: clear stuff here
		this.simulator.reset();
		this.totalplays = 0;
		this.currplayed = 0;
		this.cancelled = false;
		
		this.running = false;
		this.paused = false;
		this.error = false;
		
		this.currblock = null;
		this.currblockindex = 0;
		this.currplayed= 0;
		
		this.currbie = null;
		this.currple = null;
		
		this.paytable.clear();
		this.blocks.clear();
		this.hittable.clear();
		
		this.errorLog.clear();
		this.warningLog.clear();
		this.errorLog2.clear();
		this.warningLog2.clear();
	}
	
	private boolean loadConfigFile(File file) {
		clearAll();
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docbuilder = dbf.newDocumentBuilder();
			Document doc = docbuilder.parse(file);
			doc.getDocumentElement().normalize();
			
			this.readPaytable(doc);
		} catch (Exception e) {
			this.addErrorToLog("An exception occurred while reading config file: "
					+ e.getMessage()
					+ ", Stack Trace: "
					+ e.getStackTrace().toString());
		}
		
		this.log.writeLine("Reading Configurate File...Finished. Errors: "
				+ this.errorLog.size()
				+ ", Warnings: "
				+ this.warningLog.size());
		
		if (this.errorLog.size() > 0) {
			for (String s : this.errorLog)
				this.log.writeLine("Error: " + s);
		}
		
		if (this.warningLog.size() > 0) {
			for (String s : this.warningLog)
				this.log.writeLine("Warning: " + s);
		}
		
		return (this.errorLog.size() == 0);
	}
	
	private boolean loadBlocks(File file) {
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docbuilder = dbf.newDocumentBuilder();
			Document doc = docbuilder.parse(file);
			doc.getDocumentElement().normalize();
			this.readBlocks(doc);
			
		} catch (Exception e) {
			this.addErrorToLog2("An exception occurred while reading blocks file: "
					+ e.getMessage()
					+ ", Stack Trace: "
					+ e.getStackTrace().toString());
		}
		
		this.log.writeLine("Reading Blocks File...Finished. Errors: "
				+ this.errorLog2.size()
				+ ", Warnings: "
				+ this.warningLog2.size());
		
		if (this.errorLog2.size() > 0) {
			for (String s : this.errorLog2)
				this.log.writeLine("ERROR: " + s);
		}
		
		if (this.warningLog2.size() > 0) {
			for (String s : this.warningLog2)
				this.log.writeLine("WARNING: " + s);
		}
		
		return (this.errorLog2.size() == 0);
	}
	
	private void readPaytable(Document doc) {
		NodeList nl = doc.getElementsByTagName("PaytableEntry");
		
		log.writeLine("Reading Paytable Information:");
		
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) node;
				
				String winCode = e.getAttribute("winCode");
				String name = e.getAttribute("name");
				int winPattern, payout;
				
				try {
					winPattern = Integer.parseInt(e.getAttribute("winPattern"));
					payout = Integer.parseInt(e.getAttribute("payout"));
				} catch (NumberFormatException nfe) {
					this.addErrorToLog("PaytableEntry["
							+ Integer.toString(i)
							+ "]: Invalid value for winPattern/payout.");
					winPattern = payout = -1;
				}
				
				if (winPattern > 0 && payout > 0) {
					PaytableEntry pe = new PaytableEntry();
					pe.setWinCode(winCode);
					pe.setName(name);
					pe.setWinPattern(winPattern);
					pe.setPayout(payout);
					pe.populateBitPattern();
					this.paytable.add(pe);
					this.hittable.add(0);
				}
			}
		}
	}
	
	private void readBlocks(Document doc) {
		NodeList list = doc.getElementsByTagName("block");
		
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) node;
				
				// If in Lucky Clover mode
				if (this.mode == Mode.LUCKY_CLOVER) {
					int numplays = -1;
					int numcards = -1;
					float wager = -1;
					int repeat = -1;
				
					try {
						numplays = Integer.parseInt(e.getAttribute("numplays"));
						numcards = Integer.parseInt(e.getAttribute("numcards"));
						wager = Float.parseFloat(e.getAttribute("wager"));
						repeat = Integer.parseInt(e.getAttribute("repeat"));
					} catch (NumberFormatException nfe) {
						this.addErrorToLog2("Block[" + Integer.toString(i)
								+ "]: Invalid vaule(s) for this block.");
					}
				
					if (numplays > 0 && numcards > 0 && wager > 0 && repeat > 0 && repeat <= MAX_REPEATS) {
						Block b = new Block();
						b.setNumPlays(numplays);
						b.setNumCards(numcards);
						b.setWager(wager);
						b.setRepeat(repeat);
						b.setBlockNum(i + 1);
						this.blocks.add(b);
					} else {
						this.addErrorToLog2("Block[" + Integer.toString(i)
								+ "]: All attributes must be greater than 0.");
					}
					
				// If in regular mode		
				} else if (this.mode == Mode.REGULAR) {
					int numplays = -1;
					int numcards = -1;
					int numplayers = -1;
					float wager = -1;
					int repeat = -1;
					RegularBlock rb = new RegularBlock();
				
					try {
						numplays = Integer.parseInt(e.getAttribute("numplays"));
						numcards = Integer.parseInt(e.getAttribute("numcards"));
						numplayers = Integer.parseInt(e.getAttribute("numplayers"));
						wager = Float.parseFloat(e.getAttribute("wager"));
						repeat = Integer.parseInt(e.getAttribute("repeat"));
					} catch (NumberFormatException nfe) {
						this.addErrorToLog2("Block[" + Integer.toString(i)
								+ "]: Invalid vaule(s) for this block.");
					}
				
					if (numplays > 0 && numcards > 0 && numplayers > 0 
							&& wager > 0 && repeat > 0 && repeat <= MAX_REPEATS) {
						
						
						rb.setNumPlays(numplays);
						rb.setNumCards(numcards);
						rb.setNumplayers(numplayers);
						rb.setWager(wager);
						rb.setRepeat(repeat);
						rb.setBlockNum(i + 1);
						rb.calculatePayBack();
						this.blocks.add(rb);
					} else {
						this.addErrorToLog2("Block[" + Integer.toString(i)
								+ "]: All attributes must be greater than 0.");
					}
				}
			}
		}
			
	}
	
	/**
	 * Method used to calculate the total number of times simulation run
	 *
	 * @return    The number of times we run the simulation
	 */
	private int calculateTotalPlays() {
		int tp = 0;
		
		for (Block b : this.blocks)
			tp += b.getNumPlays() * b.getRepeat();
		
		return tp;
	}
	
	public void launch() {
		this.log.writeLine("LAUNCH NEW SESSION...");
		
		this.generateDBTableName();
		
		// Load config file
		boolean isLoadSuccessful = loadConfigFile(this.configFile);
		
		if (isLoadSuccessful) {
			// Load blocks file
			isLoadSuccessful = loadBlocks(this.blockFile);
			
			if (isLoadSuccessful) {
				start();
				produce();
			} else 
				this.setError();
		} else 
			this.setError();
		
	}
	
	private void produce() {
		Thread t = new Thread() {
			public void run() {
				EBingoModel.this.log.writeLine("START PRODUCTION"); 
				
				int totalplay = EBingoModel.this.calculateTotalPlays();
				EBingoModel.this.setTotalPlays(totalplay);
				EBingoModel.this.log.writeLine("Total Plays: " + Integer.toString(totalplay));
				
				if (totalplay > 0) {
					initializeDB();
					
					// Start to generate the results
					try {
						EBingoModel.this.log.writeLine("GENERATING RESULTS...");
						
						switch (EBingoModel.this.mode) {
							case LUCKY_CLOVER:
								doLuckyCloverMode();
								break;
								
							case OLD_GLORY:
								doOldGloryMode();
								break;
								
							case REGULAR:
								doRegularMode();
								break;
						}	
						
					} catch (Exception e) {
						EBingoModel.this.stop();
						EBingoModel.this.log.writeLine("ERROR: Results generation encountered error. "
								+ "Message: " + e.getMessage());
						e.printStackTrace();
						
					} finally {
						// Close database connection when done;
						finalizeDB();
						EBingoModel.this.stop();
					}
				} else { // If totalplay == 0
					EBingoModel.this.stop();
				}
				
				EBingoModel.this.log.writeLine("END OF PRODUCTION");
			}
		};
		
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		
	}
	
	private void doLuckyCloverMode() throws Exception {
		EBingoModel.this.currblock = EBingoModel.this.blocks
				.get(EBingoModel.this.getBlockIndex());
		
		EBingoModel.this.currbie = new BasicInfoEntry();
		EBingoModel.this.currple = new PercentLosersEntry();
		//TODO new bunch of "Entries" here
		
		while (EBingoModel.this.currblock != null
				&& !EBingoModel.this.cancelled) {
			
			if (!EBingoModel.this.paused) {
				
				Result r = simulator.generateResults();
			
				if (r != null) {
					r.setRecordNumber(EBingoModel.this.currplayed + 1);
					r.setBlockNumber(EBingoModel.this.currblock.getBlockNum());
					r.setWager(EBingoModel.this.currblock.getWager());	
					
					EBingoModel.this.currbie.updateBIE(r);
					
					if (EBingoModel.this.genPlayResults) 
						Database.insertIntoTable(EBingoModel.this.getLCResultsDBTableName(), r);
				
					EBingoModel.this.incrementCurrPlay();
					
					EBingoModel.this.currple.updatePLE(r);
					//TODO update bunch of "Entries" here
					
					if (currblock != null) {
						if (currblock.getRepeatComplete()) {
							currblock.repeatcomplete = false;
						}
					
						if (currblock.getBlockComplete()) {
							currblock.blockcomplete = false;
						}
					}
				} // If result generated is valid 
			} // If paused
		} // If every block is done
	}
	
	private void doOldGloryMode() throws Exception {
		// TODO implement this if Old Glory is that different than Lucky Clover,
		// otherwise merge this method to doLuckyClover()
	}
	
	private void doRegularMode() throws Exception {
		EBingoModel.this.currblock = EBingoModel.this.blocks
				.get(EBingoModel.this.getBlockIndex());
		
		EBingoModel.this.currbie = new BasicInfoEntry();
		//TODO new bunch of "Entries" here
		
		while (EBingoModel.this.currblock != null
				&& !EBingoModel.this.cancelled) {
			
			if (!EBingoModel.this.paused) {
				
				RegularResult rr = simulator.generateRegularResults();
			
				if (rr != null) {
					rr.setRecordNum(EBingoModel.this.currplayed + 1);
					rr.setBlockNum(EBingoModel.this.currblock.getBlockNum());
					rr.setWager(EBingoModel.this.currblock.getWager());	
					
					
					//EBingoModel.this.currbie.updateBIE(rr);
					//TODO update bunch of "Entries" here
					
					if (EBingoModel.this.genPlayResults) 
						Database.insertIntoTable(EBingoModel.this.getRBResultsDBTableName(), rr);
				
					EBingoModel.this.incrementCurrPlay();
					
				
				} // If result generated is valid 
			} // If paused
		} // If every block is done
	}
	
	/**
	 * Creates Database connection and create blocks tables in the DB
	 */
	private void initializeDB() {
		try {
			Database.createConnection();
			Database.setMode(this.mode);
			
			if (EBingoModel.this.blocks != null
					&& EBingoModel.this.blocks.size() > 0) {
				for (int i = 0; i < EBingoModel.this.blocks.size(); i++) {
					Block b = EBingoModel.this.blocks.get(i);
					
					if (this.mode == Mode.LUCKY_CLOVER)
						Database.insertIntoTable(getLCBlocksDBname(), b);
					else 
						Database.insertIntoTable(getRBBlockDBName(), b);
				}
				Database.flushBatch();
			}
			
		} catch (Exception e) {
			EBingoModel.this.setError();
			EBingoModel.this.log.writeLine("ERROR: "
					+ "Failed to connect to DB while creating blocks tables. "
					+ "Message: " + e.getMessage());
		}

	}
	
	private void finalizeDB() {
		// Create paytable database table
		try {
			Database.flushBatch();
			
			if (EBingoModel.this.paytable != null
					&& EBingoModel.this.paytable.size() > 0) {
				for (int i = 0; i < EBingoModel.this.paytable.size(); i++) {
					PaytableEntry pe = EBingoModel.this.paytable.get(i);
					pe.setEntryID(i);
					
					int hits = EBingoModel.this.hittable.get(i);
					
					if (this.mode == Mode.LUCKY_CLOVER)
						Database.insertIntoTable(getLCPaytableDBName(), pe, hits);
					else 
						Database.insertIntoTable(getRBPaytableDBName(), pe, hits);
				}
				Database.flushBatch();
			}
		} catch (Exception e) {
			EBingoModel.this.setError();
			EBingoModel.this.log.writeLine("ERROR: "
					+ "Failed to connect to DB while creating paytable tables. "
					+ "Message: " + e.getMessage());
		}
		
		// Close DB Connection
		try {
			Database.shutdownConnection();
		} catch (SQLException e) {
			EBingoModel.this.log.writeLine("ERROR: Closing database encountered error. "
					+ "Message: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void incrementCurrPlay() {
		this.currplayed++;
		//TODO repeat/block control flow here
		// If the current block runs out of plays
		if (this.currblock.incrementCurrPlay()) {
			// If the current block needs to be repeated
			if (this.currblock.currrepeat < this.currblock.repeat) {
				currblock.incrementCurrRepeat();
				currblock.setRepeatComplete(true);
				currblock.reset();
			// If the block is done
			} else {
				currblockindex++;
				currblock.setBlockComplete(true);
				
				currbie.flushBIE();
				currple.flushPLE();
				// If there are more blocks
				if (currblockindex < blocks.size()) {
					currblock = blocks.get(currblockindex);
				} else {
					this.currblock = null;
				}
			}
		}
		
		UpdateViews();
	}
	
	
	/** Classes **/

	//Simulator class
	public class Simulator {
		private EBingoModel model = null;
		private Result r;
		private RegularResult rr;
		private int[] balls;
		private long cardID = 0;
		
		private List<Integer> array1_15 = new ArrayList<Integer>(15);
		private List<Integer> array16_30 = new ArrayList<Integer>(15);
		private List<Integer> array31_45 = new ArrayList<Integer>(15);
		private List<Integer> array46_60 = new ArrayList<Integer>(15);
		private List<Integer> array61_75 = new ArrayList<Integer>(15);
		private List<Integer> array1_75 = new ArrayList<Integer>(75);
		
		public Simulator(EBingoModel model) {
			this.model = model;
			
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
		
		public Result generateResults() {
			balls = new int[NUM_ON_CARDS];
			
			if (model.currblock != null) {
				r = new Result(model.currblock.getNumCards());
				r.setBlockNumber(model.currblock.getBlockNum());
				r.setRecordNumber(model.currplayed);
			}
			
			try {
				generateBalls();
				
				for (int i = 0; i < r.getNumCards(); i++) {
					Card c = generateCard();
					r.addCard(c);
				}
				
				// Cheat balls and cards here
				/*
				 * balls[0] = 23;
				 * ...
				 * 
				 * 
				 */
			} catch (Exception e) {
				log.writeLine("Genration of Balls and Cards encountered problem: ");
				log.writeLine(e.getMessage());
				model.stop();
			}
			
			// Loop through all the cards 
			for (int i = 0; i < r.numcards; i++) {
				int outcome = generateOutCome(r.getCard(i));
				
				calculatePayout(outcome, i);
				
				// If the card is a winning card
				if (r.getCard(i).isWin()) {
					r.incrementNumWinningCards();
					// No flashing red squares if there is a winning card
					r.setFlashingRedSquare(false);
					r.addCreditsWon(r.getCard(i).getCreditsWon()); 
				
				// If the card was not a winning card
				} else {
					// Update red square information
					r.addRedSquares(r.getCard(i).getNumRedSquares());
				}
			}
			return r;
		}
		
		/**
		 * The method called only in Regular Bingo mode to generate a Result
		 */
		private RegularResult generateRegularResults() {
			int currBallDrawn = 0;
			balls = new int[75];
			RegularBlock currblock;
			
			if (model.currblock != null) {
				currblock = (RegularBlock)model.currblock;
				rr = new RegularResult(currblock.getNumCards(), currblock.getNumplayers());
				rr.setBlockNum(model.currblock.getBlockNum());
				rr.setRecordNum(model.currplayed);
			}
			
			// Generate all the cards for all the players playing the game
			for (int i = 0; i < rr.getNumPlayers(); i++) {
				for (int j = 0; j < rr.getNumCards(); j++) {
					Card c = generateCard();
					rr.getPlayer(i).getCards().add(c);
				}
			}
			
			
			while (currBallDrawn < 75 && rr.playerswon.size() == 0) {
				// Generate 1 ball at a time
				generateRegularBalls(currBallDrawn);
				currBallDrawn++;
				
				// Only calculate payout after 4 balls are drawn
				if (balls[3] != 0) {
					// Loop through all the players
					for (int i = 0; i < rr.getNumPlayers(); i++) {
						Player p = rr.getPlayer(i);
						
						// Loop through all the cards 
						for (int j = 0; j < rr.getNumCards(); j++) {
							int outcome = generateOutCome(rr.getPlayer(i).getCard(j));
							
							calculateRegularPayout(outcome, i, j);
							
							// If the card is a winning card
							if (rr.getPlayer(i).getCard(j).isWin()) {
								if (!rr.playerswon.contains(i))
									rr.playerswon.add(i);
								
								p.addDollarWon(p.getCard(j).getDollarsWon());
								p.addCardsWon(j);
								p.addWinName(p.getCard(j).getWinName());
							} 
						}
					}
				}
				
			}
			
			rr.setBalls(balls);
			resetRegular();
			return rr;
		}

		private void generateRegularBalls(int index) {
			if (balls[0] == 0) {
				Collections.shuffle(array1_75);
				balls[index] = array1_75.get(index);
			} else if (index < 75) {
				balls[index] = array1_75.get(index);
			}
		}
		
		private void generateBalls() {
			// Generate the ball numbers under "B"
			Collections.shuffle(array1_15);
			for (int i = 0; i < 5; i++)
				balls[i] = array1_15.get(i);
			
			// Generate the ball numbers under "I" 
			Collections.shuffle(array16_30);
			for (int i = 5; i < 10; i++)
				balls[i] = array16_30.get(i - 5);
			
			// Generate the ball numbers under "N" 
			Collections.shuffle(array31_45);
			for (int i = 10; i < 14; i++) 
				balls[i] = array31_45.get(i - 10);
			
			// Generate the ball numbers under "G" 
			Collections.shuffle(array46_60);
			for (int i = 14; i < 19; i++) 
				balls[i] = array46_60.get(i - 14);
			
			// Generate the ball numbers under "O"
			Collections.shuffle(array61_75);
			for (int i = 19; i < 24; i++) 
				balls[i] = array61_75.get(i - 19);
			
			// Shuffle the balls array
			shuffleArray(balls);
			
			r.setBalls(balls);
		}
		
		private Card generateCard() {
			Card card = new Card();
			
			// Generate the card numbers under "B" 
			Collections.shuffle(array1_15);
			for (int j = 0; j < 5; j++)
				card.setNumberOnCard(j, array1_15.get(j));
			
			// Generate the card numbers under "I"
			Collections.shuffle(array16_30);
			for (int j = 5; j < 10; j++)
				card.setNumberOnCard(j, array16_30.get(j - 5)); 
			
			// Generate the card numbers under "N"
			Collections.shuffle(array31_45);
			for (int j = 10; j < 14; j++)
				card.setNumberOnCard(j, array31_45.get(j - 10));
			
			// Generate the card numbers under "G"
			Collections.shuffle(array46_60);
			for (int j = 14; j < 19; j++)
				card.setNumberOnCard(j, array46_60.get(j - 14));
			
			// Generate the card numbers under "O"
			Collections.shuffle(array61_75);
			for (int j = 19; j < 24; j++)
				card.setNumberOnCard(j, array61_75.get(j - 19));
			
			// Rotate the numbers on card
			card.rotateNumberOnCard();
			
			// Assign a card ID
			card.setID(cardID++);
				
			return card;
		}
		
		/**
		 *  From the covered spots on the card we generated an integer that represents the game outcome.
		 *  The use of an integer to represent the game outcome comes from: http://www.durangobill.com/BingoHowTo.html
		 *  
		 * @param card	The card needs to be examined
		 * @return 		The integer representing the outcome of the card
		 */
		private int generateOutCome(Card card) {
			int outcome = 0;
			
			// Loop through balls
			for (int i = 0; i < balls.length && balls[i] != 0; i++) {
				// Loop through numbers on cards
				for (int j = 0; j < NUM_ON_CARDS; j++) {
					if (balls[i] == card.getNumberOnCard(j)){
						double tmp = Math.pow(2,(23-j));
						outcome += (int) tmp;
						break;
					}
				}
			}
			return outcome;
		}
		
		/**
		 * This method calculate the payout of one card, gets called multiple times if 
		 * multiple cards are played in one play.
		 * 
		 * @param outcome		The game outcome integer generated by calling generateOutCome();
		 * @param cardindex		The current card index. 			
		 */
		private void calculatePayout(int outcome, int cardindex) {
			int bestpayout = 0;
			int tmp = 0;
			int besthitindex = -1;
			
			// Loop through all the winning patterns to see if we have one or more matches on the card.
			for (int i = 0; i < EBingoModel.this.paytable.size() ; i++ ) {
				int winpattern = EBingoModel.this.paytable.get(i).getWinPattern();
				tmp = outcome & winpattern;
				
				int num1sintmp = bit_count(tmp);
				int num1sinwin = bit_count(winpattern);
				
				// If the integer representing the outcome matches the win pattern
				if (tmp == winpattern){
					// Set the card to be a winning card
					r.getCard(cardindex).setWin(true);
					
					// Update the payout and winname of the card 
					if (EBingoModel.this.paytable.get(i).getPayout() > bestpayout) {
						besthitindex = i;
						bestpayout = EBingoModel.this.paytable.get(i).getPayout();
						r.getCard(cardindex).setCreditsWon(bestpayout);
						r.getCard(cardindex).setWinName(EBingoModel.this.paytable.get(i).getName());
					}
				
				// Find the red square locations if the outcome has a red square (skip if in Regular Bingo mode)		
				} else if (model.mode != Mode.REGULAR && num1sintmp == num1sinwin - 1) {
					findRedSquares(cardindex, tmp, winpattern);
				}
			}
			
			if (besthitindex >= 0)
				// Increment hit count of the winning pattern by 1
				EBingoModel.this.incrementHits(besthitindex);
	      	
		}
		
		/**
		 * This method calculate the payout of regular bingo 
		 * 
		 * @param outcome		The game outcome integer generated by calling generateOutCome();
		 * @param playerindex   The current player index;
		 * @param cardindex		The current card index. 			
		 */
		private void calculateRegularPayout(int outcome, int playerindex, int cardindex) {
			int bestpayout = 0;
			int tmp = 0;
			int besthitindex= -1;
			
			// Loop through all the winning patterns to see if we have one or more matches on the card.
			for (int i = 0; i < EBingoModel.this.paytable.size() ; i++ ) {
				PaytableEntry pe = EBingoModel.this.paytable.get(i);
				int winpattern = pe.getWinPattern();
				tmp = outcome & winpattern;
				
				// If the integer representing the outcome matches the win pattern
				if (tmp == winpattern){
					// Set the card to be a winning card
					rr.getPlayer(playerindex).getCard(cardindex).setWin(true);
					
					// Update the payout and winname of the card 
					if (pe.getPayout() > bestpayout) {
						besthitindex = i;
						
						//TODO figure out how to calculate payout
						bestpayout = pe.getPayout();
						rr.getPlayer(playerindex).getCard(cardindex).setCreditsWon(bestpayout);
						rr.getPlayer(playerindex).getCard(cardindex).setWinName(pe.getName());
					}	
				}
			}
			
			if (besthitindex >= 0)
				// Increment hit count of the winning pattern by 1
				EBingoModel.this.incrementHits(besthitindex);
		}
		
		/**
		 * Method used to find the location of the red square for each winning pattern
		 * 
		 * @param cardindex      The index of the the current card being examined
		 * @param outcome		 The outcome of the card
		 * @param winpattern     The winpattern of the paytable entry
		 */
		private void findRedSquares(int cardindex, int outcome, int winpattern) {
			int redsquareIndex = 0;
			
			outcome ^= winpattern;
			
			redsquareIndex = NUM_ON_CARDS - Integer.toBinaryString(outcome).length();
		
			r.getCard(cardindex).addRedSquareLocation((byte)redsquareIndex);
		}
		
		/** Utility Methods **/
		/**
		 * Method used to count the number of "1"s in a binary number.
		 * Algorithm from "compprog.wordpress.com/2007/11/06/binary-numbers-counting-bits/"
		 * 
		 * @param n    A binary number
		 * @return	   The number of "1"s in the binary number
		 */
		private int bit_count(int n){
			int count = 0;
			
			while (n != 0){
				++count;
				n &= n - 1;
			}
			return count;
		}
		
		/**
		 * This method implements Fisher�Yates shuffle, and is used to shuffle an array
		 * @param array   A integer array to be shuffled
		 */
		private void shuffleArray(int[] array) {
		    Random ran = new Random();
		    
		    for (int i = array.length - 1; i >= 0; i--) {
		      int random_index = ran.nextInt(i + 1);
		      
		      // Swap an element at a random index with the one at the current index
		      int a = array[random_index];
		      array[random_index] = array[i];
		      array[i] = a;
		    }
		}
		
		private void resetRegular() {
			balls = new int[75];
		}
		
		public void reset() {
			if (model.currblock != null)
				r = new Result(model.currblock.getNumCards());
			
			this.cardID = 0;
		}
		
	}

	// PaytableEntry class
	public class PaytableEntry {
		private String winCode;
		private String name;
		private int payout;
		private int winPattern;
		private String binaryWinPattern = "";
		private int entryID;
		private int bitPattern[] = new int[NUM_ON_CARDS];
		
		public void setEntryID(int value) {
			this.entryID = value;
		}
		
		public void setWinCode(String value) {
			this.winCode = value;
		}
		
		public void setName(String value) {
			this.name = value;
		}
		
		public void setPayout(int value) {
			this.payout = value;
		}
		
		public void setWinPattern(int value) {
			this.winPattern = value;
		}
		
		/**
		 * Method used to create the bit pattern array corresponding to the win pattern
		 * and write the array to the log.
		 */
		public void populateBitPattern() {
			int winpattern_shifted;
			int winpattern_bits[] = new int[NUM_ON_CARDS];
			int bitsIndex = 0;
			
			winpattern_shifted = this.winPattern;
			
			while (winpattern_shifted != 0) {
				winpattern_bits[bitsIndex] = (winpattern_shifted & 1);
				winpattern_shifted >>= 1;
				bitsIndex++;
			} 
			
			this.bitPattern = winpattern_bits;
			
			// Output to the log
			if (EBingoModel.this.outputToLog) {
				log.writeLine("Bit Pattern for " + this.name.toUpperCase() + " (" + Integer.toBinaryString(this.winPattern) + ") " + "Win Amount: " + this.payout);
				log.writeLine( winpattern_bits[0] + " " + winpattern_bits[1] + " " + winpattern_bits[2]+ " " + winpattern_bits[3]+ " " + winpattern_bits[4]);
				log.writeLine( winpattern_bits[5] +" " + winpattern_bits[6] +  " " + winpattern_bits[7]+  " " + winpattern_bits[8]+  " " + winpattern_bits[9]);
				log.writeLine( winpattern_bits[10] +  " " + winpattern_bits[11] + " X " + winpattern_bits[12]+ " " +  winpattern_bits[13]);
				log.writeLine( winpattern_bits[14] +  " " + winpattern_bits[15] +  " " + winpattern_bits[16]+  " " + winpattern_bits[17]+  " " + winpattern_bits[18]);
				log.writeLine( winpattern_bits[19] +  " " + winpattern_bits[20] +  " " + winpattern_bits[21]+  " " + winpattern_bits[22]+  " " + winpattern_bits[23]);
			}
		}
		
		public int getEntryID() {
			return this.entryID;
		}
		
		public String getWinCode() {
			return this.winCode;
		}
		
		public String getName() {
			return this.name;
		}
		
		public int getPayout() {
			return this.payout;
		}
		
		public int getWinPattern() {
			return this.winPattern;
		}
		
		public int[] getBitPattern() {
			return this.bitPattern;
		}

		public String getBinaryWinPattern() {
			String temp = Integer.toBinaryString(winPattern);
			if (NUM_ON_CARDS - temp.length() > 0) {
				for (int i = 0; i < (NUM_ON_CARDS - temp.length()); i++) {
					this.binaryWinPattern += "0";
				}
				
				this.binaryWinPattern += temp;
			} else {
				this.binaryWinPattern = temp;
			}
			
			return this.binaryWinPattern;
		}
	}
	
	// Block class
	public class Block {
		private int numplays = 0;
		private int numcards = 1;
		private int currplay = 0;
		private float wager = 0;
		private int repeat = 0;
		private long blocknum = 0;
		
		private int currrepeat = 1;
		private boolean blockcomplete = false;
		private boolean repeatcomplete = false;
		
		public void setNumPlays(int value) {
			this.numplays = value;
		}
		
		public void setNumCards(int value) {
			this.numcards = value;
		}
		
		public void setWager(float value) {
			this.wager = value;
		}
		
		public void setRepeat(int value) {
			this.repeat = value;
		}
		
		public void setBlockNum(long value) {
			this.blocknum = value;
		}
		
		public void incrementCurrRepeat() {
			this.currrepeat++;
		}
		
		public void setBlockComplete(boolean value) {
			this.blockcomplete = value;
		}
		
		public void setRepeatComplete(boolean value) {
			this.repeatcomplete = value;
		}
		
		public int getNumPlays() {
			return this.numplays;
		}
		
		public int getNumCards() {
			return this.numcards;
		}
		
		public float getWager() {
			return this.wager;
		}
		
		public int getRepeat() {
			return this.repeat;
		}
		
		public long getBlockNum() {
			return this.blocknum;
		}
		
		public int getCurrPlay() {
			return this.currplay;
		}
		
		public boolean getBlockComplete() {
			return this.blockcomplete;
		}
		
		public boolean getRepeatComplete() {
			return this.repeatcomplete;
		}
		
		public boolean incrementCurrPlay() {
			this.currplay++;
			return (this.currplay >= this.numplays);
		}
		
		// Reset at the end of each repeat
		public void reset() {
			this.currplay = 0;
			
		}
	}
	
	public class RegularBlock extends Block {
		private int numplayers = 0;
		private double totalpayback = 0;
		
		public RegularBlock() {
			super();
		}
		
		public double getTotalPayBack() {
			return this.totalpayback;
		}
		
		public void calculatePayBack() {
			totalpayback = super.wager * super.numcards * numplayers;
		}

		public int getNumplayers() {
			return numplayers;
		}
		
		public void setNumplayers(int value) {
			this.numplayers = value;
		}
	}
	
	// Result class
	public class Result {
		private int[] balls = new int[NUM_ON_CARDS];
		private List<Card> cards = new ArrayList<Card>();
		private int numcards = 0;
		
		private long recordNumber = 0;
		private long blockNumber = 0;
		private float wager = 0;
		
		private int creditswon = 0;
		private int redsquares = 0;
		private int numwinningcards = 0;
		private boolean isRedSquareFlashing = true;
		
		public Result(int cards) {
			numcards = cards;
		}

		public int[] getBalls() {
			return balls;
		}

		public int getBall(int index) {
			return balls[index];
		}
		
		public void setBalls(int[] balls) {
			this.balls = balls;
		}

		public List<Card> getCards() {
			return cards;
		}
		
		public Card getCard(int index) {
			return cards.get(index);
		}
		
		public int getNumCards() {
			return numcards;
		}
		
		public void addCard(Card card) {
			this.cards.add(card);
		}

		public long getRecordNumber() {
			return recordNumber;
		}
		
		public void setRecordNumber(long recordNumber) {
			this.recordNumber = recordNumber;
		}

		public long getBlockNumber() {
			return blockNumber;
		}

		public void setBlockNumber(long blockNumber) {
			this.blockNumber = blockNumber;
		}

		public float getWager() {
			return this.wager;
		}
		
		public void setWager(float value) {
			this.wager = value;
		}
		
		public int getCreditsWon() {
			return creditswon;
		}

		public void addCreditsWon(int creditswon) {
			this.creditswon += creditswon;
		}
		
		public double getDollarsWon() {
			return EBingoModel.this.roundTwoDecimals(this.creditswon * EBingoModel.this.currblock.wager);
		}
		
		public int getRedSquares() {
			for (Card c : this.cards) {
				this.redsquares += c.getNumRedSquares();
			}
			
			return redsquares;
		}
		
		public void addRedSquares(int value) {
			this.redsquares += value;
		}
		
		public void incrementNumWinningCards() {
			this.numwinningcards++;
		}
		
		public int getNumWinningCards() {
			return numwinningcards;
		}
		
		public boolean isRedSquareFlashing() {
			return isRedSquareFlashing;
		}
		
		public void setFlashingRedSquare(boolean value) {
			isRedSquareFlashing = value;
		}
		
		public void reset() {
			this.creditswon = 0;
			
		}
	}

	public class RegularResult {
		private long recordNumber = 0;
		private long blockNumber = 0;
		private float wager = 0;
		
		private int[] balls = new int[75];
		private int numcards = 0;
		private int numplayers = 0;
		
		private List<Player> players;
		private List<Integer> playerswon = new ArrayList<Integer>();
		
		public RegularResult(int numcards, int numplayers) {
			this.numcards = numcards;
			this.numplayers = numplayers;
			
			this.players = new ArrayList<Player>(numplayers);
			
			for (int i = 0; i < numplayers; i++) {
				players.add(new Player(numcards));
			}
		}
		
		public int[] getBalls() {
			return this.balls;
		}
		
		public void setBalls(int[] b) {
			this.balls = b;
		}
		
		public long getRecordNum() {
			return this.recordNumber;
		}
		
		public void setRecordNum(long value) {
			this.recordNumber = value;
		}
		
		public long getBlockNum() {
			return this.blockNumber;
		}
		
		public void setBlockNum(long value) {
			this.blockNumber = value;
		}
		
		public float getWager() {
			return this.wager;
		}
		
		public void setWager(float value) {
			this.wager = value;
		}
		
		public int getNumCards() {
			return this.numcards;
		}
		
		public int getNumPlayers() {
			return this.numplayers;
		}
		
		public List<Integer> getPlayersWon() {
			return this.playerswon;
		}
		
		public Player getPlayer(int index) {
			return this.players.get(index);
		}
	}
	
	public class Player {
		private double dollarwon = 0;
		private int numcards = 0;
		
		private List<Card> cards;
		private List<Integer> cardswon = new ArrayList<Integer>();
		private List<String> winnames = new ArrayList<String>();
		
		public Player(int numcards)	{
			this.numcards = numcards;
			cards = new ArrayList<Card>(numcards);
		}
		
		public int getNumCards() {
			return this.numcards;
		}
		
		public double getDollarWon() {
			return this.dollarwon;
		}
		
		public void addDollarWon(double value) {
			this.dollarwon += value;
		}
		
		public List<Card> getCards() {
			return this.cards;
		}
		
		public Card getCard(int index) {
			return this.cards.get(index);
		}
		
		public List<Integer> getCardsWon() {
			return this.cardswon;
		}
		
		public void addCardsWon(int index) {
			this.cardswon.add(index);
		}
		
		public List<String> getWinNames() {
			return this.winnames;
		}
		
		public void addWinName(String value) {
			this.winnames.add(value);
		}
		
	}
	
	public class Card {
		private long id = 0;
		private int creditswon = 0;
		private String winname = "";
		
		private boolean isWin = false;
		private short numredsquares = 0;
		private List<Byte> redsquarelocations = new ArrayList<Byte>();

		private int[] numbersoncard = new int[NUM_ON_CARDS];
		
		public long getID() {
			return id;
		}

		public int[] getNumbersOnCard() {
			return numbersoncard;
		}
		
		public int getNumberOnCard(int index) {
			return numbersoncard[index];
		}
		
		public short getNumRedSquares() {
			this.numredsquares = (short)this.redsquarelocations.size();
			return numredsquares;
		}
		
		public double getDollarsWon() {
			return EBingoModel.this.roundTwoDecimals(this.creditswon * EBingoModel.this.currblock.wager);
		}
		
		public boolean isWin() {
			return isWin;
		}
		
		public int getCreditsWon() {
			return creditswon;
		}
		
		public String getWinName() {
			return winname;
		}
		
		public List<Byte> getRedSquareLocations() {
			return redsquarelocations;
		}
		
		public void addRedSquareLocation(Byte value) {
			// Only add the red square location if not already in the list
			if (! this.redsquarelocations.contains(value))
				redsquarelocations.add(value);
		}
		
		public void setWin(boolean value) {
			isWin = value;
		}
		
		public void setCreditsWon(int value) {
			creditswon = value;
		}
		
		public void setWinName(String value) {
			winname = value;
		}
		
		public void setID(long value) {
			this.id = value;
		}
		
		public void setNumberOnCard(int index, int value) {
			this.numbersoncard[index] = value;
		}
		
		/**
		 * Utility method used to rotate the numbers on the card after the card is generated
		 */
		public void rotateNumberOnCard() {
			int[] cardRotated = new int[NUM_ON_CARDS];
			
			cardRotated[0] = numbersoncard[0];
			cardRotated[1] = numbersoncard[5];
			cardRotated[2] = numbersoncard[10];
			cardRotated[3] = numbersoncard[14];
			cardRotated[4] = numbersoncard[19];
			cardRotated[5] = numbersoncard[1];
			cardRotated[6] = numbersoncard[6];
			cardRotated[7] = numbersoncard[11];
			cardRotated[8] = numbersoncard[15];
			cardRotated[9] = numbersoncard[20];
			cardRotated[10] = numbersoncard[2];
			cardRotated[11] = numbersoncard[7];
			cardRotated[12] = numbersoncard[16];
			cardRotated[13] = numbersoncard[21];
			cardRotated[14] = numbersoncard[3];
			cardRotated[15] = numbersoncard[8];
			cardRotated[16] = numbersoncard[12];
			cardRotated[17] = numbersoncard[17];
			cardRotated[18] = numbersoncard[22];
			cardRotated[19] = numbersoncard[4];
			cardRotated[20] = numbersoncard[9];
			cardRotated[21] = numbersoncard[13];
			cardRotated[22] = numbersoncard[18];
			cardRotated[23] = numbersoncard[23];
			
			this.numbersoncard = cardRotated;
		}
		
	}
	
	public class BasicInfoEntry {
		private long blockID = 0;
		private int numplays = 0;
		private int numcards = 0;
		
		private int wins = 0;
		private int losses = 0;
		private int ldws = 0;
		private int pushes = 0;
		private int multiwins = 0;
		private int redsquares = 0;
		private int flashingRedSquares = 0;
		
		public BasicInfoEntry() {
			if (EBingoModel.this.currblock != null) {
				this.blockID = EBingoModel.this.currblock.getBlockNum();
				this.numplays = EBingoModel.this.currblock.getNumPlays();
				this.numcards = EBingoModel.this.currblock.getNumCards();
			}
		}
		
		public int getWins() {
			return this.wins;
		}
		
		public int getLosses() {
			return this.losses;
		}
		
		public int getPushes() {
			return this.pushes;
		}
		
		public int getLDWs() {
			return this.ldws;
		}
		
		public int getMultiWins() {
			return this.multiwins;
		}
		
		public int getRedSquares() {
			return this.redsquares;
		}
		
		public int getFlashingRedSquares() {
			return this.flashingRedSquares;
		}
		
		public long getBlockID() {
			return this.blockID;
		}
		
		public int getNumPlays() {
			return this.numplays;
		}
		
		public int getNumCards() {
			return this.numcards;
		}
		
		public void incrementWins() {
			this.wins++;
		}
		
		public void incrementLosses() {
			this.losses++;
		}
		
		public void incrementPushes() {
			this.pushes++;
		}
		
		public void incrementLDWs() {
			this.ldws++;
		}
		
		public void incrementMultiWins() {
			this.multiwins++;
		}
		
		public void addRedSquares(int value) {
			this.redsquares += value;
		}
		
		public void addFlashingRedSquares(int value) {
			this.flashingRedSquares += value;
		}
		
		public void updateBIE(Result r) {
			float wagered = r.numcards * r.wager;
			int numwinningcards = 0;
			
			if (r.getCreditsWon() > 0) {
				if (r.getDollarsWon() - wagered < 0) {
					this.incrementLDWs();
				} else if (r.getDollarsWon() - wagered == 0) {
					this.incrementPushes();
				} else {
					this.incrementWins();
				}
				
				// Check for multiwins
				for (Card c : r.getCards()) {
					if (c.getCreditsWon() > 0) {
						numwinningcards++;
					}
				}
				
				// Update multiwins
				if (numwinningcards > 1) {
					this.incrementMultiWins();
				}
			} else {
				this.incrementLosses();
			}
			
			// Add red flashing squares
			if (r.isRedSquareFlashing) {
				this.addFlashingRedSquares(r.getRedSquares());
			}
			this.addRedSquares(r.getRedSquares());
		}
		
		public void flushBIE() {
			try {
				Database.flushBatch();
				Database.insertIntoTable(getLCBasicInfoDBname(), currbie, blocks.size());
			} catch (Exception e) {
				log.writeLine("Inserting BasicInfoEntry encountered problem: " + e.getMessage());
				e.printStackTrace();
			} finally {
				EBingoModel.this.currbie = new BasicInfoEntry();
			}
		}
	}
	
	public class PercentLosersEntry {
		private long id = 0;
		private int numplays = 0;
		private int numcards = 0;
		private int repeats = 0;
		
		private int wins = 0;
		private int ldws = 0;
		private int losses = 0;
		
		private double initBalance = 0;
		private double bet = 0;
		private int currIndex = 0;
		private double lpavg = 0;
		private double lpmedian = 0;
		private double lpSD = 0;
		private int rsavg = 0;
		private int rsmedian = 0;
		private int rsSD = 0;
		
		private List<Integer> losspercentages = new ArrayList<Integer>();
		private List<Range> ranges = new ArrayList<Range>();
		private List<Double> balances = new ArrayList<Double>();
		private List<Integer> redsquares = new ArrayList<Integer>();
		
		public PercentLosersEntry() {
			if (EBingoModel.this.currblock != null) {
				this.id = currblock.blocknum;
				this.numplays = EBingoModel.this.currblock.numplays;
				this.numcards = EBingoModel.this.currblock.numcards;
				this.repeats = EBingoModel.this.currblock.repeat;
			
				this.initBalance = numplays * numcards * EBingoModel.this.currblock.wager;
				this.bet = numcards * EBingoModel.this.currblock.wager;
				
				for (int i = 0; i < this.repeats; i++) {
					this.balances.add(initBalance);
					this.redsquares.add(0);
				}
			
				double percent = 1;
				while (percent >= -9) {
					if (percent == -9) {
						ranges.add(new Range(-9, -9));
						percent -= 0.25;
					} else {
						ranges.add(new Range(percent - 0.25, percent));
						percent -= 0.25;
					}
				}
			
				for (int i = 0; i < ranges.size(); i++) {
					this.losspercentages.add(0);
				}
			}
		}
		
		public long getID() {
			return this.id;
		}
		
		public int getNumPlays() {
			return this.numplays;
		}
		
		public int getNumCards() {
			return this.numcards;
		}
		
		public int getWins() {
			return this.wins;
		}
		
		private void incrementCurrBalanceIndex() {
			this.currIndex++;
		}
		
		private void incrementWins() {
			this.wins++;
		}
		
		public int getLDWs() {
			return this.ldws;
		}
		
		private void incrementLDWs() {
			this.ldws++;
		}
		
		public int getLosses() {
			return this.losses;
		}
		
		private void incrementLosses() {
			this.losses++;
		}
		
		public double getAvgLossBalance() {
			return this.lpavg;
		}
		
		public List<Integer> getRedSquares() {
			return this.redsquares;
		}
		
		public int getRedSquare(int index) {
			return this.redsquares.get(index);
		}
		
		public void addRedSquare(int value) {
			this.redsquares.set(currIndex, value);
		}
		
		public double getMedianLossBalance() {
			return this.lpmedian;
		}
		
		public double getLossBalanceSD() {
			return this.lpSD;
		}
		
		public int getAvgRedSquares() {
			return this.rsavg;
		}
		
		public int getMedianRedSquares() {
			return this.rsmedian;
		}
		
		public int getRedSquaresSD() {
			return this.rsSD;
		}
		
		public List<Integer> getLossPercentages() {
			return this.losspercentages;
		}
		
		public int getLossPercentage(int index) {
			return this.losspercentages.get(index);
		}
		
		public void incrementLossPercentage(int index) {
			this.losspercentages.set(index, losspercentages.get(index) + 1);
		}
		
		public void addBalance(double value) {
			this.balances.set(currIndex, balances.get(currIndex) + value);
		}
		
		public Range getRange(int index) {
			return this.ranges.get(index);
		}
		
		public double getBet() {
			return this.bet;
		}
		
		public void updatePLE(Result r) {
			this.addBalance((r.creditswon - 1) * this.bet);
			this.addRedSquare(r.redsquares);
			
			// Update win/lose and ldws
			if (r.creditswon > 0) {
				if (((r.creditswon -1) * bet) >= 0)
					this.incrementWins();
				else this.incrementLDWs();
			} else {
				this.incrementLosses();
			}
			
			// If one repeat is completed
			if (EBingoModel.this.currblock.getRepeatComplete()) {
				updateLossPercentage();
				updateLossBalance();
				incrementCurrBalanceIndex();
			}
		}
		
		public void flushPLE() {
			updateLossPercentage();
			updateLossBalance();
			calculatAvgLossBalance();
			calculateLossBalanceSD();
			calculateMedianLossBalance();
			calculateAvgRedSquares();
			calculateMedianRedSquares();
			calculateRedSquaresSD();
			
			try {
				Database.flushBatch();
				Database.insertIntoTable(EBingoModel.this.getPercentLosersDBTableName(),
						this, EBingoModel.this.blocks.size());
			} catch (SQLException e) {
				EBingoModel.this.log.writeLine("Inserting PercentLosers Table encountered problem: "
						+ e.getMessage());
				e.printStackTrace();
			} finally {
				EBingoModel.this.currple = new PercentLosersEntry();
			}
		}

		private void calculateMedianLossBalance() {
			List<Double> sortedbalances = this.balances;
			Collections.sort(sortedbalances);
			
			int mid = sortedbalances.size() / 2;
			
			this.lpmedian = (sortedbalances.size() % 2 == 1) ?
					sortedbalances.get(mid) :
						(sortedbalances.get(mid - 1) + sortedbalances.get(mid)) /2 ;
			
		}

		private void calculateLossBalanceSD() {
			double sdbalance = 0;
			
			for (double b : this.balances)
				sdbalance += (lpavg - b) * (lpavg - b);
			
			sdbalance /= this.balances.size();
			this.lpSD = Math.sqrt(sdbalance);
		}

		private void calculatAvgLossBalance() {
			int total = 0;
			
			for (double b : this.balances)
				total += b;
			
			this.lpavg = total / repeats;
		}
		
		private void updateLossBalance() {
			double currbalance = this.balances.get(currIndex);
			
			this.balances.set(currIndex, currbalance - this.initBalance);
		}

		private void updateLossPercentage() {
			double losspercentage;
			
			losspercentage = (this.initBalance - balances.get(currIndex))
					/ (double)this.initBalance;
			losspercentage = Double.valueOf(EBingoModel.this.twoDForm.format(losspercentage));
			
			for (int i = 0; i < this.ranges.size(); i++) {
				boolean isInRange = ranges.get(i).isLossPercentageInRange(losspercentage);
				
				if (isInRange) {
					this.incrementLossPercentage(i);
					break;
				}
			}
		}
		
		private void calculateAvgRedSquares() {
			int total = 0;
			
			for (int i: this.redsquares)
				total += i;
			
			this.rsavg = total / repeats;
		}
		
		private void calculateMedianRedSquares() {
			List<Integer> sortedrs = this.redsquares;
			Collections.sort(sortedrs);
			
			int mid = sortedrs.size() / 2;
			
			this.rsmedian = (sortedrs.size() % 2 == 1) ?
					sortedrs.get(mid) :
						(sortedrs.get(mid - 1) + sortedrs.get(mid)) /2;
		}
		
		private void calculateRedSquaresSD() {
			int rssd = 0;
		
			for (int i : this.redsquares)
				rssd += (rsavg - i) * (rsavg - i);
			
			rssd /= this.redsquares.size();
			this.rsSD = (int)Math.sqrt(rssd);
		}
	}
	
	/** Utility Classes **/
	public class Range {
		public double low = 0;
		public double high = 0;
		
		public Range (double l, double h) {
			this.low = l;
			this.high = h;
		}
		
		public boolean isLossPercentageInRange(double percentage) {
			// when loss percentage is [-9, infinity)
			if (low == -9 && high == -9)
				return (percentage <= low);
			// When loss percentage is between [0, -9)
			else 
				return (percentage <= high && percentage > low);
		}
		
		public boolean isTotalSpinCountInRange(int count) {
			if (low == high)
				return (count >= low);
			else
				return (count < high && count >= low);
		}
		
		public boolean isPeakBalanceInRange(double balance) {
			if (high == 100)
				return (balance == high);
			else if (low == 5000)
				return (balance >= low);
			else 
				return (balance < high && balance >= low);
		}
		
		public boolean isPrizeSizeInRange(double prizewon) {
			if (low == high) 
				return (prizewon >= low);
			else 
				return (prizewon >= low && prizewon < high);
			
		}
				
	}
	
	
	/** Utility Methods **/
	public final DecimalFormat twoDForm = new DecimalFormat("#.##");

	public final double roundTwoDecimals(double d) {
		return Double.valueOf(twoDForm.format(d));
	}
}
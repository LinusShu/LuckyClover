package com.eBingo;

import java.io.File;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
		LUCKY_CLOVER, OLD_GLORY, TREASURE_ISLAND, 
		TROPICAL_TREASURES, HIGH_STICKS, REGULAR
	}
	
	public enum CornerColour {
		NONE, GREEN, YELLOW, RED, RAINBOW
	}
	
	/** Database names **/
	public static String LC_RESULTS_TABLE_NAME = "LuckyCLover_Results";
	public static String LC_PAYTABLE_NAME = "LuckyCLover_Paytable";
	public static String LC_BLOCKS_TABLE_NAME = "LuckyCLover_Blocks";
	public static String LC_BASICINFO_TABLE_NAME = "LuckyClover_BasicInfo";
	
	public static String OG_RESULTS_TABLE_NAME = "OldGlory_Results";
	public static String OG_PAYTABLE_NAME = "OldGlory_Paytable";
	public static String OG_BLOCKS_TABLE_NAME = "OldGlory_Blocks";
	public static String OG_BASICINFO_TABLE_NAME = "OldGlory_BasicInfo";
	
	public static String TI_RESULTS_TABLE_NAME = "TreasureIsland_Results";
	public static String TI_PAYTABLE_NAME = "TreasueIsland_Paytable";
	public static String TI_BLOCKS_TABLE_NAME = "TreasureIsland_Blocks";
	public static String TI_BASICINFO_TABLE_NAME = "TreasureIsland_BasicInfo";
	
	public static String TT_RESULTS_TABLE_NAME = "TropicalTreasures_Results";
	public static String TT_PAYTABLE_NAME = "TropicalTreasues_Paytable";
	public static String TT_BLOCKS_TABLE_NAME = "TropicalTreasures_Blocks";
	public static String TT_BASICINFO_TABLE_NAME = "TropicalTreasure_BasicInfo";
	
	public static String HS_RESULTS_TABLE_NAME = "HighSticks_Results";
	public static String HS_PATTERN_NAME = "HighSticks_WinPatterns";
	public static String HS_PAYTABLE_NAME = "HighSticks_Paytable";
	public static String HS_BLOCKS_TABLE_NAME = "HighSticks_Blocks";
	public static String HS_BASICINFO_TABLE_NAME = "HighSticks_BasicInfo";
	
	public static String RB_RESULTS_TABLE_NAME = "RegularBingo_Results";
	public static String RB_PAYTABLE_NAME = "RegularBingo_Paytable";
	public static String RB_BLOCKS_TABLE_NAME = "RegularBingo_Blocks";
	
	public static String PERCENT_LOSERS_TABLE_NAME = "PercentLosers";
	public static String GAMBLERS_RUIN_TABLE_NAME = "GamblersRuin";
	
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
	private TTBlock currttblock = null;
	private int currblockindex = 0;
	private boolean repeatcomplete = false;
	private boolean blockcomplete = false;
	
	private Mode mode = Mode.LUCKY_CLOVER;
	private boolean genPlayResults = false;
	private boolean genGambersRuin = false;
	
	private BasicInfoEntry currbie = null;
	private TTBasicInfoEntry currttbie = null;
	private HSBasicInfoEntry currhsbie = null;
	
	private PercentLosersEntry currple = null;
	private GamblersRuinEntry currgre = null;
	//TODO add current "Entries" as needed
	
	private List<ViewInterface> views = new ArrayList<ViewInterface>();
	private List<String> errorLog = new ArrayList<String>();
	private List<String> warningLog = new ArrayList<String>();
	private List<String> errorLog2 = new ArrayList<String>();
	private List<String> warningLog2 = new ArrayList<String>();
	
	private List<PaytableEntry> paytable = new ArrayList<PaytableEntry>();
	private List<TTPaytableEntry> ttpaytable = new ArrayList<TTPaytableEntry>();
	private List<HSPaytableEntry> hspaytable = new ArrayList<HSPaytableEntry>();
	private List<Block> blocks = new ArrayList<Block>();
	private List<TTBlock> ttblocks = new ArrayList<TTBlock>();
	private List<Integer> hittable = new ArrayList<Integer>();
	private List<Integer> bonushittable = new ArrayList<Integer>();
	private List<Integer> hshittable = new ArrayList<Integer>();
	
	private Map<Integer, Range> rbonustable = new HashMap<Integer, Range>();
	private Map<Integer, Range> sbonustable = new HashMap<Integer, Range>(); 
	
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
	
	public void setGenGamblersRuin(boolean value) {
		this.genGambersRuin = value;
		this.UpdateViews();
	}
	
	public boolean getGenGamblersRuin() {
		return this.genGambersRuin;
	}
	
	public void setError() {
		this.error = true;
		this.UpdateViews();
	}
	
	public void incrementHits(int index) {
		this.hittable.set(index, hittable.get(index) + 1);
	}
	
	public void incrementBonusHits(int index) {
		this.bonushittable.set(index, bonushittable.get(index) + 1);
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
	
	public String getOGResultsDBTableName() {
		return this.buildDBTableName(OG_RESULTS_TABLE_NAME);
	}
	
	public String getOGPaytableDBName() {
		return this.buildDBTableName(OG_PAYTABLE_NAME);
	}
	
	public String getOGBlocksDBname() {
		return this.buildDBTableName(OG_BLOCKS_TABLE_NAME);
	}
	
	public String getOGBasicInfoDBname() {
		return this.buildDBTableName(OG_BASICINFO_TABLE_NAME);
	}
	
	public String getTIResultsDBTableName() {
		return this.buildDBTableName(TI_RESULTS_TABLE_NAME);
	}
	
	public String getTIPaytableDBName() {
		return this.buildDBTableName(TI_PAYTABLE_NAME);
	}
	
	public String getTIBlocksDBname() {
		return this.buildDBTableName(TI_BLOCKS_TABLE_NAME);
	}
	
	public String getTIBasicInfoDBname() {
		return this.buildDBTableName(TI_BASICINFO_TABLE_NAME);
	}
	
	public String getTTResultsDBTableName() {
		return this.buildDBTableName(TT_RESULTS_TABLE_NAME);
	}
	
	public String getTTPaytableDBName() {
		return this.buildDBTableName(TT_PAYTABLE_NAME);
	}
	
	public String getTTBlocksDBname() {
		return this.buildDBTableName(TT_BLOCKS_TABLE_NAME);
	}
	
	public String getTTBasicInfoDBname() {
		return this.buildDBTableName(TT_BASICINFO_TABLE_NAME);
	}
	
	public String getHSResultsDBTableName() {
		return this.buildDBTableName(HS_RESULTS_TABLE_NAME);
	}
	
	public String getHSPaytableDBName() {
		return this.buildDBTableName(HS_PAYTABLE_NAME);
	}
	
	public String getHSPatterntableDBName() {
		return this.buildDBTableName(HS_PATTERN_NAME);
	}
	
	public String getHSBlocksDBname() {
		return this.buildDBTableName(HS_BLOCKS_TABLE_NAME);
	}
	
	public String getHSBasicInfoDBname() {
		return this.buildDBTableName(HS_BASICINFO_TABLE_NAME);
	}
	
	public String getRBResultsDBTableName() {
		return this.buildDBTableName(RB_RESULTS_TABLE_NAME);
	}
	
	public String getRBPaytableDBName() {
		return this.buildDBTableName(RB_PAYTABLE_NAME);
	}
	
	public String getRBBlocksDBName() {
		return this.buildDBTableName(RB_BLOCKS_TABLE_NAME);
	}
	
	public String getPercentLosersDBTableName() {
		return this.buildDBTableName(PERCENT_LOSERS_TABLE_NAME);
	}
	
	public String getGamblersRuinDBname() {
		return this.buildDBTableName(GAMBLERS_RUIN_TABLE_NAME);
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
	
	public double getCurrBalance() {
		if (currblock != null)
			return this.currblock.getCurrBalance();
		else return 0;
	}
	
	public List<Block> getBlocks() {
		return this.blocks;
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
		
		switch (this.mode) {
		case LUCKY_CLOVER:
			this.log.writeLine("Set Paytable DB Table Name: " + this.getLCPaytableDBName());
			this.log.writeLine("Set Blocks DB Table Name: " + this.getLCBlocksDBname());
			this.log.writeLine("Set Results DB Table Name: " + this.getLCResultsDBTableName());
			break;
		case OLD_GLORY:
			this.log.writeLine("Set Paytable DB Table Name: " + this.getOGPaytableDBName());
			this.log.writeLine("Set Blocks DB Table Name: " + this.getOGBlocksDBname());
			this.log.writeLine("Set Results DB Table Name: " + this.getOGResultsDBTableName());
			break;
		case TREASURE_ISLAND:
			this.log.writeLine("Set Paytable DB Table Name: " + this.getTIPaytableDBName());
			this.log.writeLine("Set Blocks DB Table Name: " + this.getTIBlocksDBname());
			this.log.writeLine("Set Results DB Table Name: " + this.getTIResultsDBTableName());
			break;
		case TROPICAL_TREASURES:
			this.log.writeLine("Set Paytable DB Table Name: " + this.getTTPaytableDBName());
			this.log.writeLine("Set Blocks DB Table Name: " + this.getTTBlocksDBname());
			this.log.writeLine("Set Results DB Table Name: " + this.getTTResultsDBTableName());
			break;
		case HIGH_STICKS:
			this.log.writeLine("Set Paytable DB Table Name: " + this.getHSPaytableDBName());
			this.log.writeLine("Set Blocks DB Table Name: " + this.getHSBlocksDBname());
			this.log.writeLine("Set Results DB Table Name: " + this.getHSResultsDBTableName());
			break;
		default:
			this.log.writeLine("Set Paytable DB Table Name: " + this.getRBPaytableDBName());
			this.log.writeLine("Set Blocks DB Table Name: " + this.getRBBlocksDBName());
			this.log.writeLine("Set Results DB Table Name: " + this.getRBResultsDBTableName());
			break;
			
		}
			
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
		this.currttblock = null;
		this.currblockindex = 0;
		this.currplayed= 0;
		
		this.currbie = null;
		this.currttbie = null;
		this.currple = null;
		this.currgre = null;
		
		this.paytable.clear();
		this.hspaytable.clear();
		this.ttpaytable.clear();
		
		this.blocks.clear();
		this.ttblocks.clear();
		
		this.hittable.clear();
		this.bonushittable.clear();
		this.hshittable.clear();
		
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
		
		this.log.writeLine("Reading Configuration File...Finished. Errors: "
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
				
				// If in Tropical Treasures Mode
				if (this.mode == Mode.TROPICAL_TREASURES) {
					String winCode = e.getAttribute("winCode");
					int picks, hits;
					double payout;
					
					try {
						picks = Integer.parseInt(e.getAttribute("picks"));
						hits = Integer.parseInt(e.getAttribute("hits"));
						payout = Double.parseDouble(e.getAttribute("payout"));
					} catch (NumberFormatException nfe) {
						this.addErrorToLog("PaytableEnetry[" + Integer.toString(i)
								+ "]: Invalid value for picks/hits/payout.");
						picks = hits = -1;
						payout = -1;
					}
					
					if (picks > 0 && hits > 0 && payout > 0) {
						TTPaytableEntry tpe = new TTPaytableEntry();
						tpe.setWinCode(winCode);
						tpe.setPicks(picks);
						tpe.setHits(hits);
						tpe.setPayout(payout);
						this.ttpaytable.add(tpe);
						this.hittable.add(0);
						this.bonushittable.add(0);
					}
					
				// If not in Tropical Treasures mode	
				} else {
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
					
		// Read High Sticks paytable
		if (this.mode == Mode.HIGH_STICKS) {
			nl = doc.getElementsByTagName("HSPaytableEntry");
			// Read payouts
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;
					
					String hswinCode = e.getAttribute("winCode");
					int ballsCalled;
					double hspayout;
					
					try {
						ballsCalled = Integer.parseInt(e.getAttribute("ballsCalled"));
						hspayout = Double.parseDouble(e.getAttribute("payout"));
					} catch (NumberFormatException nfe) {
						this.addErrorToLog("TTPaytableEnetry[" + Integer.toString(i)
								+ "]: Invalid value for picks/hits/payout.");
						ballsCalled = -1;
						hspayout = -1;
					}
					
					if (ballsCalled > 0 && hspayout > 0) {
						HSPaytableEntry hspe = new HSPaytableEntry();
						hspe.setWinCode(hswinCode);
						hspe.setBallsCalled(ballsCalled);
						hspe.setPayout(hspayout);
						this.hspaytable.add(hspe);
						this.hshittable.add(0);
					}
				}
			}
			
			// Read regular bonus multipliers and odds
			nl = doc.getElementsByTagName("RBonusPrize");
			int start = 0;
			
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;
					
					int multiplier, slice;
					
					try {
						multiplier = Integer.parseInt(e.getAttribute("multiplier"));
						slice = Integer.parseInt(e.getAttribute("slice"));
					} catch (NumberFormatException nfe) {
						this.addErrorToLog("RBonusPrize[" + Integer.toString(i)
								+ "]: Invalid value for multiplier/slice.");
						multiplier = -1;
						slice = -1;
					}
					
					if (multiplier > 0 && slice > 0) {
						Range range = null;
						
						if (i == 0)
							range = new Range(slice, slice);
						else 
							range = new Range(start, start + slice);
						start += slice;
						
						this.rbonustable.put(multiplier, range);
					}
				}
			}
			
			// Read regular bonus multipliers and odds
			nl = doc.getElementsByTagName("SBonusPrize");
			start = 0;
			
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;
					
					int multiplier, slice;
					
					try {
						multiplier = Integer.parseInt(e.getAttribute("multiplier"));
						slice = Integer.parseInt(e.getAttribute("slice"));
					} catch (NumberFormatException nfe) {
						this.addErrorToLog("SBonusPrize[" + Integer.toString(i)
								+ "]: Invalid value for multiplier/slice.");
						multiplier = -1;
						slice = -1;
					}
					
					if (multiplier > 0 && slice > 0) {
						Range range = null;
						
						if (i == 0)
							range = new Range(slice, slice);
						else 
							range = new Range(start, start + slice);
						start += slice;
						
						this.sbonustable.put(multiplier, range);
					}
				}
			}
		}
						
		
	}
	
	private void readBlocks(Document doc) {
		// If simulating Gambler's Ruin
		if (this.genGambersRuin) {
			NodeList list = doc.getElementsByTagName("grblock");
			
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;

					int numcards = -1;
					float wager = -1;
					double bankroll = -1;
					int repeat = -1;

					try {
						numcards = Integer.parseInt(e.getAttribute("numcards"));
						wager = Float.parseFloat(e.getAttribute("wager"));
						bankroll = Double.parseDouble(e
								.getAttribute("bankroll"));
						repeat = Integer.parseInt(e.getAttribute("repeat"));
					} catch (NumberFormatException nfe) {
						this.addErrorToLog2("Block[" + Integer.toString(i)
								+ "]: Invalid vallue(s) for this block.");
					}

					if (numcards > 0 && wager > 0 && bankroll > 0 && repeat > 0
							&& repeat <= MAX_REPEATS) {
						Block b = new GRBlock();
						b.setNumCards(numcards);
						b.setWager(wager);
						b.setBankroll(bankroll);
						b.setRepeat(repeat);
						b.setBlockNum(i + 1);
						b.setCurrBalance(bankroll);
						this.blocks.add(b);
					}
				}
			}
		
		// If not simulating gambler's ruin
		} else {
			NodeList list = doc.getElementsByTagName("block");
		
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;
				
					// If in Lucky Clover, Old Glory or Treasure Island mode
					if (this.mode == Mode.LUCKY_CLOVER 
							|| this.mode == Mode.OLD_GLORY || this.mode == Mode.TREASURE_ISLAND) {
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
									+ "]: Invalid value(s) for this block.");
						}
					
						if (numplays > 0 && numcards > 0 && wager > 0 && repeat > 0 && repeat <= MAX_REPEATS) {
							if (this.mode == Mode.TREASURE_ISLAND) {
								if (numcards == numplays * 2 || numcards == (numplays * 2 - 1)) {
									Block b = new Block();
									b.setNumPlays(numplays);
									b.setNumCards(numcards);
									b.setWager(wager);
									b.setRepeat(repeat);
									b.setBlockNum(i + 1);
									this.blocks.add(b);
								}
							} else {
								Block b = new Block();
								b.setNumPlays(numplays);
								b.setNumCards(numcards);
								b.setWager(wager);
								b.setRepeat(repeat);
								b.setBlockNum(i + 1);
								this.blocks.add(b);
							}
						} else {
							this.addErrorToLog2("Block[" + Integer.toString(i)
									+ "]: All attributes must be greater than 0.");
						}
					
					// If in High Sticks Bingo/Bingo Latte mode	
					} else if (this.mode == Mode.HIGH_STICKS) {
						int numplays = -1;
						int numcards = -1;
						double wager = -1;
						int repeat = -1;
						boolean fixed = false;
					
						try {
							numplays = Integer.parseInt(e.getAttribute("numplays"));
							numcards = Integer.parseInt(e.getAttribute("numcards"));
							wager = Double.parseDouble(e.getAttribute("wager"));
							repeat = Integer.parseInt(e.getAttribute("repeat"));
							fixed = Boolean.parseBoolean(e.getAttribute("fixed"));
						} catch (NumberFormatException nfe) {
							this.addErrorToLog2("Block[" + Integer.toString(i)
									+ "]: Invalid value(s) for this block.");
						}
					
						if (numplays > 0 && numcards > 0 && wager > 0 && repeat > 0 && repeat <= MAX_REPEATS) {
							Block hsb = new HSBlock();
							hsb.setNumPlays(numplays);
							hsb.setNumCards(numcards);
							hsb.setWager(wager);
							hsb.setRepeat(repeat);
							hsb.setBlockNum(i + 1);
							hsb.setFixed(fixed);
							this.blocks.add(hsb);
						} else {
							this.addErrorToLog2("HSBlock[" + Integer.toString(i)
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
							nfe.printStackTrace();
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
					// If simulating Tropical Treasures Bingo
					} else if (this.mode == Mode.TROPICAL_TREASURES) {
						int numplays = -1;
						boolean fixedcard = false;
						int picks = -1;
						float wager = -1;
						int repeat = -1;
						
						try {
							numplays = Integer.parseInt(e.getAttribute("numplays"));
							fixedcard = Boolean.parseBoolean(e.getAttribute("fixedcard"));
							picks = Integer.parseInt(e.getAttribute("picks"));
							wager = Float.parseFloat(e.getAttribute("wager"));
							repeat = Integer.parseInt(e.getAttribute("repeat"));
						} catch (NumberFormatException nfe) {
							this.addErrorToLog2("Block[" + Integer.toString(i)
									+ "]: Invalid value(s) for this block.");
							nfe.printStackTrace();
						}
						
						if (numplays > 0 && picks > 0 && wager > 0
								&& repeat > 0 && repeat <= MAX_REPEATS) {
							TTBlock ttb = new TTBlock();
							ttb.setBlockNum(i + 1);
							ttb.setNumPlays(numplays);
							ttb.setFixedCard(fixedcard);
							ttb.setPicks(picks);
							ttb.setWager(wager);
							ttb.setRepeat(repeat);
							this.ttblocks.add(ttb);
						} else {
							this.addErrorToLog2("Block[" + Integer.toString(i)
									+ "]: All attributes must be greater than 0.");
						}
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
		
		if (this.mode == Mode.TROPICAL_TREASURES) {
			for (TTBlock ttb : this.ttblocks) 
				tp += ttb.getNumPlays() * ttb.getRepeat();
		} else {
			for (Block b : this.blocks)
				tp += b.getNumPlays() * b.getRepeat();
		}
		
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
				EBingoModel.this.log.writeLine("Total Plays: " + Long.toString(totalplay));
				
				if (totalplay > 0 || EBingoModel.this.genGambersRuin) {
					initializeDB();
					
					// Start to generate the results
					try {
						EBingoModel.this.log.writeLine("GENERATING RESULTS...");
						
						switch (EBingoModel.this.mode) {
						
						case TREASURE_ISLAND:
							doTreasureIslandMode();
							break;
						case TROPICAL_TREASURES:
							doTropicalTreasuresMode();
							break;
						case HIGH_STICKS:
							doHighSticksMode();
							break;
						case REGULAR:
							doRegularMode();
							break;
						default:
							doEBingoMode();
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
				} else { // If totalplay is 0
					EBingoModel.this.stop();
				}
				
				EBingoModel.this.log.writeLine("END OF PRODUCTION");
			}
		};
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		
	}
	
	private void doTreasureIslandMode() throws Exception {
		EBingoModel.this.currblock = EBingoModel.this.blocks.get(EBingoModel.this.getBlockIndex());
		TreasureIslandGenerator tig = null;
		
		if (EBingoModel.this.currblock != null) {
			tig = new TreasureIslandGenerator(this);
		}
		
		EBingoModel.this.currbie = new BasicInfoEntry();
		
		tig.generatePlays();
		//TODO finish TI implementation
	}
	
	private void doEBingoMode() throws Exception {
		if (EBingoModel.this.currblock != null)
			EBingoModel.this.currblock = EBingoModel.this.blocks
					.get(EBingoModel.this.getBlockIndex());
		
		EBingoModel.this.currbie = new BasicInfoEntry();
		EBingoModel.this.currple = new PercentLosersEntry();
		
		if (EBingoModel.this.genGambersRuin)
			EBingoModel.this.currgre = new GamblersRuinEntry();
		
		while (EBingoModel.this.currblock != null
				&& !EBingoModel.this.cancelled) {
			
			if (!EBingoModel.this.paused) {
				
				Result r = simulator.generateResults();
			
				if (r != null) {
					r.setRecordNumber(EBingoModel.this.currplayed + 1);
					r.setBlockNumber(EBingoModel.this.currblock.getBlockNum());
					r.setWager(EBingoModel.this.currblock.getWager());	
					
					EBingoModel.this.currbie.updateBIE(r);
					
					if (EBingoModel.this.genPlayResults) {
						if (EBingoModel.this.mode == Mode.LUCKY_CLOVER)
							Database.insertIntoTable(EBingoModel.this.getLCResultsDBTableName(), r);
						else if (EBingoModel.this.mode == Mode.OLD_GLORY)
							Database.insertIntoTable(EBingoModel.this.getOGResultsDBTableName(), r);
						else 
							Database.insertIntoTable(EBingoModel.this.getTIResultsDBTableName(), r);
					}
					
					// Increment play
					if (EBingoModel.this.genGambersRuin) {
						EBingoModel.this.incrementGRCurrPlay(r);
					} else {
						//TODO update bunch of "Entries" here
						EBingoModel.this.incrementCurrPlay();
						EBingoModel.this.currple.updatePLE(r);
					}
					

					if (this.repeatcomplete) {
						this.repeatcomplete = false;
					}
				
					if (EBingoModel.this.blockcomplete) {
						if (!EBingoModel.this.genGambersRuin) {
							currple.flushPLE();
						}
						EBingoModel.this.blockcomplete = false;
					}
				} // If result generated is valid 
			} // If paused
		} // If every block is done
	}
	
	private void doTropicalTreasuresMode() throws Exception {
		EBingoModel.this.currttblock = EBingoModel.this.ttblocks
				.get(EBingoModel.this.getBlockIndex());
		
		EBingoModel.this.currttbie = new TTBasicInfoEntry();
		
		while (EBingoModel.this.currttblock != null
				&& !EBingoModel.this.cancelled) {
			
			if (!EBingoModel.this.paused) {
				TropicalTreasuresResult ttr = simulator.generateTTResults();
				
				if (ttr != null) {
					ttr.setRecordNumber(EBingoModel.this.currplayed + 1);
					ttr.setBlockNumber(EBingoModel.this.currttblock.getBlockNum());
					ttr.setNumPlays(EBingoModel.this.currttblock.getNumPlays());	
					
					EBingoModel.this.currttbie.updateTTBIE(ttr);
					
					if (EBingoModel.this.genPlayResults) {
						Database.insertIntoTable(EBingoModel.this.getTTResultsDBTableName(), ttr);
					}
					
					// Increment play
					EBingoModel.this.incrementCurrPlay();

					if (this.repeatcomplete) {
						this.repeatcomplete = false;
					}
				
					if (EBingoModel.this.blockcomplete) {
						Database.flushBatch();
						EBingoModel.this.blockcomplete = false;
					}
				} // If result generated is valid 
			} // not paused
		} // while not empty
	}
	
	private void doHighSticksMode() throws Exception {
		EBingoModel.this.currblock = EBingoModel.this.blocks
				.get(EBingoModel.this.getBlockIndex());
		
		EBingoModel.this.currhsbie = new HSBasicInfoEntry();
		
		while (EBingoModel.this.currblock != null
				&& !EBingoModel.this.cancelled) {
			
			if (!EBingoModel.this.paused) {
				
				HighSticksResult hsr = simulator.generateHSResults();
			
				if (hsr != null) {	
					
					EBingoModel.this.currhsbie.updateHSBIE(hsr);
					
					if (EBingoModel.this.genPlayResults) 
						Database.insertIntoTable(EBingoModel.this.getHSResultsDBTableName(), hsr);
				
					EBingoModel.this.incrementCurrPlay();
					
					if (this.repeatcomplete) {
						this.repeatcomplete = false;
					}
				
					if (EBingoModel.this.blockcomplete) {
						Database.flushBatch();
						EBingoModel.this.blockcomplete = false;
					}
				} // If result generated is valid 
			} // If paused
		} // If every block is done
	}
	
	private void doRegularMode() throws Exception {
		EBingoModel.this.currblock = EBingoModel.this.blocks
				.get(EBingoModel.this.getBlockIndex());
		
		EBingoModel.this.currbie = new BasicInfoEntry();
		
		while (EBingoModel.this.currblock != null
				&& !EBingoModel.this.cancelled) {
			
			if (!EBingoModel.this.paused) {
				
				RegularResult rr = simulator.generateRegularResults();
			
				if (rr != null) {
					rr.setRecordNum(EBingoModel.this.currplayed + 1);
					rr.setBlockNum(EBingoModel.this.currblock.getBlockNum());
					rr.setWager(EBingoModel.this.currblock.getWager());	
					
					
					//EBingoModel.this.currbie.updateBIE(ttr);
					
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
					
					switch (this.mode) {
					case LUCKY_CLOVER:
						Database.insertIntoTable(getLCBlocksDBname(), b);
						break;
					case OLD_GLORY:
						Database.insertIntoTable(getOGBlocksDBname(), b);
						break;
					case TREASURE_ISLAND:
						Database.insertIntoTable(getTIBlocksDBname(), b);
						break;
					case HIGH_STICKS:
						Database.insertIntoTable(getHSBlocksDBname(), b);
						break;
					case REGULAR:
						Database.insertIntoTable(getRBBlocksDBName(), b);
						break;
					default:
						break;
					}
				}
				
			// If in Tropical Treasures Bingo Mode	
			} else if (EBingoModel.this.ttblocks != null
					&& EBingoModel.this.ttblocks.size() > 0) {
				for (int i = 0; i < EBingoModel.this.ttblocks.size(); i++) {
					TTBlock ttb = EBingoModel.this.ttblocks.get(i);
					Database.insertIntoTable(getTTBlocksDBname(), ttb);	
				}
			}
			
			Database.flushBatch();
			
		} catch (Exception e) {
			EBingoModel.this.setError();
			EBingoModel.this.log.writeLine("ERROR: "
					+ "Failed to connect to DB while creating blocks tables. "
					+ "Message: " + e.getMessage());
			e.printStackTrace();
		}

	}
	
	private void finalizeDB() {
		// Create paytable database table
		try {
			// If in Tropical Treasures Bingo mode
			if (EBingoModel.this.ttpaytable != null
					&& EBingoModel.this.ttpaytable.size() > 0) {
				for (int i = 0; i < EBingoModel.this.ttpaytable.size(); i++) {
					TTPaytableEntry tpe = EBingoModel.this.ttpaytable.get(i);
					int hits = EBingoModel.this.hittable.get(i);
					int bonushits = EBingoModel.this.bonushittable.get(i);
					
					Database.insertIntoTable(getTTPaytableDBName(), tpe, hits, bonushits);
				}
			
			// If in other EBingo mode
			} else if (EBingoModel.this.paytable != null
					&& EBingoModel.this.paytable.size() > 0) {
				for (int i = 0; i < EBingoModel.this.paytable.size(); i++) {
					PaytableEntry pe = EBingoModel.this.paytable.get(i);
					pe.setEntryID(i);
					
					int hits = EBingoModel.this.hittable.get(i);
					
					switch (this.mode) {
					case LUCKY_CLOVER:
						Database.insertIntoTable(getLCPaytableDBName(), pe, hits);
						break;
					case OLD_GLORY:
						Database.insertIntoTable(getOGPaytableDBName(), pe, hits);
						break;
					case TREASURE_ISLAND:
						Database.insertIntoTable(getTIPaytableDBName(), pe, hits);
						break;
					case HIGH_STICKS:
						Database.insertIntoTable(getHSPatterntableDBName(), pe, hits);
						break;
					case REGULAR:
						Database.insertIntoTable(getRBPaytableDBName(), pe, hits);
						break;
					default:
						break;
						
					}
				}
			}
			
			Database.flushBatch();

			// If in High Sticks mode, insert the paytable after the win pattern table is inserted
			if (EBingoModel.this.hspaytable != null
					&& EBingoModel.this.hspaytable.size() > 0) {
				for (int i = 0; i < EBingoModel.this.hspaytable.size(); i++) {
					HSPaytableEntry hspe = EBingoModel.this.hspaytable.get(i);
					Database.insertIntoTable(getHSPaytableDBName(), hspe, EBingoModel.this.hshittable.get(i));
				}
				Database.flushBatch();
			 }
			 
		} catch (Exception e) {
			EBingoModel.this.setError();
			EBingoModel.this.log.writeLine("ERROR: "
					+ "Failed to connect to DB while creating paytable tables. "
					+ "Message: " + e.getMessage());
			e.printStackTrace();
		}
		
		// Close DB Connection
		try {
			Database.shutdownConnection();
			System.out.println("DB connection shutted down");
		} catch (SQLException e) {
			EBingoModel.this.setError();
			EBingoModel.this.log.writeLine("ERROR: Closing database encountered error. "
					+ "Message: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void incrementCurrPlay() {
		this.currplayed++;
		
		// If in Tropical Treasures Bingo mode
		if (this.mode == Mode.TROPICAL_TREASURES) {
			// If the current block runs out of plays
			if (this.currttblock.incrementCurrPlay()) {
				// If the current block needs to be repeated
				if (this.currttblock.currrepeat < this.currttblock.repeat) {
					currttblock.incrementCurrRepeat();
					this.repeatcomplete = true;
					currttblock.reset();
				// If the block is done
				} else {
					currblockindex++;
					this.blockcomplete = true;
					
					currttbie.flushTTBIE();
					
					// If there are more blocks
					if (currblockindex < ttblocks.size()) {
						currttblock = ttblocks.get(currblockindex);
					} else {
						this.currttblock = null;
					}
				}
			}
			
		// If in other modes	
		} else {
			// If the current block runs out of plays
			if (this.currblock.incrementCurrPlay()) {
				// If the current block needs to be repeated
				if (this.currblock.currrepeat < this.currblock.repeat) {
					currblock.incrementCurrRepeat();
					this.repeatcomplete = true;
					currblock.reset();
				// If the block is done
				} else {
					currblockindex++;
					this.blockcomplete = true;
					
					// Flush the BasicInfoEntry(s) 
					if (this.mode == Mode.HIGH_STICKS)
						currhsbie.flushHSBIE();
					else 
						currbie.flushBIE();
					
					// If there are more blocks
					if (currblockindex < blocks.size()) {
						currblock = blocks.get(currblockindex);
					} else {
						this.currblock = null;
					}
				}
			}
		}
		UpdateViews();
	}
	
	private void incrementGRCurrPlay(Result r) {
		this.currplayed++;
		
		// If the currgrblock ran out of money
		if (this.currblock.incrementCurrPlay(r)) {
			// If currgrblock needs to be repeated
			if (this.currblock.currrepeat < this.currblock.repeat) {
				this.repeatcomplete = true;
				this.currgre.updateGRE();
				this.currgre.resetBalance();
				this.currblock.currrepeat++;
				this.currblock.reset();
			} else {
				this.blockcomplete = true;
				this.currgre.updateGRE();
				this.currblockindex++;
				
				// If there are more blocks
				if (this.currblockindex < this.blocks.size()) {
					this.currblock = this.blocks.get(currblockindex);
					this.currgre = new GamblersRuinEntry();
				} else {
					this.currblock = null;
				}
			}
		}
		this.UpdateViews();
	}
	
	
	/** Classes **/

	//Simulator class
	public class Simulator {
		private EBingoModel model = null;
		private TreasureIslandGenerator tig = null;
		private Result r;
		private RegularResult rr;
		private HighSticksResult hsr;
		private TropicalTreasuresResult ttr;
		private int[] balls;
		private long cardID = 0;
		private long numcards = 0;
		private Random ran = new Random();
		
		private List<Integer> array1_15 = new ArrayList<Integer>(15);
		private List<Integer> array16_30 = new ArrayList<Integer>(15);
		private List<Integer> array31_45 = new ArrayList<Integer>(15);
		private List<Integer> array46_60 = new ArrayList<Integer>(15);
		private List<Integer> array61_75 = new ArrayList<Integer>(15);
		private List<Integer> array1_75 = new ArrayList<Integer>(75);
		
		private List<Integer> lastpicks = new ArrayList<Integer>();
		private List<Card> lastcards = null;
		
		public Simulator(EBingoModel model) {
			this.model = model;
			tig = new TreasureIslandGenerator(this.model);
			
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
			// Set Treasure Island mode's total number of cards 
			if (model.currblock != null) {
				// Do not reset the number of cards every play
				if (model.currblock.currplay == 0)
					this.numcards = model.currblock.getNumCards();
			}
			
			// Treasure Island mode draws 25 balls
			if (model.getMode() == Mode.TREASURE_ISLAND)
				balls = new int[25];
			else 
				balls = new int[NUM_ON_CARDS];
			
			if (model.currblock != null) {
				r = new Result(model.currblock.getNumCards());
				r.setBlockNumber(model.currblock.getBlockNum());
				r.setRecordNumber(model.currplayed);
			}
			
			try {
				generateBalls();
				
				if (model.getMode() == Mode.TREASURE_ISLAND) {
					for (int i = 0; i < 2 && i < this.numcards; i++) {
						Card c = generateCard(new Card());
						r.addCard(c);
						this.numcards --;
					}
				} else {
				
					for (int i = 0; i < r.getNumCards(); i++) {
						Card c = generateCard(new Card());
						r.addCard(c);
					}
				}
				
				// Cheat balls and cards here
				/*
				 * balls[0] = 23;
				 * ...
				 * 
				 * 
				 */
			} catch (Exception e) {
				log.writeLine("Generation of Balls and Cards encountered problem: ");
				log.writeLine(e.getMessage());
				e.printStackTrace();
				model.setError();
				model.stop();
			}
			
			// Loop through all the cards 
			for (int i = 0; i < r.cards.size(); i++) {
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
		 * The method called only in Tropical Treasures Bingo mode
		 * @return  the TropicalTreausresResult object of this play
		 */
		private TropicalTreasuresResult generateTTResults() {
			if (model.currttblock != null) {
				ttr = new TropicalTreasuresResult(model.currttblock.getPicks(), model.currttblock.isFixedCard());
				ttr.setWager(currttblock.getWager());
				
				// If the picks are not fixed per play 
				// or it is first play generate the picks
				if (!ttr.isFixed() || model.currttblock.currplay == 0)
					generateTTPicks(ttr.getNumPicks());
				else 
					ttr.setPicks(this.lastpicks);
				
				generateTTBalls();
				
				generateTTHits();
				
				calculateTTPayout();
			}
			
			return ttr;
		}
		
		/**
		 * This method generates the picks in Tropical Treasures Bingo mode
		 * @param numpicks
		 */
		private void generateTTPicks(int numpicks) {
			Collections.shuffle(this.array1_75);
			
			for (int i = 0; i < numpicks; i++) 
				this.ttr.addPick(this.array1_75.get(i));
			
			this.lastpicks = this.ttr.getPicks();
		}
		
		/**
		 * This method generates the 21 called balls in Tropical Treasures Bingo mode
		 */
		private void generateTTBalls() {
			Collections.shuffle(this.array1_75);
			
			for (int i = 0; i < 21; i++) 
				this.ttr.addBall(this.array1_75.get(i));
		}
		
		/**
		 * This method generates the hits according to the 21 called balls and the picks in Tropical Treasures Bingo mode
		 */
		private void generateTTHits() {
			for (int i : this.ttr.balls) {
				if (this.ttr.picks.contains(i))
					this.ttr.addHits(i);
			}
		}
		
		public HighSticksResult generateHSResults() {
			int balldrawn = 0;
			this.balls = new int[25];
			
			// If the currblock is not null
			if (model.currblock != null) {
				hsr = new HighSticksResult();
				hsr.setBlockNumber(model.currblock.getBlockNum());
				hsr.setRecordNumber(model.currplayed);
				hsr.setNumcards(model.currblock.getNumCards());
				hsr.setNumplays(model.currblock.getNumPlays());
				hsr.setWager(model.currblock.getWager());
				hsr.setFixed(model.currblock.isFixed());
			
				// Generate the cards for the first play and save a copy in lastcards
				if (model.currplayed == 0) {
					for (int i = 0; i < hsr.numcards; i++) 
						hsr.addCard(generateCard(new HSCard()));
					this.lastcards = new ArrayList<Card>(hsr.getCards());
				// Generate cards if necessary
				} else if (!hsr.isFixed()) {
					for (int i = 0; i < hsr.numcards; i++) 
						hsr.addCard(generateCard(new HSCard()));
					
				} else {
					for (Card c : this.lastcards)
						c.resetWins();
					
					hsr.setCards(this.lastcards);
				}
				
				// Drawing balls
				while (balldrawn < 25) {
					// Call balls one at a time
					generateHSBalls(balldrawn);
					
					// Calculate payout after 4 balls are drawn
					if (balls[3] != 0) {
						// Check all cards for any wins
						for (int i = 0; i < hsr.numcards; i++) {
							int outcome = this.generateOutCome(hsr.getCard(i));
							
							calculateHSPayout(outcome, balldrawn, i);
						}
					}
					balldrawn++;
				}
				
				// Calculate bonus payout if there is any bonus win
				if (hsr.getNumBonusWins() > 0) {
					calculateHSBonusWin();
				}
				
				return hsr;
				
			// Done if the currblock is null	
			} else {
				return null;
			}
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
					Card c = generateCard(new Card());
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
		
		private void generateBalls() {
			// If it's in Treasure Island Mode
			if (EBingoModel.this.getMode() == Mode.TREASURE_ISLAND) {
				generateTIBalls();
			} else { 
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
			}	
			
			r.setBalls(balls);
		}
		
		private void generateTIBalls() {
			Collections.shuffle(this.array1_75);
			
			for (int i = 0; i < this.balls.length; i++) {
				balls[i] = this.array1_75.get(i);
			}
		}
		
		private void generateHSBalls(int index) {
			if (balls[0] == 0) {
				Collections.shuffle(array1_75);
				balls[index] = array1_75.get(index);
			} else if (index < 25) {
				balls[index] = array1_75.get(index);
			}
			
			hsr.addBall(array1_75.get(index));
		}
		
		private void generateRegularBalls(int index) {
			if (balls[0] == 0) {
				Collections.shuffle(array1_75);
				balls[index] = array1_75.get(index);
			} else if (index < 75) {
				balls[index] = array1_75.get(index);
			}
		}
		
		private Card generateCard(Card card) {
			
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
					
					// *2 payout if playing 2 cards in Treasure Island mode
					if (model.getMode() == Mode.TREASURE_ISLAND
							&& cardindex == 1 ) {
						int payout = 0;
						if (model.paytable.get(i).getName().equalsIgnoreCase("letter_x")) 
							payout = 400;
					    else 
							payout = model.paytable.get(i).getPayout() * 2;
					    
					    if (payout > bestpayout) {
					    	besthitindex = i;
							bestpayout = payout;
							r.getCard(cardindex).setCreditsWon(bestpayout);
							r.getCard(cardindex).setWinName(EBingoModel.this.paytable.get(i).getName());
					    }
						
						
					// Update the payout and winname of the card 
					} else if (EBingoModel.this.paytable.get(i).getPayout() > bestpayout) {
						besthitindex = i;
						bestpayout = EBingoModel.this.paytable.get(i).getPayout();
						r.getCard(cardindex).setCreditsWon(bestpayout);
						r.getCard(cardindex).setWinName(EBingoModel.this.paytable.get(i).getName());
					}
				
				// Find the red square locations if the outcome has a red square 
				//	(skip if in Regular Bingo mode or Treasure Island mode or the card already matches a win pattern)		
				} else if (model.getMode() != Mode.REGULAR && model.getMode() != Mode.TREASURE_ISLAND
						&& !r.getCard(cardindex).isWin && num1sintmp == num1sinwin - 1) {
					findRedSquares(cardindex, tmp, winpattern);
				}
			}
			
			if (besthitindex >= 0)
				// Increment hit count of the winning pattern by 1
				EBingoModel.this.incrementHits(besthitindex);
	      	
		}
		
		/**
		 * The method calculates the payout of each Tropical Treasures Bingo play
		 */
		private void calculateTTPayout() {
			int numhits = ttr.getHits().size();
			int numpicks = ttr.getNumPicks();
			int multiplier = (int)(ttr.getWager() / 0.05);
			
			double payout = 0;
			
			if (ttr.getNumHits() > 0) {
				for (int i = 0; i < model.ttpaytable.size(); i++) {
					TTPaytableEntry tpe = model.ttpaytable.get(i);
					
					if (tpe.getPick() == numpicks && tpe.getHits() == numhits) {
						payout = tpe.getPayout();
						
						// Check for any bonus wins
						if (ttr.getLastBall() == ttr.getLastHit()) {
							payout *= (ttr.numpicks < 10) ? 2 : 5;
							model.incrementBonusHits(i);
							ttr.setBonusWin(true);
						} else {
							model.incrementHits(i);
						}
						
						break;
					}
				}
				
				// Set the play payout
				ttr.setDollarWon(payout * multiplier);
			}
			
		}
		
		/**
		 * This method calculates the payout for the High Sticks bingo game per card per play
		 * 
		 * @param outcome     The game outcome integer on the current ball drawn.
		 * @param ballindex   The number of balls drawn so far.
		 * @param cardindex   The index of the card we are currently checking.
		 */
		private void calculateHSPayout(int outcome, int ballindex, int cardindex) {
			double payout = 0;
			int multiplier = (int)(hsr.getWager() / 0.01);
			
			// Check all winning patterns for any wins
			for (int i = 0; i < EBingoModel.this.paytable.size(); i++) {
				PaytableEntry pe = EBingoModel.this.paytable.get(i);
				int winpattern = pe.getWinPattern();
				int tmp = outcome & winpattern;
				
				int bonus_trigger_count = bit_count(winpattern) - bit_count(tmp);
				
				// If the integer representing the outcome matches the win pattern
				if (tmp == winpattern) {
					// Set winning card
					hsr.getCard(cardindex).setWin(true);
					
					// Only calculate regular payout if there are no wins on that card yet
					if (!pe.getWinCode().equals("bonus")
							&& hsr.getCard(cardindex).getDollarsWon() == 0) {
						hsr.getCard(cardindex).setWinName(pe.getName());
						payout = getHSRegularPayout(ballindex + 1) * multiplier;
						hsr.getCard(cardindex).setDollarsWon(payout);
						hsr.addDollarWon(payout);
							
					// If the card triggers bonus	
					} else if (pe.getWinCode().equals("bonus") 
							&& !hsr.getCard(cardindex).isBonusWin()) {
						hsr.getCard(cardindex).setBonusWin(true);
						hsr.getCard(cardindex).updateCornerColour(bonus_trigger_count);
					}
				}
				
				// Set the coloured corners if needed	
			    if (pe.getWinCode().equals("bonus") && !hsr.getCard(cardindex).isBonusWin()
						&& bonus_trigger_count <= 3 && bonus_trigger_count >= 0) {
					hsr.getCard(cardindex).updateCornerColour(bonus_trigger_count);
				}
			}
			
		}
		
		/**
		 * This method calculates the bonus payout of High Sticks bingo game
		 * 
		 */
		private void calculateHSBonusWin() {
			int multiplier = 0;
			int pick_left = 3;
			List<Integer> picks = new ArrayList<Integer>(9);
			double bonuspayout = 0;
			
			// Populate the list of picks and shuffle it
			for (int i = 0; i < 9; i++)
				picks.add(i + 1);
			Collections.shuffle(picks);
			
			while (pick_left > 0) {
				int slice = ran.nextInt(100) + 1;
				
				// Find super prize multipliers is the number in the picks list at the given index is 9
				if (picks.get(pick_left) == 9) {
					multiplier += getSuperHSBonusWin(slice);
				
				// Find regular prize multiplier
				} else {
					multiplier += getRegularHSBonusWin(slice);
				}	
				pick_left--;
			}
			
			bonuspayout = hsr.getWager() * hsr.getNumBonusWins() * multiplier;
			hsr.setBonusPayout(bonuspayout);
			hsr.addDollarWon(bonuspayout);
		}
		
		/**
		 * This method finds the High Sticks regular bonus multiplier base on the give chance
		 * @param slice		The randomly generated number that indicates the chance	
		 * @return			The multiplier
		 */
		private int getRegularHSBonusWin(int slice) {
			int multiplier = 0;
			
			Iterator<Map.Entry<Integer, Range>> entries = 
					EBingoModel.this.rbonustable.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<Integer, Range> entry = entries.next();
				
				if (entry.getValue().isInRange(slice)) 
					multiplier += entry.getKey(); 
			}
			
			return multiplier;
		}
		
		/**
		 * This method finds the High Sticks super bonus multiplier base on the give chance
		 * @param slice		The randomly generated number that indicates the chance	
		 * @return			The muliplier
		 */
		private int getSuperHSBonusWin(int slice) {
			int multiplier = 0;
			
			Iterator<Map.Entry<Integer, Range>> entries = 
					EBingoModel.this.sbonustable.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<Integer, Range> entry = entries.next();
				
				if (entry.getValue().isInRange(slice)) 
					multiplier += entry.getKey(); 
			}
			
			return multiplier;
		}
		
		/**
		 * This method calculates the payout of regular bingo 
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
		
		private double getHSRegularPayout(int ballscalled) {
			double payout = 0;
			
			for (int i = 0; i < EBingoModel.this.hspaytable.size(); i++) {
				HSPaytableEntry hspe = EBingoModel.this.hspaytable.get(i);
				
				if (hspe.getBallsCalled() == ballscalled) {
					payout = hspe.getPayout();
					EBingoModel.this.hshittable.set(i, EBingoModel.this.hshittable.get(i) + 1);
					break;
				}
			}
			
			return payout;
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

	// PaytableEntry classes
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
	
	public class TTPaytableEntry {
		private String winCode = "";
		private int picks = 0;
		private int hits = 0;
		private double payout = 0;
		
		public void setWinCode(String value) {
			this.winCode = value;
		}
		
		public void setPicks(int value) {
			this.picks = value;
		}
		
		public void setHits(int value) {
			this.hits = value;
		}
		
		public void setPayout(double value) {
			this.payout = value;
		}
		
		public String getWinCode() {
			return this.winCode;
		}
		
		public int getPick() {
			return this.picks;
		}
		
		public int getHits() {
			return this.hits;
		}
		
		public double getPayout() {
			return EBingoModel.this.roundTwoDecimals(this.payout);
		}
	}
	
	public class HSPaytableEntry {
		private String winCode = "";
		private int ballsCalled = 0;
		private double payout = 0;
		
		public void setWinCode(String value) {
			this.winCode = value;
		}
		
		public void setBallsCalled(int value) {
			this.ballsCalled = value;
		}
		
		public void setPayout(double value) {
			this.payout = value;
		}
		
		public String getWinCode() {
			return this.winCode;
		}
		
		public int getBallsCalled() {
			return this.ballsCalled;
		}
		
		public double getPayout() {
			return this.payout;
		}
	}
	
	// Block classes
	public class Block {
		private int numplays = 0;
		private int numcards = 1;
		private int currplay = 0;
		private double wager = 0;
		private int repeat = 0;
		private long blocknum = 0;
		
		private int currrepeat = 1;
		
		public void setNumPlays(int value) {
			this.numplays = value;
		}

		public void setNumCards(int value) {
			this.numcards = value;
		}
		
		public void setWager(double value) {
			this.wager = value;
		}
		
		public void setRepeat(int value) {
			this.repeat = value;
		}
		
		public void setBlockNum(long value) {
			this.blocknum = value;
		}
		
		public void setFixed(boolean value) {
			// To be implemented in HSBlock
		}
		
		public void incrementCurrRepeat() {
			this.currrepeat++;
		}
		
		public int getNumPlays() {
			return this.numplays;
		}
		
		public int getNumCards() {
			return this.numcards;
		}
		
		public double getWager() {
			return this.wager;
		}
		
		public int getRepeat() {
			return this.repeat;
		}
		
		public long getBlockNum() {
			return this.blocknum;
		}
		
		public boolean isFixed() {
			// To be implemented in HSBlock
			return true;
		}
		
		public int getCurrPlay() {
			return this.currplay;
		}
		
		public void setBankroll(double value) {
			// To be implemented in GRBlock
		}
		
		public double getBankroll() {
			// To be implemented in GRBlock
			return 0;
		}
		
		public boolean incrementCurrPlay(Result r) {
			return false;
			// To be implemented in GRBlock
		}
		
		public boolean incrementCurrPlay() {
			this.currplay++;
			return (this.currplay >= this.numplays);
		}
		
		// Reset at the end of each repeat
		public void reset() {
			this.currplay = 0;
			
		}
		
		public void setCurrBalance(double bankroll) {
			// To be implemented in GRBlock
		}
		
		public double getCurrBalance() {
			return 0;
			// To be implemented in GRBlock
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
	
	public class GRBlock extends Block {
		private double bankroll = 0;
		private double currbalance = 0;
		
		public GRBlock () {
			super();
		}
		
		public int getCurrSpin() {
			return super.currplay;
		}
		
		public void addCurrBalance(double value) {
			this.currbalance += value;
		}
		
		@Override
		public void setCurrBalance(double value) {
			this.currbalance = value;
		}
		
		public void placeWager() {
			currbalance -= super.wager * super.numcards;
		}
		
		@Override
		public void setBankroll(double value) {
			this.bankroll = value;
		}
		
		@Override
		public double getBankroll() {
			return this.bankroll;
		}
		
		@Override
		public double getCurrBalance() {
			return EBingoModel.this.roundTwoDecimals(this.currbalance);
		}
		
		public void updateCurrBalance(Result r) {
			// Update the total wins 
			EBingoModel.this.currgre.addWins(r.creditswon * r.wager);
			
			currbalance += r.creditswon * r.wager;
			
			// Update the peak balance
			if (currbalance > EBingoModel.this.currgre.currpeakbalance)
				EBingoModel.this.currgre.setCurrPeakBalance(currbalance);
			
			// Update the win/loss/ldw info
			if (r.creditswon > 0) {
				if ((r.creditswon * r.wager - r.getBet()) < 0 )
					EBingoModel.this.currgre.incrementLDWs();
				else 
					EBingoModel.this.currgre.incrementWins();
			} else {
				EBingoModel.this.currgre.incrementLosses();
			}
		}
		
		@Override
		public boolean incrementCurrPlay(Result r) {
			placeWager();
			
			if (this.currbalance < 0)
				return true;
			else {
				updateCurrBalance(r);
				super.currplay++;
				return false;
			}
		}
		
		@Override
		public void reset() {
			super.currplay = 0;
			currbalance = (double)this.bankroll;
		}
		
	}
	
	public class TTBlock {
		private int numplays = 0;
		private int currplay = 0;
		private double wager = 0;
		private int repeat = 0;
		private long blocknum = 0;
		private int picks = 0;
		boolean fixed = false;
		
		private int currrepeat = 1;
		
		public void setNumPlays(int value) {
			this.numplays = value;
		}

		public void setRepeat(int value) {
			this.repeat = value;
		}

		public void setWager(double value) {
			this.wager = value;
		}
		
		public void setPicks(int value) {
			this.picks = value;
		}
		
		public void setFixedCard(boolean value) {
			this.fixed = value;
		}
		
		public void setBlockNum(long value) {
			this.blocknum = value;
		}
		
		public void incrementCurrRepeat() {
			this.currrepeat++;
		}
		
		public int getNumPlays() {
			return this.numplays;
		}
		
		public int getPicks() {
			return this.picks;
		}
		
		public double getWager() {
			return EBingoModel.this.roundTwoDecimals(this.wager);
		}

		public boolean isFixedCard() {
			return this.fixed;
		}
		
		public long getBlockNum() {
			return this.blocknum;
		}
		
		public int getCurrPlay() {
			return this.currplay;
		}
		
		public int getRepeat() {
			return this.repeat;
		}
		
		public void setBankroll(double value) {
			// To be implemented in GRBlock
		}
		
		public double getBankroll() {
			// To be implemented in GRBlock
			return 0;
		}
		
		public boolean incrementCurrPlay(Result r) {
			return false;
			// To be implemented in GRBlock
		}
		
		public boolean incrementCurrPlay() {
			this.currplay++;
			return (this.currplay >= this.numplays);
		}
		
		// Reset at the end of each repeat
		public void reset() {
			this.currplay = 0;
			
		}
		
		public void setCurrBalance(double bankroll) {
			// To be implemented in GRBlock
		}
		
		public double getCurrBalance() {
			return 0;
			// To be implemented in GRBlock
		}
	}
	
	public class HSBlock extends Block {
		boolean fixed = false;
		
		
		public HSBlock() {
			super();
		}
		
		@Override
		public void setFixed(boolean value) {
			this.fixed = value;
		}
		
		@Override
		public boolean isFixed() {
			return this.fixed;
		}
	}
	
	/** Result classes **/
	public class Result {
		private int[] balls = new int[NUM_ON_CARDS];
		private List<Card> cards = new ArrayList<Card>();
		private int numcards = 0;
		
		private long recordNumber = 0;
		private long blockNumber = 0;
		private double wager = 0;
		
		private int creditswon = 0;
		private int redsquares = 0;
		private int numwinningcards = 0;
		private boolean isRedSquareFlashing = true;
		
		public Result(int cards) {
			numcards = cards;
		}

		public double getBet() {
			return this.wager * this.numcards;
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

		public double getWager() {
			return this.wager;
		}
		
		public void setWager(double d) {
			this.wager = d;
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
		
		public int getTotalRedSquares() {
			int total = 0;
			for (Card c : this.cards) {
				total += c.getNumRedSquares();
			}
			
			return total;
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

	public class TropicalTreasuresResult {
		private long recordNumber = 0;
		private long blockNumber = 0;
		private int numplays = 0;
		private double wager = 0;
		private int numpicks = 0;
		private boolean fixed = false;
		private boolean bonuswin = false;
		private double dollarwon = 0;
		
		private List<Integer> balls = new ArrayList<Integer>(21);
		private List<Integer> picks;
		private List<Integer> hits = new ArrayList<Integer>();
		
		public TropicalTreasuresResult(int picks, boolean fixed) {
			this.numpicks = picks;
			this.fixed = fixed;
			this.picks = new ArrayList<Integer>(picks);
		}

		public void setRecordNumber(long value) {
			this.recordNumber = value;
		}
		
		public void setBlockNumber(long value) {
			this.blockNumber = value;
		}
		
		public void setNumPlays(int value) {
			this.numplays = value;
		}
		
		public void setWager(double value) {
			this.wager = value;
		}
		
		public void setNumPicks(int value) {
			this.numpicks = value;
		}
		
		public void setFixed(boolean value) {
			this.fixed = value;
		}
		
		public void setDollarWon(double value) {
			this.dollarwon = value;
		}
		
		public void setBonusWin(boolean value) {
			this.bonuswin = value;
		}
		
		public void addBall(int value) {
			this.balls.add(value);
		}
		
		public void addPick(int value) {
			this.picks.add(value);
		}
		
		public void setPicks(List<Integer> values) {
			this.picks = values;
		}
		
		public void addHits(int value) {
			this.hits.add(value);
		}
		
		public long getRecordNumber() {
			return this.recordNumber;
		}
		
		public long getBlockNumber() {
			return this.blockNumber;
		}
		
		public int getNumPlay() {
			return this.numplays;
		}
		
		public double getWager() {
			return EBingoModel.this.roundTwoDecimals(this.wager);
		}
		
		public int getNumPicks() {
			return this.numpicks;
		}
		
		public boolean isFixed() {
			return this.fixed;
		}
		
		public double getDollarWon() {
			return EBingoModel.this.roundTwoDecimals(this.dollarwon);
		}

		public boolean getBonusWin() {
			return this.bonuswin;
		}
		
		public List<Integer> getBalls() {
			return this.balls;
		}
		
		public int getBall(int index) {
			return this.balls.get(index);
		}
		
		public int getLastBall() {
			return getBall(balls.size() - 1);
		}
		
		public List<Integer> getPicks() {
			return this.picks;
		}
		
		public int getPick(int index) {
			return this.picks.get(index);
		}
		
		public List<Integer> getHits() {
			return this.hits;
		}
		
		public int getHit(int index) {
			return this.hits.get(index);
		}
		
		public int getNumHits() {
			return this.hits.size();
		}
		
		public int getLastHit() {
			return getHit(hits.size() - 1);
		}
	}
	
	public class HighSticksResult {
		private long recordNumber = 0;
		private long blockNumber = 0;
		private int numplays = 0;
		private double wager = 0;
		private int numcards = 0;
		private boolean isFixed = false;

		private double dollarwon = 0;
		private double bonuspayout = 0;
		
		private List<Integer> balls = new ArrayList<Integer>(25);
		private List<Card> cards = new ArrayList<Card>();

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

		public int getNumplays() {
			return numplays;
		}

		public void setNumplays(int numplays) {
			this.numplays = numplays;
		}

		public double getWager() {
			return wager;
		}

		public void setWager(double wager) {
			this.wager = wager;
		}

		public int getNumcards() {
			return numcards;
		}

		public void setNumcards(int numcards) {
			this.numcards = numcards;
		}
		
		public boolean isFixed() {
			return isFixed;
		}

		public void setFixed(boolean isFixed) {
			this.isFixed = isFixed;		
		}
		
		public double getDollarWon() {
			return EBingoModel.this.roundTwoDecimals(dollarwon);
		}

		public void addDollarWon(double dollarwon) {
			this.dollarwon += dollarwon;
		}
		
		public double getBonusPayout() {
			return EBingoModel.this.roundTwoDecimals(bonuspayout);
		}
		
		public void setBonusPayout(double value) {
			this.bonuspayout = value;
		}
		
		public void addBall(int value) {
			this.balls.add(value);
		}
		
		public List<Integer> getBalls() {
			return this.balls;
		}
		
		public void addCard(Card c) {
			this.cards.add(c);
		}
		
		public void setCards(List<Card> cards) {
			this.cards = cards;
		}
		
		public Card getCard(int index) {
			return this.cards.get(index);
		}
		
		public List<Card> getCards() {
			return this.cards;
		}
		
		public int getNumBonusWins() {
			int count = 0;
			
			for (Card c : this.cards) {
				if (c.isBonusWin())
					count++;
			}
			
			return count;
		}
	}
	
	public class RegularResult {
		private long recordNumber = 0;
		private long blockNumber = 0;
		private double wager = 0;
		
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
		
		public double getWager() {
			return this.wager;
		}
		
		public void setWager(double value) {
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
		
		public List<Double> getPlayerWinAmount() {
			List<Double> winamounts = new ArrayList<Double>();
			
			for (int i : this.playerswon) {
				winamounts.add(this.players.get(i).getDollarWon());
			}
			
			return winamounts;
		}
		
		public Player getPlayer(int index) {
			return this.players.get(index);
		}
	}
	
	/** Player classes **/
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
	
	public class Card implements Cloneable {
		private long id = 0;
		private int creditswon = 0;
		private String winname = "";
		private short ti_id = 1;
		
		private boolean isWin = false;
		private short numredsquares = 0;
		private List<Byte> redsquarelocations = new ArrayList<Byte>();

		private int[] numbersoncard = new int[NUM_ON_CARDS];
		
		public long getID() {
			return id;
		}

		public short getTIID() {
			return this.ti_id;
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
			this.redsquarelocations.clear();
			this.numredsquares = 0;
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
		
		public void setTIID(short value) {
			this.ti_id = value;
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
		
		public void updateCornerColour(int value) {
			//To be implemented in HSCard class
		}
		
		public CornerColour getCornerColour() {
			//To be implemented in HSCard class
			return null;
		}
		
		public void setDollarsWon(double value) {
			//To be implemented in HSCard class
		}

		public boolean isBonusWin() {
			//To be implemented in HSCard class
			return false;
		}

		public void setBonusWin(boolean value) {
			//To be implemented in HSCard class
		}
		
		public void resetWins() {
			this.creditswon = 0;
			this.isWin = false;
			this.numredsquares = 0;
			this.redsquarelocations.clear();
		}
	}
	
	public class HSCard extends Card implements Cloneable{
		private CornerColour cc = CornerColour.NONE;
		private double dollarswon = 0;
		private boolean bonuswin = false;
		
		public void updateCornerColour(int value) {
			switch (value) {
			case 3:
				this.cc = CornerColour.GREEN;
				break;
			case 2:
				this.cc = CornerColour.YELLOW;
				break;
			case 1:
				this.cc = CornerColour.RED;
				break;
			case 0:
				this.cc = CornerColour.RAINBOW;
				break;
			}
		}
		
		@Override
		public CornerColour getCornerColour() {
			return this.cc;
		}
		
		@Override
		public void setDollarsWon(double value) {
			this.dollarswon = value;
		}
		
		@Override
		public double getDollarsWon() {
			return this.dollarswon;
		}
		
		@Override
		public void setBonusWin(boolean value) {
			this.bonuswin = value;
		}
		
		@Override
		public boolean isBonusWin() {
			return this.bonuswin;
		}
		
		@Override
		public void resetWins() {
			this.bonuswin = false;
			this.cc = CornerColour.NONE;
			this.dollarswon = 0;
			
			super.isWin = false;
			super.winname = "";
		}
	}
	
	public class BasicInfoEntry {
		private long blockID = 0;
		private int numplays = 0;
		private int numcards = 0;
		
		private long wins = 0;
		private long losses = 0;
		private long ldws = 0;
		private long pushes = 0;
		private long multiwins = 0;
		private long redsquares = 0;
		private long flashingRedSquares = 0;
		
		public BasicInfoEntry() {
			if (EBingoModel.this.currblockindex < EBingoModel.this.blocks.size()) {
				this.blockID = EBingoModel.this.blocks.get(currblockindex).getBlockNum();
				this.numplays = EBingoModel.this.blocks.get(currblockindex).getNumPlays();
				this.numcards = EBingoModel.this.blocks.get(currblockindex).getNumCards();
			}
		}
		
		public long getWins() {
			return this.wins;
		}
		
		public long getLosses() {
			return this.losses;
		}
		
		public long getPushes() {
			return this.pushes;
		}
		
		public long getLDWs() {
			return this.ldws;
		}
		
		public long getMultiWins() {
			return this.multiwins;
		}
		
		public long getRedSquares() {
			return this.redsquares;
		}
		
		public long getFlashingRedSquares() {
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
			double wagered = r.numcards * r.wager;
			int numwinningcards = 0;
			
			if (EBingoModel.this.getMode() == Mode.TREASURE_ISLAND) {
				wagered = r.wager * r.getCards().size();
			}
			
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
				this.addFlashingRedSquares(r.getTotalRedSquares());
			}
			this.addRedSquares(r.getTotalRedSquares());
		}
		
		public void flushBIE() {
			try {
				Database.flushBatch();
				if (EBingoModel.this.mode == Mode.LUCKY_CLOVER)
					Database.insertIntoTable(getLCBasicInfoDBname(), currbie, blocks.size());
				else if (EBingoModel.this.mode == Mode.OLD_GLORY)
					Database.insertIntoTable(getOGBasicInfoDBname(), currbie, blocks.size());
				else if (EBingoModel.this.mode == Mode.TREASURE_ISLAND)
					Database.insertIntoTable(EBingoModel.this.getTIBasicInfoDBname(), currbie, blocks.size());
					
				Database.flushBatch();
			} catch (Exception e) {
				log.writeLine("Inserting BasicInfoEntry encountered problem: " + e.getMessage());
				e.printStackTrace();
			} finally {
				EBingoModel.this.currbie = new BasicInfoEntry();
			}
		}
	}
	
	public class TTBasicInfoEntry {
		private long blockID = 0;
		private int numplays = 0;
		private int picks = 0;
		private boolean fixed = false;
		
		private long wins = 0;
		private long losses = 0;
		private long ldws = 0;
		private long pushes = 0;
		
		private double basepay = 0;
		private double bonuspay = 0;
		
		List<Long> basehits = new ArrayList<Long>(11);
		List<Long> bonushits = new ArrayList<Long>(11);
		
		public TTBasicInfoEntry() {
			if (EBingoModel.this.currblockindex < EBingoModel.this.ttblocks.size()) {
				this.blockID = EBingoModel.this.ttblocks.get(currblockindex).getBlockNum();
				this.numplays = EBingoModel.this.ttblocks.get(currblockindex).getNumPlays();
				this.picks = EBingoModel.this.ttblocks.get(currblockindex).getPicks();
				this.fixed = EBingoModel.this.ttblocks.get(currblockindex).isFixedCard();
			}
			
			for (int i = 0; i < 11; i++) {
				this.basehits.add((long) 0);
				this.bonushits.add((long) 0);
			}
		}
		
		public long getBlockID() {
			return this.blockID;
		}
		
		public int getNumPlays() {
			return this.numplays;
		}
		
		public int getPicks() {
			return this.picks;
		}
		
		public boolean isFixed() {
			return this.fixed;
		}
		
		public long getWins() {
			return this.wins;
		}
		
		public long getLosses() {
			return this.losses;
		}
		
		public long getLDWs() {
			return this.ldws;
		}
		
		public long getPushes() {
			return this.pushes;
		}
		
		public double getBasePay() {
			return EBingoModel.this.roundTwoDecimals(this.basepay);
		}
		
		public double getBonusPay() {
			return EBingoModel.this.roundTwoDecimals(this.bonuspay);
		}
		
		public long getBaseHit(int index) {
			return this.basehits.get(index);
		}
		
		public int getHitsSize() {
			return this.basehits.size();
		}
		
		public long getBonusHit(int index) {
			return this.bonushits.get(index);
		}
		
		public void addBasePay(double value) {
			this.basepay += value;
		}
		
		public void addBonusPay(double value) {
			this.bonuspay += value;
		}
		
		public void incrementBaseHits(int index) {
			this.basehits.set(index, basehits.get(index) + 1);
		}
		
		public void incrementBonusHits(int index) {
			this.bonushits.set(index, bonushits.get(index) + 1);
		}
		
		public void updateTTBIE(TropicalTreasuresResult ttr) {
			double wagered = ttr.numpicks * ttr.wager;
			int hit_index = ttr.hits.size();
			
			// update win/loss/ldw/push info
			if (ttr.dollarwon > 0) {
				if (ttr.dollarwon - wagered < 0) {
					this.ldws++;
				} else if (ttr.dollarwon - wagered == 0) {
					this.pushes++;
				} else {
					this.wins++;
				}				
			} else {
				this.losses++;
			}

			// update hits info
			if (ttr.bonuswin || 
					(ttr.getNumHits() > 0 && ttr.getLastBall() == ttr.getLastHit())) {
				this.incrementBonusHits(hit_index);
				this.addBonusPay(ttr.dollarwon);
			} else {
				this.incrementBaseHits(hit_index);
				this.addBasePay(ttr.dollarwon);
			}
		}
		
		public void flushTTBIE() {
			try {
				Database.flushBatch();
				Database.insertIntoTable(EBingoModel.this.getTTBasicInfoDBname(), currttbie, ttblocks.size());	
				Database.flushBatch();
			} catch (Exception e) {
				log.writeLine("Inserting BasicInfoEntry encountered problem: " + e.getMessage());
				e.printStackTrace();
			} finally {
				EBingoModel.this.currttbie = new TTBasicInfoEntry();
			}
		}
		
	}
	
	public class HSBasicInfoEntry {
		private long blockID = 0;
		private int numplays = 0;
		private int numcards = 0;
		private boolean fixed = false;
		
		private long wins = 0;
		private long losses = 0;
		private long ldws = 0;
		private long pushes = 0;
		private long numcc = 0;
		
		private double basepay = 0;
		private double bonuspay = 0;
		
		List<Long> corner_colours = new ArrayList<Long>(5);
		
		public HSBasicInfoEntry() {
			if (EBingoModel.this.currblockindex < EBingoModel.this.blocks.size()) {
				this.blockID = EBingoModel.this.blocks.get(currblockindex).getBlockNum();
				this.numplays = EBingoModel.this.blocks.get(currblockindex).getNumPlays();
				this.numcards = EBingoModel.this.blocks.get(currblockindex).getNumCards();
				this.fixed = EBingoModel.this.blocks.get(currblockindex).isFixed();
			}
			
			for (int i = 0; i < 5; i++) {
				this.corner_colours.add((long) 0);
			}
		}
		
		public long getBlockID() {
			return blockID;
		}

		public int getNumplays() {
			return numplays;
		}

		public int getNumcards() {
			return numcards;
		}

		public boolean isFixed() {
			return fixed;
		}

		public long getWins() {
			return wins;
		}

		private void incrementWins() {
			this.wins++;
		}

		public long getLosses() {
			return losses;
		}

		private void incrementLosses() {
			this.losses++;
		}

		public long getLDWs() {
			return ldws;
		}

		private void incrementLDWs() {
			this.ldws++;
		}

		public long getPushes() {
			return pushes;
		}

		private void incrementPushes() {
			this.pushes++;
		}

		public long getNumCC() {
			return this.numcc;
		}
		
		private void incrementNumCC() {
			this.numcc++;
		}
		
		public double getBasePay() {
			return basepay;
		}

		private void addBasePay(double basepay) {
			this.basepay += basepay;
		}

		public double getBonuspay() {
			return bonuspay;
		}

		private void addBonusPay(double bonuspay) {
			this.bonuspay += bonuspay;
		}
		
		public List<Long> getCornerColours() {
			return corner_colours;
		}

		public long getCornerColour(int index) {
			return this.corner_colours.get(index);
		}
		
		private void incrementCornerColours(int index) {
			this.corner_colours.set(index, corner_colours.get(index) + 1);
		}
		
		public void updateHSBIE(HighSticksResult hsr) {
			double wagered = hsr.getNumcards() * hsr.getWager();
			
			// update win/loss/ldw/push and base/bonus payout info
			if (hsr.getDollarWon() > 0) {
				if (hsr.getDollarWon() - wagered < 0) {
					this.incrementLDWs();
				} else if (hsr.getDollarWon() - wagered == 0) {
					this.incrementPushes();
				} else {
					this.incrementWins();
				}
				
				if (hsr.getBonusPayout() > 0) {
					this.addBonusPay(hsr.getBonusPayout());
					this.addBasePay(hsr.getDollarWon() - hsr.getBonusPayout());
				} else {
					this.addBasePay(hsr.getDollarWon());
				}
				
			} else {
				this.incrementLosses();
			}

			// update coloured corner counts and bonus counts
			boolean hasColouredCorners = false;
			for (Card c : hsr.getCards()) {
				switch (c.getCornerColour()) {
				case NONE:
					this.incrementCornerColours(0);
					break;
				case GREEN:
					this.incrementCornerColours(1);
					hasColouredCorners = true;
					break;
				case YELLOW:
					this.incrementCornerColours(2);
					hasColouredCorners = true;
					break;
				case RED:
					this.incrementCornerColours(3);
					hasColouredCorners = true;
					break;
				case RAINBOW:
					this.incrementCornerColours(4);
					hasColouredCorners = true;
					break;
				}
			}
			
			if (hasColouredCorners)
				incrementNumCC();
		}
		
		public void flushHSBIE() {
			try {
				Database.flushBatch();
				Database.insertIntoTable(EBingoModel.this.getHSBasicInfoDBname(), currhsbie, blocks.size());	
				Database.flushBatch();
			} catch (Exception e) {
				log.writeLine("Inserting BasicInfoEntry encountered problem: " + e.getMessage());
				e.printStackTrace();
			} finally {
				EBingoModel.this.currhsbie = new HSBasicInfoEntry();
			}
		}
		
	}
	
	public class PercentLosersEntry {
		private long id = 0;
		private int numplays = 0;
		private int numcards = 0;
		private int repeats = 0;
		
		private long wins = 0;
		private long ldws = 0;
		private long losses = 0;
		
		private double initBalance = 0;
		private double bet = 0;
		private double wager = 0;
		private int currIndex = 0;
		private double lpavg = 0;
		private double lpmedian = 0;
		private double lpSD = 0;
		private long rsavg = 0;
		private long rsmedian = 0;
		private long rsSD = 0;
		private int rsplayavg = 0;
		private int rs_nonflashingplayavg = 0;
		
		private List<Integer> losspercentages = new ArrayList<Integer>();
		private List<Range> ranges = new ArrayList<Range>();
		private List<Double> balances = new ArrayList<Double>();
		private List<Long> redsquares = new ArrayList<Long>();
		private List<Integer> rsplays = new ArrayList<Integer>();  
		private List<Integer> rs_noflashingplays = new ArrayList<Integer>();
		
		public PercentLosersEntry() {
			if (EBingoModel.this.currblock != null) {
				this.id = currblock.blocknum;
				this.numplays = EBingoModel.this.currblock.numplays;
				
				if (EBingoModel.this.getMode() == Mode.TREASURE_ISLAND) {
					this.numcards = 2;
				}
					this.numcards = EBingoModel.this.currblock.numcards;
				
				this.wager = EBingoModel.this.currblock.wager;
				this.repeats = EBingoModel.this.currblock.repeat;
			
				this.initBalance = numplays * numcards * EBingoModel.this.currblock.wager;
				this.bet = numcards * EBingoModel.this.currblock.wager;
				
				for (int i = 0; i < this.repeats; i++) {
					this.balances.add(initBalance);
					this.redsquares.add((long)0);
					this.rsplays.add(0);
					this.rs_noflashingplays.add(0);
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
		
		public long getWins() {
			return this.wins;
		}
		
		private void incrementCurrBalanceIndex() {
			this.currIndex++;
		}
		
		private void incrementWins() {
			this.wins++;
		}
		
		public long getLDWs() {
			return this.ldws;
		}
		
		private void incrementLDWs() {
			this.ldws++;
		}
		
		public long getLosses() {
			return this.losses;
		}
		
		private void incrementLosses() {
			this.losses++;
		}
		
		public double getAvgLossBalance() {
			return this.lpavg;
		}
		
		public List<Long> getRedSquares() {
			return this.redsquares;
		}
		
		public long getRedSquare(int index) {
			return this.redsquares.get(index);
		}
		
		public void addRedSquare(int value) {
			this.redsquares.set(currIndex, redsquares.get(currIndex) + value);
		}
		
		public double getMedianLossBalance() {
			return this.lpmedian;
		}
		
		public double getLossBalanceSD() {
			return this.lpSD;
		}
		
		public long getAvgRedSquares() {
			return this.rsavg;
		}
		
		public long getMedianRedSquares() {
			return this.rsmedian;
		}
		
		public long getRedSquaresSD() {
			return this.rsSD;
		}
		
		public int getAvgRSPlay() {
			return this.rsplayavg;
		}
		
		public int getAvgRSNPlay() {
			return this.rs_nonflashingplayavg;
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
		
		public void incrementRSPlay() {
			this.rsplays.set(currIndex, rsplays.get(currIndex) + 1);
		}
		
		public void incrementRSNPlay() {
			this.rs_noflashingplays.set(currIndex, this.rs_noflashingplays.get(currIndex) + 1);
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
			// If there is only one card played in Treasure Island mode
			if (EBingoModel.this.getMode() == Mode.TREASURE_ISLAND) {
				// If there is only one card played in Treasure Island mode
				if (r.getCards().size() == 1) 
					this.bet = 1 * this.wager;
				// If there two cards are played in Treasure Island mode
				else 
					this.bet = 2 * this.wager;
			}
			
			// Update balance
			this.addBalance((r.creditswon * this.wager) -  this.bet);
			
			// Update flashing red squares counts
			if (r.redsquares > 0) {
				this.incrementRSNPlay();
				
				if (r.isRedSquareFlashing) {
					this.addRedSquare(r.redsquares);
					this.incrementRSPlay();
				}
				
			}
			
			// Update win/lose and ldws
			if (r.creditswon > 0) {
				if (((r.creditswon * this.wager) - this.bet) >= 0)
					this.incrementWins();
				else this.incrementLDWs();
			} else {
				this.incrementLosses();
			}
			
			// If one repeat is completed
			if (EBingoModel.this.repeatcomplete) {
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
			calculateAvgRSPlays();
			
			try {
				Database.insertIntoTable(EBingoModel.this.getPercentLosersDBTableName(),
						this);
				Database.flushBatch();
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
		
		private void calculateAvgRSPlays() {
			long total = 0;
			long total2 = 0;
			
			for (int i = 0; i < repeats; i++) {
				total += this.rsplays.get(i);
				total2 += this.rs_noflashingplays.get(i);
			}
			
			this.rsplayavg = (int) (total / repeats);
			this.rs_nonflashingplayavg = (int)(total2 / repeats);
		}
		
		private void calculateAvgRedSquares() {
			long total = 0;
			
			for (long l: this.redsquares)
				total += l;
			
			this.rsavg = total / repeats;
		}
		
		private void calculateMedianRedSquares() {
			List<Long> sortedrs = this.redsquares;
			Collections.sort(sortedrs);
			
			int mid = sortedrs.size() / 2;
			
			this.rsmedian = (sortedrs.size() % 2 == 1) ?
					sortedrs.get(mid) :
						(sortedrs.get(mid - 1) + sortedrs.get(mid)) /2;
		}
		
		private void calculateRedSquaresSD() {
			long rssd = 0;
		
			for (long l : this.redsquares)
				rssd += (rsavg - l) * (rsavg - l);
			
			rssd /= this.redsquares.size();
			this.rsSD = (int)Math.sqrt(rssd);
		}
	}
	
	public class GamblersRuinEntry {
		private long id = 0;
		private int numcards = 0;
		private double bet = 0;
		
		private long totalplays = 0;
		private double totalwin = 0;
		private double paybackpercentage = 0;
		
		private int numplays = 0;
		private double currpeakbalance = 0;
		
		private int wins = 0;
		private int ldws = 0;
		private int losses = 0;
		
		private int avgplay = 0;
		private int medianplay = 0;
		private int playsd = 0;
		private int maxplay = 0;
		
		private double avgpb = 0;
		private double medianpb = 0;
		private double pbsd = 0;
		private double maxpb = 0;
		
		private List<Range> playranges = new ArrayList<Range>();
		private List<Integer> plays = new ArrayList<Integer>();
		
		private List<Range> peakbalanceranges = new ArrayList<Range>();
		private List<Double> peakbalances = new ArrayList<Double>();
		
		private List<Integer> allplays = new ArrayList<Integer>();
		private List<Double> allpbs = new ArrayList<Double>();
		
		public GamblersRuinEntry() {
			if (EBingoModel.this.currblock != null) {
				this.id = EBingoModel.this.currblock.blocknum;
				this.numcards = EBingoModel.this.currblock.numcards;
				this.bet = numcards * EBingoModel.this.currblock.wager;
				this.currpeakbalance = EBingoModel.this.currblock.getBankroll();
			
				// Initialize the ranges
				int low = 0;
				int increment = 100;
				for (int i = 0; i < 12; i++) {
					int high = low + increment;
					Range r = new Range(low, high);
					low = high;
					playranges.add(i, r);
				}
				increment = 1000;
				for (int i = 12; i < 20; i++) {
					int high = low + increment;
					Range r = new Range(low, high);
					low = high;
					playranges.add(i, r);
				}
				
				increment = 10000;
				for (int i = 20; i < 24; i++) {
					int high = low + increment;
					Range r = new Range(low, high);
					low = high;
					playranges.add(i, r);
				}
				playranges.add(24, new Range(low, low));
				
				
				low = 100;
				increment = 100;
				peakbalanceranges.add(0, new Range(low, low));
				for (int i = 1; i < 10; i++) {
					int high = low + increment;
					Range r = new Range(low, high);
					low = high;
					peakbalanceranges.add(i, r);
				}
				increment = 1000;
				for (int i = 10; i < 14; i++) {
					int high = low + increment;
					Range r = new Range(low, high);
					low = high;
					peakbalanceranges.add(i, r);
				}
				peakbalanceranges.add(14, new Range(low, low));
				
				// Initialize the array lists
				for (int i = 0; i < playranges.size(); i++)
					this.plays.add(0);
				
				for (int i = 0; i < peakbalanceranges.size(); i++)
					this.peakbalances.add(0d);
			}
		}
		
		public void incrementLosses() {
			this.losses++;
		}

		public void incrementWins() {
			this.wins++;
		}

		public void incrementLDWs() {
			this.ldws++;
		}

		public void setCurrPeakBalance(double value) {
			this.currpeakbalance = value;
		}

		public void addWins(double value) {
			this.totalwin += value;
		}

		public void resetBalance() {
			this.currpeakbalance = (double)EBingoModel.this.currblock.getBankroll();
		}

		public long getID() {
			return this.id;
		}
		
		public int getNumCards() {
			return this.numcards;
		}
		
		public long getTotalPlays() {
			return this.totalplays;
		}
		
		public double getTotalBalance() {
			return this.totalwin;
		}
		
		public double getPayBackPercentage() {
			return this.paybackpercentage;
		}
		
		public int getWins() {
			return this.wins;
		}
		
		public int getLDWs() {
			return this.ldws;
		}
		
		public int getLosses() {
			return this.losses;
		}
		
		public int getAvgPlay() {
			return this.avgplay;
		}
		
		public int getMedianPlay() {
			return this.medianplay;
		}
		
		public int getPlaySD() {
			return this.playsd;
		}
		
		public int getMaxPlay() {
			return this.maxplay;
		}
		
		public double getAvgPB() {
			return this.avgpb;
		}
		
		public double getMedianPB() {
			return this.medianpb;
		}
		
		public double getPBSD() {
			return this.pbsd;
		}
		
		public double getMaxPB() {
			return this.maxpb;
		}
		
		public List<Range> getPlayRanges() {
			return this.playranges;
		}
		
		public Range getPlayRange(int index) {
			return this.playranges.get(index);
		}
		
		public int getPlay(int index) {
			return this.plays.get(index);
		}
		
		public List<Range> getPBRanges() {
			return this.peakbalanceranges;
		}
		
		public Range getPBRange(int index) {
			return this.peakbalanceranges.get(index);
		}
		
		public double getPB(int index) {
			return this.peakbalances.get(index);
		}
		
		public void incrementNumSpins(int index) {
			this.plays.set(index, plays.get(index) + 1);
		}
		
		public void incrementPeakBalances(int index) {
			this.peakbalances.set(index, peakbalances.get(index) + 1);
		}
		
		/**
		 * This method is called at the end of each block repeat and at the each of each block
		 */
		public void updateGRE() {
			this.numplays = EBingoModel.this.currblock.getCurrPlay() + 1;
			this.totalplays += numplays;
			
			// Update spin ranges
			for (int i = 0; i < playranges.size(); i++) {
				if (playranges.get(i).isInRange(numplays)) {
					this.incrementNumSpins(i);
					break;
				}
			}
			
			// Update peak balance ranges
			for (int i = 0; i < peakbalanceranges.size(); i++) {
				if (peakbalanceranges.get(i).isPeakBalanceInRange(currpeakbalance)) {
					this.incrementPeakBalances(i);
					break;
				}
			}
			
			
			this.allplays.add(numplays);
			this.allpbs.add(currpeakbalance);
			
			// If a block is completed
			if (EBingoModel.this.blockcomplete) {
				calculateMedians();
				calculateAverages();
				calculateSDs();
				updateMax();
				calculatePaybackPercentage();
				
				try {
					Database.flushBatch();
					Database.insertIntoTable(EBingoModel.this.getGamblersRuinDBname(), this);
					Database.flushBatch();
				} catch (Exception e) {
					EBingoModel.this.log.writeLine("Inserting GRE encountered problem: "
							+ e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		private void calculateMedians() {
			Collections.sort(allplays);
			Collections.sort(allpbs);
			
			int mid_play = allplays.size() / 2;
			int mid_pb = allpbs.size() / 2;
			
			// Get median of num of plays
			this.medianplay = (allplays.size() % 2 == 1) ? allplays.get(mid_play) :
				(allplays.get(mid_play - 1) + allplays.get(mid_play)) / 2;
			
			// Get median of peak balances
			this.medianpb = (allpbs.size() % 2 == 1) ? allpbs.get(mid_pb) :
				(allpbs.get(mid_pb - 1) + allpbs.get(mid_pb)) / 2;
		}
		
		private void calculateAverages() {
			int totalplay = 0;
			int totalpb = 0;
			
			// Calculate avg for num of plays
			for (int i : allplays) 
				totalplay += i;
			this.avgplay = totalplay / allplays.size();
			
			// Calculate SD for peak balances
			for (double d : allpbs) 
				totalpb += d;
			this.avgpb = totalpb / allpbs.size();
		}
		
		private void calculateSDs() {
			long sdplays = 0;
			double sdpb = 0;
			
			// Calculate SD for num of plays
			for (int i : this.allplays)
				sdplays += (this.avgplay - i) * (this.avgplay - i);
			sdplays /= this.allplays.size();
			this.playsd = (int)Math.sqrt(sdplays);
			
			// Calculate SD for peak balances
			for (double d : this.allpbs) 
				sdpb += (this.avgpb - d) * (this.avgpb - d);
			sdpb /= this.allpbs.size();
			this.pbsd = (int)Math.sqrt(sdpb);
		}
		
		private void updateMax() {
			this.maxplay = Collections.max(allplays);
			this.maxpb = Collections.max(allpbs);
		}
		
		private void calculatePaybackPercentage() {
			this.paybackpercentage = (float)(this.totalwin / (this.totalplays * this.bet));
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
		
		public boolean isInRange(int num) {
			if (low == high)
				return (num <= low);
			else 
				return (num > low && num <= high);
		}
		
		public boolean isLossPercentageInRange(double percentage) {
			// when loss percentage is [-9, infinity)
			if (low == -9 && high == -9)
				return (percentage <= low);
			// When loss percentage is between [0, -9)
			else 
				return (percentage <= high && percentage > low);
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
	
	public final float roundTwoDecimals(float f) {
		return Float.valueOf(twoDForm.format(f));
	}
	
}

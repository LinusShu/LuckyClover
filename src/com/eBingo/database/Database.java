package com.eBingo.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eBingo.EBingoModel.BasicInfoEntry;
import com.eBingo.EBingoModel.Block;
import com.eBingo.EBingoModel.Card;
import com.eBingo.EBingoModel.GamblersRuinEntry;
import com.eBingo.EBingoModel.Mode;
import com.eBingo.EBingoModel.PaytableEntry;
import com.eBingo.EBingoModel.PercentLosersEntry;
import com.eBingo.EBingoModel.Player;
import com.eBingo.EBingoModel.Range;
import com.eBingo.EBingoModel.RegularResult;
import com.eBingo.EBingoModel.Result;
import com.eBingo.EBingoModel.TTBlock;
import com.eBingo.EBingoModel.TTPaytableEntry;
import com.eBingo.EBingoModel.TropicalTreasuresResult;

public class Database {

	public static String DEFAULT_DB_NAME = "EBingoResDB";
	
	private static String dbName = DEFAULT_DB_NAME;
	private static String URL = "jdbc:derby:" + dbName + ";create=true";
	private static final String URL_SHUTDOWN = "jdbc:derby:;shutdown=true";
	private static final int MAX_BATCH_LIMIT = 1000;
	
	private static Connection conn = null;
	private static List<String> listOfTables;
	private static int batchRequests = 0;
	private static PreparedStatement st = null;
	
	
	public static void setMode(Mode m) {
	}
	
	public static void setDBName(String name) {
		Database.dbName = name;
		Database.URL = "jdbc:derby:" + dbName + ";create=true";
	}
	
	public static String getDBname() {
		return dbName;
	}
	
	private static void populateListofTables() throws SQLException {
		listOfTables = new ArrayList<String>();
		DatabaseMetaData md = conn.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		while (rs.next()) {
			listOfTables.add(rs.getString(3).toUpperCase());
		}
	}
	
	/**
	 * Creates a database connection by implicitly loading the derby driver 
	 * and connecting to the database.
	 * 
	 * @throws Exception
	 */
	public static void createConnection() throws Exception {
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		Class.forName(driver).newInstance();
		
		conn = DriverManager.getConnection(Database.URL);
		conn.setAutoCommit(false);
	}
	
	/**
	 * Always shutdown the database when done;
	 * improves loading time for next startup.
	 * 
	 * @throws SQLException
	 */
	public static void shutdownConnection() throws SQLException {
		try {
			if (st != null) {
				st.close();
				st = null;
			}
			
			conn.commit();
			DriverManager.getConnection(URL_SHUTDOWN);
		} catch (SQLException e) {
			if (!e.getSQLState().equals("XJ015")) {
				throw e;
			}
		}
	}

	/**
	 * Creates a table in the DB if it doesn't exist already
	 * 
	 * @param tableName: name of the table to create
	 * @param query: query to create the table
	 * @throws SQLException
	 */
	private static void createTable(String tableName, String query) throws SQLException {
		tableName = tableName.toUpperCase();
		
		if (!Database.doesTableExist(tableName)) {
			Statement st = null;
			
			try {
				st = conn.createStatement();
				st.executeUpdate(query);
			} catch (SQLException e) {
				throw e;
			} finally {
				if (st != null) {
					st.close();
					listOfTables.add(tableName);
				}
			}
		} else return;
	}
	
	/**
	 * Inserts the paytable entries read from the config file
	 * 
	 * @param tableName: name of the paytable DB table
	 * @param pe: the paytable entry to be inserted
	 * @throws SQLException
	 */
	public static void insertIntoTable(String tableName, PaytableEntry pe, int hits)
		throws SQLException {
			tableName = tableName.toUpperCase();
			
			// Create table if does not exist
			if(!Database.doesTableExist(tableName)) {
				String query = "create table " + tableName 
						+ " (ID integer NOT NULL, "
						+ "NAME varchar(60) NOT NULL, "
						+ "WINPATTERN varchar(30) NOT NULL, "
						+ "PAYOUT integer NOT NULL, "
						+ "HITS integer NOT NULL)";
				
				Database.createTable(tableName, query);
			}
			
			// Add entry to the table if table already exists
			String query = "insert into " + tableName
					+ " (ID, NAME, WINPATTERN, PAYOUT, HITS) "
					+ "values(?, ?, ?, ?, ?)";
			
			try {
				if (st == null)
					st = conn.prepareStatement(query);
				
				st.setInt(1, pe.getEntryID());
				st.setString(2, pe.getName());
				st.setString(3, pe.getBinaryWinPattern());
				st.setInt(4, pe.getPayout());
				st.setInt(5, hits);
				
				st.addBatch();
				batchRequests++;
				
				if (batchRequests >= MAX_BATCH_LIMIT) {
					batchRequests = 0;
					st.executeBatch();
				}
				
			} catch (SQLException e) {
				throw e;
			}
	}
	
	/**
	 * Inserts the Tropical Treasures Bingo paytable entries read from the config file
	 * 
	 * @param tableName: name of the paytable DB table
	 * @param pe: the paytable entry to be inserted
	 * @param hit: the hit count for non-bonus wins
	 * @param bonushit: the hit count for bonus wins
	 * @throws SQLException
	 */
	public static void insertIntoTable(String tableName, TTPaytableEntry tpe, int hit, int bonushit)
			throws SQLException {
				tableName = tableName.toUpperCase();
				
				// Create table if does not exist
				if(!Database.doesTableExist(tableName)) {
					String query = "create table " + tableName 
							+ " (ID varchar(10) NOT NULL, "
							+ "PICKS integer NOT NULL, "
							+ "HITS integer NOT NULL, "
							+ "PAYOUT float NOT NULL, "
							+ "REGULAR_HITS integer NOT NULL, "
							+ "BONUS_HITS integer NOT NULL)";
					
					Database.createTable(tableName, query);
				}
				
				// Add entry to the table if table already exists
				String query = "insert into " + tableName
						+ " (ID, PICKS, HITS, PAYOUT, REGULAR_HITS, BONUS_HITS) "
						+ "values(?, ?, ?, ?, ?, ?)";
				
				try {
					if (st == null)
						st = conn.prepareStatement(query);
					
					st.setString(1, tpe.getWinCode());
					st.setInt(2, tpe.getPick());
					st.setInt(3, tpe.getHits());
					st.setFloat(4, tpe.getPayout());
					st.setInt(5, hit);
					st.setInt(6, bonushit);
					
					st.addBatch();
					batchRequests++;
					
					if (batchRequests >= MAX_BATCH_LIMIT) {
						batchRequests = 0;
						st.executeBatch();
					}
					
				} catch (SQLException e) {
					throw e;
				}
		}
	
	/**
	 * Inserts the blocks information read from the block file
	 * 
	 * @param tableName: name of the blocks DB table
	 * @param b: the block to be inserted
	 * @throws SQLException
	 */
	public static void insertIntoTable(String tableName, Block b) throws SQLException{
		tableName = tableName.toUpperCase();
		
		// Create the table if does not exist yet
		if (!Database.doesTableExist(tableName)) {
			String query = "create table " + tableName 
					+ " (BLOCKNUMBER bigint NOT NULL, "
					+ "NUMOFPLAYS integer NOT NULL, "
					+ "NUMOFCARDS integer NOT NULL, "
					+ "WAGER double NOT NULL)";
			
			Database.createTable(tableName, query);
		}
		
		// Insert the block if table already exists
		String query = "insert into " + tableName
				+ " (BLOCKNUMBER, NUMOFPLAYS, NUMOFCARDS, WAGER) "
				+ "values(?, ?, ?, ?)";
		
		try {
			if (st == null) 
				st = conn.prepareStatement(query);
			
			st.setLong(1, b.getBlockNum());
			st.setInt(2, b.getNumPlays());
			st.setInt(3, b.getNumCards());
			st.setDouble(4, b.getWager());
			
			st.addBatch();
			batchRequests++;
			
			if (batchRequests >= MAX_BATCH_LIMIT) {
				batchRequests = 0;
				st.executeBatch();
			}
		
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Inserts the blocks information read from the block file
	 * 
	 * @param tableName: name of the blocks DB table
	 * @param b: the block to be inserted
	 * @throws SQLException
	 */
	public static void insertIntoTable(String tableName, TTBlock b) throws SQLException{
		tableName = tableName.toUpperCase();
		
		// Create the table if does not exist yet
		if (!Database.doesTableExist(tableName)) {
			String query = "create table " + tableName 
					+ " (BLOCKNUMBER bigint NOT NULL, "
					+ "NUMOFPLAYS integer NOT NULL, "
					+ "FIXEDCARD boolean NOT NULL, "
					+ "PICKS integer NOT NULL, "
					+ "WAGER float NOT NULL)";
			
			Database.createTable(tableName, query);
		}
		
		// Insert the block if table already exists
		String query = "insert into " + tableName
				+ " (BLOCKNUMBER, NUMOFPLAYS, FIXEDCARD, PICKS, WAGER) "
				+ "values(?, ?, ?, ?, ?)";
		
		try {
			if (st == null) 
				st = conn.prepareStatement(query);
			
			st.setLong(1, b.getBlockNum());
			st.setInt(2, b.getNumPlays());
			st.setBoolean(3, b.isFixedCard());
			st.setInt(4, b.getPicks());
			st.setFloat(5, b.getWager());
			
			st.addBatch();
			batchRequests++;
			
			if (batchRequests >= MAX_BATCH_LIMIT) {
				batchRequests = 0;
				st.executeBatch();
			}
		
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Inserts the result into the table
	 * @param tableName	The name of the database table
	 * @param r	 The Result object
	 * @throws SQLException
	 */
	public static void insertIntoTable(String tableName, Result r) throws SQLException {
		tableName = tableName.toUpperCase();
		
		String balls = "";
		String ballsQ = "";
		String ballsI = "";
		
		String cards = "";
		String cardsQ = "";
		String cardsI = "";
		
		// Create the table if does not exist yet
		if (!Database.doesTableExist(tableName)) {
			
			// Prepare balls query string
			for (int i = 0; i < r.getBalls().length; i++) {
				balls += ", BALL_" + i + " integer NOT NULL";
				ballsI += ", BALL_" + i;
				ballsQ += ", ?";
			}
			
			// Prepare cards query string
			for (int i = 0; i < r.getCards().size(); i++) {
				cards += ", CARD" + i + "_ID" + " bigint NOT NULL"
						+ ", CARD" + i + "_DOLLARWON" + " double NOT NULL"
						+ ", CARD" + i + "_WINNAME" + " varchar(30) NOT NULL"
						+ ", CARD" + i + "_REDSQUARES" + " integer NOT NULL"
						+ ", CARD" + i + "_RSLOCATIONS" + " varchar(50) NOT NULL";
				
				
				cardsI += ", CARD" + i + "_ID" 
						+ ", CARD" + i + "_DOLLARWON"
						+ ", CARD" + i + "_WINNAME"
						+ ", CARD" + i + "_REDSQUARES" 
						+ ", CARD" + i + "_RSLOCATIONS";
				
				cardsQ += ", ?, ?, ?, ?, ?";
				
				for (int j = 0; j < r.getCard(i).getNumbersOnCard().length; j++) {
					cards += ", CARD" + i + "_NUM" + j + " integer NOT NULL";
					cardsI += ", CARD" + i + "_NUM" + j;
					cardsQ += ", ?";
					
				}
				
			}
			
			String query = "create table " + tableName 
					+ " (PLAYID bigint NOT NULL, "
					+ "BLOCKID bigint NOT NULL, "
					+ "NUMOFCARDS integer NOT NULL, "
					+ "WAGER float NOT NULL, "
					+ "TOTALWIN double NOT NULL"
					+ balls + cards + ")";
			
			Database.createTable(tableName, query);
		}
		
		// Insert the result if table already exists
		String query = "insert into " + tableName
				+ " (PLAYID, BLOCKID, NUMOFCARDS, WAGER, TOTALWIN" + ballsI + cardsI + ") "
				+ "values(?, ?, ?, ?, ?" + ballsQ + cardsQ + ")";
		
		try {
			if (st == null) 
				st = conn.prepareStatement(query);
			
			st.setLong(1, r.getRecordNumber());
			st.setLong(2, r.getBlockNumber());
			st.setInt(3, r.getNumCards());
			st.setFloat(4, r.getWager());
			st.setDouble(5, r.getDollarsWon());
			
			int index = 6;
			
			for (int i = 0; i < r.getBalls().length; i++) {
				st.setInt(index, r.getBall(i));
				index++;
			}
			
			for (int i = 0; i < r.getCards().size(); i++) {
				Card c = r.getCard(i);
				
				st.setLong(index, c.getID());
				index++;
				
				st.setDouble(index, c.getDollarsWon());
				index++;
				
				st.setString(index, c.getWinName());
				index++;
				
				st.setShort(index, c.getNumRedSquares());
				index++;
				
				st.setString(index, c.getRedSquareLocations().toString());
				index++;
				
				for (int j = 0; j < c.getNumbersOnCard().length; j++) {
					st.setInt(index, c.getNumberOnCard(j));
					index++;
				}
			}
			
			st.addBatch();
			batchRequests++;
			
			if (batchRequests >= MAX_BATCH_LIMIT) {
				batchRequests = 0;
				st.executeBatch();
			}
		
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Inserts the result into the table
	 * @param tableName	The name of the database table
	 * @param r	 The TropicalTreasuresResult object
	 * @throws SQLException
	 */
	public static void insertIntoTable(String tableName, TropicalTreasuresResult ttr) 
			throws SQLException {
		tableName = tableName.toUpperCase();
		
		// Create the table if does not exist yet
		if (!Database.doesTableExist(tableName)) {
			
			String query = "create table " + tableName 
					+ " (PLAYID bigint NOT NULL, "
					+ "BLOCKID bigint NOT NULL, "
					+ "NUMOFPLAYS integer NOT NULL, "
					+ "WAGER float NOT NULL, "
					+ "PICKS varchar(50) NOT NULL, "
					+ "BALLS varchar(100) NOT NULL, "
					+ "HITS varchar(50) NOT NULL, "
					+ "BONUSWIN boolean NOT NULL, "
					+ "WINAMOUNT double NOT NULL)";
			
			Database.createTable(tableName, query);
		}
		
		// Insert the result if table already exists
		String query = "insert into " + tableName
				+ " (PLAYID, BLOCKID, NUMOFPLAYS, WAGER, PICKS, BALLS, HITS, BONUSWIN, WINAMOUNT) "
				+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		try {
			if (st == null) 
				st = conn.prepareStatement(query);
			
			st.setLong(1, ttr.getRecordNumber());
			st.setLong(2, ttr.getBlockNumber());
			st.setInt(3, ttr.getNumPlay());
			st.setFloat(4, ttr.getWager());
			st.setString(5, ttr.getPicks().toString());
			st.setString(6, ttr.getBalls().toString());
			st.setString(7, ttr.getHits().toString());
			st.setBoolean(8, ttr.getBonusWin());
			st.setDouble(9, ttr.getDollarWon());
			
			st.addBatch();
			batchRequests++;
			
			if (batchRequests >= MAX_BATCH_LIMIT) {
				batchRequests = 0;
				st.executeBatch();
			}
		
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Inserts the Regular Bingo result into the table
	 * @param tableName	The name of the database table
	 * @param r	 The RegularResult object
	 * @throws SQLException
	 */
	public static void insertIntoTable(String tableName, RegularResult rr) throws SQLException {
		tableName = tableName.toUpperCase();
		
		String players = "";
		String playersQ = "";
		String playersI = "";
		
		// Create the table if does not exist yet
		if (!Database.doesTableExist(tableName)) {
			
		 	// Prepare cards query string
			for (int i = 0; i < rr.getNumPlayers(); i++) {
				
				for (int j = 0; j < rr.getNumCards(); j++ ) {
						
					players	+= ", P" + i + "_CARD" + j + "_NUMS varchar(100) NOT NULL";
					
					playersI += ", P" + i + "_CARD" + j + "_NUMS";
					
					playersQ += ", ?";
				}
				
			}
			
			String query = "create table " + tableName 
					+ " (PLAYID bigint NOT NULL, "
					+ "BLOCKID bigint NOT NULL, "
					+ "NUMOFCARDS integer NOT NULL, "
					+ "NUMOFPLAYERS integer NOT NULL, "
					+ "WAGER float NOT NULL, "
					+ "BALLS varchar(300) NOT NULL, "
					+ "PLAYERWON varchar(50) NOT NULL, "
					+ "WINAMOUNT varchar(100) NOT NULL"
					+ players + ") ";
			
			Database.createTable(tableName, query);
		}
		
		// Insert the result if table already exists
		String query = "insert into " + tableName
				+ " (PLAYID, BLOCKID, NUMOFCARDS, NUMOFPLAYERS, WAGER, BALLS, PLAYERWON, WINAMOUNT" + playersI + ") "
				+ "values(?, ?, ?, ?, ?, ?, ?, ?" + playersQ + ")";
		
		try {
			if (st == null) 
				st = conn.prepareStatement(query);
			
			st.setLong(1, rr.getRecordNum());
			st.setLong(2, rr.getBlockNum());
			st.setInt(3, rr.getNumCards());
			st.setInt(4, rr.getNumPlayers());
			st.setFloat(5, rr.getWager());
			st.setString(6, Arrays.toString(rr.getBalls()));
			st.setString(7, rr.getPlayersWon().toString());
			st.setString(8, rr.getPlayerWinAmount().toString());
			
			int index = 9;
			
			for (int i = 0; i < rr.getNumPlayers(); i++) {
				Player p = rr.getPlayer(i);
				
				for (int j = 0; j < p.getNumCards(); j++) {
					st.setString(index, Arrays.toString(p.getCard(j).getNumbersOnCard()));
					index++;
				}
			
			}
			
			st.addBatch();
			batchRequests++;
			
			if (batchRequests >= MAX_BATCH_LIMIT) {
				batchRequests = 0;
				st.executeBatch();
			}
		
		} catch (SQLException e) {
			throw e;
		}
	}
	
	public static void insertIntoTable(String tableName, BasicInfoEntry bie, int blocksize) 
			throws SQLException {
		tableName = tableName.toUpperCase();
		
		if (!Database.doesTableExist(tableName)) {
			String query = "create table " + tableName 
					+ " (ID bigint NOT NULL, "
					+ "PLAYS integer NOT NULL, "
					+ "CARDS integer NOT NULL, "
					+ "WINS bigint NOT NULL, "
					+ "LOSSES bigint NOT NULL, " 
					+ "LDWS bigint NOT NULL, "
					+ "PUSHES bigint NOT NULL, " 
					+ "MULTIWINS bigint NOT NULL, "
					+ "REDSQUARES bigint NOT NULL, "
					+ "FLASHING_REDSQUARES bigint NOT NULL)";

			Database.createTable(tableName, query);
		}
		
		// Add entry to the table if table already exists
		String query = "insert into " + tableName
				+ " (ID, PLAYS, CARDS, WINS, LOSSES, LDWS, PUSHES, MULTIWINS, REDSQUARES, FLASHING_REDSQUARES) "
				+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		try {
			if (st == null)
				st = conn.prepareStatement(query);
			
			st.setLong(1, bie.getBlockID());
			st.setInt(2, bie.getNumPlays());
			st.setInt(3, bie.getNumCards());
			st.setLong(4, bie.getWins());
			st.setLong(5, bie.getLosses());
			st.setLong(6, bie.getLDWs());
			st.setLong(7, bie.getPushes());
			st.setLong(8, bie.getMultiWins());
			st.setLong(9, bie.getRedSquares());
			st.setLong(10, bie.getFlashingRedSquares());
			
			st.addBatch();
			batchRequests++;
			
			if (batchRequests >= blocksize) {
				batchRequests = 0;
				st.executeBatch();
			}
			
		} catch (SQLException e) {
			throw e;
		}
		
	}
	
	public static void insertIntoTable(String tableName, PercentLosersEntry ple)
			throws SQLException {
		tableName = tableName.toUpperCase();
		
		String losspercentages = "";
		String losspercentagesI = "";
		String losspercentagesQ = "";
		
		for (int i = 0; i < ple.getLossPercentages().size(); i++) {
			Range r = ple.getRange(i);
			
			if (i < 4) {
				losspercentages += ", LOSS" + (int)(r.high * 100) + "_" + (int)(r.low * 100) + " integer NOT NULL";
				losspercentagesI += ", LOSS" + (int)(r.high * 100) + "_" + (int)(r.low * 100);
				
			} else if (i >= 4 && i < ple.getLossPercentages().size() - 1) {
				losspercentages += ", WIN" + (int)(r.high * -100) + "_" + (int)(r.low * -100) + " integer NOT NULL";
				losspercentagesI += ", WIN" + (int)(r.high * -100) + "_" + (int)(r.low * -100);
				
			} else {
				losspercentages += ", WIN900UP integer NOT NULL";
				losspercentagesI += ", WIN900UP";
			}
			
			losspercentagesQ += ",?";
		}
		
		if (!Database.doesTableExist(tableName)) {
			String query = "create table " + tableName 
					+ " (ID bigint NOT NULL, "
					+ "PLAYS integer NOT NULL, "
					+ "CARDS integer NOT NULL, "
					+ "WINS bigint NOT NULL, "
					+ "LDWS bigint NOT NULL, " 
					+ "LOSSES bigint NOT NULL, "
					+ "AVGLP integer NOT NULL, " 
					+ "MEDIANLP integer NOT NULL, "
					+ "LPSD integer NOT NULL, "
					+ "AVGRS bigint NOT NULL, "
					+ "MEDIANRS bigint NOT NULL, "
					+ "RSSD bigint NOT NULL, "
					+ "AVGRSNPLAYS integer NOT NULL, "
					+ "AVGRSPLAYS integer NOT NULL"
					+ losspercentages + ")";

			Database.createTable(tableName, query);
		}
		
		// Otherwise, add the LossPercentageEntry to the table
		String query = "insert into " + tableName
				+ "(ID, PLAYS, CARDS, WINS, LDWS, LOSSES, AVGLP, MEDIANLP, LPSD, AVGRS, MEDIANRS, RSSD, AVGRSNPLAYS, AVGRSPLAYS" 
				+ losspercentagesI + ") "
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?" + losspercentagesQ + ")";
				
				try {
					if (st == null)
						st = conn.prepareStatement(query);
					
					st.setLong(1, ple.getID());
					st.setInt(2, ple.getNumPlays());
					st.setInt(3, ple.getNumCards());
					st.setLong(4, ple.getWins());
					st.setLong(5, ple.getLDWs());
					st.setLong(6, ple.getLosses());
					st.setInt(7, (int)ple.getAvgLossBalance());
					st.setInt(8, (int)ple.getMedianLossBalance());
					st.setInt(9, (int)ple.getLossBalanceSD());
					st.setLong(10, ple.getAvgRedSquares());
					st.setLong(11, ple.getMedianRedSquares());
					st.setLong(12, ple.getRedSquaresSD());
					st.setInt(13, ple.getAvgRSNPlay());
					st.setInt(14, ple.getAvgRSPlay());
					
					int index = 14;
					for (int i = 0; i < ple.getLossPercentages().size(); i++) {
						st.setInt(++index, ple.getLossPercentage(i));
					}
					
					st.addBatch();
					batchRequests++;
					
					if (batchRequests >= MAX_BATCH_LIMIT) {
						st.executeBatch();
						batchRequests = 0;
					}
				} catch (SQLException e) {
					throw e;
				}
	}
	
	public static void insertIntoTable(String tableName, GamblersRuinEntry gre) 
			throws SQLException {
		tableName = tableName.toUpperCase();
		
		String playranges = "";
		String playrangesQ = "";
		String playrangesI = "";
		
		if (gre.getPlayRanges() != null) {
			for (int i = 0; i < gre.getPlayRanges().size(); i++) {
				Range r = gre.getPlayRange(i);
				
				if (r.low == r.high) {
					playranges += ", PLAYS" + (int)r.low + "UP integer NOT NULL";
					playrangesI += ", PLAYS" + (int)r.low + "UP";
				} else {
					playranges += ", PLAYS" + (int)r.low + "_" + (int)r.high + " integer NOT NULL";
					playrangesI += ", PLAYS" + (int)r.low + "_" + (int)r.high;
				}
				
				playrangesQ += ",?";
			}
		}
		
		
		String peakbalanceranges = "";
		String peakbalancerangesQ = "";
		String peakbalancerangesI = "";
		
		if (gre.getPBRanges() != null) {
			for (int i = 0; i < gre.getPBRanges().size(); i++) {
				Range r = gre.getPBRange(i);
				
				if (r.high == 100) {
					peakbalanceranges += ", PB" + (int)r.low + " integer NOT NULL";
					peakbalancerangesI += ", PB" + (int)r.low;
				} else if (r.low == 5000) {
					peakbalanceranges += ", PB" + (int)r.low  + "UP integer NOT NULL";
					peakbalancerangesI += ", PB" + (int)r.low + "UP";
				} else {
					peakbalanceranges += ", PB" + (int)r.low + "_" + (int)r.high + " integer NOT NULL";
					peakbalancerangesI += ", PB" + (int)r.low + "_" + (int)r.high;
				}
				peakbalancerangesQ += ",?";
			}
		}
		
		try {
		// Create table if does not exist
			if (!Database.doesTableExist(tableName)) {
	
				String query = "create table " + tableName 
						+ " (ID bigint NOT NULL, "
						+ "NUMOFCARDS integer NOT NULL, "
						+ "NUMOFPLAYS bigint NOT NULL, "
						+ "WINS integer NOT NULL, "
						+ "LOSSES integer NOT NULL, "
						+ "LDWS integer NOT NULL"
						+ playranges 
						+ ", MAX_PLAYS integer NOT NULL"
						+ ", AVG_PLAYS integer NOT NULL"
						+ ", MEDIAN_PLAYS integer NOT NULL"
						+ ", SD_PLAYS integer NOT NULL"
						+ peakbalanceranges
						+ ", MAX_PB integer NOT NULL"
						+ ", AVG_PB integer NOT NULL"
						+ ", MEDIAN_PB integer NOT NULL" 
						+ ", SD_PB integer NOT NULL" 
						+ ", PAYBACK float NOT NULL)";
				
				Database.createTable(tableName, query);
			}
			
			// Otherwise, add the LossPercentageEntry to the table
			String query = "insert into " + tableName
					+ "(ID, NUMOFCARDS, NUMOFPLAYS, WINS, LOSSES, LDWS" 
					+ playrangesI + ", MAX_PLAYS, AVG_PLAYS, MEDIAN_PLAYS, SD_PLAYS" 
					+ peakbalancerangesI + ", MAX_PB, AVG_PB, MEDIAN_PB, SD_PB, PAYBACK) "
					+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" + playrangesQ  + peakbalancerangesQ + ")";
			
			
			if (st == null)
				st = conn.prepareStatement(query);
	
			st.setLong(1, gre.getID());
			st.setInt(2, gre.getNumCards());
			st.setLong(3, gre.getTotalPlays());
			st.setInt(4, gre.getWins());
			st.setInt(5, gre.getLosses());
			st.setInt(6, gre.getLDWs());
			
			int index = 6;
			for (int i = 0; i < gre.getPlayRanges().size(); i++) 
				st.setInt(++index, gre.getPlay(i));
			
			st.setInt(++index, gre.getMaxPlay());
			st.setInt(++index, gre.getAvgPlay());
			st.setInt(++index, gre.getMedianPlay());
			st.setInt(++index, gre.getPlaySD());

			for (int i = 0; i < gre.getPBRanges().size(); i++) 
				st.setInt(++index, (int)gre.getPB(i));
			
			st.setInt(++index, (int)gre.getMaxPB());
			st.setInt(++index, (int)gre.getAvgPB());
			st.setInt(++index, (int)gre.getMedianPB());
			st.setInt(++index, (int)gre.getPBSD());
			st.setFloat(++index, gre.getPayBackPercentage());
			
			st.addBatch();
			batchRequests++;
			
			if (batchRequests >= MAX_BATCH_LIMIT) {
				st.executeBatch();
				batchRequests = 0;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		
		
		
	}
	
	public static void dropTable(String tableName) throws SQLException {
		tableName = tableName.toUpperCase();
		
		if (Database.doesTableExist(tableName)) {
			Statement st = null;
			String query = "drop table " + tableName;
			
			try {
				st = conn.createStatement();
				st.executeUpdate(query);
			} catch (SQLException e) {
				throw e;
			} finally {
				if (st != null) {
					st.close();
					listOfTables.remove(tableName);
				}
			}
		} else return;
	}
	
	private static boolean doesTableExist(String tableName) throws SQLException{
		if (listOfTables == null) {
			Database.populateListofTables();
		}
		
		return listOfTables.contains(tableName);
	}
	
	public static void flushBatch() throws SQLException {
		if (batchRequests > 0) {
			batchRequests = 0;
			
			try {
				st.executeBatch();
				st = null;
			} catch (SQLException e) {
				throw e;
			}
		} else if (st != null) {
			st = null;
		}
	}
	
	
	
}

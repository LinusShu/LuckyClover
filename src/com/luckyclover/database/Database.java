package com.luckyclover.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.luckyclover.LuckyCloverModel.Block;
import com.luckyclover.LuckyCloverModel.PaytableEntry;

public class Database {

	public static String DEFAULT_DB_NAME = "LuckyCloverResDB";
	
	private static String dbName = DEFAULT_DB_NAME;
	private static String URL = "jdbc:derby:" + dbName + ";create=true";
	private static final String URL_SHUTDOWN = "jdbc:derby:;shutdown=true";
	private static final int MAX_BATCH_LIMIT = 1000;
	
	private static Connection conn = null;
	private static ArrayList<String> listOfTables;
	private static int batchRequests = 0;
	private static PreparedStatement st = null;
	
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
	public static void insertIntoTable(String tableName, PaytableEntry pe)
		throws SQLException {
			tableName = tableName.toUpperCase();
			
			// Create table if does not exist
			if(!Database.doesTableExist(tableName)) {
				String query = "create table " + tableName 
						+ " (ID integer NOT NULL, "
						+ "NAME varchar(30) NOT NULL, "
						+ "WINPATTERN bigint NOT NULL, "
						+ "PAYOUT smallint NOT NULL)";
				
				Database.createTable(tableName, query);
			}
			
			// Add entry to the table if table already exists
			String query = "insert into " + tableName
					+ " (ID, NAME, WINPATTERN, PAYOUT) "
					+ "values(?, ?, ?, ?)";
			
			try {
				if (st == null)
					st = conn.prepareStatement(query);
				
				st.setInt(1, pe.getEntryID());
				st.setString(2, pe.getName());
				st.setLong(3, pe.getWinPattern());
				st.setInt(4, pe.getPayout());
				
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
		}
	}
	
	
	
}

package com.eBingo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EBingoLog {
	private String filename;
	private String filepath = "";
	private FileOutputStream fos = null;
	private File file;
	public static final String newline = System.getProperty("line.separator");
	
	public EBingoLog(String filename) {
		this.filename = filename;
		Date date = new Date();
		SimpleDateFormat sft = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
		String formattedDate = sft.format(date);
		
		File file = new File("logs/");
		
		if(!file.exists()) 
			file.mkdir();
		
		file = null;
		
		this.filename = "logs/" + this.filename + "_" + formattedDate + ".txt";
		
		this.clearFile();
		this.writeLine("LuckyClover Result Logs\n");
	}
	
	private void writeString(String str, Boolean append) {
		try {
			file = new File(filename);
			
			fos = new FileOutputStream(file, append);
			
			if (!file.exists()) {
				file.mkdirs();
				file.createNewFile();
			}
			
			byte[] output = str.getBytes();
			fos.write(output);
			fos.flush();
			fos.close();
			
			file = null;
			fos = null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
					fos = null;
				}
				
				if (file != null) 
					file = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void clearFile() {
		this.writeString("", false);
	}
	
	public void writeLog(String str) {
		this.writeString(str, true);
	}
	
	public void writeLine(String str) {
		writeLog(str + newline);
	}
	
	public String getFilePath() {
		return this.filename;
	}
	
}

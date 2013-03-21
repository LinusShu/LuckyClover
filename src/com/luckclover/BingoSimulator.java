package com.luckclover;


import java.io.*;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.Random;
import java.lang.Math;

    

//Reads a Comma Separated Value file and creates summary data in another CSV file.
import java.io.*;
import java.util.Arrays;

public class BingoSimulator{

 
	public static void main(String[] arg) throws Exception {

		BufferedReader CSVFile = new BufferedReader(new FileReader("C://Users//dev//Desktop//bingojavafiles//WinningCards.csv"));

		FileWriter fstream = new FileWriter("out.csv");
		BufferedWriter out = new BufferedWriter(fstream);


		int i = 0;
		int j = 0;
		int k = 0;
		float totalWins = 0;
		int[] winAmount = new int[55] ;
		int[] winningCombination = new int[55];
		int[] winningBits = new int[75];
		int[] nbrBitsWinningCombination = new int[55];
		int[] countOfWinningCombinations = new int[55] ;
		int[] nbrBingos = new int[26] ;
		int nbrBits = 0;
		int nbrBitsInK = 0;
		int m = 0;
		int total = 0;
		boolean lookingForMatch = true;
		int[] [] countOfWinSizes = new int[25] [15];
		double[] winSizes = new double [15] ;
		int ball[] = new int[26];
		int countOfEachBall[] = new int [76];
		int ballsFound = 0;
		int card[] = new int[25];
		int cardRotated[] = new int [25];
		int gameOutcome =0;
		double tempValue =0;
		int countOfWins = 0;
		int junk = 0;
		int nbrBitsInEachWin[] = new int [25];
		int nbrBitsInEachOutCome[] = new int[25];
		int countOfEachSpotOnCard[] = new int [76];
		int redSquarecount = 0;
		boolean winningCard = false;
		int num1ink = 0;
		int num1inWinComb = 0;
		int redSquareLocation[] = new int [55];
		int cRedSquare = 0;
		boolean redDup = false;
	
	
		winSizes[0] = 1.0;
		winSizes[1] = 4.0;
		winSizes[2] = 8.0;
		winSizes[3] = 12.0;
		winSizes[4] = 16.0;
		winSizes[5] = 20.0;
		winSizes[6] = 40.0;
		winSizes[7] = 100.0;
		winSizes[8] = 120.0;
		winSizes[9] = 200.0;
		winSizes[10] = 300.0;
		winSizes[11] = 400.0;
		winSizes[12] = 1000.0;
		winSizes[13] = 2000.0;
		winSizes[14] = 10000.0;
	
	
		// Read in the eBingo winning amounts and winning patterns. 
	
		String dataRow = CSVFile.readLine();
		try{
			while (dataRow != null){
				String[] dataArray = dataRow.split(",");
				//out.write(i + "input string," + i + "," + dataArray[0] + "," + dataArray[1] + "\r\n");
				winAmount[i] = (Integer.valueOf(dataArray[0])).intValue();
				winningCombination[i] = Integer.parseInt(dataArray[1].trim());
				out.write(i + "," + winAmount[i] + "," + winningCombination[i] + "," + Integer.toBinaryString(winningCombination[i]) + "\r\n");
	
				i=i+1;
				dataRow = CSVFile.readLine();
			}
		}
	
		catch (Exception e) {
			out.write("Catch" + "\r\n");
			CSVFile.close();
			out.close();
		}
	
		// Count the number of bits in each winning pattern.
		for ( i=0; i<53; i++ ) {
			nbrBits=0;
			j=winningCombination[i];
		        
			for (k=0; k<=24; k++) {
				winningBits[k] = 0;
			}
			k=1;
			do {
				nbrBitsWinningCombination[i] += (j & 1); 
				winningBits[k] = (j & 1);
				k=k+1;
				j >>= 1;
			} while (j != 0);
			out.write("nbrBits for " + i + " - " + Integer.toBinaryString(winningCombination[i]) + " - " + nbrBitsWinningCombination[i] + "winAmount," + winAmount[i] +  "\r\n");
			out.write( winningBits[5] + " " + winningBits[4] + " " + winningBits[3]+ " " + winningBits[2]+ " " + winningBits[1] + "\r\n");
			out.write( winningBits[10] +" " + winningBits[9] +  " " + winningBits[8]+  " " + winningBits[7]+  " " + winningBits[6] + "\r\n");
			out.write( winningBits[14] +  " " + winningBits[13] + "1" + winningBits[12]+ " " +  winningBits[11] + "\r\n");
			out.write( winningBits[19] +  " " + winningBits[18] +  " " + winningBits[17]+  " " + winningBits[16]+  " " + winningBits[15] + "\r\n");
			out.write( winningBits[24] +  " " + winningBits[23] +  " " + winningBits[22]+  " " + winningBits[21]+  " " + winningBits[20] + "\r\n" + "\r\n");
		}
		
	
		// Generate the 24 balls to be called in the Bingo game.
		Random randomGenerator = new Random();
	
	
		// This large loop with the variable junk is a big loop that goes around once for every bingo game played.
		// For example, if it had "junk<100000" then it would loop and play 100,000 bingo games with 100,000 bingo cards
		// and 100,000 times there would be 24 new bingo numbers between 1-75 drawn at random.
		// "Ball" is the 24 numbers drawn for this game.  There cannot be duplicates.

		for (junk=1; junk<10; junk++) {
	    
			gameOutcome=0;
			ball[1] = randomGenerator.nextInt(75);
			ball[1] = ball[1] + 1;
			ballsFound=1;
			winningCard = false;
			cRedSquare = 0;
			redSquarecount = 0;
			// out.write("ball, " + 1 + ", - ," + Integer.toBinaryString(ball[1]) + ", - ," + ball[1] +"\r\n");
			do {
				ballsFound=ballsFound+ 1;
				ball[ballsFound] = randomGenerator.nextInt(75);
				ball[ballsFound] = ball[ballsFound] + 1;
				//out.write("ball, " + ballsFound + ", -, " + Integer.toBinaryString(ball[ballsFound]) + ", -, " + ball[ballsFound] +"\r\n");
				
				for ( j=1; j<(ballsFound); j++ ) {
					if (ball[ballsFound] == ball[j]){
						ballsFound=ballsFound - 1;
						break;
					}
				}
				//out.write("ball " + i + " - " + Integer.toBinaryString(ball[i]) + " - " + ball[i] +"\r\n");
			} while (ballsFound < 24);
	
			// This is just a double check to ensure we don't have any duplicate balls.
			for (i=1; i<24; i++) {
				for (j=(i+1);j<=24;j++){
					if (ball[i]== ball[j]){
						out.write ("i,"+ i + ",j," + "XXXXXXXXXXXXXXXXXXXXXX\r\n");
					}
				}
			}
	    
	
			// Keep a count of each ball drawn so we can check at the end to see that each comes up 1/75th of the time.

			out.write("24 balls drwan for the game ");
			for (j=1; j<=24; j++) {
				countOfEachBall[ball[j]] = countOfEachBall[ball[j]] + 1;
				out.write(ball[j] + ", ");
			}
			out.write("\r\n");
	    
	
			// Generate the letters on the card under "B"
			card[1] = randomGenerator.nextInt(15);
			card[1] = card[1] + 1;
			i=1;
			do {
				i=i+1;
				card[i] = randomGenerator.nextInt(15);
				card[i] = card[i] + 1;
		        for ( j=1; j<(i); j++ ) {
		          if (card[i] == card[j]){
		              i=i - 1;
		              break;
		          }
		        }
			} while (i < 5);
	
			// Generate the letters on the card under "I"    
			card[6] = randomGenerator.nextInt(15);
			card[6] = card[6] + 16;
			i=6;
		      
			do {
				i=i+1;
		        card[i] = randomGenerator.nextInt(15);
		        card[i] = card[i] + 16;
		        for ( j=6; j<(i); j++ ) {
		          if (card[i] == card[j]){
		              i=i - 1;
		              break;
		          }
		        }
			} while (i < 10);      
	
			// Generate the letters on the card under "N". Note that column N only has 4 spots as one is free.
			card[11] = randomGenerator.nextInt(15);
			card[11] = card[11] + 31;
			i=11;
			do {
				i=i+1;
				card[i] = randomGenerator.nextInt(15);
				card[i] = card[i] + 31;
				for ( j=10; j<(i); j++ ) {
					if (card[i] == card[j]){
						i=i - 1;
						break;
					}
				}
			} while (i < 14);   
	

			// Generate the letters on the card under "G" 
			card[15] = randomGenerator.nextInt(15);
			card[15] = card[15] + 46;
			i=15;
			do {
				i=i+1;
				card[i] = randomGenerator.nextInt(15);
				card[i] = card[i] + 46;
				for ( j=15; j<(i); j++ ) {
					if (card[i] == card[j]){
						i=i - 1;
						break;
					}
				}
			} while (i < 19);  
	

			// Generate the letters on the card under "O"
			card[20] = randomGenerator.nextInt(15);
			card[20] = card[20] + 61;
			i=20;
			do {
				i=i+1;
				card[i] = randomGenerator.nextInt(15);
				card[i] = card[i] + 61;
				for ( j=20; j<(i); j++ ) {
					if (card[i] == card[j]){
						i=i - 1;
						break;
					}
				}
			} while (i < 24);  
	
	
			//      For some reason I have to rotate the Bingo card. I forget why.
			cardRotated[1] = card[5];
			cardRotated[2] = card[10];
			cardRotated[3] = card[14];
			cardRotated[4] = card[19];
			cardRotated[5] = card[24];
			cardRotated[6] = card[4];
			cardRotated[7] = card[9];
			cardRotated[8] = card[13];
			cardRotated[9] = card[18];
			cardRotated[10] = card[23];
			cardRotated[11] = card[3];
			cardRotated[12] = card[8];
			cardRotated[13] = card[17];
			cardRotated[14] = card[22];
			cardRotated[15] = card[2];
			cardRotated[16] = card[7];
			cardRotated[17] = card[12];
			cardRotated[18] = card[16];
			cardRotated[19] = card[21];
			cardRotated[20] = card[1];
			cardRotated[21] = card[6];
			cardRotated[22] = card[11];
			cardRotated[23] = card[15];
			cardRotated[24] = card[20];
	
	
			// Count to ensure that all 24 spots on the bingo card come up an equal number of times.
			for (i=1; i<25; i++) {
				countOfEachSpotOnCard[cardRotated[i]] = countOfEachSpotOnCard[cardRotated[i]]  + 1   ;
			}
	
	
	
			out.write("card view \r\n");
			out.write(cardRotated[20]+","+cardRotated[21]+","+cardRotated[22]+","+cardRotated[23]+","+cardRotated[24]+","+ ",\r\n");
			out.write(cardRotated[15]+","+cardRotated[16]+","+cardRotated[17]+","+cardRotated[18]+","+cardRotated[19]+","+ ",\r\n");
			out.write(cardRotated[11]+","+cardRotated[12]+",*,"+cardRotated[13]+","+cardRotated[14]+","+ ",\r\n");
			out.write(cardRotated[6]+","+cardRotated[7]+","+cardRotated[8]+","+cardRotated[9]+","+cardRotated[10]+","+ ",\r\n");
			out.write(cardRotated[1]+","+cardRotated[2]+","+cardRotated[3]+","+cardRotated[4]+","+cardRotated[5]+","+ ",\r\n"+ "\r\n");


			// out.write("24 called numbers," + ball[i] + ",index," + i + "\r\n");
			// From the covered spots on the card we generated an integer that represents the game outcome.
			// The use of an integer to represent the game outcome comes from:
			// http://www.durangobill.com/BingoHowTo.html
			for ( i=1; i<25; i++ ) {
				//out.write("called number," + ball[i] + ",index," + i + "\r\n");          
				for ( j=1; j<25; j++ ) {
					//     out.write("Bingo Card," + card[j] + ",index," + j + ",outcome:," + gameOutcome + ",X" + Integer.toBinaryString(gameOutcome)+ ",\r\n");      
					if (ball[i] == cardRotated[j]){
						tempValue = Math.pow(2,(24-j));
						gameOutcome = gameOutcome + (int) tempValue;
						//out.write("tempvaluefloat," + tempValue + ",tempvalueint," + (int) tempValue + "\r\n");
						//out.write("equal - ball," + ball[i] + ",card rotated," + cardRotated[j] + ",outcome," + gameOutcome + ",J" + j + ",Jjunk," + junk + "\r\n"); 
						//out.write("gameOutcome,XXX" + Integer.toBinaryString(gameOutcome)+ "\r\n"); 
						//out.write("gameOutcome,XXX" + gameOutcome+ "\r\n");
						//out.write("gameOutcome,XXX" + Integer.toBinaryString(gameOutcome)+ "\r\n");
						break;
					}
				}
			}
	      
			out.write("gameOutcome: " + Integer.toBinaryString(gameOutcome) + "\r\n");
	      
	  
	      
	      
	
			// out.write("junk," + junk + ",game outcome,X" + Integer.toBinaryString(gameOutcome) + "\r\n"); 
			// Is this a winner?
	
	
	
	
			// Determine if we have a winning card.
			for ( j=0; j<53 ; j++ ) {
				k=gameOutcome & winningCombination[j];
				if (k==winningCombination[j]){
					out.write("k: " + Integer.toBinaryString(k) + "\r\n");
					countOfWinningCombinations[j] = countOfWinningCombinations[j] + 1;
					countOfWins = countOfWins + 1;
					totalWins = totalWins + winAmount[j];
					winningCard = true;
					// nbrBitsInEachWin[nbrBits]= nbrBitsInEachWin[nbrBits] + 1;
					
					//out.write("XXX\r\n");
					//out.write("WinType," + j + ",winamount," + winAmount[j] + ",gameOutcome," + gameOutcome + "\r\n");
					//out.write( winningBits[5] + " " + winningBits[4] + " " + winningBits[3]+ " " + winningBits[2]+ " " + winningBits[1] + "\r\n");
					//out.write( winningBits[10] +" " + winningBits[9] +  " " + winningBits[8]+  " " + winningBits[7]+  " " + winningBits[6] + "\r\n");
					//out.write( winningBits[14] +  " " + winningBits[13] + "1" + winningBits[12]+ " " +  winningBits[11] + "\r\n");
					//out.write( winningBits[19] +  " " + winningBits[18] +  " " + winningBits[17]+  " " + winningBits[16]+  " " + winningBits[15] + "\r\n");
					//out.write( winningBits[24] +  " " + winningBits[23] +  " " + winningBits[22]+  " " + winningBits[21]+  " " + winningBits[20] + "\r\n" + "\r\n");
					//out.write( winningBits[25] +  " " + winningBits[26] +  " " + winningBits[27]+  " " + winningBits[28]+  " " + winningBits[29] + "\r\n" + "\r\n");
					//out.write( winningBits[30] +  " " + winningBits[31] +  " " + winningBits[32]+  " " + winningBits[33]+  " " + winningBits[34] + "\r\n" + "\r\n");
					//out.write( winningBits[35] +  " " + winningBits[36] +  " " + winningBits[37]+  " " + winningBits[38]+  " " + winningBits[39] + "\r\n" + "\r\n");
					//out.write( winningBits[40] +  " " + winningBits[41] +  " " + winningBits[42]+  " " + winningBits[43]+  " " + winningBits[44] + "\r\n" + "\r\n");
					//out.write( winningBits[45] +  " " + winningBits[46] +  " " + winningBits[47]+  " " + winningBits[48]+  " " + winningBits[49] + "\r\n" + "\r\n");
					//out.write( winningBits[50] +  " " + winningBits[51] +  " " + winningBits[52]+  " " + winningBits[53]+  " " + winningBits[54] + "\r\n" + "\r\n");
					//out.write( winningBits[55] +  " " + winningBits[56] +  " " + winningBits[57]+  " " + winningBits[58]+  " " + winningBits[59] + "\r\n" + "\r\n");
					//out.write( winningBits[60] +  " " + winningBits[61] +  " " + winningBits[62]+  " " + winningBits[63]+  " " + winningBits[64] + "\r\n" + "\r\n");
					//out.write( winningBits[65] +  " " + winningBits[66] +  " " + winningBits[67]+  " " + winningBits[68]+  " " + winningBits[69] + "\r\n" + "\r\n");
					//out.write( winningBits[70] +  " " + winningBits[71] +  " " + winningBits[72]+  " " + winningBits[73]+  " " + winningBits[74] + "\r\n" + "\r\n");
					
					//out.write("WINNER," + Integer.toBinaryString(gameOutcome) + ",type of win," + Integer.toBinaryString(winningCombination[j])+ ",pays"  + winAmount[j] + "winindex," + j + ",nbrBits," + nbrBits + "\r\n");    
	
					break;
				}
			}
	      	
			// if the card was not a winning card.
			if (winningCard == false){
				for ( j=0; j<53 ; j++ ) {
					redDup = false;
					k = gameOutcome & winningCombination[j];
					num1ink = bit_count(k);
					num1inWinComb = bit_count(winningCombination[j]);
					//out.write("k in redSquare : " + Integer.toBinaryString(k) + "\r\n");
					//out.write("num1ink: " + num1ink + "\r\n");
					
					if ((num1ink + 1) == num1inWinComb){
						k = k ^ winningCombination[j];
						
						for (int d = 0; d < cRedSquare; d++){
							if (k == redSquareLocation[d]){
								redDup = true;
								break;
							}
						}
						if (redDup == false){
							redSquareLocation[cRedSquare] = k;
							cRedSquare = cRedSquare + 1;
							redSquarecount = redSquarecount + 1;
						}
					}
				}
			}

			
			out.write("Number of Red Squares : " + redSquarecount + "\r\n");
			
			for (int d = 0; d < cRedSquare; d++){
				out.write("Red Squares Location : " + Integer.toBinaryString(redSquareLocation[d]) + "\r\n");
			}
			
			
			nbrBits=0;
		}// junk loop 
	        
	
		//xxx
	
		//for ( i=0; i<25; i++ ) {
			//out.write("nbrBitsInEachWin," + nbrBitsInEachWin[i] + ",nbrBitsInEachOutCome," + ((nbrBitsInEachOutCome[i])) + ",index," + i + "\r\n");
		//}
	
	
		// Print out various results
		for (j=0; j<=75; j++) {
			out.write("Ball," + j + ",count," + countOfEachBall[j] + "\r\n");
		}
	
		for (i=1; i<=75; i++) {
			out.write( "cardspot," + i + ",count," + countOfEachSpotOnCard[i]  + "\r\n");   ;
		}
	    
		for ( i=0; i<25; i++ ) {
			out.write("24 called numbers," + ball[i] + ",index," + i + "\r\n");
		}
	
		for ( i=0; i<25; i++ ) {
			out.write("Bingo Card Rotated," + cardRotated[i] + ",index," + i + "\r\n");
		}
	
	    
		for ( i=0; i<26; i++ ) {
			out.write("nbrBingos, " + i + " , " + nbrBingos[i] + "\r\n");
			//out.write(nbrBingos[i] + "\r\n");
		}
	
		for (i =0; i < 25; i++) {
			for (j = 0; j < 15; j++) {
				out.write(countOfWinSizes[i][j] + ",");
				total = total + countOfWinSizes[i][j];
			}
			out.write("\r\n");
		}
		out.write("Total," + total + "\r\n");
	
		for ( i=0; i<54; i++ ) {
			out.write("Win amount:, " + winAmount[i] + "," + countOfWinningCombinations[i] + "\r\n");
		}
		for ( i=0; i<25; i++ ) {
			out.write("nbrbitsineachwin," + nbrBitsInEachWin[i] + ",index," + i + "\r\n");
		}
	
		out.write("And the totalWins is:, " + totalWins + "\r\n");
		out.write("And the totalWins percentage is: " + (totalWins/junk) + "\r\n");
	                
		out.write("junk," + junk + ",CountOfWins," + countOfWins + "\r\n");
		out.write("Normal End" + "\r\n");
		CSVFile.close();
		out.close();
		
	
	}//main()
	 
	// function to count the number of "1"s in a binary number.
	// Algorithm from "compprog.wordpress.com/2007/11/06/binary-numbers-counting-bits/"
	public static int bit_count(int n){
		int count = 0;
		
		while (n != 0){
			++count;
			n &= n - 1;
		}
		return count;
	}
	
} 
    



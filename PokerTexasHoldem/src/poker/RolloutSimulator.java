package poker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import poker.Card.Suit;
import poker.Card.Value;
import poker.Player.PlayerType;

public class RolloutSimulator {
	
	
	public class EqClass{
		
		public int[] eqNum;
		public boolean suited;
		
		public EqClass(int[] eqNum, boolean suited){
			this.eqNum= eqNum;
			this.suited = suited;
		}
		
		@Override
		public int hashCode(){
			return (this.eqNum[0] * 31) ^ this.eqNum[1];
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof EqClass) {
				EqClass eqClass = (EqClass) obj;
				return (this.eqNum[0] == eqClass.eqNum[0] && this.eqNum[1] == eqClass.eqNum[1] && this.suited == eqClass.suited);
			}
			else
				return false;
		}
		
	}
	
	private Table table;
	
	private HashMap<EqClass, double[]> rolloutTable;
	
	public RolloutSimulator(){
		this.rolloutTable = new HashMap<EqClass, double[]>();
		this.table = new Table();
	}
	
	public void fillTable(){
		
		long startTime = System.currentTimeMillis();
		
		for(int i=2; i<15; i++){
			for(int j=i; j<15; j++){
				EqClass suited = new EqClass(new int[]{i, j}, true);
				EqClass unsuited = new EqClass(new int[]{i, j}, false);
				if(i==j)
					rolloutTable.put(unsuited, calcProb(unsuited));
				else {
					rolloutTable.put(suited, calcProb(suited));
					rolloutTable.put(unsuited, calcProb(unsuited));
				}
			}
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime)/1000 + " s");
	}
	
	/*
	 * Method for estimating the winning probability for a given equality class based on rollout simulations
	 */
	public double[] calcProb(EqClass eqClass){
				
		ArrayList<Card> wholeCards = new ArrayList<Card>();
		
		if(eqClass.suited){
			wholeCards.add(new Card(Value.values()[eqClass.eqNum[0]-2], Suit.CLUBS));
			wholeCards.add(new Card(Value.values()[eqClass.eqNum[1]-2], Suit.CLUBS));
		}
		else {
			wholeCards.add(new Card(Value.values()[eqClass.eqNum[0]-2], Suit.CLUBS));
			wholeCards.add(new Card(Value.values()[eqClass.eqNum[1]-2], Suit.DIAMONDS));
		}
		
		double [] winProb = new double[9];
		
		for(int i=2; i<11; i++){
			
			double wins = 0;
			double ties = 0;
			
			for(int j=0; j<10000; j++){
				
				int result = doRollout(wholeCards, i);
				
				if(result == 1)
					wins++;
				else if(result == 0)
					ties++;
			}
			
			winProb[i-2] = (wins + (ties/2))/10000;
		}
		
		return winProb;
		
	}
	
	/*
	 * Method for doing one single rollout. Returns 1 if you win, 0 if you tie and -1 if you loose
	 */
	public int doRollout(ArrayList<Card> wholeCards, int numOfPlayers){
		
		this.table = new Table();
		
		for(int i=0; i<numOfPlayers; i++){
			this.table.addPlayer(new Player(i, 0, PlayerType.NORMAL));
		}
		
		this.table.setActivePlayers(this.table.getPlayers());
		this.table.setDeck(new Deck());
		this.table.getDeck().shuffleDeck();
		
		this.table.getPlayers().get(0).addCard(this.table.getDeck().getSpecificCard(wholeCards.get(0)));
		this.table.getPlayers().get(0).addCard(this.table.getDeck().getSpecificCard(wholeCards.get(1)));
		
		//Deal cards to the other players
		for(int i=1; i<numOfPlayers; i++){
			this.table.getPlayers().get(i).addCard(this.table.getNextCard());
		}
		for(int i=1; i<numOfPlayers; i++){
			this.table.getPlayers().get(i).addCard(this.table.getNextCard());
		}
		
		
		this.table.dealFlop(false);
		this.table.dealTurn(false);
		this.table.dealRiver(false);
		
		this.table.initiateShowdown(false);
		
		
		if(this.table.getActivePlayers().contains(this.table.getPlayers().get(0))) {
			if(this.table.getActivePlayers().size() > 1){
				return 0;
			}
				
			else {
				return 1;
			}
		}
		
		return -1;
	}
	
	public void writeToFile() throws IOException{
		File f = new File("probs.txt");
		
        FileWriter fwriter = new FileWriter(f);
        BufferedWriter writer = new BufferedWriter(fwriter);
        writer.write("Cards " + "Suited " + "Num pla. " + "Prob" +"\n");
        for (int i = 2; i < 15; i++) {
        	for (int j = i; j < 15; j++) {
        		for (int k = 2; k < 11; k++) {
                    if(i == j)                    	
                    	writer.write(i + " " + j + " " + 0 + " " + k + " " + this.rolloutTable.get(new EqClass(new int[]{i, j}, false))[k-2] + "\n");
                    else {
                    	writer.write(i + " " + j + " " + 0 + " " + k + " " + this.rolloutTable.get(new EqClass(new int[]{i, j}, false))[k-2] + "\n");
                    	writer.write(i + " " + j + " " + 1 + " " + k + " " + this.rolloutTable.get(new EqClass(new int[]{i, j}, true))[k-2] + "\n");
                    }
                }
            }
        }
	
        writer.close();
		
	}
	
	public void readFromFile() throws IOException{
		File f = new File("probs.txt");

        FileReader fReader = new FileReader(f);
        BufferedReader reader = new BufferedReader(fReader);
        reader.readLine();
        for (int i = 2; i < 15; i++) {
	        for (int j = i; j < 15; j++) {
	        	if(i==j) {
	        		double [] probs = new double[9];
	        		EqClass unsuited = new EqClass(new int[]{i, j}, false);
	        		
	        		for (int k = 2; k < 11; k++) {
	        			String[] s = reader.readLine().split(" ");
		            	probs[k-2] = Double.parseDouble(s[4]);
		            }
	        		
	        		this.rolloutTable.put(unsuited, probs);
	        	}
	        	else {
	        		double [] probs1 = new double[9];
		        	double [] probs2 = new double[9];
		        	EqClass unsuited = new EqClass(new int[]{i, j}, false);
		        	EqClass suited = new EqClass(new int[]{i, j}, true);
		        	
		        	for (int k = 2; k < 11; k++) {
		            	String[] s1 = reader.readLine().split(" ");
	            		probs1[k-2] = Double.parseDouble(s1[4]);
	            		
	            		String[] s2 = reader.readLine().split(" ");
	            		probs2[k-2] = Double.parseDouble(s2[4]);
		            	
				    }
		        	
		        	this.rolloutTable.put(unsuited, probs1);
		        	this.rolloutTable.put(suited, probs2);
		        }
	               
	        }
        }
        
        reader.close();
	}
	
	/*
	 * Returns the probability to win with a certain hand against a certain number of players.
	 * Accepts any legal combination of cards, and 2-10 players(Giving anything else will crash the system)
	 */
	public double getProb(ArrayList<Card> cards, int numOfPlayers){
		
		int firstCard = cards.get(0).getValue().ordinal()+2;
		int secondCard = cards.get(1).getValue().ordinal()+2;
		
		boolean suited = cards.get(0).getSuit() == cards.get(1).getSuit();
		
		EqClass eqClass;
		
		//Make sure the cards are sorted on value
		if(firstCard > secondCard) {
			eqClass = new EqClass(new int[]{secondCard, firstCard}, suited);
		}
		else {
			eqClass = new EqClass(new int[]{firstCard, secondCard}, suited);
		}
		
		return this.rolloutTable.get(eqClass)[numOfPlayers-2];
	}
	
	public static void main(String [] args){
		RolloutSimulator rSim = new RolloutSimulator();
		//rSim.fillTable();
		
		try {
			rSim.readFromFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//Example of getting prob for a given combination of cards
		System.out.println("Prob when playing against 9 others:");
		
		ArrayList<Card> wholeCards = new ArrayList<Card>();
		wholeCards.add(new Card(Value.ACE, Suit.SPADES));
		wholeCards.add(new Card(Value.KING, Suit.SPADES));
		
		ArrayList<Card> wholeCards2 = new ArrayList<Card>();
		wholeCards2.add(new Card(Value.ACE, Suit.DIAMONDS));
		wholeCards2.add(new Card(Value.ACE, Suit.CLUBS));
		
		
		System.out.println("Suited A K: " + rSim.getProb(wholeCards, 10));
		System.out.println("Pocket A: " + rSim.getProb(wholeCards2, 10));
		
		
		
	}

}

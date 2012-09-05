package poker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import poker.Card.Suit;
import poker.Card.Value;
import poker.Player.PlayerType;

public class RolloutSimulator {
	
	
	private class EqClass{
		
		int[] eqNum;
		boolean suited;
		
		public EqClass(int[] eqNum, boolean suited){
			this.eqNum= eqNum;
			this.suited = suited;
		}
		
	}
	
	private Table table;
	
	private HashMap<EqClass, double[]> rolloutTable;
	
	public RolloutSimulator(){
		this.rolloutTable = new HashMap<EqClass, double[]>();
		this.table = new Table();
		/*
		for(int i=2; i<15; i++){
			for(int j=2; j<15; j++){
				
				EqClass eqClass = new EqClass(new int[]{i,j}, false);
				this.rolloutTable.put(eqClass, new double []{0.15, 0.12}); 
			}
		}
		*/
		
	}
	
	public void fillTable(){
		for(int i=2; i<15; i++){
			for(int j=2; j<15; j++){
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
				
				//System.out.println(result);
				
				if(result == 1)
					wins++;
				else if(result == 0)
					ties++;
			}
			
			System.out.println("Wins: " + wins);
			System.out.println("Ties: " + ties);
			
			winProb[i-2] = (wins + (ties/2))/10000;
		}
		
		return winProb;
		
	}
	
	/*
	 * Method for doing one single rollout. Returns 1 if you win, -1 if you tie and 0 if you loose
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
        for (int i = 0; i < 13; i++) {
        	for (int j = 0; j < 13; j++) {
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
	
	public void readFromFile(){
		
	}
	
	public static void main(String [] args){
		RolloutSimulator rSim = new RolloutSimulator();
		double [] prob = rSim.calcProb(rSim.new EqClass(new int[]{2, 7}, false));
		
		for(int i=0; i<9;i++){
			System.out.println(prob[i]);
		}
	}

}

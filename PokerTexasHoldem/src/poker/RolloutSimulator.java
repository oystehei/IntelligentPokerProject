package poker;

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
		
	}
	
	public double calcProb(int[] eqClass, boolean suited){
		
		ArrayList<Card> wholeCards = new ArrayList<Card>();
		
		if(eqClass[0] == eqClass[1]){
			wholeCards.add(new Card(Value.values()[eqClass[0]], Suit.CLUBS));
			wholeCards.add(new Card(Value.values()[eqClass[1]], Suit.DIAMONDS));
		}
		else if(suited){
			wholeCards.add(new Card(Value.values()[eqClass[0]], Suit.CLUBS));
			wholeCards.add(new Card(Value.values()[eqClass[1]], Suit.CLUBS));
		}
		else {
			wholeCards.add(new Card(Value.values()[eqClass[0]], Suit.CLUBS));
			wholeCards.add(new Card(Value.values()[eqClass[1]], Suit.DIAMONDS));
		}
		
		double wins = 0;
		double ties = 0;
		
		for(int i=2; i<10; i++){
			for(int j=0; j<1000; j++){
				
				Boolean result = doRollout(wholeCards, i);
				if(result)
					wins++;
				else if(!result)
					ties++;
				}
		}
		
		return (wins + (ties/2))/1000;
		
	}
	
	public Boolean doRollout(ArrayList<Card> wholeCards, int numOfPlayers){
		
		this.table = new Table();
		
		for(int i=0; i<numOfPlayers; i++){
			this.table.addPlayer(new Player(i, 0, PlayerType.NORMAL));
		}
		
		this.table.getPlayers().get(0).addCard(this.table.getDeck().getSpecificCard(wholeCards.get(0)));
		this.table.getPlayers().get(0).addCard(this.table.getDeck().getSpecificCard(wholeCards.get(1)));
		
		for(int i=1; i<numOfPlayers; i++){
			this.table.getPlayers().get(i).addCard(this.table.getNextCard());
			this.table.getPlayers().get(i).addCard(this.table.getNextCard());
		}
		
		this.table.dealFlop(false);
		this.table.dealTurn(false);
		this.table.dealRiver(false);
		
		
		
		return null;
	}
	
	public void printToFile(){
		
	}
	
	public void readFromFile(){
		
	}

}

package poker;

import java.io.IOException;
import java.util.ArrayList;

public class Player {
	
	private int playerID;
	private int money;
	private ArrayList<Card> wholeCards;
	private int currentBet;	//The current bet of the player
	public enum PlayerType{DEFENSIVE, NORMAL, AGGRESSIVE, INTELLIGENT};
	public enum Action{FOLD, CALL, RAISE};
	private PlayerType type;
	private int[] currentCardRating;
	private RolloutSimulator rSim;
	
	/*
	 * Creates a new Player instance with the specified ID, amount of money and playing style
	 */
	
	public Player(int ID, int money, PlayerType type){
		this.playerID = ID;
		this.money = money;
		this.type = type;
		this.currentBet = 0;
		this.wholeCards = new ArrayList<Card>();
		this.setCurrentCardRating(new int[] {0,0});
		try {
			this.rSim = new RolloutSimulator();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/*
	 * Public getters and setters
	 */
	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public ArrayList<Card> getCards() {
		return wholeCards;
	}
	
	public void resetCards() {
		this.wholeCards.clear();
	}
	

	public int getCurrentBet() {
		return currentBet;
	}


	public void setCurrentBet(int currentBet) {
		this.currentBet = currentBet;
	}


	public void addCard(Card card) {
		if(this.wholeCards.size() < 2){
			this.wholeCards.add(card);
		}
		else {
			System.out.println("Spilleren har allerede 2 kort");
		}
	}
	
	public void reduceMoney(int amount){
		this.money -= amount;
	}
	
	public void addMoney(int amount){
		this.money += amount;
	}
	
	public int[] getCurrentCardRating() {
		return currentCardRating;
	}

	public void setCurrentCardRating(int[] currentCardRating) {
		this.currentCardRating = currentCardRating;
	}
	
	public String printHand(){
		return this.wholeCards.get(0).toString() + " " + this.wholeCards.get(1).toString();
		
	}
	
	public void calculateCurrentRating(ArrayList<Card> cards, boolean log){
		if(log) {
			System.out.println("");		
			System.out.println("Spiller "+Integer.toString(this.playerID)+"'s totale hånd: "+cards.toString());	
		}
		CardRating rating = new CardRating();
		this.currentCardRating = rating.calcCardsPower(cards);
		
	}
	
	/*
	 * Method for deciding which action to take before the flop.
	 */
	public Action decidePreFlopAction(boolean allowedToFold, int numOfPlayers){
		
		if(this.type == PlayerType.INTELLIGENT){
			return smartPreFlopAction(allowedToFold, numOfPlayers);
		}
		
		else{
			return stupidPreFlopAction(allowedToFold);
		}
			
	}
	
	/*
	 * Method for deciding which action to take before the flop based on pure randomness.
	 */
	public Action stupidPreFlopAction(boolean allowedToFold){
		
		int randNum = (int)(Math.random()*3 + 1);
		
		Action action = Action.CALL;
		
		if(this.type == PlayerType.DEFENSIVE){
			
			if(randNum <= 2)
				action = Action.FOLD;
			else
				action = Action.CALL;
			
		}
		else if(this.type == PlayerType.NORMAL){
			
			if(randNum == 1)
				action = Action.FOLD;
			else if (randNum == 2)
				action = Action.CALL;
			else
				action = Action.RAISE;
			
		}
		else if(this.type == PlayerType.AGGRESSIVE){
			
			if(randNum >= 2)
				action = Action.RAISE;
			else
				action = Action.CALL;
		}
		
		//Player is not allowed to fold if he was the last player to raise.
		if(action == Action.FOLD && allowedToFold == false)
			action = Action.CALL;
		
		return action;
	}
	
	/*
	 * Method for deciding which action to take before the flop based on preflop rollout
	 */
	public Action smartPreFlopAction(boolean allowedToFold, int numOfPlayers){
		
		double winningProb = this.rSim.getProb(this.wholeCards, numOfPlayers);
		
		Action action;
		
		if(Math.pow(winningProb, (1/numOfPlayers)) > 0.6)
			action = Action.RAISE;
		else if(Math.pow(winningProb, (1/numOfPlayers)) > 0.3)
			action = Action.CALL;
		else
			action = Action.FOLD;
		
		if(!allowedToFold && action == Action.FOLD)
			action = Action.CALL;
		
		return action;
	}
	
	public Action decideAction(boolean allowedToFold, ArrayList<Card> sharedCards, boolean log, int numOfPLayers){
		
		if(this.type == PlayerType.INTELLIGENT)
			return smartDecideAction(allowedToFold, sharedCards, log, numOfPLayers);
		
		else
			return stupidDecideAction(allowedToFold, sharedCards, log);
	
	
	}
	
	
	/*
	 * Method for deciding action after flop, based on power rating
	 */
	public Action stupidDecideAction(boolean allowedToFold, ArrayList<Card> sharedCards, boolean log){
		
		Action action = Action.CALL;
		
		ArrayList<Card> cards = new ArrayList<Card>();
		cards.addAll(wholeCards);
		cards.addAll(sharedCards);
		calculateCurrentRating(cards, log);
		
		if(log)
			System.out.println("Spiller"+Integer.toString(getPlayerID())+" rating: "+currentCardRating[0]);
		
		if(this.type == PlayerType.DEFENSIVE){
			
			if(currentCardRating[0] < 3)
				action = Action.FOLD;
			else
				action = Action.CALL;
		}
		else if(this.type == PlayerType.NORMAL){
			
			if(currentCardRating[0] < 2)
				action = Action.FOLD;
			else if (currentCardRating[0] >= 2 && currentCardRating[0] < 5)
				action = Action.CALL;
			else
				action = Action.RAISE;
			
		}
		else if(this.type == PlayerType.AGGRESSIVE){
			
			if(currentCardRating[0] >= 4)
				action = Action.RAISE;
			else
				action = Action.CALL;
		}
		
		//Player is not allowed to fold if he was the last player to raise.
		if(action == Action.FOLD && allowedToFold == false)
			action = Action.CALL;
		
		return action;
	}
	
	
	/*
	 * Method for deciding action after flop, based on power rating
	 */
	public Action smartDecideAction(boolean allowedToFold, ArrayList<Card> sharedCards, boolean log, int numOfPlayers){
		
		Action action;
		double handStength = handStrength(sharedCards, numOfPlayers);
		
		if(handStength > 0.6)
			action = Action.RAISE;
		else if(handStength > 0.3)
			action = Action.CALL;
		else
			action = Action.FOLD;
		
		if(!allowedToFold && action == Action.FOLD)
			action = Action.CALL;
		
		return action;
		
	}
	
	public double handStrength(ArrayList<Card> sharedCards, int numOfPlayers){
		
		ArrayList<Card> hand = new ArrayList<Card>();
		hand.addAll(this.getCards());
		hand.addAll(sharedCards);
		calculateCurrentRating(hand, false);
		
		
		Deck deck = new Deck();
		for(Card c: hand){
			deck.getSpecificCard(c);
		}

		
		double wins = 0;
		double ties = 0;
		double losses = 0;
		
		for(Card c: deck.getCards()){
			for (int i = deck.getCards().indexOf(c)+1; i < deck.getCards().size(); i++) {
				ArrayList<Card> opponentHand = new ArrayList<Card>();
				opponentHand.addAll(sharedCards);
				opponentHand.add(c);
				opponentHand.add(deck.getCards().get(i));
				
				CardRating rating = new CardRating();
				int[]opponentHandRating = rating.calcCardsPower(opponentHand);
				
				if(this.currentCardRating[0]==opponentHandRating[0]){
					if(this.currentCardRating[1]==opponentHandRating[1]){
						ties++;
					}
					else if(this.currentCardRating[1]>opponentHandRating[1]){
						wins++;
					}
					else{
						losses++;
					}
				}
				else if(this.currentCardRating[0]>opponentHandRating[0]){
					wins++;
				}
				else{
					losses++;
				}
				
			}
		}
		
		return Math.pow(((wins+(ties/2))/(wins+ties+losses)), numOfPlayers);
	}
	
}

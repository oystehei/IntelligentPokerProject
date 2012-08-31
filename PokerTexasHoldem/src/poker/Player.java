package poker;

import java.util.ArrayList;

public class Player {
	
	private int playerID;
	private int money;
	private ArrayList<Card> wholeCards;
	private int currentBet;	//The current bet of the player
	public enum PlayerType{DEFENSIVE, NORMAL, AGGRESSIVE};
	public enum Action{FOLD, CALL, RAISE};
	private PlayerType type;
	
	/*
	 * Creates a new Player instance with the specified ID, amount of money and playing style
	 */
	
	public Player(int ID, int money, PlayerType type){
		this.playerID = ID;
		this.money = money;
		this.type = type;
		this.currentBet = 0;
		this.wholeCards = new ArrayList<Card>();
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
	
	public String printHand(){
		return this.wholeCards.get(0).toString() + " " + this.wholeCards.get(1).toString();
		
	}
	
	/*
	 * Method for deciding which action to take before the flop.
	 */
	public Action decidePreFlopAction(boolean allowedToFold){
		
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
	public Action decideAction(boolean allowedToFold, ArrayList<Card> sharedCards){
		
		Action action = Action.CALL;
		
		ArrayList<Card> cards = new ArrayList<Card>();
		cards.addAll(wholeCards);
		cards.addAll(sharedCards);
		
		System.out.println(cards.toString());
		
		CardRating rating = new CardRating();
		int[] cardsStrength = rating.calcCardsPower(cards);
		
		System.out.println(cardsStrength[0]);
		
		if(this.type == PlayerType.DEFENSIVE){
			
			if(cardsStrength[0] < 3)
				action = Action.FOLD;
			else
				action = Action.CALL;
		}
		else if(this.type == PlayerType.NORMAL){
			
			if(cardsStrength[0] < 2)
				action = Action.FOLD;
			else if (cardsStrength[0] >= 2 && cardsStrength[0] < 5)
				action = Action.CALL;
			else
				action = Action.RAISE;
			
		}
		else if(this.type == PlayerType.AGGRESSIVE){
			
			if(cardsStrength[0] >= 4)
				action = Action.RAISE;
			else
				action = Action.CALL;
		}
		
		//Player is not allowed to fold if he was the last player to raise.
		if(action == Action.FOLD && allowedToFold == false)
			action = Action.CALL;
		
		
		System.out.println(action.toString());
		return action;
	}

}

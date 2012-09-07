package poker;

import java.util.ArrayList;

public class Table {
	
	private int potSize;	//The size of the current pot on the table
	private ArrayList<Card> sharedCards;	//List of the shared cards currently on the table
	private ArrayList<Player> players;	//List over all the players
	private ArrayList<Player> activePlayers;	//List over the players currently active in a given round
	private int smallBlindID;		//ID of the player currently holding the small blind
	private int bigBlindID;	//ID of the player currently holding the big blind
	private int smallBlindAmount;		//The amount of the small blind
	private int bigBlindAmount;	//The amount of the big blind
	private int currentBet;	//The amount of the current bet
	private Deck deck;	//The deck on the table
	private ArrayList<Player> lastWinners;	//The player who won the last round
	
	public Table(){
		this.potSize = 0;
		this.sharedCards = new ArrayList<Card>();
		this.players = new ArrayList<Player>();
		this.activePlayers = new ArrayList<Player>();
		this.smallBlindID = 0;
		this.bigBlindID = 1;
		this.currentBet = 0;
		this.deck = new Deck();
		this.lastWinners = new ArrayList<Player>();
	}

	
	/*
	 * Public getters and setters
	 */
	public int getPotSize() {
		return potSize;
	}

	public void setPotSize(int potSize) {
		this.potSize = potSize;
	}
	
	public void addToPot( int amount){
		this.potSize += amount;
	}

	public ArrayList<Card> getSharedCards() {
		return sharedCards;
	}

	public void setSharedCards(ArrayList<Card> sharedCards) {
		this.sharedCards = sharedCards;
	}
	
	
	public ArrayList<Player> getPlayers() {
		return players;
	}
	
	public ArrayList<Player> getActivePlayers() {
		return activePlayers;
	}
	
	public void setActivePlayers(ArrayList<Player> activePlayers){
		this.activePlayers.clear();
		this.activePlayers.addAll(activePlayers);
	}
	
	public int getSmallBlindID() {
		return smallBlindID;
	}

	public void setNextSmallBlindID() {
		
		if(this.smallBlindID < this.players.size())
			this.smallBlindID++;
		else
			this.smallBlindID = 1;
	
	}

	public int getBigBlindID() {
		return bigBlindID;
	}

	public void setNextBigBlindID() {
	
		if(this.bigBlindID < this.players.size())
			this.bigBlindID++;
		else
			this.bigBlindID = 1;
		
	}

	public int getSmallBlindAmount() {
		return smallBlindAmount;
	}


	public void setSmallBlindAmount(int smallBlindAmount) {
		this.smallBlindAmount = smallBlindAmount;
	}


	public int getBigBlindAmount() {
		return bigBlindAmount;
	}


	public void setBigBlindAmount(int bigBlindAmount) {
		this.bigBlindAmount = bigBlindAmount;
	}

	public int getCurrentBet() {
		return currentBet;
	}

	public void setCurrentBet(int currentBet) {
		this.currentBet = currentBet;
	}


	public Deck getDeck() {
		return deck;
	}

	public void setDeck(Deck deck) {
		this.deck = deck;
	}
	
	
	public ArrayList<Player> getLastWinners() {
		return lastWinners;
	}


	public void setLastWinner(ArrayList<Player> lastWinners) {
		this.lastWinners = lastWinners;
	}


	public Card getNextCard(){
		return this.deck.dealCard();
	}
	
	public void clearTable(){
		this.sharedCards.clear();
		this.potSize = 0;
	}
	
	public void addPlayer(Player player){
		this.players.add(player);
	}
	
	public void dealCards(){
		this.deck.shuffleDeck();
		
		for(Player player: players){
			player.addCard(getNextCard());
		}
		for(Player player: players){
			player.addCard(getNextCard());
		}
	}
	
	
	/*
	 * Start new round
	 */
	public void startNewRound(){
		setPotSize(0);
		setNextSmallBlindID();
		setNextBigBlindID();
		this.activePlayers.addAll(getOrderOfPlayers(this.bigBlindID));
		setCurrentBet(this.bigBlindAmount);
		
		this.players.get(this.smallBlindID-1).reduceMoney(this.smallBlindAmount);
		this.players.get(this.smallBlindID-1).setCurrentBet(this.smallBlindAmount);
		
		this.players.get(this.bigBlindID-1).reduceMoney(this.bigBlindAmount);
		this.players.get(this.bigBlindID-1).setCurrentBet(this.bigBlindAmount);
		
		addToPot(this.smallBlindAmount + this.bigBlindAmount);
		dealCards();
	}
	
	
	/*
	 * Only one player left, give him the pot and reset the table
	 */
	public void endRound(ArrayList<Player> winners){
		this.lastWinners.clear();
		this.lastWinners.addAll(winners);
		if(winners.size()>1){
			for (Player player : winners) {
				player.addMoney(getPotSize()/winners.size());
			}
		}
		else{
			winners.get(0).addMoney(getPotSize());
		}
		this.activePlayers.clear();
		this.sharedCards.clear();
		
		for(Player player: this.getPlayers()){
			player.resetCards();
		}
		
		this.deck = new Deck();
	}
	
	public void dealFlop(boolean log){
		this.sharedCards.add(getNextCard());
		this.sharedCards.add(getNextCard());
		this.sharedCards.add(getNextCard());
		
		if(log)
			System.out.println("Sharedcards etter flop: "+this.sharedCards.toString());
	}
	
	public void dealTurn(boolean log){
		this.sharedCards.add(getNextCard());
		
		if(log)
			System.out.println("Sharedcards etter turn: "+this.sharedCards.toString());
	}
	
	public void dealRiver(boolean log){
		this.sharedCards.add(getNextCard());
		
		if(log)
			System.out.println("Sharedcards etter river: "+this.sharedCards.toString());
	}
	
	/*
	 * Returns a list of all players in the order of betting for a certain round. The first one to bet is the person to the left of the big blind.
	 */
	public ArrayList<Player> getOrderOfPlayers(int bigBlind){
		
		ArrayList<Player> bettingOrder = new ArrayList<Player>();
		
		if(bigBlind == this.players.size())
			return this.players;
		
		else{
			for(int i=bigBlind +1; i <= this.players.size(); i++){
				bettingOrder.add(this.players.get(i-1));
			}
			
			for(int i=1; i<=bigBlind; i++){
				bettingOrder.add(this.players.get(i-1));
			}
		}
		
		return bettingOrder;
		
	}
	
	
	public String printActivePlayers(){
		
		String active ="";
		
		for(Player player: this.activePlayers){
			active += "Spiller" + Integer.toString(player.getPlayerID()) + ", ";
		}
		
		return active;
	}
	
	
public void initiateShowdown(boolean log){
		
		ArrayList<Player> winner = new ArrayList<Player>(); 
		winner.add(this.getActivePlayers().get(0));
		int highestRating = 0;
		for(Player player : this.getActivePlayers()){
			
			ArrayList<Card> cards = new ArrayList<Card>();
			cards.addAll(player.getCards());
			cards.addAll(sharedCards);
			player.calculateCurrentRating(cards, false);
						
			if(log)
				System.out.println("Spiller"+player.getPlayerID()+" viser "+player.getCards().toString()+" med en rating på: "+player.getCurrentCardRating()[0]+", og highcard:"+player.getCurrentCardRating()[1]);
			
			if(highestRating < player.getCurrentCardRating()[0]){
				winner.clear();
				winner.add(player);
				highestRating = player.getCurrentCardRating()[0];
			}
			else if(highestRating == player.getCurrentCardRating()[0]){
				boolean tie = true;
				for (int i = 1; i < player.getCurrentCardRating().length; i++) {
					if(player.getCurrentCardRating()[i] > winner.get(0).getCurrentCardRating()[i]){
						winner.clear();
						winner.add(player);
						highestRating = player.getCurrentCardRating()[0];
						tie = false;
						break;
					}
					else if(player.getCurrentCardRating()[i] < winner.get(0).getCurrentCardRating()[i]){
						tie = false;
						break;
					}
					
				}
				if(tie){
					winner.add(player);
				}
				
			}	
		}
		this.getActivePlayers().retainAll(winner);
	}
	
	
	

}

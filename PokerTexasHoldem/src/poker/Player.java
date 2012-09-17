package poker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import poker.OpponentModeller.PlayerModelTriplet;

public class Player {


	private int playerID;
	private int money;
	private ArrayList<Card> wholeCards;
	private int currentBet;	//The current bet of the player
	public enum PlayerType{DEFENSIVE, NORMAL, AGGRESSIVE, INTELLIGENT, VERYINTELLIGENTBEST, VERYINTELLIGENTAVG};
	public enum Action{FOLD, CALL, RAISE};
	private PlayerType type;
	private int[] currentCardRating;
	private RolloutSimulator rSim;
	private HashMap<PlayerModelTriplet, Double> actionContext;
	private Action lastAction;
	private boolean bluffing;
	public int bluffCount;

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
		this.actionContext = new HashMap<PlayerModelTriplet, Double>();
		this.lastAction = null;
		this.bluffing = false;
		this.bluffCount = 0;
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

	public Action getLastAction() {
		return lastAction;
	}

	public boolean isBluffing() {
		return bluffing;
	}


	public void setBluffing(boolean bluffing) {
		this.bluffing = bluffing;
	}


	public void setLastAction(Action lastAction) {
		this.lastAction = lastAction;
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

	public HashMap<PlayerModelTriplet, Double> getActionContext() {
		return actionContext;
	}


	public void setActionContext(HashMap<PlayerModelTriplet, Double> actionContext) {
		this.actionContext = actionContext;
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
	public Action decidePreFlopAction(boolean allowedToFold, Table table, OpponentModeller opModeller, int numOfRaises, boolean gatherStats){



		if(this.type == PlayerType.VERYINTELLIGENTBEST && !gatherStats || this.type == PlayerType.VERYINTELLIGENTAVG && !gatherStats){
			return verySmartPreFlopAction(allowedToFold, table, opModeller, numOfRaises);
		}
		else if(this.type == PlayerType.INTELLIGENT || this.type == PlayerType.VERYINTELLIGENTBEST || this.type == PlayerType.VERYINTELLIGENTBEST ){
			return smartPreFlopAction(allowedToFold, table.getActivePlayers().size(), numOfRaises);
		}
		else{
			return stupidPreFlopAction(allowedToFold);
		}

	}

	/*
	 * Method for deciding which action to take before the flop based on pure randomness.
	 */
	public Action stupidPreFlopAction(boolean allowedToFold){

		int randNum = (int)(Math.random()*5 + 1);

		Action action = Action.CALL;

		if(this.type == PlayerType.DEFENSIVE){

			if(randNum <= 3)
				action = Action.FOLD;
			else if(randNum == 4)
				action = Action.CALL;
			else
				action = Action.RAISE;

		}
		else if(this.type == PlayerType.NORMAL){

			if(randNum <= 2)
				action = Action.FOLD;
			else if (randNum <= 4)
				action = Action.CALL;
			else
				action = Action.RAISE;

		}
		else if(this.type == PlayerType.AGGRESSIVE){

			if(randNum == 1)
				action = Action.FOLD;
			else if(randNum <=3)
				action = Action.CALL;
			else
				action = Action.RAISE;
		}

		//Player is not allowed to fold if he was the last player to raise.
		if(action == Action.FOLD && allowedToFold == false)
			action = Action.CALL;

		return action;
	}

	/*
	 * Method for deciding which action to take before the flop based on preflop rollout
	 */
	public Action smartPreFlopAction(boolean allowedToFold, int numOfPlayers, int numOfRaises){

		double winningProb = this.rSim.getProb(this.wholeCards, numOfPlayers);
		
		double avgWinningProb = (double) 1.0/numOfPlayers;

		Action action;
		
		if(this.bluffing){
			action = Action.RAISE;
		}
		
		else if(numOfRaises != 0){
			if(winningProb > (avgWinningProb*(1.0 + ((double) 0.1 * numOfPlayers))))
				action = Action.RAISE;
			else if(winningProb > (avgWinningProb*1.0))
				action = Action.CALL;
			
			else
				action = Action.FOLD;
		}
		else{
			if(winningProb > (avgWinningProb*(0.7 + ((double) 0.1 * numOfPlayers))))
				action = Action.RAISE;
			else if(winningProb > (avgWinningProb*0.6))
				action = Action.CALL;
			/*
			else if(winningProb < (avgWinningProb*0.05)){
				this.bluffCount++;
				this.bluffing = true;
				action = Action.RAISE;
			}
			*/
			else
				action = Action.FOLD;
		}

		if(!allowedToFold && action == Action.FOLD)
			action = Action.CALL;

		return action;
	}

	/*
	 * Method for deciding which action to take before the flop based on preflop rollout and opponent modelling
	 */
	public Action verySmartPreFlopAction(boolean allowedToFold, Table table, OpponentModeller opModeller, int numOfRaises){

		double winningProb = this.rSim.getProb(this.wholeCards, table.getActivePlayers().size());
		double avgWinningProb = (double) (1.0/table.getActivePlayers().size());

		Action action;

		Double avgOpponentStrength = 0.0;
		int numPlayerStats = 0;
		Double bestStrength = 0.0;

		for(Player player: table.getActivePlayers()){
			if(player.getLastAction()!= null && player != this){
				if(opModeller.finishedModel.containsKey(opModeller.new PlayerModelTriplet(table.getContext(player, numOfRaises), player.getLastAction(), player))) {
					Double [] contextStrength = opModeller.getStrength(opModeller.new PlayerModelTriplet(table.getContext(player, numOfRaises), player.getLastAction(), player));
					avgOpponentStrength += contextStrength[0] * contextStrength[1];
					numPlayerStats += contextStrength[1];
					if(contextStrength[0] > bestStrength)
						bestStrength = contextStrength[0];
				}
			}
		}
		if(numPlayerStats!=0){
			if(this.type == PlayerType.VERYINTELLIGENTAVG){
				avgOpponentStrength = avgOpponentStrength/numPlayerStats;
				
				if((winningProb/avgWinningProb) > avgOpponentStrength){
					action = Action.RAISE;
				}
				else if((winningProb/avgWinningProb) > ((double)avgOpponentStrength*table.getBettingRound())/4.0){
					action = Action.CALL;
				}
				else{
					if(allowedToFold)
						action = Action.FOLD;
					else
						action = Action.CALL;
				}
			}
			else {
				avgOpponentStrength = avgOpponentStrength/numPlayerStats;
				
				if((winningProb/avgWinningProb) > bestStrength){
					action = Action.RAISE;
				}
				else if((winningProb/avgWinningProb) > ((double)bestStrength*table.getBettingRound())/4.0){
					action = Action.CALL;
				}
				else{
					if(allowedToFold)
						action = Action.FOLD;
					else
						action = Action.CALL;
				}
			}
		}
		else{
			
			if(numOfRaises != 0){
				if(winningProb > (avgWinningProb*(1.0 + ((double) 0.1 * table.getActivePlayers().size()))))
					action = Action.RAISE;
				else if(winningProb > (avgWinningProb*1.0))
					action = Action.CALL;
				
				else
					action = Action.FOLD;
			}
			else{
				if(winningProb > (avgWinningProb*(0.7 + ((double) 0.1 * table.getActivePlayers().size()))))
					action = Action.RAISE;
				else if(winningProb > (avgWinningProb*0.6))
					action = Action.CALL;
				
				else
					action = Action.FOLD;
				
			}
		}

		return action;
	}

	public Action decideAction(boolean allowedToFold, ArrayList<Card> sharedCards, boolean log, int numOfPLayers, Table table, int numOfRaises, OpponentModeller opModeller, boolean gatherStats){


		if(this.type == PlayerType.VERYINTELLIGENTBEST && !gatherStats || this.type == PlayerType.VERYINTELLIGENTAVG && !gatherStats)
			return verySmartDecideAction(allowedToFold, sharedCards, log, table, numOfRaises, opModeller);
		else if(this.type == PlayerType.INTELLIGENT || this.type == PlayerType.VERYINTELLIGENTBEST || this.type == PlayerType.VERYINTELLIGENTAVG)
			return smartDecideAction(allowedToFold, sharedCards, log, numOfPLayers, numOfRaises);
		else
			return stupidDecideAction(allowedToFold, sharedCards, log, table, numOfRaises);


	}

	public void saveContex(Table table, Action playerAction, int numOfRaises, OpponentModeller opModeller, boolean preFlop){

		Double strength = 0.0;
		double avgWinningProb = (double) (1.0/table.getActivePlayers().size());
		Double percentageOfAvgProb;

		if(preFlop){
			strength = rSim.getProb(this.wholeCards, table.getActivePlayers().size());
			percentageOfAvgProb = strength/avgWinningProb;
		}
		else {
			strength = handStrength(table.getSharedCards(), table.getActivePlayers().size());
			percentageOfAvgProb = strength/avgWinningProb;
		}
		this.getActionContext().put(opModeller.new PlayerModelTriplet(table.getContext(this, numOfRaises), playerAction, this), percentageOfAvgProb);
	}


	/*
	 * Method for deciding action after flop, based on power rating
	 */
	public Action stupidDecideAction(boolean allowedToFold, ArrayList<Card> sharedCards, boolean log, Table table, int numOfRaises){

		Action action = Action.CALL;

		ArrayList<Card> cards = new ArrayList<Card>();
		cards.addAll(wholeCards);
		cards.addAll(sharedCards);
		calculateCurrentRating(cards, log);
		double potOdds = table.getPotOdds(this.currentBet);

		if(log)
			System.out.println("Spiller"+Integer.toString(getPlayerID())+" rating: "+currentCardRating[0]);

		if(this.type == PlayerType.DEFENSIVE){
			
			if(numOfRaises > 0 || potOdds > 0.1){
				if(currentCardRating[0] > 7)
					action = Action.CALL;
				else
					action = Action.FOLD;
			}
			else {
			
				if(currentCardRating[0] > 7)
					action = Action.RAISE;
				else if(currentCardRating[0] > 3)
					action = Action.CALL;
				else
					action = Action.FOLD;
			}
		}
		else if(this.type == PlayerType.NORMAL){

			if(numOfRaises > 0 || potOdds > 0.1){
				if(currentCardRating[0] > 5)
					action = Action.CALL;
				else
					action = Action.FOLD;
			}
			else {
			
				if(currentCardRating[0] > 5)
					action = Action.RAISE;
				else if(currentCardRating[0] > 3)
					action = Action.CALL;
				else
					action = Action.FOLD;
			}

		}
		else if(this.type == PlayerType.AGGRESSIVE){

			if(numOfRaises > 0 || potOdds > 0.1){
				if(currentCardRating[0] > 6)
					action = Action.RAISE;
				else if(currentCardRating[0] > 3)
					action = Action.CALL;
				else
					action = Action.FOLD;
			}
			else {
			
				if(currentCardRating[0] > 4)
					action = Action.RAISE;
				else if(currentCardRating[0] > 2)
					action = Action.CALL;
				else
					action = Action.FOLD;
			}
		}

		//Player is not allowed to fold if he was the last player to raise.
		if(action == Action.FOLD && allowedToFold == false)
			action = Action.CALL;

		return action;
	}


	/*
	 * Method for deciding action after flop, based on live rollouts
	 */
	public Action smartDecideAction(boolean allowedToFold, ArrayList<Card> sharedCards, boolean log, int numOfPlayers, int numOfRaises){

		Action action;
		double handStrength = handStrength(sharedCards, numOfPlayers);
		double avgWinningProb = (double) 1.0/numOfPlayers;
				
		if(this.bluffing){
			action = Action.RAISE;
		}
		
		else if(numOfRaises != 0){
			if(handStrength > (avgWinningProb*(1.0 + ((double) 0.1 * numOfPlayers))))
				action = Action.RAISE;
			else if(handStrength > (avgWinningProb*1.0))
				action = Action.CALL;
			
			else
				action = Action.FOLD;
		}
		else{
			if(handStrength > (avgWinningProb*(0.7 + ((double) 0.1 * numOfPlayers))))
				action = Action.RAISE;
			else if(handStrength > (avgWinningProb*0.6))
				action = Action.CALL;
			
			/*
			else if(handStrength < (avgWinningProb*0.05)){
				this.bluffCount++;
				this.bluffing = true;
				action = Action.RAISE;
			}
			*/
			else
				action = Action.FOLD;
		}

		if(!allowedToFold && action == Action.FOLD)
			action = Action.CALL;

		return action;

	}

	/*
	 * Method for deciding action after flop, based on live rollouts and opponent modelling
	 */
	public Action verySmartDecideAction(boolean allowedToFold, ArrayList<Card> sharedCards, boolean log, Table table, int numOfRaises, OpponentModeller opModeller){

		Action action;
		double handStrength = handStrength(table.getSharedCards(), table.getActivePlayers().size());
		double avgWinningProb = (double)(1.0/table.getActivePlayers().size());

		Double avgOpponentStrength = 0.0;
		int numPlayerStats = 0;
		Double bestStrength = 0.0;

		for(Player player: table.getActivePlayers()){
			if(player.getLastAction()!= null && player != this){
				if(opModeller.finishedModel.containsKey(opModeller.new PlayerModelTriplet(table.getContext(player, numOfRaises), player.getLastAction(), player))) {
					Double [] contextStrength = opModeller.getStrength(opModeller.new PlayerModelTriplet(table.getContext(player, numOfRaises), player.getLastAction(), player));
					avgOpponentStrength += contextStrength[0] * contextStrength[1];
					numPlayerStats += contextStrength[1];
					if(contextStrength[0] > bestStrength)
						bestStrength = contextStrength[0];
				}
			}
		}
		if(numPlayerStats!=0){
			if(this.type == PlayerType.VERYINTELLIGENTAVG){
				avgOpponentStrength = avgOpponentStrength/numPlayerStats;
				
				if((handStrength/avgWinningProb) > avgOpponentStrength){
					action = Action.RAISE;
				}
				else if((handStrength/avgWinningProb) > ((double)avgOpponentStrength*table.getBettingRound())/4.0){
					action = Action.CALL;
				}
				else{
					if(allowedToFold)
						action = Action.FOLD;
					else
						action = Action.CALL;
				}
			}
			else {
				avgOpponentStrength = avgOpponentStrength/numPlayerStats;
				
				if((handStrength/avgWinningProb) > bestStrength){
					action = Action.RAISE;
				}
				else if((handStrength/avgWinningProb) > ((double)bestStrength*table.getBettingRound())/4.0){
					action = Action.CALL;
				}
				else{
					if(allowedToFold)
						action = Action.FOLD;
					else
						action = Action.CALL;
				}
			}
		}
		else{

			if(numOfRaises != 0){
				if(handStrength > (avgWinningProb*(1.0 + ((double) 0.1 * table.getActivePlayers().size()))))
					action = Action.RAISE;
				else if(handStrength > (avgWinningProb*1.0))
					action = Action.CALL;
				
				else
					action = Action.FOLD;
			}
			else{
				if(handStrength > (avgWinningProb*(0.7 + ((double) 0.1 * table.getActivePlayers().size()))))
					action = Action.RAISE;
				else if(handStrength > (avgWinningProb*0.6))
					action = Action.CALL;
				
				else
					action = Action.FOLD;
			}

			if(!allowedToFold && action == Action.FOLD)
				action = Action.CALL;

		}

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
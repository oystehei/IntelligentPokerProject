package poker;

import java.util.ArrayList;

import poker.Player.Action;
import poker.Player.PlayerType;


public class PokerSimulator {
	
	private Table table;
	private int roundNumber;
	private int raiseAmount;	//The fixed amount of the raise
	private int numOfRaises;	//The current number of raises
	private int maxRaises;	//The max amount of raises each round
	private PlayerType[] types = new PlayerType[] {PlayerType.DEFENSIVE, PlayerType.NORMAL, PlayerType.AGGRESSIVE, PlayerType.DEFENSIVE, PlayerType.NORMAL, PlayerType.AGGRESSIVE, PlayerType.DEFENSIVE, PlayerType.NORMAL, PlayerType.AGGRESSIVE, PlayerType.DEFENSIVE};
	
	
	/*
	 * Description
	 */
	public PokerSimulator(int numOfPlayers, int startingMoney, int smallBlind, int bigBlind, int raiseAmount, int maxRaises){
		this.table = new Table();
		this.roundNumber = 0;
		
		this.raiseAmount = raiseAmount;
		this.maxRaises = maxRaises;
		this.table.setSmallBlindAmount(smallBlind);
		this.table.setBigBlindAmount(bigBlind);
		
		
		if(numOfPlayers >= 2 && numOfPlayers <= 10)
			for(int i=1; i <= numOfPlayers; i++){
				this.table.addPlayer(new Player(i, startingMoney, types[i-1]));
			}
		else
			System.out.println("Antallet spillere skal være mellom 2 og 10");
	}
	
	public int getNumOfRaises() {
		return numOfRaises;
	}



	public void setNumOfRaises(int numOfRaises) {
		this.numOfRaises = numOfRaises;
	}



	public void startNewRound(){
		this.roundNumber++;
		this.table.startNewRound();
		
	}
	
	
	public boolean initiatePreFlopBetting(){
		
		boolean bettingEnded = false;
		int raisePlayer = -1;	//ID of the player with the latest raise
		
		while(!bettingEnded){
			
			ArrayList<Player> activePlayers = new ArrayList<Player>();
			activePlayers.addAll(this.table.getActivePlayers());
			
						
			for(Player player: activePlayers){
				
				boolean allowedToFold = !((player.getPlayerID() == table.getBigBlindID() && raisePlayer==-1) || player.getPlayerID() == raisePlayer);
				
				Action playerAction = player.decidePreFlopAction(allowedToFold);
				
				if(playerAction == Action.FOLD){
										
					this.table.getActivePlayers().remove(player);
					
					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " kastet seg.");
					
					if(this.table.getActivePlayers().size()==1){
						bettingEnded = true;
						this.table.endRound();
						return true;
					}
						
				}
				
				else if(playerAction == Action.CALL){
					/*
					 * If the player who made the last raise calls, the betting round is over
					 */
					if(player.getPlayerID() == raisePlayer){
						bettingEnded = true;
						break;
					}
					
					player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
					this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
					player.setCurrentBet(this.table.getCurrentBet());
					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " ser.");
					
				}
				
				else if(playerAction == Action.RAISE){
					
					if(this.numOfRaises == this.maxRaises){
						/*
						 * If the player who made the last raise calls, the betting round is over
						 */
						if(player.getPlayerID() == raisePlayer){
							bettingEnded = true;
							break;
						}
						
						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
						player.setCurrentBet(this.table.getCurrentBet());
						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " ser.");
						
						
						
					}
					else{
						/*
						 * If the player who made the last raise calls, the betting round is over
						 */
						if(player.getPlayerID() == raisePlayer){
							bettingEnded = true;
							break;
						}
						
						table.setCurrentBet(this.table.getCurrentBet() + this.raiseAmount);
						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
						player.setCurrentBet(this.table.getCurrentBet());
						this.numOfRaises++;
						raisePlayer = player.getPlayerID();
						
						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " høynet.");
						
					}
				
				}
				
				
			}
				
		}
		
		return false;
		
	}
	
	
	/*
	 * Method for initiating betting between players in after the flop, turn and river cards
	 */
	public boolean initiateBetting(){
		
		boolean bettingEnded = false;
		Player raisePlayer = null;	//ID of the player with the latest raise
		
		while(!bettingEnded){
			
			ArrayList<Player> activePlayers = new ArrayList<Player>();
			activePlayers.addAll(this.table.getActivePlayers());
			
						
			for(Player player: activePlayers){
				
				boolean allowedToFold = !(player == raisePlayer || raisePlayer == null);
				
				Action playerAction = player.decideAction(allowedToFold, this.table.getSharedCards());
												
				if(playerAction == Action.FOLD){
										
					this.table.getActivePlayers().remove(player);
					
					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " kastet seg.");
					
					if(this.table.getActivePlayers().size()==1){
						bettingEnded = true;
						this.table.endRound();
						return true;
					}
						
				}
				
				else if(playerAction == Action.CALL){
					/*
					 * If the player who made the last raise calls, the betting round is over
					 */
					if(player == raisePlayer){
						bettingEnded = true;
						break;
					}
					
					player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
					this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
					player.setCurrentBet(this.table.getCurrentBet());
					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " ser.");
				}
				
				else if(playerAction == Action.RAISE){
					
					if(this.numOfRaises == this.maxRaises){
						/*
						 * If the player who made the last raise calls, the betting round is over
						 */
						if(player == raisePlayer){
							bettingEnded = true;
							break;
						}
						
						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
						player.setCurrentBet(this.table.getCurrentBet());
						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " ser.");
						
						
						
					}
					else{
						/*
						 * If the player who made the last raise calls, the betting round is over
						 */
						if(player == raisePlayer){
							bettingEnded = true;
							break;
						}
						
						table.setCurrentBet(this.table.getCurrentBet() + this.raiseAmount);
						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
						player.setCurrentBet(this.table.getCurrentBet());
						this.numOfRaises++;
						raisePlayer = player;
						
						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " høynet.");
						
					}
				
				}
				
				
			}
			
			if(raisePlayer == null)
				raisePlayer = this.table.getActivePlayers().get(0);
				
		}
		
		return false;
		
	}
	
	public void printTable(){
		System.out.println("Antall spillere: " + Integer.toString(this.table.getPlayers().size()));
		System.out.println("Runde nr.: " + Integer.toString(this.roundNumber));
		
		for(Player player: this.table.getPlayers()){
			System.out.println("Spiller: " + Integer.toString(player.getPlayerID()) + " Hånd: " + player.printHand());
		}
	}
	
	
	public static void main(String args[]){
		
		PokerSimulator pokerSim = new PokerSimulator(6, 2000, 50, 100, 50, 2);
		pokerSim.startNewRound();
		pokerSim.printTable();
		
		//Initiate pre-flop betting
		if(pokerSim.initiatePreFlopBetting())
			System.out.println("Runden er over, Spiller" + Integer.toString(pokerSim.table.getLastWinner().getPlayerID()) + " vant " + Integer.toString(pokerSim.table.getPotSize()) + "kr");
		else {
			System.out.println("Spillere fremdeles aktiv: " + pokerSim.table.printActivePlayers());
			System.out.println("Størrelsen på potten: " + Integer.toString(pokerSim.table.getPotSize()));
		}
		
		pokerSim.setNumOfRaises(0);
		pokerSim.table.dealFlop();
		//Initiate betting after flop
		if(pokerSim.initiateBetting())
			System.out.println("Runden er over, Spiller" + Integer.toString(pokerSim.table.getActivePlayers().get(0).getPlayerID()) + " vant " + Integer.toString(pokerSim.table.getPotSize()) + "kr");
		else {
			System.out.println("Spillere fremdeles aktiv etter flopp: " + pokerSim.table.printActivePlayers());
			System.out.println("Størrelsen på potten: " + Integer.toString(pokerSim.table.getPotSize()));
		}
		
		
		pokerSim.setNumOfRaises(0);
		pokerSim.table.dealTurn();
		//Initiate betting after turn
		if(pokerSim.initiateBetting())
			System.out.println("Runden er over, Spiller" + Integer.toString(pokerSim.table.getActivePlayers().get(0).getPlayerID()) + " vant " + Integer.toString(pokerSim.table.getPotSize()) + "kr");
		else {
			System.out.println("Spillere fremdeles aktiv etter turn: " + pokerSim.table.printActivePlayers());
			System.out.println("Størrelsen på potten: " + Integer.toString(pokerSim.table.getPotSize()));
		}
		
		
		pokerSim.setNumOfRaises(0);
		pokerSim.table.dealRiver();
		//Initiate betting after river
		if(pokerSim.initiateBetting())
			System.out.println("Runden er over, Spiller" + Integer.toString(pokerSim.table.getActivePlayers().get(0).getPlayerID()) + " vant " + Integer.toString(pokerSim.table.getPotSize()) + "kr");
		else {
			System.out.println("Spillere fremdeles aktiv etter river: " + pokerSim.table.printActivePlayers());
			System.out.println("Størrelsen på potten: " + Integer.toString(pokerSim.table.getPotSize()));
		}
		
		
		
		
		
				
	}

}

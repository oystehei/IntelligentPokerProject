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
				this.table.addPlayer(new Player(i, startingMoney, PlayerType.NORMAL));
			}
		else
			System.out.println("Antallet spillere skal være mellom 2 og 10");
	}
	
	public void startNewRound(){
		this.roundNumber++;
		this.table.startNewRound();
		
	}
	
	
	public void initiatePreFlopBetting(){
		
		boolean bettingEnded = false;
		int raisePlayer = this.table.getBigBlindID();	//ID of the player with the latest raise
		
		while(!bettingEnded){
			
			ArrayList<Player> activePlayers = new ArrayList<Player>();
			activePlayers.addAll(this.table.getActivePlayers());
			
						
			for(Player player: activePlayers){
				
				boolean allowedToFold = !(player.getPlayerID() == raisePlayer);
				Action playerAction = player.decidePreFlopAction(allowedToFold);
				
				if(playerAction == Action.FOLD){
										
					this.table.getActivePlayers().remove(player);
					
					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " kastet seg.");
					
					if(this.table.getActivePlayers().size()==1){
						bettingEnded = true;
						this.table.endRound();
						break;
					}
						
				}
				
				else if(playerAction == Action.CALL){
					player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
					this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
					
					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " callet.");
					
					/*
					 * If the player who made the last raise calls, the betting round is over
					 */
					if(player.getPlayerID() == raisePlayer){
						bettingEnded = true;
						break;
					}
				}
				
				else if(playerAction == Action.RAISE){
					
					if(this.numOfRaises == this.maxRaises){
						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
						
						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " callet.");
						
						/*
						 * If the player who made the last raise calls, the betting round is over
						 */
						if(player.getPlayerID() == raisePlayer){
							bettingEnded = true;
						}
						
					}
					else{
						table.setCurrentBet(this.table.getCurrentBet() + this.raiseAmount);
						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
						this.numOfRaises++;
						raisePlayer = player.getPlayerID();
						
						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " høynet.");
					}
				
				}
				
				
			}
				
		}
		
		System.out.println("Spillere fremdeles aktiv: " + this.table.printActivePlayers());
		System.out.println("Størrelsen på potten: " + Integer.toString(this.table.getPotSize()));
		
	}
	
	public void printTable(){
		System.out.println("Antall spillere: " + Integer.toString(this.table.getPlayers().size()));
		System.out.println("Runde nr.: " + Integer.toString(this.roundNumber));
		
		for(Player player: this.table.getPlayers()){
			System.out.println("Spiller: " + Integer.toString(player.getPlayerID()) + " Hånd: " + player.printHand());
		}
	}
	
	
	public static void main(String args[]){
		
		PokerSimulator pokerSim = new PokerSimulator(4, 2000, 50, 100, 50, 2);
		pokerSim.startNewRound();
		pokerSim.printTable();
		
		pokerSim.initiatePreFlopBetting();
				
	}

}

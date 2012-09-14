package poker;

import poker.Player.Action;
import poker.Player.PlayerType;


public class OpponentModeller {
	
	
	public class PlayerModelTriplet{
		
		public Context context;
		public Action action;
		public Player player:
		
		public PlayerModelTriplet(Context context, Action action, Player player){
			this.context = context;
			this.action = action;
			this.player = player;
		}
		
		@Override
		public int hashCode(){
			return (this.action.hashCode() * 31) * this.context.hashCode()+ this.player.hashCode();
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof PlayerModelTriplet) {
				PlayerModelTriplet plaModTri = (PlayerModelTriplet) obj;
				return (this.context == plaModTri.context && this.action == plaModTri.action && this.player == plaModTri.player);
			}
			else
				return false;
		}
		
	}
	
}

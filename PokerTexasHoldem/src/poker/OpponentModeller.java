package poker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import poker.Player.Action;
import poker.Table.Context;


public class OpponentModeller {


	public class PlayerModelTriplet{

		public Context context;
		public Action action;
		public Player player;

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

		public String toString(){
			return (this.context+", "+this.action+", player"+this.player.getPlayerID());
		}

	}

	public HashMap<PlayerModelTriplet, ArrayList<Double>> model;
	public HashMap<PlayerModelTriplet, Double[]> finishedModel;

	public OpponentModeller(){
		this.model = new HashMap<PlayerModelTriplet, ArrayList<Double>>();
		this.finishedModel = new HashMap<PlayerModelTriplet, Double[]>();
	}

	public void saveContex(PlayerModelTriplet plaModTri, double handStrength){
		if(this.model.containsKey(plaModTri)){

			ArrayList<Double> temp = this.model.get(plaModTri);
			temp.add(handStrength);
			this.model.put(plaModTri, temp);

		}
		else{
			ArrayList<Double> handStrengths = new ArrayList<Double>();
			handStrengths.add(handStrength);
			this.model.put(plaModTri, handStrengths);
		}
	}

	public void finishModel(){
		Set<PlayerModelTriplet> contexts = this.model.keySet();

		Iterator<PlayerModelTriplet> itr = contexts.iterator();
		while(itr.hasNext()){
			PlayerModelTriplet triplet = itr.next();
			ArrayList <Double> strengths = this.model.get(triplet);
			Double total = 0.0;
			for(Double strength: strengths){
				total += strength;
			}
			this.finishedModel.put(triplet, new Double[]{total/strengths.size(), (double) strengths.size()});
		}
	}

	public Double[] getStrength(PlayerModelTriplet triplet){
		if(this.finishedModel.containsKey(triplet))
			return this.finishedModel.get(triplet);
		else
			return null;
	}



}
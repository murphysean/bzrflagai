package com.murphysean.bzrflag.commanders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.murphysean.bzrflag.agents.GoToAgent;
import com.murphysean.bzrflag.events.BZRFlagEvent;
import com.murphysean.bzrflag.events.OccGridCompleteEvent;
import com.murphysean.bzrflag.events.OccGridEvent;
import com.murphysean.bzrflag.models.Game;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.bzrflag.models.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OccGridCommander extends AbstractCommander{
	@JsonIgnore
	protected transient List<List<Float>> occGrid;
	@JsonIgnore
	protected static transient List<Map> maps;

	public OccGridCommander(){
		super();
	}

	@Override
	public void setGame(Game game){
		super.setGame(game);

		//Set up my team right here
		for(int i = 0; i < playerCount; i++){
			GoToAgent goToAgent = new GoToAgent();
			goToAgent.setGame(game);
			goToAgent.setId(i);
			goToAgent.setCallsign(game.getTeam().getColor() + i);
			goToAgent.setTeamColor(game.getTeam().getColor());
			goToAgent.setTarget(game.getTeams().get(i % game.getTeams().size()).getBase().getCenterPoint());
			tanks.add(goToAgent);
		}

		occGrid = new ArrayList<List<Float>>();
		for(int i = 0; i < game.getWorldSize(); i++){
			occGrid.add(new ArrayList<Float>());
			for(int j = 0; j < game.getWorldSize(); j++){
				//Without a prior knowledge, what to set this to?
				//One way would be to say that it's a head or tail and so the prob is 50% either way
				//occGrid.get(i).add(0.50f);

				//Well in reality we should flip a non-occupied cell much more often than an occupied cell
				//and the likelyhood of a spot being an obstacle is overall very low 1/100, realistically even smaller than this
				occGrid.get(i).add(0.01f);
			}
		}

		if(maps != null){
			for(int i = 0; i < game.getWorldSize(); i++){
				for(int j = 0; j < game.getWorldSize(); j++){
					//Look at all the known worlds I've played in
					int o = 0;
					for(int m = 0; m < maps.size(); m++){
						if(maps.get(m).isPointOccupied(i - (game.getWorldSize() / 2), j - (game.getWorldSize() / 2)))
							o++;
					}
					if(o == 0){
						occGrid.get(i).set(j,0.01f);
					}else{
						float val = (float)o / (maps.size() + 1);
						occGrid.get(i).set(j,val);
					}
				}
			}
		}
	}

	protected void updateOccGrid(float x, float y, boolean hit){
		//Find out prior prob
		float pocc = getOccGridValue(x,y);

		/*if(pocc == 1.0f || pocc == 0.0f)
			return;

		if(pocc > 0.99f){
			occGrid.get(occgridx).set(occgridy,1.0f);
			return;
		}
		if(pocc < 0.01f){
			occGrid.get(occgridx).set(occgridy,0.0f);
			return;
		}*/

		float newpocc;
		if(hit){
			//P(hit) = P(hit | occupied) * P(occupied) + P(hit | !occupied) * P(!occupied)
			float phit = (game.getTruePositive() * pocc) + ((1 - game.getTrueNegative()) * (1 - pocc));
			//P(occupied | hit) = P(hit | occupied) * P(occupied) / P(hit)
			newpocc = (game.getTruePositive() * pocc) / phit;
		}else{
			//P(miss) = P(miss | occupied) * P(occupied) + P(miss | !occupied) * P(!occupied)
			float pmiss = ((1 - game.getTruePositive()) * pocc) + (game.getTrueNegative() * (1 - pocc));
			//P(occupied | miss) = P(miss | occupied) * P(occupied) / P(miss)
			newpocc = ((1 - game.getTruePositive()) * pocc) / pmiss;
		}

		//Set new prob
		putOccGridValue(x,y,newpocc);
	}

	@Override
	public boolean isOccGridRequired(){
		return true;
	}

	@Override
	public void bzrFlagEventHandler(BZRFlagEvent event){
		if(event instanceof OccGridEvent){
			//Handle event immediate, overriding classes should probably throw this into a message queue to prevent heavy processing/blocking on the communication thread
			for(int i = 0; i < ((OccGridEvent)event).getLine().length(); i++){
				int reading = ((OccGridEvent)event).getLine().charAt(i);
				//Reading is 48 for a 0, 49 for a 1
				if(reading == 49)
					updateOccGrid(((OccGridEvent)event).getPosition().getX(),((OccGridEvent)event).getPosition().getY() + i,true);
				else
					updateOccGrid(((OccGridEvent)event).getPosition().getX(),((OccGridEvent)event).getPosition().getY() + i,false);
			}
		}
		if(listener != null && event instanceof OccGridCompleteEvent){
			listener.onBZRFlagEvent(event);
		}
	}

	/*public List<List<Float>> getOccGrid(){
		return occGrid;
	}*/

	public float getOccGridValue(float x, float y){
		int i = (int)Math.floor(x + (game.getWorldSize() / 2));
		int j = (int)Math.floor(y + (game.getWorldSize() / 2));

		return occGrid.get(i).get(j);
	}

	public float putOccGridValue(float x, float y, float value){
		int i = (int)Math.floor(x + (game.getWorldSize() / 2));
		int j = (int)Math.floor(y + (game.getWorldSize() / 2));

		occGrid.get(i).set(j,value);

		return value;
	}

	/**
	 * The x position converstion is rather simple, in a bitmap x positions move from LTR, and are 0 indexed
	 * Add half of the world size and walla
	 * @param x
	 * @return
	 */
	public int getBitmapX(float x){
		return (int)Math.floor(x + (game.getWorldSize() / 2));
	}

	/**
	 * This one gets a little complicated. The bitmap Y values increase in the downward direction. A
	 * few examples (with a 800 world size): 400 -> 0, 0 -> 400, -400 -> 800.
	 * @param y
	 * @return
	 */
	public int getBitmapY(float y){
		//Formula ret = 800 - (y + 400)
		return (int)Math.floor(game.getWorldSize() - (y + (game.getWorldSize() / 2)));
	}

	public void setOccGrid(List<List<Float>> occGrid){
		this.occGrid = occGrid;
	}
	public static void setMaps(List<Map> maps){
		OccGridCommander.maps = maps;
	}
}
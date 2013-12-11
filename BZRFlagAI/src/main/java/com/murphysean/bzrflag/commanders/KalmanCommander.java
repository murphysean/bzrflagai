package com.murphysean.bzrflag.commanders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.murphysean.bzrflag.agents.GoToAgent;
import com.murphysean.bzrflag.agents.ShooterAgent;
import com.murphysean.bzrflag.events.BZRFlagEvent;
import com.murphysean.bzrflag.models.Game;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KalmanCommander extends AbstractCommander{

	public KalmanCommander(){
		super();
	}

	@Override
	public void setGame(Game game){
		super.setGame(game);

		//TODO Whether here or there I need to get the other teams to use the KalmanTank model so that they run the kalman filter on the data

		//Set up my team right here
		for(int i = 0; i < playerCount; i++){
			ShooterAgent shooterAgent = new ShooterAgent();
			shooterAgent.setGame(game);
			shooterAgent.setId(i);
			shooterAgent.setCallsign(game.getTeam().getColor() + i);
			shooterAgent.setTeamColor(game.getTeam().getColor());
			tanks.add(shooterAgent);
		}
	}

	@Override
	public void bzrFlagEventHandler(BZRFlagEvent event){
		if (event instanceof GoToAgent.GoToCompleteEvent){
			GoToAgent.GoToCompleteEvent goToCompleteEvent = (GoToAgent.GoToCompleteEvent)event;
		}
	}
}
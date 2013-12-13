package com.murphysean.bzrflag.commanders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.murphysean.bzrflag.agents.GoToAgent;
import com.murphysean.bzrflag.agents.ShooterAgent;
import com.murphysean.bzrflag.events.BZRFlagEvent;
import com.murphysean.bzrflag.models.Game;
import com.murphysean.bzrflag.models.KalmanTank;
import com.murphysean.bzrflag.models.Point;
import com.murphysean.bzrflag.models.Team;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KalmanCommander extends AbstractCommander{
	private ShooterAgent bond = null;
	private int targetIndex = 0;

	public KalmanCommander(){
		super();
	}

	@Override
	public void setGame(Game game){
		super.setGame(game);

		//Set up my team right here
		for(int i = 0; i < playerCount; i++){
			ShooterAgent shooterAgent = new ShooterAgent();
			shooterAgent.setGame(game);
			shooterAgent.setId(i);
			shooterAgent.setCallsign(game.getTeam().getColor() + i);
			shooterAgent.setTeamColor(game.getTeam().getColor());
			tanks.add(shooterAgent);
			if(i == 0)
				bond = shooterAgent;
		}

		//Only use the first tank, tell him to goto 0,0
		bond.setDestination(new Point(0,0));
	}

	@Override
	public void bzrFlagEventHandler(BZRFlagEvent event){
		if (event instanceof GoToAgent.GoToCompleteEvent){
			GoToAgent.GoToCompleteEvent goToCompleteEvent = (GoToAgent.GoToCompleteEvent)event;
			//Pick an enemy tank to shoot at
			Team enemyTeam = game.findTeamByColor("red");
			//bond.setTarget((KalmanTank)enemyTeam.getTanks().get(targetIndex++ % enemyTeam.getTanks().size()));
			bond.setTarget((KalmanTank)enemyTeam.getTanks().get(1));
		}else if(event instanceof ShooterAgent.AssassinateEvent){
			Team enemyTeam = game.findTeamByColor("red");
			//bond.setTarget((KalmanTank)enemyTeam.getTanks().get(targetIndex++ % enemyTeam.getTanks().size()));
			bond.setTarget((KalmanTank)enemyTeam.getTanks().get(1));
		}
	}


}
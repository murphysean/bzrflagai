package com.murphysean.bzrflag.agents;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.murphysean.bzrflag.controllers.PIDController;
import com.murphysean.bzrflag.events.BZRFlagEvent;
import com.murphysean.bzrflag.interfaces.Commander;
import com.murphysean.bzrflag.models.Game;
import com.murphysean.bzrflag.models.Point;

public class GoToAgent extends AbstractAgent{
	private static final float NINETY_DEGREES = 1.57079633f;
	private static final float FIVE_DEGREES = 0.0872664626f;

	@JsonIgnore
	protected transient Game game;
	protected Point target;
	protected PIDController distanceController;
	protected PIDController angleController;
	protected transient long arrived = 0;
	protected transient long assigned = 0;

	public GoToAgent(){
		distanceController = new PIDController();
		distanceController.setSetPoint(0.0f);
		angleController = new PIDController();
		angleController.setSetPoint(0.0f);
	}

	@JsonIgnore
	public synchronized void setGame(Game game){
		this.game = game;
	}

	public Point getTarget(){
		return target;
	}
	public void setTarget(Point target){
		this.target = target;
		assigned = System.currentTimeMillis();
	}

	@Override
	public synchronized void update(String status, int shotsAvailable, float timeToReload, String flag, float positionX, float positionY, float velocityX, float velocityY, float angle, float angleVelocity){
		//Invoke the default behavior
		super.update(status,shotsAvailable,timeToReload,flag,positionX,positionY,velocityX,velocityY,angle,angleVelocity);

		if (arrived != 0 && System.currentTimeMillis() - arrived > 3000) {
			arrived = 0;
			GoToCompleteEvent event = new GoToCompleteEvent(this, arrived, System.currentTimeMillis() - arrived);
			((Commander)game.getTeam()).bzrFlagEventHandler(event);
		}

		if(target == null)
			return;

		//Wrap speed in some kind of pd controller? Might not need to do this tbh
		float distance = (float)Math.sqrt(Math.pow(target.getX() - positionX,2.0d) + Math.pow(target.getY() - positionY,2.0d));

		//Calculate the angle to my goal
		float ang = (float)Math.atan2(target.getY() - positionY,target.getX() - positionX);
		float diff = (float)Math.atan2(Math.sin(ang - angle),Math.cos(ang - angle));
		float absdiff = Math.abs(diff);

		if(distance < 2 && absdiff < FIVE_DEGREES){
			desiredSpeed = 0f;
			desiredAngularVelocity = 0f;

			target = null;
			arrived = System.currentTimeMillis();
			return;
		}

		desiredAngularVelocity = angleController.calculate(diff * -1f);



		//If I'm greater than 90 degrees from my target angle, than speed = 0
		if(absdiff > NINETY_DEGREES){
			desiredSpeed = 0f;
		}else{
			desiredSpeed = (1 - (absdiff / NINETY_DEGREES));
			if(distance < 50)
				desiredSpeed *= (distance / 50f);
		}
	}

	@Override
	public synchronized boolean getDesiredTriggerStatus(){
		//if(timeToReload <= 0.0f)
		//	return true;
		return false;
	}

	public static class GoToCompleteEvent extends BZRFlagEvent{
		public static final String GO_TO_TANK_FINISHED = "finished";

		protected GoToAgent goToAgent;
		protected long arrived;
		protected long millisAtDestination;

		public GoToCompleteEvent(GoToAgent goToAgent, long arrived, long millisAtDestination){
			super(GO_TO_TANK_FINISHED);

			this.goToAgent = goToAgent;
			this.arrived = arrived;
			this.millisAtDestination = millisAtDestination;
		}

		public GoToAgent getGoToAgent(){
			return goToAgent;
		}
		public long getArrived(){
			return arrived;
		}
		public long getMillisAtDestination(){
			return millisAtDestination;
		}
	}
}
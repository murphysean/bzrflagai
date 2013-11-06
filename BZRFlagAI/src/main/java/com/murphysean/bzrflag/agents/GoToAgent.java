package com.murphysean.bzrflag.agents;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.murphysean.bzrflag.controllers.PIDController;
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
	}

	@Override
	public synchronized void update(String status, int shotsAvailable, float timeToReload, String flag, float positionX, float positionY, float velocityX, float velocityY, float angle, float angleVelocity){
		//Invoke the default behavior
		super.update(status,shotsAvailable,timeToReload,flag,positionX,positionY,velocityX,velocityY,angle,angleVelocity);

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
			//TODO We are there, throw an event notifying this
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
}
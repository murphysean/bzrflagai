package com.murphysean.bzrflag.agents;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.murphysean.bzrflag.models.Game;
import com.murphysean.bzrflag.models.KalmanTank;
import com.murphysean.bzrflag.models.Point;

public class ShooterAgent extends GoToAgent{
	private static final float NINETY_DEGREES = 1.57079633f;
	private static final float FIVE_DEGREES = 0.0872664626f;

	protected KalmanTank enemy = null;

	public ShooterAgent(){
		super();
	}

	@JsonIgnore
	public synchronized void setGame(Game game){
		this.game = game;
	}

	public Point getDestination(){
		return destination;
	}
	public void setDestination(Point target){
		this.destination = target;
		assigned = System.currentTimeMillis();
	}

	public KalmanTank getEnemy(){
		return enemy;
	}
	public void setEnemy(KalmanTank enemy){
		this.enemy = enemy;
	}

	@Override
	public synchronized void update(String status, int shotsAvailable, float timeToReload, String flag, float positionX, float positionY, float velocityX, float velocityY, float angle, float angleVelocity){
		//Invoke the default behavior
		super.update(status,shotsAvailable,timeToReload,flag,positionX,positionY,velocityX,velocityY,angle,angleVelocity);

		//TODO Face towards the enemy's location

		//TODO Check to see if the enemy's status has changed (You hit him)

		//TODO Utilize the kalman prediction from the other tanks to take aim and fire


	}

	@Override
	public synchronized boolean getDesiredTriggerStatus(){
		//if(timeToReload <= 0.0f)
		//	return true;
		return false;
	}
}

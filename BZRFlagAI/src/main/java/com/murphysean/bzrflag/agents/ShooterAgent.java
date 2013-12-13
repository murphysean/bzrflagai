package com.murphysean.bzrflag.agents;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.murphysean.bzrflag.events.BZRFlagEvent;
import com.murphysean.bzrflag.interfaces.Commander;
import com.murphysean.bzrflag.models.Game;
import com.murphysean.bzrflag.models.KalmanTank;
import com.murphysean.bzrflag.models.Point;

public class ShooterAgent extends GoToAgent{
	private static final float NINETY_DEGREES = 1.57079633f;
	private static final float FIVE_DEGREES = 0.0872664626f;
	private static final float ONE_DEGREE = 0.0174532925f;
	private static final float QUARTER_DEGREE = 0.00436332313f;

	protected KalmanTank target = null;
	protected Point shootAtTarget = null;

	public ShooterAgent(){
		super();

		angleController.setKp(2.0f);
	}

	public KalmanTank getTarget(){
		return target;
	}
	public void setTarget(KalmanTank target){
		this.target = target;
		target.startRun();
	}

	@Override
	public synchronized void update(String status, int shotsAvailable, float timeToReload, String flag, float positionX, float positionY, float velocityX, float velocityY, float angle, float angleVelocity){
		//Invoke the default behavior
		super.update(status,shotsAvailable,timeToReload,flag,positionX,positionY,velocityX,velocityY,angle,angleVelocity);

		if(target == null || target.getStatus() == null)
			return;

		//Check to see if the target's status has changed (You hit him)
		if(target.getStatus().equals("dead")){
			shootAtTarget = null;
			target.stopRun();
			//Notify the commander that you have hit your target, or that it has otherwise died
			AssassinateEvent event = new AssassinateEvent(target);
			target = null;
			((Commander)game.getTeam()).bzrFlagEventHandler(event);
			return;
		}

		//Face towards the target's Kalman prediction location
		if(shootAtTarget == null){
			Point targetGuess = target.getKalmanPoint();
			float distance = (float)Math.sqrt(Math.pow(targetGuess.getX() - positionX,2.0d) + Math.pow(targetGuess.getY() - positionY,2.0d));
			float time = distance / game.getShotSpeed();
			shootAtTarget = target.getFutureKalmanPoint((long)(time * 1000) + 1500);

			//shootAtTarget = target.getFutureKalmanPoint();
		}
		if(shootAtTarget != null){
			//Calculate the angle to my goal
			float ang = (float)Math.atan2(shootAtTarget.getY() - positionY,shootAtTarget.getX() - positionX);
			float diff = (float)Math.atan2(Math.sin(ang - angle),Math.cos(ang - angle));
			desiredAngularVelocity = angleController.calculate(diff * -1f);
		}
	}

	@Override
	public synchronized boolean getDesiredTriggerStatus(){
		if(target == null || position == null || shootAtTarget == null)
			return false;

		//TODO Utilize the kalman prediction from the other tanks to take aim and fire
		float ang = (float)Math.atan2(shootAtTarget.getY() - position.getY(),shootAtTarget.getX() - position.getX());
		float diff = (float)Math.atan2(Math.sin(ang - angle),Math.cos(ang - angle));
		float absdiff = Math.abs(diff);
		if(absdiff <= QUARTER_DEGREE){
			shootAtTarget = null;
			return true;
		}
		return false;
	}

	public static class AssassinateEvent extends BZRFlagEvent{
		public static final String ASSASSINATE = "assassinate";

		protected KalmanTank kalmanTank;

		public AssassinateEvent(KalmanTank kalmanTank){
			super(ASSASSINATE);

			this.kalmanTank = kalmanTank;
		}

		public KalmanTank getKalmanTank(){
			return kalmanTank;
		}
	}
}

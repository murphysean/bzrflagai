package com.murphysean.bzrflag.models;

import com.murphysean.bzrflag.events.KalmanEvent;
import com.murphysean.bzrflag.listeners.KalmanListener;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.UUID;

public class KalmanTank extends Tank{
	private static final long UPDATE_FREQUENCY_MILLI = 500;
	public static KalmanListener listener = null;

	public String sessionId;
	public boolean started = false;
	public long lastTick = 0;

	RealMatrix transitionNoise = new Array2DRowRealMatrix(6,6);
	RealMatrix stateNoise = new Array2DRowRealMatrix(6,6);

	RealVector prevMean = new ArrayRealVector(6);
	RealMatrix prevNoise = new Array2DRowRealMatrix(6,6);

	RealMatrix observationMatrix = new Array2DRowRealMatrix(2,6);
	RealMatrix observationNoise = new Array2DRowRealMatrix(2,2);

	public KalmanTank(){
		super();

		transitionNoise.setEntry(0,0,1);
		transitionNoise.setEntry(0,1,0.5);
		transitionNoise.setEntry(0,2,0.125);

		transitionNoise.setEntry(1,1,1);
		transitionNoise.setEntry(1,2,0.5);

		//Friction Coefficient
		transitionNoise.setEntry(2,1,-0.1);
		transitionNoise.setEntry(2,2,1);

		transitionNoise.setEntry(3,3,1);
		transitionNoise.setEntry(3,4,0.5);
		transitionNoise.setEntry(3,5,0.125);

		transitionNoise.setEntry(4,4,1);
		transitionNoise.setEntry(4,5,0.5);

		//Friction Coefficient
		transitionNoise.setEntry(5,4,-0.1);
		transitionNoise.setEntry(5,5,1);

		stateNoise.setEntry(0,0,0.1);
		stateNoise.setEntry(1,1,0.1);
		stateNoise.setEntry(2,2,100);
		stateNoise.setEntry(3,3,0.1);
		stateNoise.setEntry(4,4,0.1);
		stateNoise.setEntry(5,5,100);

		observationMatrix.setEntry(0,0,1);
		observationMatrix.setEntry(1,3,1);

		observationNoise.setEntry(0,0,25);
		observationNoise.setEntry(1,1,25);
	}

	public synchronized boolean startRun(){
		started = true;
		sessionId = UUID.randomUUID().toString();

		prevMean.setEntry(0,0);
		prevMean.setEntry(1,0);
		prevMean.setEntry(2,0);
		prevMean.setEntry(3,0);
		prevMean.setEntry(4,0);
		prevMean.setEntry(5,0);

		prevNoise.setEntry(0,0,100);
		prevNoise.setEntry(1,1,100);
		prevNoise.setEntry(2,2,100);
		prevNoise.setEntry(3,3,100);
		prevNoise.setEntry(4,4,100);
		prevNoise.setEntry(5,5,100);

		lastTick = System.currentTimeMillis();
		return true;
	}

	@Override
	public synchronized void update(String status,String flag,float positionX,float positionY,float angle){
		super.update(status,flag,positionX,positionY,angle);

		if(started && System.currentTimeMillis() - lastTick >= UPDATE_FREQUENCY_MILLI){
			RealVector observation = new ArrayRealVector(2);
			observation.setEntry(0,positionX);
			observation.setEntry(1,positionY);

			//Run the kalman filter
			RealMatrix common = transitionNoise.multiply(prevNoise).multiply(transitionNoise.transpose()).add(stateNoise);

			RealMatrix invertedstuff = new LUDecomposition(observationMatrix.multiply(common).multiply(observationMatrix.transpose()).add(observationNoise)).getSolver().getInverse();
			RealMatrix kalmanGain = common.multiply(observationMatrix.transpose()).multiply(invertedstuff);

			RealVector newMean = transitionNoise.operate(prevMean).add(kalmanGain.operate(observation.subtract(observationMatrix.multiply(transitionNoise).operate(prevMean))));
			RealMatrix newNoise = MatrixUtils.createRealIdentityMatrix(6).subtract(kalmanGain.multiply(observationMatrix)).multiply(transitionNoise.multiply(prevNoise).multiply(transitionNoise.transpose()).add(stateNoise));

			//Notifiy the listener, if any, of the event
			long now = System.currentTimeMillis();
			if(listener != null){
				KalmanEvent event = new KalmanEvent(sessionId,this.getCallsign(),now,newMean.getEntry(0),newMean.getEntry(1),newMean.getEntry(2),newMean.getEntry(3),newMean.getEntry(4),newMean.getEntry(5),
						newNoise.getEntry(0,0),newNoise.getEntry(1,1),newNoise.getEntry(2,2),newNoise.getEntry(3,3),newNoise.getEntry(4,4),newNoise.getEntry(5,5),
						positionX,positionY);
				listener.onKalmanEvent(event);
			}

			prevMean = newMean;
			prevNoise = newNoise;
			lastTick = now;
		}


	}

	public synchronized Point getKalmanPoint(){
		return new Point((float)prevMean.getEntry(0), (float)prevMean.getEntry(3));
	}

	protected RealMatrix predictionNoise = new Array2DRowRealMatrix(6,6);
	public synchronized Point getFutureKalmanPoint(long millisToTarget){
		if(started){
			float deltaT = (float)millisToTarget / 1000f;
			predictionNoise.setEntry(0,0,1);
			predictionNoise.setEntry(0,1,deltaT);
			predictionNoise.setEntry(0,2,(deltaT * deltaT) / 2f);

			predictionNoise.setEntry(1,1,1);
			predictionNoise.setEntry(1,2,deltaT);

			//Friction Coefficient
			predictionNoise.setEntry(2,1,-0.1);
			predictionNoise.setEntry(2,2,1);

			predictionNoise.setEntry(3,3,1);
			predictionNoise.setEntry(3,4,deltaT);
			predictionNoise.setEntry(3,5,(deltaT * deltaT) / 2f);

			predictionNoise.setEntry(4,4,1);
			predictionNoise.setEntry(4,5,deltaT);

			//Friction Coefficient
			predictionNoise.setEntry(5,4,-0.1);
			predictionNoise.setEntry(5,5,1);

			RealVector predic = predictionNoise.operate(prevMean);
			return new Point((float)predic.getEntry(0), (float)predic.getEntry(3));
		}
		return null;
	}

	public synchronized boolean stopRun(){
		started = false;
		lastTick = 0;
		return true;
	}
}

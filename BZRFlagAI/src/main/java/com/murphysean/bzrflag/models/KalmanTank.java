package com.murphysean.bzrflag.models;

public class KalmanTank extends Tank{
	private static final long UPDATE_FREQUENCY_MILLI = 500;


	public KalmanTank(){
		super();
	}

	@Override
	public synchronized void update(String status,String flag,float positionX,float positionY,float angle){
		super.update(status,flag,positionX,positionY,angle);

		//Run the kalman filter
	}
}

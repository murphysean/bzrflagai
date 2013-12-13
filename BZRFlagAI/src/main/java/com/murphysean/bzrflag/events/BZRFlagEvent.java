package com.murphysean.bzrflag.events;

public abstract class BZRFlagEvent{
	public static final String SHOT_TYPE = "shot";
	public static final String MY_TANK_TYPE = "mytank";
	public static final String OTHER_TANK_TYPE = "othertank";
	public static final String FLAG_TYPE = "flag";
	public static final String SCORE_TYPE = "score";
	public static final String TIMER_TYPE = "timer";
	public static final String OCC_GRID_TYPE = "occgrid";
	public static final String OCC_GRID_COMPETE_TYPE = "occgridcomplete";
	public static final String KALMAN_TYPE = "kalman";

	private volatile String type;

	public BZRFlagEvent(String type){
		this.type = type;
	}

	public final String getType(){
		return type;
	}
}

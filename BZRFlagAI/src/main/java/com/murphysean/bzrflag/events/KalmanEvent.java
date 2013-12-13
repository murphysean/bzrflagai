package com.murphysean.bzrflag.events;

public class KalmanEvent extends BZRFlagEvent{
	protected String sessionId;
	protected String callsign;
	protected long instant;

	protected double px;
	protected double vx;
	protected double ax;
	protected double py;
	protected double vy;
	protected double ay;

	protected double cpx;
	protected double cvx;
	protected double cax;
	protected double cpy;
	protected double cvy;
	protected double cay;

	protected float ox;
	protected float oy;

	public KalmanEvent(String sessionId, String callsign, long instant, double px, double vx, double ax, double py, double vy, double ay, double cpx, double cvx, double cax, double cpy, double cvy, double cay, float ox, float oy){
		super(KALMAN_TYPE);
		this.sessionId = sessionId;
		this.callsign = callsign;
		this.instant = instant;
		this.px = px;
		this.vx = vx;
		this.ax = ax;
		this.py = py;
		this.vy = vy;
		this.ay = ay;
		this.cpx = cpx;
		this.cvx = cvx;
		this.cax = cax;
		this.cpy = cpy;
		this.cvy = cvy;
		this.cay = cay;
		this.ox = ox;
		this.oy = oy;
	}

	public long getInstant(){
		return instant;
	}

	public String getSessionId(){
		return sessionId;
	}

	public String getCallsign(){
		return callsign;
	}

	public double getPx(){
		return px;
	}

	public double getVx(){
		return vx;
	}

	public double getAx(){
		return ax;
	}

	public double getPy(){
		return py;
	}

	public double getVy(){
		return vy;
	}

	public double getAy(){
		return ay;
	}

	public float getOx(){
		return ox;
	}

	public float getOy(){
		return oy;
	}

	public double getCpx(){
		return cpx;
	}

	public double getCvx(){
		return cvx;
	}

	public double getCax(){
		return cax;
	}

	public double getCpy(){
		return cpy;
	}

	public double getCvy(){
		return cvy;
	}

	public double getCay(){
		return cay;
	}
}

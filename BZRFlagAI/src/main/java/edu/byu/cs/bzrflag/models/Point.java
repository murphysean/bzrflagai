package edu.byu.cs.bzrflag.models;

public class Point{
	protected float x;
	protected float y;
	protected float z;

	/**
	 * Turns a space delimited string of floats into a point
	 * @param string
	 */
	public Point(String string){
		String[] points = string.split("\\s+");
		x = Float.parseFloat(points[0]);
		y = Float.parseFloat(points[1]);
		z = Float.parseFloat(points[2]);
	}

	public Point(float x, float y){
		this.x = x;
		this.y = y;
		this.z = 0f;
	}

	public Point(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float getX(){
		return x;
	}

	public void setX(float x){
		this.x = x;
	}

	public float getY(){
		return y;
	}

	public void setY(float y){
		this.y = y;
	}

	public float getZ(){
		return z;
	}

	public void setZ(float z){
		this.z = z;
	}
}

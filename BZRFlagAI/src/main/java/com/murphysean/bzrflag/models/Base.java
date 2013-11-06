package com.murphysean.bzrflag.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Base{
	protected String teamColor;
	protected List<Point> points;
	protected Point centerPoint;
	protected float radius;

	public Base(){
		points = new ArrayList<Point>();
	}

	public Base(String serverString){
		points = new ArrayList<Point>();
		String[] parts = serverString.split("\\s+");

		teamColor = parts[1];

		Point point = null;
		for(int i = 2; i < parts.length; i++){
			if(i % 2 == 0){
				point = new Point();
				point.setX(Float.valueOf(parts[i]));
			}else{
				point.setY(Float.valueOf(parts[i]));
				points.add(point);
			}
		}

		centerPoint = calculateCenterPoint();
		radius = calculateMaxRadius();
	}

	public String getTeamColor(){
		return teamColor;
	}

	public void setTeamColor(String teamColor){
		this.teamColor = teamColor;
	}

	public List<Point> getPoints(){
		return points;
	}

	public void setPoints(List<Point> points){
		this.points = points;
	}

	public Point getCenterPoint(){
		return centerPoint;
	}

	public float getRadius(){
		return radius;
	}

	private Point calculateCenterPoint(){
		float runningX = 0.0f;
		float runningY = 0.0f;
		for(Point corner : points){
			runningX += corner.getX();
			runningY += corner.getY();
		}

		Point center = new Point();
		center.setX(runningX / points.size());
		center.setY(runningY / points.size());
		return center;
	}

	private Float calculateMaxRadius(){
		float maxRadius = 0.0f;
		for(Point corner : points){
			//Calculate the distance from the center to each corner
			float distance = (float)Math.sqrt(Math.pow(corner.getX() - centerPoint.getX(),2) + Math.pow(corner.getY() - centerPoint.getY(),2));
			if(distance > maxRadius)
				maxRadius = distance;
		}

		return maxRadius;
	}
}

package edu.byu.cs.bzrflag.models;

import java.io.BufferedReader;
import java.io.IOException;

public class Box{
	protected Point position;
	protected float rotation;
	protected Point size;

	public Box(BufferedReader reader) throws IOException{
		String line = null;
		while((line = reader.readLine()) != null){
			String[] parts = line.split("\\s+");
			if(parts[0].equals("position")){
				position = new Point(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
			}else if(parts[0].equals("rotation")){
				rotation = Float.parseFloat(parts[1]);
			}else if(parts[0].equals("size")){
				size = new Point(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
			}else if(parts[0].equals("end")){
				break;
			}
		}
	}

	public boolean containsPoint(Point point){
		return containsPoint(point.getX(), point.getY());
	}

	public boolean containsPoint(float x, float y){
		//Some Calculations to figure out if this point is occupied
		if(position == null || size == null)
			return false;

		boolean containsx = false;

		if(x <= (position.getX() + size.getX()) && x >= (position.getX() - size.getX()))
			containsx = true;

		boolean containsy = false;

		if(y <= (position.getY() + size.getY()) && y >= (position.getY() - size.getY()))
			containsy = true;

		if(containsx && containsy)
			return true;
		return false;
	}

	public boolean isValid(){
		if(position == null || size == null)
			return false;

		return true;
	}
}

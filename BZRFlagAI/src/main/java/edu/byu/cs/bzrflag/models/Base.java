package edu.byu.cs.bzrflag.models;

import java.io.BufferedReader;
import java.io.IOException;

public class Base{
	protected String name;
	protected Point position;
	protected float rotation;
	protected Point size;
	protected int color;

	public Base(){

	}

	public Base(BufferedReader reader) throws IOException{
		//Read Lines till you see 'end' and pull out relavent info
		String line = null;
		while((line = reader.readLine()) != null){
			String[] parts = line.split("\\s+");
			if(parts[0].equals("name")){
				name = parts[1];
			}else if(parts[0].equals("position")){
				position = new Point(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
			}else if(parts[0].equals("rotation")){
				rotation = Float.parseFloat(parts[1]);
			}else if(parts[0].equals("size")){
				size = new Point(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
			}else if(parts[0].equals("color")){
				color = Integer.parseInt(parts[1]);
			}else if(parts[0].equals("end")){
				break;
			}
		}
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public Point getPosition(){
		return position;
	}

	public void setPosition(Point position){
		this.position = position;
	}

	public float getRotation(){
		return rotation;
	}

	public void setRotation(float rotation){
		this.rotation = rotation;
	}

	public Point getSize(){
		return size;
	}

	public void setSize(Point size){
		this.size = size;
	}

	public int getColor(){
		return color;
	}

	public void setColor(int color){
		this.color = color;
	}
}

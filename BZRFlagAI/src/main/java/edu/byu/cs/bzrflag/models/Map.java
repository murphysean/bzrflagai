package edu.byu.cs.bzrflag.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Map{
	public List<Base> bases;
	public List<Box> boxes;

	public Map(BufferedReader bufferedReader) throws IOException{
		bases = new ArrayList<Base>();
		boxes = new ArrayList<Box>();

		String line = null;
		while((line = bufferedReader.readLine()) != null){
			if(!line.startsWith("#")){
				if(line.startsWith("base")){
					Base base = new Base(bufferedReader);
					bases.add(base);
				}else if(line.startsWith("box")){
					Box box = new Box(bufferedReader);
					boxes.add(box);
				}
			}
		}
	}

	public Map(File file){
		bases = new ArrayList<Base>();
		boxes = new ArrayList<Box>();
		try{
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String line = null;
			while((line = bufferedReader.readLine()) != null){
				if(!line.startsWith("#")){
					if(line.startsWith("base")){
						Base base = new Base(bufferedReader);
					}else if(line.startsWith("box")){
						Box box = new Box(bufferedReader);
					}
				}
			}

			bufferedReader.close();
			inputStreamReader.close();
			fileInputStream.close();
		}catch(FileNotFoundException e){
			throw new RuntimeException(e);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public boolean isPointOccupied(Point point){
		return isPointOccupied(point.getX(), point.getY());
	}

	public boolean isPointOccupied(float x, float y){
		for(Box box : boxes){
			if(box.containsPoint(x, y))
				return true;
		}
		return false;
	}

	public boolean isValid(){
		for(Box box : boxes){
			if(!box.isValid())
				return false;
		}
		return true;
	}

	public int getErrorIndex(){
		for(int i = 0; i < boxes.size(); i++){
			if(!boxes.get(i).isValid())
				return i;
		}

		return -1;
	}
}

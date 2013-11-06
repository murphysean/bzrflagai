package com.murphysean.bzrflag.events;

import com.murphysean.bzrflag.models.Point;

public class OccGridCompleteEvent extends BZRFlagEvent{
	protected Point at;
	protected Point size;

	public OccGridCompleteEvent(Point at, Point size){
		super(OCC_GRID_COMPETE_TYPE);
		this.at = at;
		this.size = size;
	}

	public Point getAt(){
		return at;
	}

	public Point getSize(){
		return size;
	}
}

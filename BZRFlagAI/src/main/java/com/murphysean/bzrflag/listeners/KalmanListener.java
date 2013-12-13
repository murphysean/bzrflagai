package com.murphysean.bzrflag.listeners;

import com.murphysean.bzrflag.events.KalmanEvent;

public interface KalmanListener{
	public void onKalmanEvent(KalmanEvent kalmanEvent);
}

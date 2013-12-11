package com.murphysean.bzrflag.agents;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.murphysean.bzrflag.interfaces.Agent;
import com.murphysean.bzrflag.models.Tank;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY,property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = PFAgent.class,name = "dumb"),
		@JsonSubTypes.Type(value = PFAgent.class,name = "pf")
})
public abstract class AbstractAgent extends Tank implements Agent{
	protected volatile String type;
	protected volatile float desiredSpeed;
	protected volatile float desiredAngularVelocity;
	protected volatile boolean desiredTriggerStatus;

	public AbstractAgent(){
		type = "abstract";
		desiredSpeed = 0.0f;
		desiredAngularVelocity = 0.0f;
		desiredTriggerStatus = false;
	}

	public synchronized String getType(){
		return type;
	}

	public synchronized float getDesiredSpeed(){
		return desiredSpeed;
	}

	public synchronized void setDesiredSpeed(float desiredSpeed){
		this.desiredSpeed = desiredSpeed;
	}

	public synchronized float getDesiredAngularVelocity(){
		return desiredAngularVelocity;
	}

	public synchronized void setDesiredAngularVelocity(float desiredAngularVelocity){
		this.desiredAngularVelocity = desiredAngularVelocity;
	}

	public synchronized boolean getDesiredTriggerStatus(){
		return false;
	}

	public synchronized void setDesiredTriggerStatus(boolean desiredTriggerStatus){
		this.desiredTriggerStatus = desiredTriggerStatus;
	}
}

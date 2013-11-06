package com.murphysean.bzrflag.communicators;

import com.murphysean.bzrflag.controllers.GameController;
import com.murphysean.bzrflag.interfaces.Agent;
import com.murphysean.bzrflag.models.Game;
import com.murphysean.bzrflag.models.Tank;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class BZRFlagOutputCommunicator implements Runnable{

	private Game game;
	private GameController gameController;
	private BufferedWriter bufferedWriter;

	private BlockingQueue<String> blockingQueue;

	public BZRFlagOutputCommunicator(GameController gameController){
		this.gameController = gameController;
		bufferedWriter = gameController.getBufferedWriter();
		this.game = gameController.getGame();

		blockingQueue = new LinkedBlockingDeque<String>();
	}

	@Override
	public void run(){
		Date lastTick = new Date();
		int commandCount = 0;
		try{
			while(game.getState().equals("playing")){// && !Thread.currentThread().isInterrupted()){
				if(Thread.interrupted()){
					game.setState("interrupted");
					continue;
				}
				//Count how many messages I'm processing per second
				Date now = new Date();
				long timeElapsed = now.getTime() - lastTick.getTime();
				if(timeElapsed >= 1000){
					game.setRequestsPerSecond(commandCount / (timeElapsed / 1000l));

					commandCount = 0;
					lastTick = now;
				}

				String command = blockingQueue.take();
				bufferedWriter.write(command + "\n");
				bufferedWriter.flush();
				commandCount++;
				game.incrementCurrentAsyncRequests();
			}
		}catch(InterruptedException e){
			game.setState("interrupted");
		}catch(IOException e){
			game.setState("errored");
		}

		bufferedWriter = null;
		game = null;
		gameController = null;
	}

	public void requestMyTanks(){
		blockingQueue.offer("mytanks");
	}

	public void requestShots(){
		blockingQueue.offer("shots");
	}

	public void requestOtherTanks(){
		blockingQueue.offer("othertanks");
	}

	public void requestFlags(){
		blockingQueue.offer("flags");
	}

	public void requestTime(){
		blockingQueue.offer("timer");
	}

	public void requestOccGrids(){
		for(int i = 0; i < game.getTeam().getTanks().size(); i++){
			blockingQueue.offer("occgrid " + i);
		}
	}

	public void requestOccGrid(int tankIndex){
		blockingQueue.offer("occgrid " + tankIndex);
	}

	public void updateMyTeam(){
		for(Tank tank : game.getTeam().getTanks()){
			if(tank instanceof Agent){
				writeSpeed(tank.getId(),((Agent)tank).getDesiredSpeed());
				writeAngVel(tank.getId(),((Agent)tank).getDesiredAngularVelocity());
				if(((Agent)tank).getDesiredTriggerStatus())
					writeShoot(tank.getId());
			}
		}
	}

	public void writeTaunt(String taunt){
		blockingQueue.offer("taunt " + taunt);
	}

	public void writeSpeed(int tankIndex, float speed){
		if(Float.isInfinite(speed))
			return;
		if(Float.isNaN(speed))
			return;

		blockingQueue.offer("speed " + tankIndex + " " + speed);
	}

	public void writeAngVel(int tankIndex, float angvel){
		if(Float.isInfinite(angvel))
			return;
		if(Float.isNaN(angvel))
			return;

		blockingQueue.offer("angvel " + tankIndex + " " + angvel);
	}

	public void writeShoot(int tankIndex){
		blockingQueue.offer("shoot " + tankIndex);
	}

	public void writeQuit(){
		blockingQueue.offer("quit");
	}

	public void requestEndGame(){
		blockingQueue.offer("endgame");
	}
}

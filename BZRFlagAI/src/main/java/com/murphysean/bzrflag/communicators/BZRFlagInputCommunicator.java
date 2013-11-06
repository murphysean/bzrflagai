package com.murphysean.bzrflag.communicators;

import com.murphysean.bzrflag.controllers.GameController;
import com.murphysean.bzrflag.events.OccGridCompleteEvent;
import com.murphysean.bzrflag.events.OccGridEvent;
import com.murphysean.bzrflag.interfaces.Commander;
import com.murphysean.bzrflag.models.Game;
import com.murphysean.bzrflag.models.Point;
import com.murphysean.bzrflag.models.Shot;
import com.murphysean.bzrflag.models.Tank;
import com.murphysean.bzrflag.models.Team;

import java.io.BufferedReader;
import java.util.Date;

/**
 * The BZRFlag Input Communicator will continoulsly read the input from the bzrflag server and update the game world
 * so that it stays in sync with the server. The reading will block on input streams from the server, and upon recieving
 * a message will hand it off to a queue on the gameController. The game controller will then process this queue and
 * update the game world.
 */
public class BZRFlagInputCommunicator implements Runnable{
	private Game game;
	private GameController gameController;
	private BufferedReader bufferedReader;

	private Point occGridPoint;
	private Point occGridSize;
	private int occGridLine;

	public BZRFlagInputCommunicator(GameController gameController){
		this.gameController = gameController;
		bufferedReader = gameController.getBufferedReader();
		this.game = gameController.getGame();
	}

	@Override
	public void run(){
		Date lastTick = new Date();
		int commandCount = 0;
		try{
			while(game.getState() == "playing"){// && !Thread.currentThread().isInterrupted()){
				if(Thread.interrupted()){
					game.setState("interrupted");
					continue;
				}
				//Count how many messages I'm processing per second
				Date now = new Date();
				long timeElapsed = now.getTime() - lastTick.getTime();
				if(timeElapsed >= 1000){
					game.setResponsesPerSecond(commandCount / (timeElapsed / 1000l));

					commandCount = 0;
					lastTick = now;
				}
				String responseLine = bufferedReader.readLine();

				//Parse the line
				if(responseLine.startsWith("ack")){
					commandCount++;
					game.decrementCurrentAsyncRequests();
					continue;
				}
				if(responseLine.startsWith("ok"))
					continue;
				if(responseLine.startsWith("begin"))
					continue;
				if(responseLine.startsWith("end"))
					continue;

				if(responseLine.startsWith("fail"))
					continue;

				readResponse(responseLine);
			}
		}catch(Exception e){
			game.setState("errored");
			//Not throwing allows the thread to exit normally
			throw new RuntimeException(e);
		}
		bufferedReader = null;
		game = null;
		gameController = null;
	}

	public void readResponse(String responseLine){
		if(responseLine.startsWith("0") || responseLine.startsWith("1")){
			readOccGrid(responseLine);
			return;
		}


		String[] parts = responseLine.split("\\s+");

		if(parts[0].equals("mytank")){
			readMyTank(Integer.valueOf(parts[1]),parts[2],parts[3],Integer.valueOf(parts[4]),Float.valueOf(parts[5]),parts[6],Float.valueOf(parts[7]),Float.valueOf(parts[8]),Float.valueOf(parts[9]),Float.valueOf(parts[10]),Float.valueOf(parts[11]),Float.valueOf(parts[12]));
		}else if(parts[0].equals("shot")){
			readShot(Float.valueOf(parts[1]),Float.valueOf(parts[2]),Float.valueOf(parts[3]),Float.valueOf(parts[4]));
		}else if(parts[0].equals("othertank")){
			readOtherTank(parts[1],parts[2],parts[3],parts[4],Float.valueOf(parts[5]),Float.valueOf(parts[6]),Float.valueOf(parts[7]));
		}else if(parts[0].equals("timer")){
			game.setTimeElapsed(Float.valueOf(parts[1]));
			game.setTimeLimit(Float.valueOf(parts[2]));
		}else if(parts[0].equals("score")){
			readScore(parts[1],parts[2],Integer.valueOf(parts[3]));
		}else if(parts[0].equals("flag")){
			readFlag(parts[1],parts[2],Float.valueOf(parts[3]),Float.valueOf(parts[4]));
		}else if(parts[0].equals("team")){
			readTeam(parts[1],Integer.valueOf(parts[2]));
		}else if(parts[0].equals("obstacle")){
			readObstacle(responseLine);
		}else if(parts[0].equals("base")){
			readBase(responseLine);
		}else if(parts[0].equals("constant")){
			readConstant(parts[1],parts[2]);
		}else if(parts[0].equals("at")){
			String[] ints = parts[1].split(",");
			int x = Integer.valueOf(ints[0]);
			int y = Integer.valueOf(ints[1]);
			occGridPoint = new Point(x,y);
			occGridLine = 0;
		}else if(parts[0].equals("size")){
			String[] sizes = parts[1].split("x");
			int length = Integer.valueOf(sizes[0]);
			int height = Integer.valueOf(sizes[1]);
			occGridSize = new Point(length,height);

		}else{
			throw new RuntimeException("Unknown Response: " + responseLine);
		}
	}

	public void readTeam(String color,Integer playerCount){
		throw new RuntimeException("Method Not Implemented");
	}

	public void readObstacle(String responseLine){
		throw new RuntimeException("Method Not Implemented");
	}

	public void readBase(String responseLine){
		throw new RuntimeException("Method Not Implemented");
	}

	public void readFlag(String teamColor,String possessingTeamColor,float x,float y){
		for(Team team : game.getTeams()){
			if(team.getColor().equals(teamColor)){
				team.getFlag().setPossessingTeamColor(possessingTeamColor);
				team.getFlag().getPoint().setX(x);
				team.getFlag().getPoint().setY(y);
			}

		}
	}

	public void readScore(String teamColor,String otherTeamColor,int score){
		for(Team team : game.getTeams()){
			if(team.getColor().equals(teamColor)){
				team.setScore(otherTeamColor,score);
			}
		}
	}

	public void readShot(float x,float y,float vx,float vy){
		Shot shot = new Shot();
		shot.setPoint(new Point(x,y));
		shot.setVelocity(new Point(vx,vy));

		//TODO Notify tanks of this shot, or maybe notify team
		//Someone will need to process the shots and determine if any of them are a risk to tank/team
		//I could keep track of shot from frame to frame based on it's trajectory or something, however this seems like a waste

		//TODO The reaction to shots should be reflexive, meaning that it should be fast and not blocked by other 'thoughts', 'actions'
	}

	public void readMyTank(int tankIndex,String callsign,String status,int shotsAvailable,float timeToReload,String flag,float x,float y,float angle,float vx,float vy,float vangle){
		Tank tank = game.getTeam().getTanks().get(tankIndex);
		tank.update(status,shotsAvailable,timeToReload,flag,x,y,vx,vy,angle,vangle);
	}

	public void readOtherTank(String callsign,String color,String status,String flag,float x,float y,float angle){
		for(Team team : game.getTeams()){
			if(team.getColor().equals(color)){
				int index = Integer.valueOf(callsign.replaceAll("\\D",""));
				Tank tank = team.getTanks().get(index);
				tank.update(status,flag,x,y,angle);
				break;
			}
		}
	}

	public void readOccGrid(String line){
		int x = Math.round(occGridPoint.getX()) + occGridLine;
		int y = Math.round(occGridPoint.getY());
		OccGridEvent event = new OccGridEvent(x,y,line);

		occGridLine++;

		if(game.getTeam() instanceof Commander){
			((Commander)game.getTeam()).bzrFlagEventHandler(event);
			if(occGridLine >= occGridSize.getX()){
				//Notify commander of finished occgrid
				OccGridCompleteEvent completeEvent = new OccGridCompleteEvent(occGridPoint, occGridSize);
				((Commander)game.getTeam()).bzrFlagEventHandler(completeEvent);
			}
		}
	}

	public void readConstant(String name,String value){
		if(name.equals("team")){
			game.setTeamColor(value);
		}else if(name.equals("worldsize")){
			game.setWorldSize(Integer.valueOf(value));
		}else if(name.equals("tankangvel")){
			game.setTankAngVel(Float.valueOf(value));
		}else if(name.equals("tanklength")){
			game.setTankLength(Float.valueOf(value));
		}else if(name.equals("tankwidth")){
			game.setTankWidth(Float.valueOf(value));
		}else if(name.equals("tankradius")){
			game.setTankRadius(Float.valueOf(value));
		}else if(name.equals("tankspeed")){
			game.setTankSpeed(Float.valueOf(value));
		}else if(name.equals("tankalive")){
			game.setTankAlive(value);
		}else if(name.equals("tankdead")){
			game.setTankDead(value);
		}else if(name.equals("linearaccel")){
			game.setLinearAccel(Float.valueOf(value));
		}else if(name.equals("angularaccel")){
			game.setAngularAccel(Float.valueOf(value));
		}else if(name.equals("shotradius")){
			game.setShotRadius(Float.valueOf(value));
		}else if(name.equals("shotrange")){
			game.setShotRange(Float.valueOf(value));
		}else if(name.equals("shotspeed")){
			game.setShotSpeed(Float.valueOf(value));
		}else if(name.equals("flagradius")){
			game.setFlagRadius(Float.valueOf(value));
		}else if(name.equals("explodetime")){
			game.setExplodeTime(Float.valueOf(value));
		}else if(name.equals("truepositive")){
			game.setTruePositive(Float.valueOf(value));
		}else if(name.equals("truenegative")){
			game.setTrueNegative(Float.valueOf(value));
		}else{
			throw new RuntimeException("Invalid Constant Value");
		}
	}
}

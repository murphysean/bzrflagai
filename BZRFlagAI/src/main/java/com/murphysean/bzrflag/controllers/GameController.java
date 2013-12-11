package com.murphysean.bzrflag.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.murphysean.bzrflag.commanders.OccGridCommander;
import com.murphysean.bzrflag.commanders.PFEvolutionCommander;
import com.murphysean.bzrflag.commanders.PidgeonCommander;
import com.murphysean.bzrflag.communicators.BZRFlagInputCommunicator;
import com.murphysean.bzrflag.communicators.BZRFlagOutputCommunicator;
import com.murphysean.bzrflag.interfaces.Commander;
import com.murphysean.bzrflag.listeners.GameControllerListener;
import com.murphysean.bzrflag.models.Base;
import com.murphysean.bzrflag.models.Game;
import com.murphysean.bzrflag.models.Obstacle;
import com.murphysean.bzrflag.models.Team;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameController implements Runnable{
	protected String gameId;
	protected String host;
	protected Integer port;
	protected Game game;

	protected Socket socket;
	protected BufferedReader bufferedReader;
	protected BufferedWriter bufferedWriter;

	Thread inputThread;
	Thread outputThread;

	protected GameControllerListener gameControllerListener;

	public GameController(){
		gameId = null;
		host = null;
		port = null;
		game = null;
	}

	public GameController(String gameId, String host, Integer port){
		this.gameId = gameId;
		this.host = host;
		this.port = port;

		game = new Game(gameId,host,port);
	}

	public void close(){
		game.setState("closing");
		inputThread.interrupt();
		inputThread = null;
		outputThread.interrupt();
		outputThread = null;
		try{
			bufferedWriter.close();
			bufferedReader.close();
		}catch(IOException e){
			game.setState("closing error");
		}
		bufferedWriter = null;
		bufferedReader = null;

		try{
			socket.close();
		}catch(IOException e){
			game.setState("closing error");
		}
		socket = null;

		game.setState("closed");
		if(gameControllerListener != null)
			gameControllerListener.onClose();
	}

	@Override
	public void run(){
		if(host == null || port == null)
			return;

		try{
			game.setState("connecting");
			//Open a socket
			socket = new Socket(host,port);
			//Handshake
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			//Make one time calls to initialize the agent
			//These need to be syncronous calls
			game.setState("initializing");
			handshake();
			readConstants();
			readTeams();
			readObstacles();
			readBases();
			readFlags();

			//Make sure the threads start
			game.setState("playing");

			//Spin out threads to handle async
			BZRFlagInputCommunicator inputCommunicator = new BZRFlagInputCommunicator(this);
			inputThread = new Thread(inputCommunicator,"bzrflagInputCommunicator");
			inputThread.start();
			BZRFlagOutputCommunicator outputCommunicator = new BZRFlagOutputCommunicator(this);
			outputThread = new Thread(outputCommunicator,"bzrflagOutputCommunicator");
			outputThread.start();

			Commander commander = null;

			if(game.getCommanderType().equals("PFEvolutionCommander")){
				commander = new PFEvolutionCommander();
			}else if(game.getCommanderType().equals("OccGridCommander")){
				commander = new OccGridCommander();
			} else if (game.getCommanderType().equals("PidgeonCommander")) {
                commander = new PidgeonCommander();
            }
			commander.setGame(game);

			//These will help speed up the occGrid Impl
			int idealHertz = 100;
			int numTanks = game.getTeam().getTanks().size();
			int occGridIters = idealHertz / 2 / numTanks;
			int currTankIndex = 0;

			long its = 1;
			Date lastLoop = new Date();
			while(game.getState().equals("playing")){
				if(Thread.interrupted()){
					game.setState("interrupted");
					continue;
				}
				//if I'm not processing fast enough slow down the loop
				if(game.getCurrentAsyncRequests() > 20){
					Thread.sleep(5);
					continue;
				}

				//Slow it down to about x iterations a second
				Date now = new Date();
				long timeDiff = now.getTime() - lastLoop.getTime();
				if(timeDiff < (1000 / idealHertz))
					Thread.sleep((1000 / idealHertz) - timeDiff);

				//Send off requests for information
				outputCommunicator.requestMyTanks();
				outputCommunicator.requestShots();
				//Read OccGrid
				if(commander.isOccGridRequired() && its % occGridIters == 0){
					if(currTankIndex >= numTanks)
						currTankIndex = 0;
					outputCommunicator.requestOccGrid(currTankIndex);
					currTankIndex++;
				}

				//Read Flags
				if(its % 50 == 0)
					outputCommunicator.requestFlags();
				//Read Other Tanks
				if(its % 2 == 0)
					outputCommunicator.requestOtherTanks();

				outputCommunicator.requestTime();

				//Write Tank Movements, Shots (Could be done async, see above)
				outputCommunicator.updateMyTeam();

				its++;
				now = new Date();
				timeDiff = now.getTime() - lastLoop.getTime();
				game.setHertz(1 / (timeDiff / 1000f));
				lastLoop = now;
			}
		}catch(Exception e){
			game.setState("errored");
			throw new RuntimeException(e);
		}

		close();
	}

	protected void handshake(){
		try{
			String serverMessage = bufferedReader.readLine();
			if(!serverMessage.startsWith("bzrobots"))
				throw new RuntimeException("Invalid Server Message");

			game.setVersion(serverMessage.split(" ")[1]);
			bufferedWriter.write("agent " + game.getVersion() + "\n");
			bufferedWriter.flush();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	protected void readConstants(){
		try{
			bufferedWriter.write("constants\n");
			bufferedWriter.flush();

			//Eat up the ack
			String response = bufferedReader.readLine();
			if(!response.startsWith("ack"))
				throw new RuntimeException("Missing Ack");
			response = bufferedReader.readLine();
			if(response.equals("begin")){
				response = bufferedReader.readLine();
				while(!response.equals("end")){
					String[] parts = response.split("\\s+");
					if(parts[1].equals("team")){
						game.setTeamColor(parts[2]);
					}else if(parts[1].equals("worldsize")){
						game.setWorldSize(Integer.valueOf(parts[2]));
					}else if(parts[1].equals("tankangvel")){
						game.setTankAngVel(Float.valueOf(parts[2]));
					}else if(parts[1].equals("tanklength")){
						game.setTankLength(Float.valueOf(parts[2]));
					}else if(parts[1].equals("tankwidth")){
						game.setTankWidth(Float.valueOf(parts[2]));
					}else if(parts[1].equals("tankradius")){
						game.setTankRadius(Float.valueOf(parts[2]));
					}else if(parts[1].equals("tankspeed")){
						game.setTankSpeed(Float.valueOf(parts[2]));
					}else if(parts[1].equals("tankalive")){
						game.setTankAlive(parts[2]);
					}else if(parts[1].equals("tankdead")){
						game.setTankDead(parts[2]);
					}else if(parts[1].equals("linearaccel")){
						game.setLinearAccel(Float.valueOf(parts[2]));
					}else if(parts[1].equals("angularaccel")){
						game.setAngularAccel(Float.valueOf(parts[2]));
					}else if(parts[1].equals("shotradius")){
						game.setShotRadius(Float.valueOf(parts[2]));
					}else if(parts[1].equals("shotrange")){
						game.setShotRange(Float.valueOf(parts[2]));
					}else if(parts[1].equals("shotspeed")){
						game.setShotSpeed(Float.valueOf(parts[2]));
					}else if(parts[1].equals("flagradius")){
						game.setFlagRadius(Float.valueOf(parts[2]));
					}else if(parts[1].equals("explodetime")){
						game.setExplodeTime(Float.valueOf(parts[2]));
					}else if(parts[1].equals("truepositive")){
						game.setTruePositive(Float.valueOf(parts[2]));
					}else if(parts[1].equals("truenegative")){
						game.setTrueNegative(Float.valueOf(parts[2]));
					}else{
						throw new RuntimeException("Invalid Constant Value");
					}
					response = bufferedReader.readLine();
				}
			}
		}catch(IOException e){
			game.setState("errored");
			throw new RuntimeException(e);
		}
	}

	protected void readTeams(){
		try{
			bufferedWriter.write("teams\n");
			bufferedWriter.flush();

			//Eat up the ack
			String response = bufferedReader.readLine();
			if(!response.startsWith("ack"))
				throw new RuntimeException("Missing Ack");
			response = bufferedReader.readLine();
			if(response.equals("begin")){
				response = bufferedReader.readLine();
				while(!response.equals("end")){
					Team team = new Team(response);
					if(team.getColor().equals(game.getTeamColor())){
						game.setTeam(team);
					}else{
						game.getTeams().add(team);
					}
					response = bufferedReader.readLine();
				}
			}
		}catch(IOException e){
			game.setState("errored");
			throw new RuntimeException(e);
		}
	}

	protected void readObstacles(){
		try{
			bufferedWriter.write("obstacles\n");
			bufferedWriter.flush();

			//Eat up the ack
			String response = bufferedReader.readLine();
			if(!response.startsWith("ack"))
				throw new RuntimeException("Missing Ack");
			response = bufferedReader.readLine();
			if(response.equals("begin")){
				response = bufferedReader.readLine();
				while(!response.equals("end")){
					Obstacle obstacle = new Obstacle(response);
					game.getObstacles().add(obstacle);
					response = bufferedReader.readLine();
				}
			}
		}catch(IOException e){
			game.setState("errored");
			throw new RuntimeException(e);
		}
	}

	protected void readBases(){
		try{
			bufferedWriter.write("bases\n");
			bufferedWriter.flush();

			//Eat up the ack
			String response = bufferedReader.readLine();
			if(!response.startsWith("ack"))
				throw new RuntimeException("Missing Ack");
			response = bufferedReader.readLine();
			if(response.equals("begin")){
				response = bufferedReader.readLine();
				while(!response.equals("end")){
					Base base = new Base(response);
					assignBaseToTeam(base);
					response = bufferedReader.readLine();
				}
			}
		}catch(IOException e){
			game.setState("errored");
			throw new RuntimeException(e);
		}
	}

	protected void assignBaseToTeam(Base base){
		if(game.getTeam().getColor().equals(base.getTeamColor()))
			game.getTeam().setBase(base);
		for(Team team : game.getTeams()){
			if(team.getColor().equals(base.getTeamColor()))
				team.setBase(base);
		}
	}

	public void readFlags(){
		try{
			bufferedWriter.write("flags\n");
			bufferedWriter.flush();

			//Eat up the ack
			String response = bufferedReader.readLine();
			if(!response.startsWith("ack"))
				throw new RuntimeException("Missing Ack");
			response = bufferedReader.readLine();
			if(response.equals("begin")){
				response = bufferedReader.readLine();
				while(!response.equals("end")){
					assignFlagToTeam(response);
					response = bufferedReader.readLine();
				}
			}
		}catch(IOException e){
			game.setState("errored");
			throw new RuntimeException(e);
		}
	}

	protected void assignFlagToTeam(String serverString){
		String[] parts = serverString.split("\\s+");
		if(game.getTeam().getColor().equals(parts[1])){
			game.getTeam().getFlag().setPossessingTeamColor(parts[2]);
			game.getTeam().getFlag().getPoint().setX(Float.valueOf(parts[3]));
			game.getTeam().getFlag().getPoint().setY(Float.valueOf(parts[4]));
		}
		for(Team team : game.getTeams()){
			if(team.getColor().equals(parts[1])){
				team.getFlag().setPossessingTeamColor(parts[2]);
				team.getFlag().getPoint().setX(Float.valueOf(parts[3]));
				team.getFlag().getPoint().setY(Float.valueOf(parts[4]));
			}
		}
	}

	public String getGameId(){
		return gameId;
	}

	public String getHost(){
		return host;
	}

	public Integer getPort(){
		return port;
	}

	public Game getGame(){
		return game;
	}

	public Socket getSocket(){
		return socket;
	}

	public BufferedReader getBufferedReader(){
		return bufferedReader;
	}

	public BufferedWriter getBufferedWriter(){
		return bufferedWriter;
	}

	public GameControllerListener getGameControllerListener(){
		return gameControllerListener;
	}

	public void setGameControllerListener(GameControllerListener gameControllerListener){
		this.gameControllerListener = gameControllerListener;
	}
}

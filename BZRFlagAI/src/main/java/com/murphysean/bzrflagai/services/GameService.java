package com.murphysean.bzrflagai.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.murphysean.bzrflag.commanders.OccGridCommander;
import com.murphysean.bzrflag.controllers.GameController;
import com.murphysean.bzrflag.daos.KalmanDbHelper;
import com.murphysean.bzrflag.events.KalmanEvent;
import com.murphysean.bzrflag.listeners.GameControllerListener;
import com.murphysean.bzrflag.listeners.KalmanListener;
import com.murphysean.bzrflag.models.KalmanTank;
import com.murphysean.bzrflagai.R;
import com.murphysean.bzrflagai.activities.GameActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.byu.cs.bzrflag.models.Map;

public class GameService extends Service implements GameControllerListener, KalmanListener{
	public static final String HOST_EXTRA = "host";
	public static final String PORT_EXTRA = "port";

	private Thread gameControllerThread;
	public static GameController gameController;

	private PowerManager powerManager;
	private PowerManager.WakeLock wakeLock;

	private Handler handler;

	private KalmanDbHelper kalmanDbHelper;

	/*public static class GameServiceBinder extends Binder{
		GameService gameService;
		public GameServiceBinder(GameService gameService){
			this.gameService = gameService;
		}

		public Game getGame(){
			if(gameService.gameController != null)
				return gameService.gameController.getGame();

			return null;
		}
	}*/

	@Override
	public void onCreate(){
		super.onCreate();
		kalmanDbHelper = new KalmanDbHelper(getApplicationContext());
		gameController = null;
		handler = new Handler();
		powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
	}

	@Override
	public void onDestroy(){
		if(gameControllerThread != null){
			gameControllerThread.interrupt();
			gameControllerThread = null;
		}
		if(gameController != null){
			gameController.close();
			gameController = null;
		}
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		if(intent.hasExtra(HOST_EXTRA) && intent.hasExtra(PORT_EXTRA)){
			if(gameController == null){
				wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GameControllerThread");
				wakeLock.acquire();

				String host = intent.getStringExtra(HOST_EXTRA);
				String port = intent.getStringExtra(PORT_EXTRA);

				gameController = new GameController(UUID.randomUUID().toString(), host, Integer.parseInt(port));
				gameController.setGameControllerListener(this);
				gameControllerThread = new Thread(gameController, "GAMECONTROLLER");
				gameControllerThread.start();

				KalmanTank.listener = this;

				Notification notification = new Notification(R.drawable.abc_ic_search, "Running BZRFlag AI", System.currentTimeMillis());
				Intent notificationIntent = new Intent(this,GameActivity.class);
				PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
				notification.setLatestEventInfo(this, "BZR Flag AI", "Running BZRFlag AI", pendingIntent);
				startForeground(1, notification);

				return Service.START_NOT_STICKY;
			}
		}

		return super.onStartCommand(intent,flags,startId);
	}

	@Override
	public void onClose(){
		wakeLock.release();
		gameController = null;
		gameControllerThread = null;

		handler.post(new Runnable(){
			@Override
			public void run(){
				Toast.makeText(GameService.this.getApplicationContext(),"Connection Died",Toast.LENGTH_SHORT).show();
				GameService.this.stopForeground(true);
			}
		});
	}

	@Override
	public IBinder onBind(Intent intent){
		//return new GameServiceBinder(this);
		return null;
	}

	@Override
	public void onKalmanEvent(KalmanEvent kalmanEvent){
		ContentValues contentValues = new ContentValues();
		contentValues.put("sessionid", "alpha" + kalmanEvent.getSessionId());
		contentValues.put("callsign", kalmanEvent.getCallsign());
		contentValues.put("instant", kalmanEvent.getInstant());

		contentValues.put("px", kalmanEvent.getPx());
		contentValues.put("vx", kalmanEvent.getVx());
		contentValues.put("ax", kalmanEvent.getAx());
		contentValues.put("py", kalmanEvent.getPy());
		contentValues.put("vy", kalmanEvent.getVy());
		contentValues.put("ay", kalmanEvent.getAy());

		contentValues.put("cpx", kalmanEvent.getCpx());
		contentValues.put("cvx", kalmanEvent.getCvx());
		contentValues.put("cax", kalmanEvent.getCax());
		contentValues.put("cpy", kalmanEvent.getCpy());
		contentValues.put("cvy", kalmanEvent.getCvy());
		contentValues.put("cay", kalmanEvent.getCay());

		contentValues.put("ox", kalmanEvent.getOx());
		contentValues.put("oy", kalmanEvent.getOy());
		kalmanDbHelper.getWritableDatabase().insert("kalman", null, contentValues);
	}
}
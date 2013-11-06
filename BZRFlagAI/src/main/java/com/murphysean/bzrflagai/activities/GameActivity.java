package com.murphysean.bzrflagai.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.murphysean.bzrflag.agents.GoToAgent;
import com.murphysean.bzrflag.commanders.OccGridCommander;
import com.murphysean.bzrflag.events.BZRFlagEvent;
import com.murphysean.bzrflag.events.OccGridCompleteEvent;
import com.murphysean.bzrflag.listeners.CommanderListener;
import com.murphysean.bzrflag.models.Game;
import com.murphysean.bzrflag.models.Point;
import com.murphysean.bzrflagai.R;
import com.murphysean.bzrflagai.services.GameService;

public class GameActivity extends Activity implements CommanderListener{
	private ImageView imageView;
	private Bitmap bitmap;
	private Canvas canvas;
	private Paint paint;

	private TextView currentPosition;
	private TextView targetPosition;

	private boolean refreshing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		currentPosition = (TextView)findViewById(R.id.currentPosition);
		targetPosition = (TextView)findViewById(R.id.targetPosition);

		imageView = (ImageView)findViewById(R.id.imageView);
		imageView.setOnTouchListener(new View.OnTouchListener(){
			@Override
			public boolean onTouch(View v,MotionEvent event){

				// calculate inverse matrix
				Matrix inverse = new Matrix();
				imageView.getImageMatrix().invert(inverse);
				// map touch point from ImageView to image
				float[] touchPoint = new float[]{event.getX(),event.getY()};
				inverse.mapPoints(touchPoint);
				// touchPoint now contains x and y in image's coordinate system

				if(GameService.gameController != null){
					Game game = GameService.gameController.getGame();
					if(game.getTeam().getTanks().get(0) instanceof GoToAgent){
						Point gotopoint = new Point((float)Math.floor(touchPoint[0] - 400), (float)Math.floor((touchPoint[1] - 400) * -1));
						((GoToAgent)game.getTeam().getTanks().get(0)).setTarget(gotopoint);
					}
				}
				refreshing = false;
				return true;
			}
		});

		//Create a Bitmap
		bitmap = Bitmap.createBitmap(800,800, Bitmap.Config.ARGB_8888);
		//Create a Canvas
		canvas = new Canvas(bitmap);
		paint = new Paint();

		paint.setARGB(255,255,255,255);
		paint.setStrokeWidth(0);
		canvas.drawRGB(0,0,0);
		//canvas.drawPoint(400,400,paint);

		imageView.setImageBitmap(bitmap);

		Button button = (Button)findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				//OK, let's try something else, how about a background thread that polls that state repeatedly?
				Thread thread = new Thread(new Runnable() {
					public void run() {
						GameActivity.this.refreshing = true;
						if(GameService.gameController != null){
							Game game = GameService.gameController.getGame();
							if(game.getTeam() instanceof OccGridCommander){
								OccGridCommander occGridCommander = (OccGridCommander)game.getTeam();
								for(float x = 0 - (game.getWorldSize() / 2); x < (game.getWorldSize() / 2); x++){
									if(Thread.currentThread().isInterrupted())
										break;
									for(float y = 0 - (game.getWorldSize() / 2); y < (game.getWorldSize() / 2); y++){
										if(Thread.currentThread().isInterrupted())
											break;
										//Scale 0-1 reading to a 0-255 byte value
										//occ is to 1 as x is to 255
										float occ = occGridCommander.getOccGridValue(x,y);
										int color = Math.round(((1.0f - occ) * 255) / 1);
										paint.setARGB(255,color,color,color);
										canvas.drawPoint(occGridCommander.getBitmapX(x),occGridCommander.getBitmapY(y),paint);
									}
									sendInvalidate(game.getTeam().getTanks().get(0).getPosition(), ((GoToAgent)game.getTeam().getTanks().get(0)).getTarget());
								}
							}
						}
						GameActivity.this.refreshing = false;
					}

					private void sendInvalidate(final Point cp, final Point tp){
						if(imageView != null){
							imageView.post(new Runnable(){
								public void run() {
									imageView.invalidate();
								}
							});
						}
					}
				});
				thread.start();
			}
		});
	}

	@Override
	protected void onResume(){
		super.onResume();

		//Set myself as a listener for the commander events
		if(GameService.gameController != null){
			Game game = GameService.gameController.getGame();
			if(game.getTeam() instanceof OccGridCommander){
				OccGridCommander occGridCommander = (OccGridCommander)game.getTeam();
				occGridCommander.setListener(this);
			}
		}
	}

	@Override
	protected void onPause(){
		super.onPause();

		//Set myself as a listener for the commander events
		if(GameService.gameController != null){
			Game game = GameService.gameController.getGame();
			if(game.getTeam() instanceof OccGridCommander){
				OccGridCommander occGridCommander = (OccGridCommander)game.getTeam();
				occGridCommander.setListener(null);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game,menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()){
			case R.id.action_settings:
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBZRFlagEvent(BZRFlagEvent bzrFlagEvent){
		if(imageView != null){
			imageView.post(new Runnable(){
				private OccGridCompleteEvent occGridCompleteEvent;
				public void run(){
					//First I need to run through this portion of the screen and update the bitmap
					if(GameService.gameController != null && !GameActivity.this.refreshing){
						Game game = GameService.gameController.getGame();
						if(game.getTeam() instanceof OccGridCommander){
							OccGridCommander occGridCommander = (OccGridCommander)game.getTeam();

							for(float x = occGridCompleteEvent.getAt().getX(); x < (occGridCompleteEvent.getAt().getX() + occGridCompleteEvent.getSize().getX()); x++){
								for(float y = occGridCompleteEvent.getAt().getY(); y < (occGridCompleteEvent.getAt().getY() + occGridCompleteEvent.getSize().getY()); y++){
									float occ = occGridCommander.getOccGridValue(x,y);
									int color = Math.round(((1.0f - occ) * 255) / 1);
									paint.setARGB(255,color,color,color);
									canvas.drawPoint(occGridCommander.getBitmapX(x),occGridCommander.getBitmapY(y),paint);
								}
							}

							//Draw the tanks
							for(int t = 0; t < game.getTeam().getTanks().size(); t++){
								paint.setARGB(255,0,0,255);
								canvas.drawCircle((int)Math.floor(game.getTeam().getTanks().get(t).getPosition().getX() + (game.getWorldSize() / 2)),
										(int)Math.floor(game.getWorldSize() - (game.getTeam().getTanks().get(t).getPosition().getY() + (game.getWorldSize() / 2))), game.getTankRadius(), paint);
							}
							imageView.invalidate();
						}
					}
				}

				public Runnable setBZRFlagEvent(OccGridCompleteEvent occGridCompleteEvent){
					this.occGridCompleteEvent = occGridCompleteEvent;

					return this;
				}
			}.setBZRFlagEvent((OccGridCompleteEvent)bzrFlagEvent));
		}

		if(currentPosition != null){
			currentPosition.post(new Runnable(){
				@Override
				public void run(){
					if(GameService.gameController != null){
						Game game = GameService.gameController.getGame();
						currentPosition.setText("CP: (" + game.getTeam().getTanks().get(0).getPosition().getX() + ", " + game.getTeam().getTanks().get(0).getPosition().getY() + ")");
					}
				}
			});
		}
		if(targetPosition != null){
			targetPosition.post(new Runnable(){
				@Override
				public void run(){
					if(GameService.gameController != null){
						Game game = GameService.gameController.getGame();
						if(((GoToAgent)game.getTeam().getTanks().get(0)).getTarget() == null)
							targetPosition.setText("TP: NULL");
						else
							targetPosition.setText("TP: (" + ((GoToAgent)game.getTeam().getTanks().get(0)).getTarget().getX() + ", " + ((GoToAgent)game.getTeam().getTanks().get(0)).getTarget().getY() + ")");
					}
				}
			});
		}
	}
}
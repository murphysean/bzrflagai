package com.murphysean.bzrflagai.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.murphysean.bzrflagai.R;
import com.murphysean.bzrflagai.services.GameService;

public class CreateActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);



		findViewById(R.id.connect).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				String host = ((EditText)findViewById(R.id.host)).getText().toString();
				String port = ((EditText)findViewById(R.id.port)).getText().toString();

				Intent intent = new Intent(CreateActivity.this, GameService.class);
				intent.putExtra(GameService.HOST_EXTRA, host);
				intent.putExtra(GameService.PORT_EXTRA, port);
				startService(intent);

				//TODO Open up the game activity
				//Close this activity
				CreateActivity.this.finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.map,menu);
		return true;
	}


}
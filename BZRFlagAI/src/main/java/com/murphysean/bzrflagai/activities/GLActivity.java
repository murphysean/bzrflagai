package com.murphysean.bzrflagai.activities;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.murphysean.bzrflagai.R;
import com.murphysean.bzrflagai.renderers.BZRFlagRenderer;

public class GLActivity extends Activity{

	private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity.
		mGLView = new MyGLSurfaceView(this);
		setContentView(mGLView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	class MyGLSurfaceView extends GLSurfaceView {

		public MyGLSurfaceView(Context context){
			super(context);

			// Create an OpenGL ES 2.0 context
			setEGLContextClientVersion(2);
			// Set the Renderer for drawing on the GLSurfaceView
			setRenderer(new BZRFlagRenderer());
		}
	}
}

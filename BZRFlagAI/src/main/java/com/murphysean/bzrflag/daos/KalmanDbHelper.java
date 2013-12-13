package com.murphysean.bzrflag.daos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class KalmanDbHelper extends SQLiteOpenHelper{
	public KalmanDbHelper(Context context){
		//super(context,Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "nightdb.db",null,1);
		super(context,context.getExternalFilesDir(null).getAbsolutePath() + "/" + "kalman.db",null,1);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("CREATE TABLE kalman (sessionid TEXT, callsign TEXT, instant INTEGER, px REAL, vx REAL, ax REAL, py REAL, vy REAL, ay REAL, cpx REAL, cvx REAL, cax REAL, cpy REAL, cvy REAL, cay REAL, ox REAL, oy REAL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

	}
}
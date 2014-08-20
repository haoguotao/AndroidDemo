package com.example.ui;

import java.util.Vector;

import com.broadcaster.app.MBCommon;
import com.broadcaster.app.MBroadcaster;
import com.example.mockbroadcaster.R;
import com.mucp.tools.MTools;
import com.network.judt.NetTools;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnGestureListener
{
	TextView txtAddr, txtPort, txtLocalAddr;
	ImageView imgMicphone, imgMusicplayer;
	CheckBox ckboxNetState;
	LinearLayout llSettings;

	GestureDetector mGd;
	
	MBroadcaster mBroadcaster;
	boolean mBroadcastOn = false;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		mBroadcaster = new MBroadcaster();

		mGd = new GestureDetector(this);
		
		ckboxNetState = (CheckBox)this.findViewById(R.id.ckboxNet);
		ckboxNetState.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				mBroadcaster.setNetworkState(isChecked);
			}
			
		});

		llSettings = (LinearLayout) this.findViewById(R.id.llSettings);
		llSettings.setVisibility(View.INVISIBLE);

		txtAddr = (TextView) this.findViewById(R.id.txtAddr);
		txtPort = (TextView) this.findViewById(R.id.txtPort);
		txtLocalAddr = (TextView)this.findViewById(R.id.txtLocalAddr);

		imgMicphone = (ImageView) this.findViewById(R.id.imgMicphone);
		imgMicphone.setAlpha(30);

		imgMusicplayer = (ImageView) this.findViewById(R.id.imgMusicplayer);

		imgMicphone.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch (event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					imgMicphone.setAlpha(255);
					imgMusicplayer.setAlpha(30);
					startPTT();
					break;
				case MotionEvent.ACTION_UP:
					imgMusicplayer.setAlpha(255);
					imgMicphone.setAlpha(30);
					stopPTT();
					break;				
				default:
					break;
				}

				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		return mGd.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		float x = e2.getX() - e1.getX();
		float y = e2.getY() - e1.getY();
		float x_limit = 20;
		float y_limit = 20;
		float x_abs = Math.abs(x);
		float y_abs = Math.abs(y);
		if (x_abs >= y_abs)
		{
			if (x > x_limit || x < -x_limit)
			{
				if (x > 0)
					onGestureResult(MBCommon.GESTURE_RIGHT);
				else
					onGestureResult(MBCommon.GESTURE_LEFT);
			}
		}
		else
		{		
			if (y > y_limit || y < -y_limit)
			{
				if (y > 0)
					onGestureResult(MBCommon.GESTURE_DOWN);				
				else
					onGestureResult(MBCommon.GESTURE_UP);
			}
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		return false;
	}

	private void onGestureResult(int direction)
	{
		switch (direction)
		{
		case MBCommon.GESTURE_UP:
			Log.d("MBGesture", "向上滑动");
			llSettings.setVisibility(View.INVISIBLE);
			break;
		case MBCommon.GESTURE_DOWN:
			Log.d("MBGesture", "向下滑动");
			txtLocalAddr.setText(NetTools.getLocalHostIP());
			llSettings.setVisibility(View.VISIBLE);
			break;
		case MBCommon.GESTURE_LEFT:
			Log.d("MBGesture", "向左滑动");
			break;
		case MBCommon.GESTURE_RIGHT:
			Log.d("MBGesture", "向右滑动");
			Intent intent = new Intent();			
			intent.setClass(this, SettingsActivity.class);
			//startActivity(intent);
			startActivityForResult(intent, 0);
			break;
		default:
			break;
		}
	}
	
	private void startPTT()
	{
		mBroadcaster.start(txtAddr.getText().toString(), txtPort.getText().toString());
	}
	
	private void stopPTT()
	{
		mBroadcaster.stop();
	}
	
}

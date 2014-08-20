package com.example.ui;

import com.example.mockbroadcaster.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SettingsActivity extends Activity
{
	Button mBtnSave, mBtnCancel;
	Spinner mSpAudioMime, mSpAudioFormat, mSpSampleRateInHz, mSpAudioCHNumber, mSpAudioBitRate;
	String[] mSpMimeItems = {"audio/3gpp","audio/amr-wb","audio/mp4a-latm"};
	String[] mSpSampleRateItems = {"44100","22050","11025"};
	String[] mSpCHNumberItems = {"单声道","立体声"};
	String[] mSpBitRateItems = {"256","192","128","64"};
	String[] mSpFormatItems = {"PCM_16BIT","PCM_8BIT"};
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		ArrayAdapter<String> amAdapterMime=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mSpMimeItems);
		ArrayAdapter<String> amAdapterHz=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mSpSampleRateItems);
		ArrayAdapter<String> amAdapterCHN=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mSpCHNumberItems);
		ArrayAdapter<String> amAdapterBR=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mSpBitRateItems);
		ArrayAdapter<String> amAdapterF=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mSpFormatItems);
		
		mSpAudioMime = (Spinner)this.findViewById(R.id.spAudioMime);
		mSpAudioMime.setAdapter(amAdapterMime);
		
		mSpSampleRateInHz = (Spinner)this.findViewById(R.id.spSampleRateInHz);
		mSpSampleRateInHz.setAdapter(amAdapterHz);
		
		mSpAudioFormat = (Spinner)this.findViewById(R.id.spAudioFormat);
		mSpAudioFormat.setAdapter(amAdapterF);
		
		mSpAudioBitRate = (Spinner)this.findViewById(R.id.spAudioBitRate);
		mSpAudioBitRate.setAdapter(amAdapterBR);
		
		mSpAudioCHNumber = (Spinner)this.findViewById(R.id.spAudioCHNumber);
		mSpAudioCHNumber.setAdapter(amAdapterCHN);
		
		mBtnSave = (Button)this.findViewById(R.id.btnSave);
		mBtnCancel = (Button)this.findViewById(R.id.btnCancel);
		
		mBtnSave.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				
			}
			
		});
		
		mBtnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				SettingsActivity.this.finish();
			}
			
		});
	}
}

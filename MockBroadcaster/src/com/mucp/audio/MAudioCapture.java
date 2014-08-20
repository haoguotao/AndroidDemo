package com.mucp.audio;


import java.util.ArrayList;

import com.broadcaster.app.MBCommon;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class MAudioCapture
{
	ArrayList<Handler> mHandlers = new ArrayList<Handler>();
	
	AudioRecord mAudioRecord;
	byte[] mARBuffer;
	Thread mARThread;
	boolean mAudioRecordOn = false;;

	
	public void addHandler(Handler handler)
	{
		if(!this.mHandlers.contains(handler))
			this.mHandlers.add(handler);
	}
	
	public void removeHandler(Handler handler)
	{
		if(this.mHandlers.contains(handler))
			this.mHandlers.remove(handler);		
	}

	public boolean initCapture(int sampleRateInHz, int channelConfig, int audioFormat)
	{
		try
		{
			int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);// 获得缓冲区字节大小
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize);
			mARBuffer = new byte[bufferSize];
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public void disposeCapture()
	{
		if (mAudioRecord == null) return;

		mAudioRecord.release();
		mAudioRecord = null;
	}

	public boolean start()
	{
		if (mAudioRecord == null) return false;

		mARThread = new Thread()
		{
			public void run()
			{
				doAudioRecord();
			}
		};
		mARThread.start();
		return true;
	}

	public void stop()
	{
		mAudioRecordOn = false;
	}

	private void onCatchAudioData(byte[] buffer, int size)
	{
		for(int i=0 ; i< mHandlers.size(); i++)
		{
			Bundle bd = new Bundle();
			bd.putByteArray("AudioRawData", buffer);
			bd.putInt("AudioRawDataSize", size);

			Message msg = new Message();
			msg.what = MBCommon.MessageType_OnCatchAudioRawData;
			msg.setData(bd);

			mHandlers.get(i).sendMessage(msg);	
		}
	}

	private void doAudioRecord()
	{
		mAudioRecordOn = true;
		try
		{
			mAudioRecord.startRecording();
			while (mAudioRecordOn)
			{
				int result = mAudioRecord.read(mARBuffer, 0, mARBuffer.length);
				this.onCatchAudioData(mARBuffer, result);
			}

			mAudioRecord.stop();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mAudioRecordOn = false;
	}
}

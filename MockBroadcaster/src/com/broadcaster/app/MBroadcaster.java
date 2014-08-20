package com.broadcaster.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.jni.udt.AcceptResult;
import com.jni.udt.JniUDT;
import com.mucp.audio.MADecodeResult;
import com.mucp.audio.MAudioCapture;
import com.mucp.audio.MAudioCodec;
import com.mucp.audio.MAudioParamaters;
import com.mucp.audio.MAudioPlayer;
import com.network.judt.UDTClient;
import com.network.judt.UDTServer;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MBroadcaster
{
//	int sampleRateInHz = 44100;
//	@SuppressWarnings("deprecation")
//	int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
//	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
//	int bitRate = 128*1024;
	MAudioParamaters mAudioParamaters;
	
	MAudioPlayer mAudioPlayer;
	MAudioCapture mACapture;
	MAudioCodec mEncodec;
	MAudioCodec mDecodec;
	
	UDTServer mServer;
	UDTClient mClient;

	Handler mHandler;
	
	SendThread mSendThread;	
	Queue<byte[]> mQueueSend = new LinkedList<byte[]>();
	boolean mSendThreadOn = false;
	
	boolean mNetworkState = true;

	File mFAudioRaw, mFAudioCompress;
	FileOutputStream mFOAR, mFOAC;

	public MBroadcaster()
	{
		//prepareTestFile();		
		
		prepareHandler();
		
		prepareAudioParamaters();

		prepareNetwork();
		prepareAudioCapture();
		prepareAudioCodec();
		prepareAudioPlayer();
	}
	
	private void prepareAudioParamaters()
	{
		this.mAudioParamaters = new MAudioParamaters();
	}
	
	public void setAudioParamaters(MAudioParamaters ap)
	{
		this.mAudioParamaters = ap;
	}
	
	public MAudioParamaters getAudioParamaters()
	{
		return this.mAudioParamaters;
	}
		
	protected void finalize()
	{
		mAudioPlayer = null;
		
		if(mSendThreadOn)
			mSendThreadOn = false;
		
		if(mACapture != null) mACapture.disposeCapture();
		if(mEncodec != null) mEncodec.disposeCodec();
		if(mDecodec != null) mDecodec.disposeCodec();
		if(mClient != null) mClient.Close();
	}

	public boolean start(String addr, String port)
	{
		if(mNetworkState)
		{
			if(mClient == null)
			{
				mClient = new UDTClient();
				mClient.Connect(addr, Integer.valueOf(port));	
			}
			
			mSendThread = new SendThread(mClient, mQueueSend);
			mSendThread.start();
		}
		
		mACapture.start();
		
		Log.d("MBStop","PTT Start!");
		
		return true;
	}

	public boolean stop()
	{
		mACapture.stop();
		
		if(mClient != null)
		{
			mClient.Close();
			mClient = null;
		}
		
		mQueueSend.clear();
		
		mSendThreadOn = false;
		
		Log.d("MBStop","PTT Stop!");
		
		return true;
	}

	public void setNetworkState(boolean isEnable)
	{
		mNetworkState = isEnable;
	}
	
	public boolean getNetworkState()
	{
		return mNetworkState;
	}
	
	@SuppressLint("HandlerLeak")
	private void prepareHandler()
	{
		mHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{				
				super.handleMessage(msg);
				Bundle bd = msg.getData();
				switch (msg.what)
				{
				case MBCommon.MessageType_OnCatchAudioRawData:					
					int size = bd.getInt("AudioRawDataSize");
					byte[] data = bd.getByteArray("AudioRawData");
					onCatchAudioRawData(data, size);
					break;
				case MBCommon.MessageType_OnClientAccept:					
					AcceptResult ar = (AcceptResult)bd.getSerializable("AcceptResult");
					onClientAccepted(ar);
					break;
				case MBCommon.MessageType_OnCatchNetData:
					int bfsize = bd.getInt("BufferSize");
					byte[] amrbuffer = bd.getByteArray("AmrBuffer");
					onCatchAmrAudioData(amrbuffer, bfsize);
					break;
				case MBCommon.MessageType_Unknown:
					break;
				default:
					break;
				}

				bd.clear();
				bd = null;
				msg = null;						
			}
		};		
	}

	private void prepareAudioCapture()
	{
		mACapture = new MAudioCapture();
		mACapture.addHandler(mHandler);	
		//mACapture.initCapture(sampleRateInHz, channelConfig, audioFormat);
		mACapture.initCapture(this.mAudioParamaters.getSampleRateInHz(), this.mAudioParamaters.getChannelConfig(), this.mAudioParamaters.getAudioFormat());
	}

	private void prepareAudioCodec()
	{
		mEncodec = new MAudioCodec(true);
		mDecodec = new MAudioCodec(false);	

//		int channels = (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) ? 1 : 2;
//		mEncodec.initCodec(sampleRateInHz, channels, bitRate);
//		mDecodec.initCodec(sampleRateInHz, channels, bitRate);
		int channels = (this.mAudioParamaters.getChannelConfig() == AudioFormat.CHANNEL_CONFIGURATION_MONO) ? 1 : 2;
		mEncodec.initCodec(this.mAudioParamaters.getSampleRateInHz(), channels, this.mAudioParamaters.getBitRate());
		mDecodec.initCodec(this.mAudioParamaters.getSampleRateInHz(), channels, this.mAudioParamaters.getBitRate());
	}
	
	private void prepareNetwork()
	{
		mServer = new UDTServer("127.0.0.1", "9001");
		mServer.registerListener(mHandler);
		mServer.star();
	}
	
	private void prepareAudioPlayer()
	{
		mAudioPlayer = new MAudioPlayer(AudioManager.STREAM_MUSIC, this.mAudioParamaters.getSampleRateInHz(), this.mAudioParamaters.getChannelConfig(), this.mAudioParamaters.getAudioFormat(), AudioTrack.MODE_STREAM);	
		mAudioPlayer.Start();
	}
	
	private void prepareTestFile()
	{
		try
		{
			mFAudioRaw = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioRaw.aac");
			mFAudioCompress = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioCompress.pcm");
			
			if(mFAudioRaw.exists()) mFAudioRaw.createNewFile();
			if(mFAudioCompress.exists()) mFAudioCompress.createNewFile();
			
			mFOAR = new FileOutputStream(mFAudioRaw);
			mFOAC = new FileOutputStream(mFAudioCompress);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void save2TestFile(FileOutputStream fos, byte[] data, int size)
	{
		try
		{
			fos.write(data, 0, size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void onCatchAudioRawData(byte[] data, int size)
	{
		if(size <= 0) return;
		
		byte[] amrBuffer = new byte[size];
		int amrSize = mEncodec.DoEncode(data, amrBuffer);
		Log.d("MBEncodeSize","Encode size ++ " + String.valueOf(amrSize) + "+++");
		
		if(amrSize > 0)
		{
			if (mNetworkState) 
				sendAudioBuffer(amrBuffer, amrSize);
			else 
				onCatchAmrAudioData(amrBuffer, amrSize);
		}
		
		data = null;
		amrBuffer = null;
	}
	
	private void onCatchAmrAudioData(byte[] data,  int size)
	{
		if(size <= 0) return;
		//byte[] rawBuffer = new byte[8192 * 2];
		//byte[] rawBuffer = new byte[size*2];		
		//int rawSize = mDecodec.DoDecode(data, size, rawBuffer);
		MADecodeResult mdr = mDecodec.DoDecodeEx(data, size);
		int rawSize = mdr.getRawSize();
		byte[] rawBuffer = mdr.getRawBuffer();
		Log.d("MBDecodeSize", "Decode size -- " + String.valueOf(rawSize) + "---");
		
		if(rawSize > 0) 
			playAudio(rawBuffer, rawSize);
		
		rawBuffer = null;
		mdr = null;
	}
	
	private void onClientAccepted(AcceptResult ar)
	{
		Log.d("MBClientAccept", ar.getHostAddress() + ":" + ar.getHostPort());
		if(!ar.AcceptState()) return;
		
		new RecvThread(ar.getJniUDT()).start();
	}
	
	private void playAudio(byte[] src, int size)
	{
//		byte[] _buffer = new byte[size];
//		
//		System.arraycopy(src, 0, _buffer, 0, size);
		
		mAudioPlayer.Play(src, size);
	}
	
	private void sendAudioBuffer(byte[] buffer, int size)
	{
		byte[] _buffer = new byte[size];
		
		System.arraycopy(buffer, 0, _buffer, 0, size);
		buffer = null;
		
		if(!mQueueSend.offer(_buffer))
			Log.d("MBQueueSend", "Offer failed, buffer size is  ----" + String.valueOf(size) + "------");
	}
	
	class RecvThread extends Thread
	{
		JniUDT _judt;
		public RecvThread(JniUDT judt)
		{
			_judt = judt;
		}
		
		public void run()
		{
			int rssize = 0;
			byte[] rsBuffer = new byte[512];
			do
			{
				rssize = _judt.Recv(rsBuffer, 0, rsBuffer.length, 0);
				Log.d("MBAcceptClient", "Recv data size : " + rssize);
				
				if(rssize > 0)
				{
					Bundle bd = new Bundle();					
					bd.putByteArray("AmrBuffer", rsBuffer);
					bd.putInt("BufferSize", rssize);
					Message msg = new Message();
					msg.what = MBCommon.MessageType_OnCatchNetData;
					msg.setData(bd);
					mHandler.sendMessage(msg);
				}
			}while(rssize > 0);
			
			_judt.Close();
		}
	}
	
	class SendThread extends Thread
	{
		Queue<byte[]> _queue;
		UDTClient _client;
		public SendThread(UDTClient client, Queue<byte[]> queue)
		{
			this._queue = queue;
			this._client = client;
		}
		
		@Override
		public void run()
		{
			mSendThreadOn = true;
			while(mSendThreadOn)
			{				
				try
				{
					byte[] _buffer = _queue.poll();				
					
					if((_buffer != null) && (_client != null))
					{
						int _ssize = _client.Send(_buffer, 0, _buffer.length);						
						Log.d("MBSendThread", "send size is " + String.valueOf(_ssize));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}			
			}
		}
	}
	
}

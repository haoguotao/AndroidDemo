package com.mucp.audio;

import java.util.LinkedList;
import java.util.Queue;
import android.annotation.SuppressLint;
import android.media.AudioTrack;
import android.util.Log;

public class MAudioPlayer
{
	AudioTrack mAudioTrack = null;
	PlayThread mPlayThread;
	Queue<byte[]> mQueuePlay = new LinkedList<byte[]>();
	boolean mPlayThreadOn = false;

	@SuppressLint("NewApi")
	public MAudioPlayer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int mode)
	{
		int minBufSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		mAudioTrack = new AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, minBufSize * 2, mode);
	}
	
	@Override
	protected void finalize()
	{
		mQueuePlay.clear();
		mQueuePlay = null;
		mPlayThreadOn = false;
		
		if(mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED)
		{
			mAudioTrack.stop();
		}		
		mAudioTrack.release();
		mAudioTrack = null;
	}

	public boolean Start()
	{
		if(mAudioTrack == null) return false;
		
		if(mPlayThread == null)
		{
			mPlayThread = new PlayThread(mAudioTrack, mQueuePlay);
			mPlayThread.start();
		}
		
		return true;
	}
	
	public void Stop()
	{
		mPlayThreadOn = false;
	}
	
	public boolean Play(byte[] buffer, int size)
	{
		return mQueuePlay.offer(buffer);		
	}
	
	public void Pause()
	{
		mAudioTrack.pause();
	}

	public boolean SpeakerSwitch(boolean isLoudSpeaker)
	{		
		Log.d("MAudioPlayer", "—Ô…˘∆˜◊¥Ã¨…Ë÷√Œ™: " + String.valueOf(isLoudSpeaker));
		return true;
	}

	public boolean SilenceSwitch(boolean isMute)
	{
		float volume = isMute ? 0.0f : 1.0f;
		
		mAudioTrack.setStereoVolume(volume,  volume);
		
		Log.d("MAudioPlayer", "æ≤“Ù◊¥Ã¨…Ë÷√Œ™£∫"+ String.valueOf(isMute));

		return true;
	}
	
	class PlayThread extends Thread
	{
		AudioTrack _audioTrack;
		Queue<byte[]> _queue;
		public PlayThread(AudioTrack at, Queue<byte[]> pq)
		{
			this._queue = pq;
			this._audioTrack = at;
		}
		
		public void run()
		{
			mPlayThreadOn = true;			
			_audioTrack.play();
			
			while(mPlayThreadOn && (_queue != null))
			{
				try
				{
					if(_queue.size() <= 0) continue;
					
					byte[] _buffer = _queue.poll();
					
					if(_buffer == null) continue;

					if (_buffer.length > 0)
					{
						int pSize = _audioTrack.write(_buffer, 0, _buffer.length);
						_buffer = null;
						Log.d("MAudioPlayer", "Audio play size -- " + String.valueOf(pSize) + "---");
					}	
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			_audioTrack.stop();
		}
	}
}

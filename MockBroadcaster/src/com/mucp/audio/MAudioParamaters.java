package com.mucp.audio;

import android.media.AudioFormat;

public class MAudioParamaters
{
	int sampleRateInHz;
	int channelConfig;
	int audioFormat;
	int bitRate;
	
	public MAudioParamaters()
	{
		sampleRateInHz = 44100;		
		channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		bitRate = 128*1024;
	}

	public MAudioParamaters(int sr, int cc, int af, int br)
	{
		sampleRateInHz = sr;		
		channelConfig = cc;
		audioFormat = af;
		bitRate = br;
	}
	
	public int getSampleRateInHz()
	{
		return this.sampleRateInHz;
	}
	
	public int getChannelConfig()
	{
		return this.channelConfig;
	}
	
	public int getAudioFormat()
	{
		return this.audioFormat;
	}
	
	public int getBitRate()
	{
		return this.bitRate;
	}
}

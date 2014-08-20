package com.mucp.audio;

import java.nio.ByteBuffer;

import com.mucp.tools.MTools;
import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.util.Log;

public class MAudioCodec
{
	  public static final String AUDIO_MIME_TYPE = "audio/3gpp"; //AMR-NB
	 // public static final String AUDIO_MIME_TYPE = "audio/amr-wb";//AMR-WB
	 // public static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";// AAC

	boolean mIsEncoder = false;
	MediaCodec mCodec;
	String mCodecName;

	public MAudioCodec(boolean isEncoder)
	{
		this.mIsEncoder = isEncoder;
	}

	public boolean initCodec(int sampleRate, int channels, int bitrate)
	{
		if (mIsEncoder) initEncoder(sampleRate, channels, bitrate);
		else initDecoder(sampleRate, channels, bitrate);
		return true;
	}

	@SuppressLint("NewApi")
	public void disposeCodec()
	{
		if (mCodec != null)
		{
			mCodec.stop();
			mCodec.release();
			mCodec = null;
		}
	}

	@SuppressLint("NewApi")
	private void initEncoder(int sampleRate, int channels, int bitrate)
	{
		MediaFormat formater = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, sampleRate, channels);
		formater.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
		formater.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channels);
		formater.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);

		MediaCodecInfo mci = MTools.SelectCodec(AUDIO_MIME_TYPE, true);
		if (mci != null)
		{
			mCodecName = mci.getName();
			mCodec = MediaCodec.createByCodecName(mci.getName());
		}
		else mCodec = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
		mCodec.configure(formater, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		mCodec.start();

	}

	@SuppressLint("NewApi")
	private void initDecoder(int sampleRate, int channels, int bitrate)
	{
		MediaFormat formater = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, sampleRate, channels);

		MediaCodecInfo mci = MTools.SelectCodec(AUDIO_MIME_TYPE, false);
		if (mci != null)
		{
			mCodecName = mci.getName();
			mCodec = MediaCodec.createByCodecName(mCodecName);
		}
		else mCodec = MediaCodec.createDecoderByType(AUDIO_MIME_TYPE);
		mCodec.configure(formater, null, null, 0);
		mCodec.start();
	}

	@SuppressLint("NewApi")
	public int DoEncode(byte[] in, byte[] out)
	{
		int outOffset = 0;
		ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
		ByteBuffer[] outputBuffers = mCodec.getOutputBuffers();

		for (int inOffset = 0; inOffset < in.length;)
		{
			int inputBufferIndex = mCodec.dequeueInputBuffer(-1);
			int _size = ((inOffset + inputBuffers[inputBufferIndex].capacity()) < in.length) ? inputBuffers[inputBufferIndex].capacity() : (in.length - inOffset);
			if (inputBufferIndex >= 0)
			{
				inputBuffers[inputBufferIndex].clear();
				inputBuffers[inputBufferIndex].put(in, inOffset, _size);
				mCodec.queueInputBuffer(inputBufferIndex, 0, _size, 0, 0);
				inOffset += _size;
			}

			BufferInfo bi = new BufferInfo();
			int outputBufferIndex = mCodec.dequeueOutputBuffer(bi, 0);
			int adtSize = AUDIO_MIME_TYPE.equalsIgnoreCase("audio/mp4a-latm") ? 7 : 0;
			while (outputBufferIndex >= 0)
			{
				outputBuffers[outputBufferIndex].position(bi.offset);
				outputBuffers[outputBufferIndex].limit(bi.offset + bi.size);

				if (adtSize == 7) addADTStoPacket(out, outOffset, bi.size + adtSize);
				outputBuffers[outputBufferIndex].get(out, outOffset + adtSize, bi.size);
				outputBuffers[outputBufferIndex].position(bi.offset);
				outOffset += (bi.size + adtSize);

				mCodec.releaseOutputBuffer(outputBufferIndex, false);
				outputBufferIndex = mCodec.dequeueOutputBuffer(bi, 0);
			}

			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
			{
				ByteBuffer[] tmp = mCodec.getOutputBuffers();
			}
			else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
			{
				MediaFormat tmp = mCodec.getOutputFormat();
			}
		}

		return outOffset;
	}

	@SuppressLint("NewApi")
	public MADecodeResult DoDecodeEx(byte[] in, int len)
	{
		byte[] out = new byte[8192];
		int outOffset = 0;
		ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
		ByteBuffer[] outputBuffers = mCodec.getOutputBuffers();

		for (int inOffset = 0; inOffset < len;)
		{
			int inputBufferIndex = mCodec.dequeueInputBuffer(-1);
			int _size = ((inOffset + inputBuffers[inputBufferIndex].capacity()) < len) ? inputBuffers[inputBufferIndex].capacity() : len;
			if (inputBufferIndex >= 0)
			{
				inputBuffers[inputBufferIndex].clear();
				inputBuffers[inputBufferIndex].put(in, 0, _size);
				mCodec.queueInputBuffer(inputBufferIndex, 0, _size, 0, 0);
				inOffset += _size;
			}

			BufferInfo bi = new BufferInfo();
			int outputBufferIndex = mCodec.dequeueOutputBuffer(bi, 0);

			while (outputBufferIndex >= 0)
			{
				if ((outOffset + bi.size) > out.length)
				{
					// break;
					Log.d("MAudioCodec", "Decode out buffer before expand size is : " + out.length);
					out = MTools.expandByteArray(out, outOffset + bi.size - out.length);
					Log.d("MAudioCodec", "Decode out buffer expand to : " + out.length);
				}

				outputBuffers[outputBufferIndex].position(bi.offset);
				outputBuffers[outputBufferIndex].limit(bi.offset + bi.size);
				outputBuffers[outputBufferIndex].get(out, outOffset, bi.size);
				outOffset += bi.size;

				mCodec.releaseOutputBuffer(outputBufferIndex, false);
				outputBufferIndex = mCodec.dequeueOutputBuffer(bi, 0);
			}

			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
			{
				ByteBuffer[] tmp = mCodec.getOutputBuffers();
			}
			else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
			{
				MediaFormat tmp = mCodec.getOutputFormat();
			}
		}

		MADecodeResult mdr = new MADecodeResult(out, outOffset);
		out = null;
		return mdr;
	}

	@SuppressLint("NewApi")
	public int DoDecode(byte[] in, int len, byte[] out)
	{
		int outOffset = 0;
		ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
		ByteBuffer[] outputBuffers = mCodec.getOutputBuffers();

		int inputBufferIndex = mCodec.dequeueInputBuffer(-1);
		if (inputBufferIndex >= 0)
		{
			inputBuffers[inputBufferIndex].clear();
			inputBuffers[inputBufferIndex].put(in, 0, len);
			mCodec.queueInputBuffer(inputBufferIndex, 0, len, 0, 0);
		}

		BufferInfo bi = new BufferInfo();
		int outputBufferIndex = mCodec.dequeueOutputBuffer(bi, 0);

		while (outputBufferIndex >= 0)
		{
			if ((outOffset + bi.size) > out.length)
			{
				break;
				// Log.d("MAudioCodec",
				// "Decode out buffer before expand size is : " + out.length);
				// out = MTools.expandByteArray(out,
				// outOffset+bi.size-out.length);
				// Log.d("MAudioCodec", "Decode out buffer expand to : " +
				// out.length);
			}

			outputBuffers[outputBufferIndex].position(bi.offset);
			outputBuffers[outputBufferIndex].limit(bi.offset + bi.size);
			outputBuffers[outputBufferIndex].get(out, outOffset, bi.size);
			outOffset += bi.size;

			mCodec.releaseOutputBuffer(outputBufferIndex, false);
			outputBufferIndex = mCodec.dequeueOutputBuffer(bi, 0);
		}

		if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
		{
			ByteBuffer[] tmp = mCodec.getOutputBuffers();
		}
		else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
		{
			MediaFormat tmp = mCodec.getOutputFormat();
		}

		return outOffset;
	}

	@SuppressLint("InlinedApi")
	private void addADTStoPacket(byte[] packet, int offset, int packetLen)
	{
		int profile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
		int freqIdx = 4; // 44.1KHz
		int chanCfg = 1; // CPE //1:表示单声道，2：表示双声道。

		packet[offset + 0] = (byte) 0xFF;
		packet[offset + 1] = (byte) 0xF9;
		packet[offset + 2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
		packet[offset + 3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
		packet[offset + 4] = (byte) ((packetLen & 0x7FF) >> 3);
		packet[offset + 5] = (byte) (((packetLen & 7) << 5) + 0x1F);
		packet[offset + 6] = (byte) 0xFC;
	}
}

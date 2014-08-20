package com.mucp.audio;

public class MADecodeResult
{
	private int rawSize;
	private byte[] rawBuffer;
	
	public int getRawSize()
	{
		return rawSize;
	}
	
	public byte[] getRawBuffer()
	{
		return rawBuffer;
	}
	
	public MADecodeResult(byte[] raw, int size)
	{
		if(size != 0)
		{
			this.rawBuffer = new byte[size];
			System.arraycopy(raw, 0, rawBuffer, 0, size);
		}
		this.rawSize = size;
	}
}

package com.network.judt;

import com.jni.udt.JniUDT;

public class UDTClient
{
	JniUDT mJudt;
	String mSrvAddr;
	int mSrvPort;
	
	boolean mIsConnected = false;
	
	public UDTClient()
	{
		mJudt = new JniUDT();
	}
	
	public boolean Connect(String addr, int port)
	{
		int result = mJudt.Connect(addr, port);
		mIsConnected = (result >= 0);
		return mIsConnected;
	}
	
	public void Close()
	{
		mIsConnected = false;
		mJudt.Close();
		mJudt = null;
	}
	
	public int Send(byte[] buffer, int offset, int size)
	{
		return mJudt.Send(buffer, offset, size, 0);
	}

}

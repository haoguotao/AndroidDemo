package com.jni.udt;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AcceptResult  implements Serializable
{
	int mSocket;
	JniUDT mJniUDT;
	String mHostAddress;
	String mHostPort;
	
	public JniUDT getJniUDT()
	{
		return mJniUDT;
	}
	
	public boolean AcceptState()
	{		
		return (mSocket != -1);
	}
	
	public int getSocket()
	{
		return mSocket;
	}
	
	public String getHostAddress()
	{
		return mHostAddress;
	}
	
	public String getHostPort()
	{
		return mHostPort;
	}
	
	public AcceptResult(int socket, String address, String port)
	{
		this.mSocket = socket;
		this.mHostAddress = address;
		this.mHostPort = port;
		
		mJniUDT = new JniUDT(mSocket);
	}
}

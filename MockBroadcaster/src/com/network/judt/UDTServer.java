package com.network.judt;

import com.broadcaster.app.MBCommon;
import com.jni.udt.AcceptResult;
import com.jni.udt.JniUDT;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UDTServer
{
	final int MAX_BACKLOG = 10;
	Handler mHandler;
	JniUDT mJudt;
	String mAddr;
	int mPort;
	Thread mSrvThread;
	boolean mSrvThreadOn = false;;
	
	public UDTServer(String localAddr, String localPort)
	{
		mJudt = new JniUDT();
		mAddr = localAddr;
		mPort = Integer.valueOf(localPort);
	}

	public boolean star()
	{
		int result = mJudt.Bind(mPort);
		
		result = mJudt.listen(MAX_BACKLOG);
		
		mSrvThread = new Thread()
		{
			public void run()
			{
				mSrvThreadOn = true;
				while(mSrvThreadOn)
				{
					AcceptResult ar = mJudt.accept();
		        	if(!ar.AcceptState())
		        	{
		        		Log.d("UDTServer", "accept socket invalid");
		            	continue;
		        	}
		        	String ip = ar.getHostAddress();
		        	String pt = ar.getHostPort();
		        	Log.d("UDTServer", "Found new client connected: "+ip +":" +pt);
		        	
		        	onCatchData(ar);
				}
			}
		};
		
		mSrvThread.start();
		
		return true;
	}
	
	public void stop()
	{
		mSrvThreadOn = false;
	}
	
	public void registerListener(Handler handler)
	{
		this.mHandler = handler;
	}
	
	private void onCatchData(AcceptResult ar)
	{
		if(mHandler == null) return;
		
		Bundle bd = new Bundle();		
		bd.putSerializable("AcceptResult", ar);
		Message msg = new Message();
		msg.what = MBCommon.MessageType_OnClientAccept;
		msg.setData(bd);
		
		mHandler.sendMessage(msg);
	}

}

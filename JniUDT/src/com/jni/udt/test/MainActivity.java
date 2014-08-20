package com.jni.udt.test;

import com.jni.udt.AcceptResult;
import com.jni.udt.JniUDT;
import com.jni.udt.R;
import com.jni.udt.R.layout;
import com.jni.udt.R.menu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private boolean mTestOn = false;
	private Handler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final TextView txtAddress = (TextView)this.findViewById(R.id.txtAddress);
		final TextView txtPort = (TextView)this.findViewById(R.id.txtPort);
		final TextView txtTestInfo = (TextView)this.findViewById(R.id.txtTestInfo);
		Button btnStart = (Button)this.findViewById(R.id.btnStart);
		Button btnStop = (Button)this.findViewById(R.id.btnStop);
		
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{				
				switch(msg.what)
				{
				case 0:
					txtTestInfo.setText(String.valueOf(msg.arg1));
					break;
				case 1:
					break;
				default:
					break;
				}
			}
		};
		
		btnStart.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				//startSendTest(txtAddress.getText().toString(), txtPort.getText().toString());
				startRecvTest(txtAddress.getText().toString(), txtPort.getText().toString());
			}

		});
		
		btnStop.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				stopTest();
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void startRecvTest(String addr, String port)
	{
		final String _addr = addr;
		final String _port = port;
				
		new Thread()
		{
			public void run()
			{
				recv(_addr, _port);
			}
		}.start();		
	}

	private void startSendTest(String addr, String port)
	{
		final String _addr = addr;
		final String _port = port;
				
		new Thread()
		{
			public void run()
			{
				send(_addr, _port);
			}
		}.start();		
	}
	
	private void stopTest()
	{
		mTestOn = false;
	}
	
	private void recv(String addr, String port)
	{
		int result = 0;
		
        JniUDT judt = new JniUDT();
        
        result = judt.Bind(Integer.valueOf(port));
        Log.d("UDT-JNI", "socket bind result = " + result);
        
        result = judt.listen(10);
        Log.d("UDT-JNI", "socket listen result = " + result);

        mTestOn = true;
        while(mTestOn)
        {
        	AcceptResult ar = judt.accept();
        	if(!ar.AcceptState())
        	{
        		System.out.print("accept socket invalid");
            	continue;
        	}
        	String ip = ar.getHostAddress();
        	String pt = ar.getHostPort();
        	Log.d("UDT-JNI", "Found new client connected: "+ip +":" +pt);
        	
        	new RecvThread(ar).start();
        }
	}
	
	private void send(String addr, String port)
	{
		int result = 0;
		JniUDT judt = new JniUDT();
        
		result = judt.Connect(addr, Integer.valueOf(port));
        Log.d("UDT-JNI", "connect result = " + result);
        
        int data_size = 100000;
        byte[] buffer = new byte[data_size];
        for (int i = 0; i < data_size; i++) buffer[i] = (byte)i;
        
        mTestOn = true;

        int sent_size = 0;
        Message msg = new Message();
        while ((sent_size != data_size) && mTestOn)
        {
        	int send_count = judt.Send(buffer, sent_size, data_size-sent_size, 0);
            Log.d("UDT-JNI", "send count = " + send_count);
            msg.what = 0;
            msg.arg1 = send_count;
            mHandler.sendMessage(msg);
            sent_size += send_count;
        }
        
        result = judt.Close();
        Log.d("UDT-JNI", "close result = " + result);
        
        judt = null;
	}

	class RecvThread extends Thread
	{
		JniUDT _jniUdt;
		int mSocket;
		
		public RecvThread(int socket)
		{
			mSocket = socket;			
		}
		
		public RecvThread(AcceptResult ar)
		{
			_jniUdt = ar.getJniUDT();
		}
		
		public void run()
		{
			byte[] buffer = new byte[100000];
			while(true)
			{
				int rsize = 0;
			    int rs = 0;
				while(rsize < buffer.length)
				{
					rs = _jniUdt.Recv(buffer, 0, buffer.length, 0);
					Log.d("UDT-JNI", "Rec data length : " + rs);
					rsize += rs;
				}
				
				if(rsize < buffer.length)
					break;
			}
			
			Log.d("UDT-JNI", "Rec finished!");
		}
	}
}

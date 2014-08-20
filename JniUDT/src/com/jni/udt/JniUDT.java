package com.jni.udt;

public class JniUDT
{
	static
	{
		System.loadLibrary("stlport_shared");
		System.loadLibrary("udt");
		System.loadLibrary("udtjni");
	}
	
	int mSocket;  
	
	public JniUDT()
	{
		startup();
		mSocket = socket();
	}
	
	public JniUDT(int socket)
	{
		startup();
		mSocket = socket;
	}

	protected void finalize()
	{
		if(mSocket > 0)
			close(mSocket);
		
		cleanup();
	}
	
	public int Connect(String ip, int port)
	{
		return this.connect(mSocket, ip, port);
	}
	
	public int Close()
	{
		return close(mSocket);
	}
	
	public int Send(byte[] buffer, int offset, int size, int flags)
	{
		return send(mSocket, buffer, offset, size, flags);
	}
	
	public int Recv(byte[] buffer, int offset, int size, int flags)
	{
		return recv(mSocket, buffer, offset, size, flags);
	}

	public int Bind(int port)
	{
		return bind(mSocket, port);
	}
	
	public int listen(int backlog)
	{
		return listen(mSocket, backlog);
	}
	
	public AcceptResult accept()
	{
		return accept(mSocket);
	}
	
	private static native int startup();

	private static native int cleanup();

	private static native int socket();

	private native int connect(int socket, String ip, int port);

	private static native int close(int socket);

	private static native int send(int socket, byte[] buffer, int offset, int size, int flags);

	private static native int recv(int socket, byte[] buffer, int offset, int size, int flags);
	
	private static native int bind(int socket, int port);
	
	private static native int listen(int socket, int backlog);
	
	private static native AcceptResult accept(int socket);
}

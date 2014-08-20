package com.network.judt;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.util.Log;

public class NetTools
{
	public static String getLocalHostIP()
	{
		String hosts = "";
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements())
            {
                NetworkInterface nif = en.nextElement();
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                while (inet.hasMoreElements())
                {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ip.getHostAddress()))
                    {                    	
                    	Log.e("MBNetTools", "����IPΪ��"+ip.getHostAddress());
                    	hosts += ip.getHostAddress()+",";                        
                    }
                }
            }
        }
        catch (SocketException e)
        {
            Log.e("MBNetTools", "��ȡ����ip��ַʧ�ܣ�");
            e.printStackTrace();
        }
        return hosts;
	}
}

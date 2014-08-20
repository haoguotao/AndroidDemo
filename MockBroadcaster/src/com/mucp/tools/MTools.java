package com.mucp.tools;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.util.Log;

public class MTools
{
	@SuppressLint("NewApi")
	public static MediaCodecInfo SelectCodec(String mimeType, boolean isEncoder)
	{
		MediaCodecInfo codecInfo;
		int codecCount = MediaCodecList.getCodecCount();
		for (int i = 0; i < codecCount; i++)
		{
			codecInfo = MediaCodecList.getCodecInfoAt(i);

			if(isEncoder && !codecInfo.isEncoder())
			{
				continue;
			}
			else if(!isEncoder && codecInfo.isEncoder())
			{
				continue;
			}

			String[] types = codecInfo.getSupportedTypes();
			for (int j = 0; j < types.length; j++)
			{
				if (types[j].equalsIgnoreCase(mimeType)) 
				{
					Log.d("MAudioCodec","SelectCodec name is " + codecInfo.getName());
					return codecInfo; 
				}
			}
		}
		return null;
	}
	
	public  static byte[]  expandByteArray(byte[] src, int ex_size)  
	{
		byte[] tmp = new byte[src.length + ex_size];
		System.arraycopy(src, 0, tmp, 0, src.length);
		src = null;
		return tmp;
	} 
}

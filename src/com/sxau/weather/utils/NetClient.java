package com.sxau.weather.utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class NetClient {
	public static final String BASE_WEATHER_URL="http://www.weather.com.cn/data/cityinfo/";
	public static String connServerForResult(String cityNumber) {
		// TODO Auto-generated method stub

		String cityWeather="";
		String urlString=BASE_WEATHER_URL+cityNumber+".html";
		HttpClient httpClient=new DefaultHttpClient();
		HttpGet httpGet=new HttpGet(urlString);
		try {
			HttpResponse httpResponse=httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK) {
				String resultString=EntityUtils.toString(httpResponse.getEntity());
				//Log.d("test", resultString);
				//×ªÂë
				cityWeather=new String(resultString.getBytes("ISO-8859-1"),"UTF-8");
				 
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//Ö´ÐÐgetÇëÇó
		return cityWeather;
	}
	

}

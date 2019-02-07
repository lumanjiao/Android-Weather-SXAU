package com.sxau.weather.db;

import java.util.ArrayList;
import java.util.List;

import com.sxau.weather.bean.City;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CityDB {//查询数据库
	private SQLiteDatabase db;
	
	public CityDB(Context context,String path){
		db=context.openOrCreateDatabase(path, Context.MODE_PRIVATE, null);
	}
	public List<City> getAllCity(){//获取所有城市，返回对象
		List<City> cityList=new ArrayList<City>();
		String sql="select * from city";
		Cursor cursor=db.rawQuery(sql, null);//将查询结果赋给游标
		while(cursor.moveToNext()){
			String name=cursor.getString(cursor.getColumnIndex("name"));
			String number=cursor.getString(cursor.getColumnIndex("number"));
			String pinyin=cursor.getString(cursor.getColumnIndex("pinyin"));
			City city=new City(name, number, pinyin);
			cityList.add(city);			
		}
		if (cursor!=null) {
			cursor.close();
		}
		return cityList;
	}
	public List<City> getCurCity(String curName){//获取城市，返回对象
		List<City> cityList=new ArrayList<City>();
		String sql="select * from city where name like '"+curName+"%' or pinyin like '"+curName+"%'";
		Cursor cursor=db.rawQuery(sql, null);//将查询结果赋给游标
		while(cursor.moveToNext()){
			String name=cursor.getString(cursor.getColumnIndex("name"));
			String number=cursor.getString(cursor.getColumnIndex("number"));
			String pinyin=cursor.getString(cursor.getColumnIndex("pinyin"));
			City city=new City(name, number, pinyin);
			cityList.add(city);
			
		}
		if (cursor!=null) {
			cursor.close();
		}
		return cityList;
	}
	public City getCityWithName(String cityName) {
		// TODO Auto-generated method stub
		City city=null;
		if (cityName.endsWith("市")||cityName.endsWith("区")||cityName.endsWith("县")) {
			cityName=cityName.substring(0,cityName.length()-1);
		}
		String sql="select * from city where name='"+cityName+"'";
		Cursor cursor=db.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			city=new City(cursor.getString(cursor.getColumnIndex("name")), cursor.getString(cursor.getColumnIndex("number")), cursor.getString(cursor.getColumnIndex("pinyin")));
		}
		if (cursor!=null) {
			cursor.close();
		}
		return city;
	}
	

}

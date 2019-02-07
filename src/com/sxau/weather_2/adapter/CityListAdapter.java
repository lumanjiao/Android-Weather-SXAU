package com.sxau.weather_2.adapter;

import java.util.List;

import com.sxau.weather.bean.City;
import com.sxau.weather_2.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CityListAdapter extends BaseAdapter {
	
	private List<City> cityList;
	private Context context;

	public CityListAdapter(List<City> cityList,Context context) {
		super();
		this.cityList = cityList;
		this.context=context;
	}

	@Override
	//获取list总数量
	public int getCount() {
		// TODO Auto-generated method stub
		return cityList.size();
	}
    
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}
    //返回现在当前的位置
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
    //获取下一个要显示的视图
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if(convertView==null){
			convertView=LayoutInflater.from(context).inflate(R.layout.item_cityname, null);
		}
		
		TextView tvCityName=(TextView)convertView.findViewById(R.id.tv_cityname);
		tvCityName.setText(cityList.get(position).getName());
		return convertView;
	}

}

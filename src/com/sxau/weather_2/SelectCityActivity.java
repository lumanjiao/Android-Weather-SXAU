package com.sxau.weather_2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sxau.weather.app.MyApplication;
import com.sxau.weather.bean.City;
import com.sxau.weather.db.CityDB;
import com.sxau.weather_2.adapter.CityListAdapter;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class SelectCityActivity extends Activity implements TextWatcher{
	private ImageView img_return;
	private List<City> cityList;
	private ListView lv_city_list;
	private CityListAdapter cityListAdapter;
	private EditText et_city_name;
	private CityDB cityDB;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_city);
		img_return=(ImageView)findViewById(R.id.img_return);//���ҵ���
		et_city_name=(EditText)findViewById(R.id.et_city_name);
		lv_city_list=(ListView)findViewById(R.id.lv_city_list);
		//���ü���
		img_return.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SelectCityActivity.this.finish();
			}
		});
		et_city_name.addTextChangedListener(this);
		lv_city_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
				Bundle bundle=new Bundle();//������
				City city=cityList.get(position);//�ȴ�citylist�л�ȡcity
				//Ȼ��ѿ����л���city�ŵ�bundle��
				bundle.putSerializable("city", city);
				intent.putExtras(bundle);
				setResult(MainActivity.RESULT_CODE, intent);
				finish();//�ر�activity
			}
		});
		initData();//��������֮��ķ�����Ҫ�ŵ�activity�е���
	}
	//��ʼ������,Ϊ�˴��뿴�����ṹ��������Щд��һ��������
	private void initData() {
		// TODO Auto-generated method stub
		cityList=new ArrayList<City>();
		if (MyApplication.dbExist) {
			cityDB=new CityDB(SelectCityActivity.this, MyApplication.SQL_PATH);
			cityList=cityDB.getAllCity();
		}
		
		cityListAdapter=new CityListAdapter(cityList,SelectCityActivity.this);
		lv_city_list.setAdapter(cityListAdapter);
	}
	
	
	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
		
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}
	@Override//�����ı������ݱ仯
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
		
		String curName=s.toString();
		//����գ�Ȼ�������
		cityList.clear();
		cityList.addAll(cityDB.getCurCity(curName));
		
		cityList=cityDB.getCurCity(curName);
		cityListAdapter.notifyDataSetChanged();
		
		
	}

}

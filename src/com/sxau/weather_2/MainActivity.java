package com.sxau.weather_2;

import android.animation.AnimatorSet.Builder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;
import com.sxau.weather.app.MyApplication;
import com.sxau.weather.bean.City;
import com.sxau.weather.bean.CityWeather;
import com.sxau.weather.bean.WeatherInfo;
import com.sxau.weather.db.CityDB;
import com.sxau.weather.test.TestService;
import com.sxau.weather.utils.NetClient;
import com.sxau.weather.utils.NetUtil;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends Activity implements OnClickListener,
		AMapLocationListener {
	private ImageView imgSelectCity, img_update, img_location;
	private TextView tv_temp, tv_city_name, tv_ptime, tv_weather;
	private City curCity;
	private SharedPreferences spWeather;
	public static final int REQUEST_CODE = 100;
	public static final int RESULT_CODE = 1;
	// ����AMapLocationClient�����
	public AMapLocationClient mLocationClient = null;
	// ����mLocationOption����
	public AMapLocationClientOption mLocationOption = null;
	private NotificationManager nm;
	private Notification notification;
	private long firstTime = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				CityWeather cityWeather = (CityWeather) msg.obj;
				updateView(cityWeather);
				saveWeather(cityWeather);
				img_update.clearAnimation();
				break;

			default:
				break;
			}
		};
	};

	// ��һ���������ڡ�
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		spWeather = MainActivity.this.getSharedPreferences("weather",
				MODE_PRIVATE);
		imgSelectCity = (ImageView) findViewById(R.id.img_select_city);
		tv_temp = (TextView) findViewById(R.id.tv_temp);
		tv_city_name = (TextView) findViewById(R.id.tv_city_name);
		tv_weather = (TextView) findViewById(R.id.tv_weather);
		tv_ptime = (TextView) findViewById(R.id.tv_ptime);
		img_location = (ImageView) findViewById(R.id.img_location);
		img_update = (ImageView) findViewById(R.id.img_update);// 1.�ҿؼ�
		img_update.setOnClickListener(this);// 2.���ü���
		imgSelectCity.setOnClickListener(this);
		img_location.setOnClickListener(this);

		// ��ʼ����λ
		mLocationClient = new AMapLocationClient(getApplicationContext());
		// ���ö�λ�ص�����
		mLocationClient.setLocationListener(this);

		// 100��Ϊrequestcode��ֵ��������ǻص�����������һ��activity
		// sp,sharedpereference
		CityWeather cityWeather = getWeatherFromSP();
		updateView(cityWeather);
		if (NetUtil.getNetworkState(MainActivity.this) != NetUtil.NETWORN_NONE) {
			configAndStartLocation();
		}
		TestBroadcastReceiver testBroadcastReceiver = new TestBroadcastReceiver();
		IntentFilter filter = new IntentFilter("123");
		registerReceiver(testBroadcastReceiver, filter);
		// UmengUpdateAgent.update(this);
	}

	// resultCodeҲ��һ�ֱ�ǡ���SelectCityActivity�е�setResult���ش����ݡ�Ϊ��֪������һ��activity���ص�
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
			// String cityName=data.getStringExtra("city");
			curCity = (City) data.getSerializableExtra("city");// ���л����ݣ�������activity֮�䴫��
			// Toast.makeText(MainActivity.this,
			// city.getName()+city.getNumber()+city.getPinyin(),
			// Toast.LENGTH_SHORT).show();
			getWeather(curCity);
		}
	}

	private void getWeather(final City city) {
		// TODO Auto-generated method stub
		if (NetUtil.getNetworkState(MainActivity.this) == NetUtil.NETWORN_NONE) {
			Toast.makeText(MainActivity.this, "�����ֻ�����", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		Thread getweatherThread = new Thread() {// ��һ�����̣߳���ȡ����
			public void run() {
				// ���ս���������־
				String cityWeather = NetClient.connServerForResult(city
						.getNumber());// httpЭ������ʽ��get��post
				if (!TextUtils.isEmpty(cityWeather)) {
					// try {
					// ѡ�У�ctrl+/,ע��������
					// JSONObject jsonObject=new JSONObject(cityWeather);
					// JSONObject
					// jsonObject2=jsonObject.getJSONObject("weatherinfo");
					// String temp1=jsonObject2.getString("temp1");//��ȡ����¶�
					// String temp2=jsonObject2.getString("temp2");
					//
					// String city=jsonObject2.getString("city");
					Gson gson = new Gson();
					WeatherInfo weatherInfo = gson.fromJson(cityWeather,
							WeatherInfo.class);
					// ���е�UI�������������̡߳����������������߳�
					Message message = mHandler.obtainMessage();
					message.what = 1;// ���
					message.obj = weatherInfo.getWeatherinfo();
					mHandler.sendMessage(message);// �����չ����handler������Ϣ

					// } catch (JSONException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
				}

			};
		};
		getweatherThread.start();
	}

	// ���ݻ���1
	private void saveWeather(CityWeather cityWeather) {
		// TODO Auto-generated method stub
		Editor editor = spWeather.edit();

		editor.putString("cityname", cityWeather.getCity());// ͨ����ֵ�Եķ�ʽ�洢
		editor.putString("temp1", cityWeather.getTemp1());
		editor.putString("temp2", cityWeather.getTemp2());
		editor.putString("ptime", cityWeather.getPtime());
		editor.commit();
	}

	// ���л�ȡ����.���ݻ���2
	private CityWeather getWeatherFromSP() {
		// TODO Auto-generated method stub
		String cityName = spWeather.getString("cityname", "̫��");
		String temp1 = spWeather.getString("temp1", "0��");
		String temp2 = spWeather.getString("temp2", "10��");
		String ptime = spWeather.getString("ptime", "16:00����");
		String weather = spWeather.getString("weather", "��");
		CityWeather cityWeather = new CityWeather(cityName, null, temp1, temp2,
				null, null, null, ptime);
		return cityWeather;
	}

	// ���ݴ�������cityweatherֵˢ�¶��󣬸�����ͼ
	private void updateView(CityWeather cityWeather) {
		// TODO Auto-generated method stub
		tv_temp.setText(cityWeather.getTemp1() + "~" + cityWeather.getTemp2());
		tv_city_name.setText(cityWeather.getCity());
		tv_ptime.setText("����"+cityWeather.getPtime()+"����");
		tv_weather.setText(cityWeather.getWeather());
		if (cityWeather.getTemp1().startsWith("-")
				|| cityWeather.getTemp2().startsWith("-")) {
			showNotification();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.img_select_city:
			Intent intentSelectCity = new Intent(MainActivity.this,
					SelectCityActivity.class);
			intentSelectCity.setClass(MainActivity.this,
					SelectCityActivity.class);
			startActivityForResult(intentSelectCity, REQUEST_CODE);

			break;
		case R.id.img_update:
			if (curCity != null) {
				// �������
				Animation animation = AnimationUtils.loadAnimation(
						MainActivity.this, R.anim.img_rotate);
				img_update.startAnimation(animation);
				getWeather(curCity);
			}
			Intent intent = new Intent(MainActivity.this, TestService.class);
			startService(intent);
			// ServiceConnection serviceConnection=null;
			// bindService(intent, serviceConnection, BIND_AUTO_CREATE);
			break;
		case R.id.img_location:
			configAndStartLocation();
			break;

		default:
			break;
		}
	}

	private void configAndStartLocation() {
		// TODO Auto-generated method stub
		// ��ʼ����λ����
		mLocationOption = new AMapLocationClientOption();
		// ���ö�λģʽΪ�߾���ģʽ��Battery_SavingΪ�͹���ģʽ��Device_Sensors�ǽ��豸ģʽ
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// �����Ƿ񷵻ص�ַ��Ϣ��Ĭ�Ϸ��ص�ַ��Ϣ��
		mLocationOption.setNeedAddress(true);
		// �����Ƿ�ֻ��λһ��,Ĭ��Ϊfalse
		mLocationOption.setOnceLocation(true);
		// �����Ƿ�ǿ��ˢ��WIFI��Ĭ��Ϊǿ��ˢ��
		mLocationOption.setWifiActiveScan(true);
		// �����Ƿ�����ģ��λ��,Ĭ��Ϊfalse��������ģ��λ��
		mLocationOption.setMockEnable(false);
		// ���ö�λ���,��λ����,Ĭ��Ϊ2000ms
		mLocationOption.setInterval(2000);
		// ����λ�ͻ��˶������ö�λ����
		mLocationClient.setLocationOption(mLocationOption);
		// ������λ
		mLocationClient.startLocation();
	}

	@Override
	// ��λ����ص�����
	public void onLocationChanged(AMapLocation amapLocation) {
		// TODO Auto-generated method stub
		if (amapLocation != null) {
			if (amapLocation.getErrorCode() == 0) {
				CityDB cityDB = new CityDB(MainActivity.this,
						MyApplication.SQL_PATH);
				final City city = cityDB
						.getCityWithName(amapLocation.getCity());
				if (city != null) {
					if (city.getName().equals(
							spWeather.getString("cityname", ""))) {
						curCity = city;
						getWeather(city);
					} else {
						// �����
						AlertDialog.Builder builder = new AlertDialog.Builder(
								MainActivity.this,
								AlertDialog.THEME_DEVICE_DEFAULT_DARK);
						builder.setTitle("��ʾ");
						builder.setMessage("���з����仯���Ƿ��л���");
						builder.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										curCity = city;
										getWeather(city);
									}
								});
						builder.setNegativeButton("ȡ��", null);// ���ȡ��֮��dialog��ʧ
						builder.create().show();
					}

				} else {
					Toast.makeText(MainActivity.this, "���޸ó���������Ϣ",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				// ��ʾ������ϢErrCode�Ǵ����룬errInfo�Ǵ�����Ϣ������������
				Log.e("AmapError",
						"location Error, ErrCode:"
								+ amapLocation.getErrorCode() + ", errInfo:"
								+ amapLocation.getErrorInfo());
			}
		}
	}

	@Override
	// ������������˳�
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (System.currentTimeMillis() - firstTime > 2000) {
			firstTime = System.currentTimeMillis();
			Toast.makeText(MainActivity.this, "�ٵ��һ�η��ؼ��˳�", Toast.LENGTH_SHORT)
					.show();
		} else {
			MainActivity.this.finish();
		}
	}

	// ��ʾ֪ͨ
	private void showNotification() {
		// TODO Auto-generated method stub
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				MainActivity.this);
		builder.setTicker("������ʾ");
		builder.setContentTitle("�������ʾ");
		builder.setContentText("����Ԥ����ע�������ů");
		builder.setSmallIcon(R.drawable.ic_launcher);
		Intent intent = new Intent(MainActivity.this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent
				.getActivity(MainActivity.this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		notification = builder.build();
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(0, notification);// ��ʾ֪ͨ
	}

	// �㲥���ն�,Ȼ��ȥAndroidmainfest�ļ���ȥע��
	public static class TestBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("123")) {
				Log.d("test", intent.getStringExtra("key"));
			}
		}

	}

}

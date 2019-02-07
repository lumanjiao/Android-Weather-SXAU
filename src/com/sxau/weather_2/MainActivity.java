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
	// 声明AMapLocationClient类对象
	public AMapLocationClient mLocationClient = null;
	// 声明mLocationOption对象
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

	// 第一个生命周期。
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
		img_update = (ImageView) findViewById(R.id.img_update);// 1.找控件
		img_update.setOnClickListener(this);// 2.设置监听
		imgSelectCity.setOnClickListener(this);
		img_location.setOnClickListener(this);

		// 初始化定位
		mLocationClient = new AMapLocationClient(getApplicationContext());
		// 设置定位回调监听
		mLocationClient.setLocationListener(this);

		// 100即为requestcode的值，用来标记回调回来的是哪一个activity
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

	// resultCode也是一种标记。是SelectCityActivity中的setResult来回传数据。为了知道是哪一个activity返回的
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
			// String cityName=data.getStringExtra("city");
			curCity = (City) data.getSerializableExtra("city");// 序列化数据，便于在activity之间传递
			// Toast.makeText(MainActivity.this,
			// city.getName()+city.getNumber()+city.getPinyin(),
			// Toast.LENGTH_SHORT).show();
			getWeather(curCity);
		}
	}

	private void getWeather(final City city) {
		// TODO Auto-generated method stub
		if (NetUtil.getNetworkState(MainActivity.this) == NetUtil.NETWORN_NONE) {
			Toast.makeText(MainActivity.this, "请检查手机网络", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		Thread getweatherThread = new Thread() {// 开一个子线程，获取数据
			public void run() {
				// 接收结果并输出日志
				String cityWeather = NetClient.connServerForResult(city
						.getNumber());// http协议请求方式。get与post
				if (!TextUtils.isEmpty(cityWeather)) {
					// try {
					// 选中，ctrl+/,注释若干行
					// JSONObject jsonObject=new JSONObject(cityWeather);
					// JSONObject
					// jsonObject2=jsonObject.getJSONObject("weatherinfo");
					// String temp1=jsonObject2.getString("temp1");//获取最低温度
					// String temp2=jsonObject2.getString("temp2");
					//
					// String city=jsonObject2.getString("city");
					Gson gson = new Gson();
					WeatherInfo weatherInfo = gson.fromJson(cityWeather,
							WeatherInfo.class);
					// 所有的UI操作都是在主线程。而联网操作在子线程
					Message message = mHandler.obtainMessage();
					message.what = 1;// 标记
					message.obj = weatherInfo.getWeatherinfo();
					mHandler.sendMessage(message);// 借助刚构造的handler发送消息

					// } catch (JSONException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
				}

			};
		};
		getweatherThread.start();
	}

	// 数据缓存1
	private void saveWeather(CityWeather cityWeather) {
		// TODO Auto-generated method stub
		Editor editor = spWeather.edit();

		editor.putString("cityname", cityWeather.getCity());// 通过键值对的方式存储
		editor.putString("temp1", cityWeather.getTemp1());
		editor.putString("temp2", cityWeather.getTemp2());
		editor.putString("ptime", cityWeather.getPtime());
		editor.commit();
	}

	// 从中获取数据.数据缓存2
	private CityWeather getWeatherFromSP() {
		// TODO Auto-generated method stub
		String cityName = spWeather.getString("cityname", "太谷");
		String temp1 = spWeather.getString("temp1", "0℃");
		String temp2 = spWeather.getString("temp2", "10℃");
		String ptime = spWeather.getString("ptime", "16:00发布");
		String weather = spWeather.getString("weather", "晴");
		CityWeather cityWeather = new CityWeather(cityName, null, temp1, temp2,
				null, null, null, ptime);
		return cityWeather;
	}

	// 根据传进来的cityweather值刷新对象，更新视图
	private void updateView(CityWeather cityWeather) {
		// TODO Auto-generated method stub
		tv_temp.setText(cityWeather.getTemp1() + "~" + cityWeather.getTemp2());
		tv_city_name.setText(cityWeather.getCity());
		tv_ptime.setText("今天"+cityWeather.getPtime()+"发布");
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
				// 动画添加
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
		// 初始化定位参数
		mLocationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// 设置是否返回地址信息（默认返回地址信息）
		mLocationOption.setNeedAddress(true);
		// 设置是否只定位一次,默认为false
		mLocationOption.setOnceLocation(true);
		// 设置是否强制刷新WIFI，默认为强制刷新
		mLocationOption.setWifiActiveScan(true);
		// 设置是否允许模拟位置,默认为false，不允许模拟位置
		mLocationOption.setMockEnable(false);
		// 设置定位间隔,单位毫秒,默认为2000ms
		mLocationOption.setInterval(2000);
		// 给定位客户端对象设置定位参数
		mLocationClient.setLocationOption(mLocationOption);
		// 启动定位
		mLocationClient.startLocation();
	}

	@Override
	// 定位结果回调函数
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
						// 警告框
						AlertDialog.Builder builder = new AlertDialog.Builder(
								MainActivity.this,
								AlertDialog.THEME_DEVICE_DEFAULT_DARK);
						builder.setTitle("提示");
						builder.setMessage("城市发生变化，是否切换？");
						builder.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										curCity = city;
										getWeather(city);
									}
								});
						builder.setNegativeButton("取消", null);// 点击取消之后，dialog消失
						builder.create().show();
					}

				} else {
					Toast.makeText(MainActivity.this, "暂无该城市天气信息",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				// 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
				Log.e("AmapError",
						"location Error, ErrCode:"
								+ amapLocation.getErrorCode() + ", errInfo:"
								+ amapLocation.getErrorInfo());
			}
		}
	}

	@Override
	// 连续点击。。退出
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (System.currentTimeMillis() - firstTime > 2000) {
			firstTime = System.currentTimeMillis();
			Toast.makeText(MainActivity.this, "再点击一次返回键退出", Toast.LENGTH_SHORT)
					.show();
		} else {
			MainActivity.this.finish();
		}
	}

	// 显示通知
	private void showNotification() {
		// TODO Auto-generated method stub
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				MainActivity.this);
		builder.setTicker("低温提示");
		builder.setContentTitle("晴雨表提示");
		builder.setContentText("低温预警，注意防寒保暖");
		builder.setSmallIcon(R.drawable.ic_launcher);
		Intent intent = new Intent(MainActivity.this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent
				.getActivity(MainActivity.this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		notification = builder.build();
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(0, notification);// 显示通知
	}

	// 广播接收端,然后去Androidmainfest文件中去注册
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

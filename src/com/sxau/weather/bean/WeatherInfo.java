package com.sxau.weather.bean;

public class WeatherInfo {

	private CityWeather weatherinfo;

	public WeatherInfo(CityWeather weatherinfo) {
		super();
		this.weatherinfo = weatherinfo;
	}

	public CityWeather getWeatherinfo() {
		return weatherinfo;
	}

	public void setWeatherinfo(CityWeather weatherinfo) {
		this.weatherinfo = weatherinfo;
	}
	
}

package com.sxau.weather.bean;

import java.io.Serializable;

public class City implements Serializable{//设置city类可序列化
	private String name;
	private String number;
	private String pinyin;
	
	
	public City(String name, String number, String pinyin) {
		super();
		this.name = name;
		this.number = number;
		this.pinyin = pinyin;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	

}

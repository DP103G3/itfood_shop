package tw.dp103g3.itfood_shop.shop;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.task.CommonTask;

public class Dish implements Serializable {
	private static final String TAG = "TAG_Dish";
	private int id;
	private String name;
	private String info;
	private byte state;
	private int shop_id;
	private int price;
	
	public Dish(int id, String name, String info, byte state, int shop_id, int price) {
		super();
		this.id = id;
		this.name = name;
		this.info = info;
		this.state = state;
		this.shop_id = shop_id;
		this.price = price;
	}
	
	public Dish(int id, String name, String info, int shop_id, int price) {
		super();
		this.id = id;
		this.name = name;
		this.info = info;
		this.shop_id = shop_id;
		this.price = price;
	}

	public void setFields(String name, String info,  int price) {
		this.name = name;
		this.info = info;
		this.price = price;
	}

	public void Account(int id, byte state) {
		this.id = id;
		this.state = state;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public byte getState() {
		return state;
	}

	public void setState(byte state) {
		this.state = state;
	}

	public int getShop_id() {
		return shop_id;
	}

	public void setShop_id(int shop_id) {
		this.shop_id = shop_id;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}
}

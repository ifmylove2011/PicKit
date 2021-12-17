package com.xter.pickit.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by XTER on 2018/4/21.
 * 配置存储管理--SharedPreferenceManager
 * 建议context使用ApplicationContext
 */
public class SPM {

	private static final String SPLITOR = ",";

	public static void saveBoolean(Context context, String name, String key, boolean value) {
		try {
			SharedPreferences sp = context.getSharedPreferences(name, 0);
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean(key, value);
			editor.apply();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void saveInt(Context context, String name, String key, int value) {
		try {
			SharedPreferences sp = context.getSharedPreferences(name, 0);
			SharedPreferences.Editor editor = sp.edit();
			editor.putInt(key, value);
			editor.apply();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void saveStr(Context context, String name, String key, String value) {
		try {
			SharedPreferences sp = context.getSharedPreferences(name, 0);
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(key, value);
			editor.apply();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean getBoolean(Context context, String name, String key, boolean defaultValue) {
		SharedPreferences sp = context.getSharedPreferences(name, 0);
		return sp != null && sp.getBoolean(key, defaultValue);
	}

	public static int getInt(Context context, String name, String key, int defaultValue) {
		SharedPreferences sp = context.getSharedPreferences(name, 0);
		if (sp != null) {
			return sp.getInt(key, defaultValue);
		}
		return -1;
	}

	public static String getStr(Context context, String name, String key, String defaultValue) {
		SharedPreferences sp = context.getSharedPreferences(name, 0);
		if (sp != null) {
			return sp.getString(key, defaultValue);
		}
		return "";
	}

	/**
	 * 清除配置表
	 *
	 * @param name 配置文件名
	 */
	public static void removeSP(Context context, String name) {
		SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		sp.edit().clear().apply();
	}

	public static void saveStringArray(Context context, String name, String key, String value) {
		if (!TextUtils.isEmpty(value)) {
			List<String> localArray = getStringArray(context, name, key);
			List<String> newArray = getStringArray(value);
			if (newArray != null) {
				if (localArray == null) {
					localArray = new ArrayList<>();
				}
				for (String n : newArray) {
					if (!localArray.contains(n)) {
						localArray.add(n);
					}
				}
			}
			StringBuilder sb = new StringBuilder();
			for (String l : localArray) {
				sb.append(l).append(SPLITOR);
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			saveStr(context, name, key, sb.toString());
		}
	}

	public static void deleteStringInArray(Context context, String name, String key, String value) {
		List<String> localArray = getStringArray(context, name, key);
		List<String> keyArray = getStringArray(value);
		if (localArray != null && keyArray != null) {
			for (String k : keyArray) {
				if (localArray.contains(k)) {
					localArray.remove(k);
				}
			}
			if (localArray.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (String l : localArray) {
					sb.append(l).append(SPLITOR);
				}
				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}
				saveStr(context, name, key, sb.toString());
			}
		}
	}


	public static List<String> getStringArray(Context context, String name, String key) {
		String value = getStr(context, name, key, "");
		return getStringArray(value);
	}

	public static List<String> getStringArray(String value) {
		if (!TextUtils.isEmpty(value)) {
			List<String> temp = new ArrayList<>();
			if (value.contains(SPLITOR)) {
				String[] array = value.split(SPLITOR);
				temp.addAll(Arrays.asList(array));
			} else {
				temp.add(value);
			}
			return temp;
		}
		return null;
	}

}

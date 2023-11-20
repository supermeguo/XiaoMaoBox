package com.github.cattv.osc.util;


import android.content.Context;
import android.content.SharedPreferences;

import com.github.cattv.osc.base.App;

public class SharedPreferencesUtils {
public static final String CONFIG_MODEL="CONFIG_MODEL";
    /**
     * 保存在手机里面的文件名
     */
    private static final String FILE_NAME = "sp";

    /**
     * 保存数据的方法&#xff0c;我们需要拿到保存数据的具体类型&#xff0c;然后根据类型调用不同的保存方法
     *
     * @paramcontext
     * @paramkey
     * @paramobject
     */
    public static void setParam( String key, Object object) {
        App context = App.getInstance();
        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if ("String".equals(type)) {
            editor.putString(key, ((String) object));
        } else if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) object);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) object);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) object);
        }

        editor.commit();
    }


    /**
     * 得到保存数据的方法&#xff0c;我们根据默认值得到保存的数据的具体类型&#xff0c;然后调用相对于的方法获取值
     *
     * @return
     * @paramcontext
     * @paramkey
     * @paramdefaultObject
     */
    public static Object getParam(String key, Object defaultObject) {
        App context = App.getInstance();
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        if ("String".equals(type)) {
            return sp.getString(key, (String) defaultObject);
        } else if ("Integer".equals(type)) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if ("Boolean".equals(type)) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if ("Float".equals(type)) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if ("Long".equals(type)) {
            return sp.getLong(key, (Long) defaultObject);
        }

        return null;
    }

    /**
     * 清除所有数据
     *
     * @paramcontext
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear().commit();
    }

    /**
     * 清除指定数据
     *
     * @paramcontext
     */
    public static void clearAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("定义的键名");
        editor.commit();
    }

}
			
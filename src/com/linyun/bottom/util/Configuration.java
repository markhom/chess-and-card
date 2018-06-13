package com.linyun.bottom.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Kinglf on 2016/10/17.
 */
public class Configuration extends Properties 
{
    private static final long serialVersionUID = -2296275030489943706L;
    private static Configuration instance = new Configuration();
    private static final String SERVER_PATH = System.getProperty("user.dir"); 
    public static synchronized Configuration getInstance()
    {
        return instance;
    }

    public String getProperty(String key, String defaultValue)
    {
        String val = getProperty(key);
        return (val == null || val.isEmpty()) ? defaultValue : val;
    }

    public String getString(String name, String defaultValue) 
    {
        return this.getProperty(name, defaultValue);
    }

    public int getInt(String name, int defaultValue)
    {
        String val = this.getProperty(name);
        return (val == null || val.isEmpty()) ? defaultValue : Integer.parseInt(val);
    }

    public long getLong(String name, long defaultValue) 
    {
        String val = this.getProperty(name);
        return (val == null || val.isEmpty()) ? defaultValue : Integer.parseInt(val);
    }

    public float getFloat(String name, float defaultValue)
    {
        String val = this.getProperty(name);
        return (val == null || val.isEmpty()) ? defaultValue : Float.parseFloat(val);
    }

    public double getDouble(String name, double defaultValue)
    {
        String val = this.getProperty(name);
        return (val == null || val.isEmpty()) ? defaultValue : Double.parseDouble(val);
    }

    public byte getByte(String name, byte defaultValue)
    {
        String val = this.getProperty(name);
        return (val == null || val.isEmpty()) ? defaultValue : Byte.parseByte(val);
    }

    public Configuration()
    {
    	Properties pro = null;
        try 
        {
        	InputStream fis = new FileInputStream(SERVER_PATH + File.separator + "config/redis.properties");
    		pro = new Properties();
    		pro.load(fis);
        }
        catch (IOException ioe) 
        {

        }
    }
}
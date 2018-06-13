/**
 * Juice
 * com.juice.orange.game.container
 * Container.java
 */
package com.linyun.bottom.container;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;

import com.linyun.bottom.rmi.JuiceInvocationHandler;
import com.linyun.bottom.rmi.RemoteConfig;
import com.linyun.bottom.util.ClassUtils;

/**
 * @author shaojieque
 * 2013-3-22
 */
public class Container {
	private static ConcurrentHashMap<String, Channel> sessionChannelMap = new ConcurrentHashMap<String, Channel>();
	private static ConcurrentHashMap<String, String> channelSessionMap = new ConcurrentHashMap<String, String>();
	private static ConcurrentHashMap<String, GameSession> sessionMap = new ConcurrentHashMap<String, GameSession>();
	private static Map<String, Object> serverMap = new HashMap<String, Object>();
	private static Map<String, String> pathMap = new HashMap<String, String>();
	private static List<NotificationListener> listeners = new ArrayList<NotificationListener>();
	private static ConcurrentHashMap<String, RemoteConfig> remoteConfigMap = new ConcurrentHashMap<String, RemoteConfig>();
	
	public static void addSessionChannel(String sessionId, Channel channel)
	{
		sessionChannelMap.put(sessionId, channel);
	}
	public static void removeSessionChannel(String sessionId) 
	{
		if (sessionChannelMap.containsKey(sessionId))
		{
			sessionChannelMap.remove(sessionId);
		}
	}
	public static Channel getSessionChannel(String sessionId) 
	{
		return sessionChannelMap.get(sessionId);
	}
	
	public static void addChannelSession(Channel channel, String sessionId) {
		channelSessionMap.put(getChannelInfo(channel), sessionId);
	}
	
	public static String getChannelSession(Channel channel) {
		return channelSessionMap.get(getChannelInfo(channel));
	}
	
	private static String getChannelInfo(Channel channel) {
		StringBuilder desc = new StringBuilder();
		desc.append(channel.getId()).append(channel.getRemoteAddress().toString());
		return desc.toString();
	}
	
	public static void removeChannelSession(Channel channel) {
		channelSessionMap.remove(getChannelInfo(channel));
	}
	
	public static void addSession(GameSession session) {
		sessionMap.put(session.getSessionId(), session);
	}
	
	public static void removeSession(String sessionId) {
		sessionMap.remove(sessionId);
	}
	
	public static GameSession getSessionById(String sessionId) {
		return sessionMap.get(sessionId);
	}
	
	public static void registerServer(String name, Object server) {
		serverMap.put(name, server);
	}
	
	public static Object getServer(String name) {
		return serverMap.get(name);
	}
	
	public static void registerServerPath(String name, String path) {
		pathMap.put(name, path);
	}
	
	public static String getServerPath(String name) {
		return pathMap.get(name);
	}
	
	public static List<GameSession> getSessions(){
		List<GameSession> list = new ArrayList<GameSession>();
		for (Entry<String, GameSession>entry: sessionMap.entrySet()) {
			list.add(entry.getValue());
		}
		return list;
	}
	
	public static void addNotificationListener(NotificationListener listener) {
		listeners.add(listener);
	}
	
	public static void notifyListener(Object obj) {
		if (listeners == null || listeners.size() == 0) return;
		for(NotificationListener listener : listeners) {
			listener.handler(obj);
		}
	}
	
	
	public static void addRemoteConfig(RemoteConfig config) {
		remoteConfigMap.put(config.getName(), config);
	}
	
	public static Map<String, RemoteConfig> getRemoteConfigs() {
		return remoteConfigMap;
	}
	
	public static Object createRemoteService(Class<?> clazz, String prefix) {
		if (clazz == null)
		      throw new NullPointerException("clazz must not be null for RemoteProxy.create()");
	   /* JuiceInvocationHandler handler = new JuiceInvocationHandler(JuiceRemoteManager.getChannel(prefix), 
	    		clazz.getName());*/
		JuiceInvocationHandler handler = new JuiceInvocationHandler(prefix, 
	    		clazz.getName());
	    ClassLoader loader = Thread.currentThread().getContextClassLoader();
	    return Proxy.newProxyInstance(loader, new Class<?>[]{clazz}, handler);
	}
	
	public static Object createLocalService(Class<?> clazz) {
		if (clazz == null)
		      throw new NullPointerException("clazz must not be null for RemoteProxy.create()");
		String clazzName = clazz.getName();
		Object obj = ClassUtils.getObject(clazzName);
	    return obj;
	}
}

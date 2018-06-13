/**
 * Juice
 * com.juice.orange.game.rmi
 * JuiceRemoteManager.java
 */
package com.linyun.bottom.rmi;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shaojieque
 * 2013-4-17
 */
public final class JuiceRemoteManager {
	private static Map<String, Transport> transportMap = new HashMap<String, Transport>();
	
	public static void addTransport(Transport t) {
		transportMap.put(t.getId(), t);
	}
	
	public static Transport removeTransport(String id) {
		return transportMap.remove(id);
	}
	
	public static Transport getTransport(String id) {
		return transportMap.get(id);
	}
	
	public static boolean isRetrun(String id) {
		return transportMap.get(id).getResult()==null?false:true;
	}
	
	//
	public static void setupRemoteServer() {
		/*for(Entry<String, RemoteConfig> entry : Container.getConfigs().entrySet()) {
			RemoteConfig config = entry.getValue();
			RemoteServer server = new RemoteServer();
			server.setupRemoteServer(config);
		}*/
	}
}

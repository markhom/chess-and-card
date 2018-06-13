/**
 * Juice
 * com.juice.orange.game.rmi
 * RemoteConfig.java
 */
package com.linyun.bottom.rmi;

/**
 * @author shaojieque 
 * 2013-4-18
 */
public class RemoteConfig {
	private String name;
	private String address;
	private int port;

	public RemoteConfig(){
		
	}
	
	public RemoteConfig(String name, String address, int port) {
		this.name = name;
		this.address = address;
		this.port = port;
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}

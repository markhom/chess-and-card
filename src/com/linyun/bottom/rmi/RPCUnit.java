/**
 * 
 */
package com.linyun.bottom.rmi;

/**
 * 服务器之间通信单元
 * 
 * @author queshaojie
 * 
 *         lewan
 */
public class RPCUnit {
	private Object unit;
	private RemoteConfig remoteConfig;

	public RPCUnit(Object unit, RemoteConfig remoteConfig) {
		this.unit = unit;
		this.remoteConfig = remoteConfig;
	}

	public Object getUnit() {
		return unit;
	}

	public RemoteConfig getRemoteConfig() {
		return remoteConfig;
	}
}

package com.linyun.data.server;

import java.util.Map;
import java.util.Map.Entry;

import com.linyun.bottom.container.Container;
import com.linyun.bottom.rmi.RPCUnit;
import com.linyun.bottom.rmi.RemoteConfig;
import com.linyun.bottom.rmi.thread.RPCPoolManager;

/*import com.juice.orange.game.container.Container;
import com.juice.orange.game.rmi.RPCUnit;
import com.juice.orange.game.rmi.RemoteConfig;
import com.juice.orange.game.rmi.thread.RPCPoolManager;*/
//import com.luck.game.rps.common.utils.UUIDUtils;

public class ActionAware 
{
	public static final String REMOTE_PREFIX_GAMER = "RPSLobbyServer";
	public static final String REMOTE_PREFIX_GAMESER = "RPSGameServer";

	//public static CommonAction commonAction = (CommonAction) Container.createRemoteService(CommonAction.class, REMOTE_PREFIX_Common);

	/*public String generateId(String userId)
	{
		String id = UUIDUtils.generateID();
		return id;
	}*/

	public void JuiceApiCmdNotify(String userId, String cmddata)
	{
		String msg = cmddata + ":" + userId;
		Map<String, RemoteConfig> remoteMap = Container.getRemoteConfigs();
		for (Entry<String, RemoteConfig> entry : remoteMap.entrySet())
		{
			String serverName = entry.getKey();
			RemoteConfig config = entry.getValue();
			if (serverName.startsWith(REMOTE_PREFIX_GAMER) || serverName.startsWith(REMOTE_PREFIX_GAMESER))
			{
				RPCUnit unit = new RPCUnit(msg, config);
				RPCPoolManager.getInstance().addRequest(unit);
			}
		}
	}
}

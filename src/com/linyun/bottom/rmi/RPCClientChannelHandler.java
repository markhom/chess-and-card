/**
 * Juice
 * com.juice.orange.game.rmi
 * RPCClientChannelHandler.java
 */
package com.linyun.bottom.rmi;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.linyun.bottom.exception.JuiceException;


/**
 * @author shaojieque 
 * 2013-4-16
 */
public class RPCClientChannelHandler extends SimpleChannelUpstreamHandler {
	public final static int DEFAULT_RPC_CLIENT_THREADS = 2;
	//
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (e.getMessage() instanceof Result) {
			Result result = (Result) e.getMessage();
			Transport t = JuiceRemoteManager.getTransport(result.getId());
			if (t!=null) {
				t.setResult(result);
				t.getLatch().countDown();
			} else {
				throw new JuiceException("Can not find RPC Client receive data by resultId:" + result.getId());
			}
		}
	}
}

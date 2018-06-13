/**
 * Juice
 * com.juice.orange.game.handler
 * StaleConnectionTrackingHandler.java
 */
package com.linyun.bottom.handler;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.linyun.bottom.channel.ChannelTimeTrack;
import com.linyun.bottom.container.Container;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.log.LoggerFactory;


/**
 * Keeps track of all connections and automatically closes the ones that are stale.
 * @author shaojieque 
 * 2013-3-21
 */
public class StaleConnectionTrackingHandler extends SimpleChannelHandler 
{
	private static Logger logger = LoggerFactory.getLogger(StaleConnectionTrackingHandler.class);
	//
	private static ConcurrentHashMap<Integer, ChannelTimeTrack> stamps = new ConcurrentHashMap<Integer, ChannelTimeTrack>();
	private final long timeout;

	public StaleConnectionTrackingHandler(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		stamp(e.getChannel());
		super.channelOpen(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		stamp(e.getChannel());
		super.messageReceived(ctx, e);
	}

	private void stamp(Channel channel) {
		try {
			int channelId = channel.getId();
			ChannelTimeTrack channelTrack = stamps.get(channelId);
			if (channelTrack == null) {
				channelTrack = new ChannelTimeTrack(channel, System.currentTimeMillis());
			} else {
				channelTrack.setTimestamp(System.currentTimeMillis());
			}
			stamps.put(channelTrack.getId(), channelTrack);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			closeStaleConnections();
		}
	}

	public void closeStaleConnections() 
	{
		try 
		{
			for(Entry<Integer, ChannelTimeTrack>entry:stamps.entrySet()){
				ChannelTimeTrack channelTrack = entry.getValue();
				if (!channelTrack.getChannel().isConnected() || isStale(channelTrack.getTimestamp())) 
				{
					GameSession session = null;
					String sessionId = Container.getChannelSession(channelTrack.getChannel());
					if (sessionId != null) {
						session = Container.getSessionById(sessionId);
						if (session != null) {
							session.setStatus(GameSession.STATUS_UNCONN);
							Container.notifyListener(session);
							Container.removeSession(session.getSessionId());
						}
						Container.removeSessionChannel(sessionId);
					}
					Container.removeChannelSession(channelTrack.getChannel());
					channelTrack.getChannel().close();
				}
			}
		}
		catch(Exception e) 
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Stops tracking this channel for staleness. This happens for WebSockets
	 * and EventSource connections.
	 * 
	 * @param channel
	 */
	public static void stopTracking(Channel channel) {
		stamps.remove(channel.getId());
	}

	private boolean isStale(Long timeStamp) {
		return System.currentTimeMillis() - timeStamp > timeout;
	}
}

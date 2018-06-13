/**
 * Juice
 * com.juice.orange.game.channel
 * ChannelTimeTrack.java
 */
package com.linyun.bottom.channel;

import org.jboss.netty.channel.Channel;

/**
 * @author shaojieque 
 * 2013-6-19
 */
public class ChannelTimeTrack {
	public int id;
	public Channel channel;
	public long timestamp;

	public ChannelTimeTrack(Channel channel, long timestamp) {
		this.id = channel.getId();
		this.channel = channel;
		this.timestamp = timestamp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}

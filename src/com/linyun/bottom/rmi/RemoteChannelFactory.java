/**
 * Juice
 * com.juice.orange.game.rmi
 * RemoteChannelFactory.java
 */
package com.linyun.bottom.rmi;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 * @author shaojieque 
 * 2013-8-6
 */
public class RemoteChannelFactory {
	public final static int DEFAULT_CHANNEL_NUM = 3;
	private static final String SEPSIGN = ":";
	private static ClientBootstrap clientBootstrap;
	private static Random random = new Random();
	
	private static ConcurrentHashMap<String, FutureTask<List<Channel>>> channels = 
			new ConcurrentHashMap<String, FutureTask<List<Channel>>>();

	public RemoteChannelFactory() {
		if (clientBootstrap == null) {
			clientBootstrap = setupBootstrap();
		}
	}

	public Channel get(RemoteConfig config, final int connectTimeout) 
			throws Exception {
		return get(config, connectTimeout, DEFAULT_CHANNEL_NUM);
	}

	public Channel get(RemoteConfig config, final int connectTimeout, final int clientNums)
			throws Exception {
		final String targetIP = config.getAddress();
		final int targetPort = config.getPort();
		String key = getKey(config);
		FutureTask<List<Channel>> futrue = channels.get(key);
		//
		if (futrue != null) {
			if (clientNums == 1) {
				return futrue.get().get(0);
			} else {
				return futrue.get().get(random.nextInt(clientNums));
			}
		} else {
			FutureTask<List<Channel>> task = new FutureTask<List<Channel>>(
					new Callable<List<Channel>>() {
						public List<Channel> call() throws Exception {
							List<Channel> clients = new ArrayList<Channel>(
									clientNums);
							for (int i = 0; i < clientNums; i++) {
								clients.add(createChannel(targetIP, targetPort,
										connectTimeout));
							}
							return clients;
						}
					});
			FutureTask<List<Channel>> currentTask = channels.putIfAbsent(key,
					task);
			if (currentTask == null) {
				task.run();
			} else {
				task = currentTask;
			}
			if (clientNums == 1)
				return task.get().get(0);
			else {
				return task.get().get(random.nextInt(clientNums));
			}
		}
	}

	public String getKey(RemoteConfig config) {
		String targetIP = config.getAddress();
		int targetPort = config.getPort();
		StringBuilder strbuffer = new StringBuilder(targetIP);
		strbuffer.append(SEPSIGN).append(targetPort);
		String key = strbuffer.toString();
		return key;
	}
	
	public void removeClient(String key) {
		try {
			channels.remove(key);
		} catch (Exception e) {
		}
	}

	protected Channel createChannel(String targetIP, int targetPort,
			int connectTimeout) throws Exception {
		ChannelFuture future = clientBootstrap.connect(new InetSocketAddress(
				targetIP, targetPort));
		future.awaitUninterruptibly(connectTimeout);
		if (!future.isDone()) {
			throw new Exception("Create connection to " + targetIP + ":"
					+ targetPort + " timeout!");
		}
		if (future.isCancelled()) {
			throw new Exception("Create connection to " + targetIP + ":"
					+ targetPort + " cancelled by user!");
		}
		if (!future.isSuccess()) {
			throw new Exception("Create connection to " + targetIP + ":"
					+ targetPort + " error", future.getCause());
		}
		return future.getChannel();
	}

	//
	private ClientBootstrap setupBootstrap() {
		Executor bossExecutor = Executors.newCachedThreadPool();
		Executor workExecutor = Executors.newCachedThreadPool();
		ChannelFactory factory = new NioClientSocketChannelFactory(
				bossExecutor, workExecutor);
		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline popeline = Channels.pipeline();
				popeline.addLast("decoder", new DefaultObjectFrameDecoder());
				popeline.addLast("handler", new RPCClientChannelHandler());
				return popeline;
			}
		});
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		return bootstrap;
	}
}

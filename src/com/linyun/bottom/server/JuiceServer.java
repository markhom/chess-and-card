/**
 * Juice
 * com.juice.orange.game.server
 * JuiceWebServer.java
 */
package com.linyun.bottom.server;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.linyun.bottom.exception.ConnectionException;
import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.exception.PrintStackTraceExceptionHandler;
import com.linyun.bottom.handler.ConnectionTrackingHandler;
import com.linyun.bottom.handler.DefaultFrameDecoder;
import com.linyun.bottom.handler.DefaultJuiceHandler;
import com.linyun.bottom.handler.IJuiceHandler;
import com.linyun.bottom.handler.JuiceChannelHandler;
import com.linyun.bottom.handler.PathMatchHandler;
import com.linyun.bottom.handler.StaleConnectionTrackingHandler;
import com.linyun.bottom.util.NameThreadFactory;
import com.linyun.bottom.util.threadpool.JuicePoolManager;
import com.linyun.bottom.util.threadpool.JuiceThreadPool;


/**
 * @author shaojieque 
 * 2013-3-19
 */
public class JuiceServer implements IJuiceServer {
	//private static final Logger logger = LoggerFactory.getLogger(JuiceServer.class);
	public static final String Transport_HTTP = "http";
	public static final String Transport_Socket = "socket";
	public static final String Transport_Remote = "remote";
	//
	private static final long DEFAULT_STALE_CONNECTION_TIMEOUT = 5 * 3 * 1000;  //5秒的心跳周期    三个周期后如果还没有数据交互  则认为客户端已经掉线 关闭该channel

	private final SocketAddress socketAddress;
	private final URI publicUri;
	private final List<IJuiceHandler> handlers = new ArrayList<IJuiceHandler>();
	private final List<ExecutorService> executorServices = new ArrayList<ExecutorService>();
	private final Executor executor;

	private ServerBootstrap bootstrap;
	private Channel channel;

	protected long nextId = 1;
	private Thread.UncaughtExceptionHandler exceptionHandler;
	private Thread.UncaughtExceptionHandler ioExceptionHandler;
	private ConnectionTrackingHandler connectionTrackingHandler;
	private StaleConnectionTrackingHandler staleConnectionTrackingHandler;
	private int maxInitialLineLength = 4096;
	private int maxHeaderSize = 8192;
	private int maxChunkSize = 8192;
	private int maxContentLength = 65536;
	private String transport = Transport_Socket;
	private int poolNum = Runtime.getRuntime().availableProcessors() * 2;
	//
	public JuiceServer(int port) {
		this(Executors.newCachedThreadPool(new NameThreadFactory(
				"JUICE-SERVER-Handler")), port);
	}

	public JuiceServer(ExecutorService executorService, int port) {
		this((Executor) executorService, port);
		executorServices.add(executorService);
	}

	public JuiceServer(final Executor executor, int port) {
		this(executor, new InetSocketAddress(port), localUri(port));
	}

	public JuiceServer(final Executor executor, SocketAddress address, URI uri) {
		this.executor = executor;
		this.socketAddress = address;
		this.publicUri = uri;
		// Uncaught exceptions from handlers get dumped to console by default.
		// To change, call uncaughtExceptionHandler()
		uncaughtExceptionHandler(new PrintStackTraceExceptionHandler());
		//
		connectionExceptionHandler(new ConnectionException());
		//
		setupDefaultHandlers();
	}

	protected void setupDefaultHandlers() {
		add(new DefaultJuiceHandler());
	}

	@Override
	public IJuiceServer add(IJuiceHandler handler) {
		handlers.add(handler);
		return this;
	}

	@Override
	public IJuiceServer add(String path, IJuiceHandler handler) {
		return add(new PathMatchHandler(path, handler));
	}

	@Override
	public Future<? extends IJuiceServer> start() {
		//
		FutureTask<JuiceServer> future = new FutureTask<JuiceServer>(
				new Callable<JuiceServer>() 
				{
					@Override
					public JuiceServer call() throws Exception
					{
						if (isRunning()) 
						{
							throw new IllegalStateException(
									"Server already started.");
						}

						bootstrap = new ServerBootstrap();
						if (Transport_HTTP.equals(transport))
						{
							bootstrap.setPipelineFactory(createHttpFactory());
						}
						else if (Transport_Socket.equals(transport))
						{
							bootstrap.setPipelineFactory(createSocketFactory());
						}
						else 
						{
							throw new JuiceException("can't support Transport:" + transport);
						}
						//
						staleConnectionTrackingHandler = new StaleConnectionTrackingHandler(DEFAULT_STALE_CONNECTION_TIMEOUT);
						ScheduledExecutorService staleCheckExecutor = Executors.newSingleThreadScheduledExecutor(new NameThreadFactory("JUICE-STALE-CONNECTION-CHECK-THREAD"));
						staleCheckExecutor.scheduleWithFixedDelay(
								new Runnable() 
								{
									@Override
									public void run()
									{
										staleConnectionTrackingHandler.closeStaleConnections();
									}
								}, DEFAULT_STALE_CONNECTION_TIMEOUT / 2, DEFAULT_STALE_CONNECTION_TIMEOUT / 2, TimeUnit.MILLISECONDS);
						executorServices.add(staleCheckExecutor);
						//
						connectionTrackingHandler = new ConnectionTrackingHandler();
						ExecutorService bossExecutor = Executors.newCachedThreadPool(new NameThreadFactory("JUICE-BOSS-THREAD"));
						executorServices.add(bossExecutor);
						ExecutorService workExecutor = Executors.newCachedThreadPool(new NameThreadFactory("JUICE-WORK-THREAD"));
						executorServices.add(workExecutor);
						
						//
						bootstrap.setFactory(new NioServerSocketChannelFactory(bossExecutor, workExecutor));
						channel = bootstrap.bind(socketAddress);
						//
						initThreadPool();
						return JuiceServer.this;
					}
				});

		final Thread thread = new Thread(future, "JUICE-START-THREAD");
		thread.start();
		
		return future;
	}

	public boolean isRunning() {
		return channel != null && channel.isBound();
	}

	/** 初始化线程池*/
	private void initThreadPool() {
		for(int i = 0; i < poolNum; i++){
			JuiceThreadPool jtp = new JuiceThreadPool();
			jtp.initWorkThread();
			JuicePoolManager.getInstance().addThreadPool(jtp);
		}
	}
	
	@Override
	public Future<IJuiceServer> stop() {
		FutureTask<IJuiceServer> future = new FutureTask<IJuiceServer>(
				new Callable<IJuiceServer>() {
					@Override
					public IJuiceServer call() throws Exception {
						if (channel != null) {
							channel.close();
						}
						if (connectionTrackingHandler != null) {
							connectionTrackingHandler.closeAllConnections();
							connectionTrackingHandler = null;
						}
						//
						if (bootstrap != null) {
							bootstrap.releaseExternalResources();
						}
						// shut down all services & give them a chance to
						// terminate
						for (ExecutorService service : executorServices) {
							shutdownAndAwaitTermination(service);
						}

						bootstrap = null;
						if (channel != null) {
							channel.getCloseFuture().await();
						}
						return JuiceServer.this;
					}
				});
		// don't use Executor here - it's just another resource we need to
		// manage -
		// thread creation on shutdown should be fine
		final Thread thread = new Thread(future, "JUICE-SHUTDOW-THREAD");
		thread.start();
		return future;
	}

	// See JavaDoc for ExecutorService
	private void shutdownAndAwaitTermination(ExecutorService executorService) {
		executorService.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
				executorService.shutdownNow(); // Cancel currently executing
												// tasks
				// Wait a while for tasks to respond to being cancelled
				if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
					System.err.println("ExecutorService did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			executorService.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public IJuiceServer uncaughtExceptionHandler(
			UncaughtExceptionHandler handler) {
		this.exceptionHandler = handler;
		return this;
	}

	@Override
	public Executor getExecutor() {
		return executor;
	}

	@Override
	public URI getURI() {
		return publicUri;
	}

	@Override
	public int getPort() {
		if (publicUri.getPort() == -1) {
			return publicUri.getScheme().equalsIgnoreCase("https") ? 443 : 80;
		}
		return publicUri.getPort();
	}

	public IJuiceServer maxChunkSize(int maxChunkSize) {
		this.maxChunkSize = maxChunkSize;
		return this;
	}

	public IJuiceServer maxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
		return this;
	}

	public IJuiceServer maxHeaderSize(int maxHeaderSize) {
		this.maxHeaderSize = maxHeaderSize;
		return this;
	}

	public IJuiceServer maxInitalLineLength(int maxInitalLineLength) {
		this.maxInitialLineLength = maxInitalLineLength;
		return this;
	}

	@Override
	public IJuiceServer connectionExceptionHandler(
			Thread.UncaughtExceptionHandler ioExceptionHandler) {
		this.ioExceptionHandler = ioExceptionHandler;
		return this;
	}

	private static URI localUri(int port) {
		try {
			return URI.create("http://"
					+ InetAddress.getLocalHost().getHostName()
					+ (port == 80 ? "" : (":" + port)) + "/");
		} catch (UnknownHostException e) {
			throw new RuntimeException(
					"can not create URI from localhost hostname - use constructor to pass an explicit URI",
					e);
		}
	}

	protected long timestamp() {
		return System.currentTimeMillis();
	}

	protected Object nextId() {
		return nextId++;
	}

	private ChannelPipelineFactory createHttpFactory() {
		return new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline()
					throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				long timestamp = timestamp();
				Object id = nextId();
				// TODO SSL
				pipeline.addLast("staleconnectiontracker", staleConnectionTrackingHandler);
				pipeline.addLast("connectiontracker", connectionTrackingHandler);
				pipeline.addLast("decoder", new HttpRequestDecoder(
						maxInitialLineLength, maxHeaderSize, maxChunkSize));
				pipeline.addLast("aggregator",
						new HttpChunkAggregator(maxContentLength));
				pipeline.addLast("decompressor", new HttpContentDecompressor());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("compressor", new HttpContentCompressor());
				pipeline.addLast("handler", new JuiceChannelHandler(executor, handlers, id, timestamp,
						exceptionHandler, ioExceptionHandler));
				return pipeline;
			}
		};
	}
	
	private ChannelPipelineFactory createSocketFactory(){
		return new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline()
					throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				long timestamp = timestamp();
				Object id = nextId();
				//
				pipeline.addLast("staleconnectiontracker", staleConnectionTrackingHandler);
				pipeline.addLast("connectiontracker", connectionTrackingHandler);
				pipeline.addLast("decoder", new DefaultFrameDecoder());
				pipeline.addLast("handler", new JuiceChannelHandler(executor, handlers, id, timestamp,
						exceptionHandler, ioExceptionHandler));
				return pipeline;
			}
		};
	}

	@Override
	public void setTransport(String transport) {
		this.transport = transport;
	}

	@Override
	public void setThreadPool(int num) {
		this.poolNum = num;
	}
}

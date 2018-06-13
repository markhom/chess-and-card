package com.linyun.pay.server;

import java.io.File;
import java.io.FileWriter;
import org.apache.log4j.Logger;

import com.linyun.bottom.bootstrap.BootstrapProperties;
import com.linyun.bottom.cached.RedisResource;
import com.linyun.bottom.container.Container;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.rmi.JuiceRemoteManager;
import com.linyun.bottom.rmi.RemoteConfig;
import com.linyun.bottom.server.IJuiceServer;
import com.linyun.bottom.server.JuiceServers;
import bsh.Interpreter;

/**
 * @author shaojieque
 * 2013-4-18
 */
public class ServerBootstrap 
{
	public static Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);
	/** 获取根目录*/
	public static String ROOT_DIR = System.getProperty("user.dir");
	
	/**
	 * 主函数
	 */
	public static void main(String[] args) throws Exception
	{
		// 读取服务器配置
		Interpreter interpreter = new Interpreter();
		interpreter.source(ROOT_DIR + File.separator + "script/juice.bsh");
		
		//
		Object _bootstrap = interpreter.get("bootstrap");		
		if (_bootstrap != null && _bootstrap instanceof BootstrapProperties) {
			BootstrapProperties bootstrap = (BootstrapProperties) _bootstrap;
			setupBootstrap(bootstrap);
		}
		
		Object _remotes = interpreter.get("remotes");
		if (_remotes != null) {
			RemoteConfig[] remotes = (RemoteConfig[]) _remotes;
			setupRemoteServer(remotes);
		}
		
		RedisResource.getInstance();
		
		Application app =  new Application();
		app.init();
		findAppPID();
	}
	
	// 初始化服务器启动配置
	private static void setupBootstrap(BootstrapProperties bp) throws Exception {
		logger.info("Configuration Server bootstrap params.......");
		logger.info("Server protocol:\t" + bp.getProtocol());
		logger.info("Server port:\t" + bp.getPort());
		IJuiceServer server = JuiceServers.createWebServer(bp.getPort());
		server.setTransport(bp.getProtocol());
		server.start().get();
	}
	
	// 初始化配置远程服务器配置
	private static void setupRemoteServer(RemoteConfig[] configs) throws Exception {
		logger.info("Configuration Remote Server.......");
		for (RemoteConfig config : configs) {
			logger.info("Add Remote Server - Name:" + config.getName() + ";Address:" + config.getAddress() +
					";Port:" + config.getPort());
			Container.addRemoteConfig(config);
		}
		JuiceRemoteManager.setupRemoteServer();
	}
	
	private static void findAppPID() throws Exception {
		String processName = java.lang.management.ManagementFactory
				.getRuntimeMXBean().getName();
		String pid = processName.split("@")[0];
		logger.info("Application PID:\t" + pid);
		FileWriter writer = new FileWriter(ROOT_DIR + File.separator + "server.pid");
		writer.write(pid);
		writer.flush();
		writer.close();
	}
}

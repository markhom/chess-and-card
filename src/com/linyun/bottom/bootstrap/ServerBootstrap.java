/**
 * Juice
 * com.juice.orange.game.bootstrap
 * ServerBootstrap.java
 */
package com.linyun.bottom.bootstrap;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import bsh.Interpreter;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.linyun.bottom.container.Container;
import com.linyun.bottom.database.ConnectionDataBaseHelper;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.rmi.JuiceRemoteManager;
import com.linyun.bottom.rmi.RemoteConfig;
import com.linyun.bottom.server.IJuiceServer;
import com.linyun.bottom.server.JuiceServers;

/**
 * @author shaojieque
 * 2013-4-18
 */
public class ServerBootstrap {
	public static Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);
	public static String ROOT_DIR = System.getProperty("user.dir");
	
	public static void main(String[] args) throws Exception{
		Interpreter interpreter = new Interpreter();
		interpreter.source(ROOT_DIR + File.separator + "script/juice.bsh");
		Object _database = interpreter.get("database");
		if (_database != null && _database instanceof DataBaseProperties) {
			DataBaseProperties database = (DataBaseProperties) _database;
			setupDataBase(database);
		}
		//
		Object _bootstrap = interpreter.get("bootstrap");
		if (_bootstrap != null && _bootstrap instanceof BootstrapProperties) {
			BootstrapProperties bootstrap = (BootstrapProperties) _bootstrap;
			setupBootstrap(bootstrap);
		}
		//
		Object _remotes = interpreter.get("remotes");
		if (_remotes != null) {
			RemoteConfig[] remotes = (RemoteConfig[]) _remotes;
			setupRemoteServer(remotes);
		}
	}
	
	
	private static void setupDataBase(DataBaseProperties dbp) throws Exception {
		logger.info("connect to database:" + dbp.getUrl());
		// create a new configuration object
		BoneCPConfig config = new BoneCPConfig();
		// set the JDBC url
		config.setJdbcUrl(dbp.getUrl());
		config.setUsername(dbp.getUserName()); // set the username
		config.setPassword(dbp.getPassword()); // set the password
		config.setPartitionCount(3);
		config.setMaxConnectionsPerPartition(20);
		config.setMinConnectionsPerPartition(10);
		config.setAcquireIncrement(3);
		config.setPoolAvailabilityThreshold(20);
		config.setReleaseHelperThreads(2);
		config.setIdleMaxAge(240, TimeUnit.MINUTES);
		config.setIdleConnectionTestPeriod(10, TimeUnit.MINUTES);
		config.setStatementsCacheSize(20);
		config.setStatementReleaseHelperThreads(3);
		// setup the connection
		BoneCP connectionPool = new BoneCP(config); 
		ConnectionDataBaseHelper.setBoneCP(connectionPool);
	}
	
	private static void setupBootstrap(BootstrapProperties bp) throws Exception {
		logger.info("Configuration Server bootstrap params.......");
		logger.info("Server protocol:\t" + bp.getProtocol());
		logger.info("Server port:\t" + bp.getPort());
		IJuiceServer server = JuiceServers.createWebServer(bp.getPort());
		server.setTransport(bp.getProtocol());
		server.start().get();
	}
	
	private static void setupRemoteServer(RemoteConfig[] configs) throws Exception {
		logger.info("Configuration Remote Server.......");
		for (RemoteConfig config : configs) {
			logger.info("Add Remote Server - Name:" + config.getName() + ";Address:" + config.getAddress() +
					";Port:" + config.getPort());
			Container.addRemoteConfig(config);
		}
		JuiceRemoteManager.setupRemoteServer();
	}
}

/**
 * Juice
 * com.juice.orange.game.database
 * ConnectionHelper.java
 */
package com.linyun.bottom.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.jolbox.bonecp.BoneCP;

/**
 * @author shaojieque
 * 2013-3-21
 */
public class ConnectionDataBaseHelper {
	private static BoneCP connectPool;
	
	public static void setBoneCP(BoneCP pool) {
		connectPool = pool;
	}
	
	public static Connection getConnection() throws SQLException{
		return connectPool.getConnection();
	}
}

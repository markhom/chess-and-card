/**
 * Juice
 * com.juice.orange.game.database
 * IJuiceDBHandler.java
 */
package com.linyun.bottom.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author shaojieque
 * 2013-3-21
 */
public interface IJuiceDBHandler<T> {
	 T handler(ResultSet rs) throws SQLException;
}

/**
 * Juice
 * com.juice.orange.game.database
 * ConnectionResource.java
 */
package com.linyun.bottom.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.log.LoggerFactory;


/**
 * @author shaojieque 2013-3-21
 */
public class ConnectionResource extends ConnectionDataBaseHelper {
	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionResource.class);

	/**
	 * execute insert or update SQL
	 */
	protected void saveOrUpdate(String sql, Object... args) {
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			setArgs(ps, args);
			ps.executeUpdate();
		} catch (Exception e) {
			logger.error(sql, e);
		} finally {
			releaseResources(ps, null);
			closeConnection(conn);
		}
	}

	/**
	 * execute query SQL ,return a object result
	 */
	protected <T> T queryForObject(String sql, IJuiceDBHandler<T> handler,
			Object... args) {
		T t = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			setArgs(ps, args);
			rs = ps.executeQuery();
			if (rs.next()) {
				t = handler.handler(rs);
			}
		} catch (SQLException e) {
			logger.error(sql, e);
		} catch (Exception e) {
			logger.error(sql, e);
		} finally {
			releaseResources(ps, rs);
			closeConnection(conn);
		}
		return t;
	}

	/**
	 * execute query SQL ,return a list of object result
	 */
	protected <T> List<T> queryForList(String sql, IJuiceDBHandler<T> handler,
			Object... args) {
		PreparedStatement ps = null;
		Connection conn = null;
		ResultSet rs = null;
		List<T> list = new ArrayList<T>();
		try {
			conn = ConnectionDataBaseHelper.getConnection();
			ps = conn.prepareStatement(sql);
			setArgs(ps, args);
			rs = ps.executeQuery();
			while (rs.next()) {
				T t = handler.handler(rs);
				list.add(t);
			}
		} catch (SQLException e) {
			logger.error(sql, e);
		} catch (Exception e) {
			logger.error(sql, e);
		} finally {
			releaseResources(ps, rs);
			closeConnection(conn);
		}
		return list;
	}

	protected void executeUpdate(String sql) {
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = ConnectionDataBaseHelper.getConnection();
			ps = conn.prepareStatement(sql);
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error(sql, e);
		} catch (Exception e) {
			logger.error(sql, e);
		} finally {
			releaseResources(ps, null);
			closeConnection(conn);
		}
	}

	/**
	 * release connection resource
	 */
	protected void releaseResources(Statement stmt, ResultSet rs)
			throws JuiceException {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new JuiceException("", e);
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				throw new JuiceException("", e);
			}
		}
	}

	private void closeConnection(Connection conn) {
		try {
			if (conn != null) {
				try {
					if (!conn.getAutoCommit()) {
						conn.rollback();
					}
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logger.error("close DB connection error!", e);
		}
	}

	private void setArgs(PreparedStatement ps, Object... args)
			throws SQLException {
		if (args == null) {
			return;
		}
		for (int i = 0; i < args.length; i++) {
			ps.setObject(i + 1, args[i]);
		}
	}
}

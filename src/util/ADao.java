package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ADao {
	private static final Logger LOG = LogManager.getLogger(ADao.class);

	public boolean isWorking() {
		return true;
	}

	public static String printSQLException(SQLException e) {
		LOG.error("\n----- SQLException -----");
		LOG.error("  SQL State:  " + e.getSQLState());
		LOG.error("  Error Code: " + e.getErrorCode());
		LOG.error("  Message:    " + e.getMessage());
		if (e.getMessage().contains("Table/View")
				|| e.getMessage().contains(" does not exist.")) {
			LOG.fatal(e.getMessage());
			return "error__NoTable";
		}
		if (e.getMessage().contains("Error connecting to server")) {
			LOG.info(e.getMessage());
			return "error__connection";
		}
		return "error__unknown";
	}

	public String getAutoInfos(int port, String host) {
		Connection conn = null;
		Properties props = new Properties();
		props.put("user", "agent");
		props.put("password", "catallena7");
		String protocol = null;
		StringBuffer sb = new StringBuffer();
		if (host.matches("localhost.localdomain")) {// TEST
			host = "192.168.178.131";
		}
		protocol = "jdbc:derby://" + host + ":" + port + "/";

		PreparedStatement pst = null;
		ResultSet rs = null;
		Statement s = null;
		try {
			DriverManager.setLoginTimeout(5);
			conn = DriverManager.getConnection(
					protocol + "derbyDB;create=true", props);
			String sql = "SELECT COUNT(*) CNT FROM AUTO_JOBS WHERE STATUS='PEND'";
			LOG.trace(host + ":" + sql);
			s = conn.createStatement();
			rs = s.executeQuery(sql);

			while (rs.next()) {
				sb.append(rs.getInt("CNT"));
			}
			sb.append("split");

			sql = "SELECT AUTOGENT_LAST_CHECK_TIMESTAMP FROM AGENT_MGR";
			LOG.trace(host + ":" + sql);
			rs = s.executeQuery(sql);

			while (rs.next()) {
				sb.append(rs.getTimestamp("AUTOGENT_LAST_CHECK_TIMESTAMP"));
			}
			if (sb.length() > 0)
				LOG.info(host + ":res=" + sb);

		} catch (SQLException sqle) {
			return(printSQLException(sqle));
		} finally {
			try {
				if (pst != null) {
					pst.close();
					pst = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
					return sb.toString();
				}
			} catch (SQLException e) {
				return(printSQLException(e));
			}
		}
		return sb.toString();
	}
}

package main;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import util.ADao;
import util.RDao;

public class Worker implements Callable<Boolean> {
	private static final Logger LOG = LogManager.getLogger(Worker.class);
	int thNo;
	int thAll;
	String rdbUrl;
	String rdbUser;
	String rdbPassword;
	String dbType;
	int agentPort;
	String sql;

	public Worker(int thNo, int thAll, String rdbUrl, String rdbUser,
			String rdbPasswd, int agentPort, String dbType, String sql) {
		this.thNo = thNo;
		this.thAll = thAll;
		this.rdbUrl = rdbUrl;
		this.rdbUser = rdbUser;
		this.rdbPassword = rdbPasswd;
		this.agentPort = agentPort;
		this.dbType = dbType;
		this.sql = sql;
	}

	@Override
	public Boolean call() throws Exception {
		RDao rDao = new RDao();
		Connection conn = rDao.getConnection(rdbUrl, rdbUser, rdbPassword);
		ArrayList<String> hosts = rDao.getHostsMT(conn, thNo-1, thAll, sql);
		// ArrayList<String> hosts = rDao.getHostsTest(conn);
		int i = 0;
		ADao adao = new ADao();
		DateTime start = new DateTime();
		for (String host : hosts) {
			LOG.trace(thNo + "-" + i + ":Checking:" + host);
			i++;
			String line = adao.getAutoInfos(agentPort, host);
			LOG.info("outFrADao=" + line);
			if (!line.startsWith("error__") && line.length() > 0) {
				rDao.updateAutogentStatus(conn, host, line, dbType);
			}
		}
		rDao.setWorkingTimestamp(conn, rdbUrl, thNo);
		DateTime end = new DateTime();
		Duration elapsedTime = new Duration(start, end);
		LOG.info(elapsedTime);
		rDao.disconnect(conn);
		return true;
	}
}
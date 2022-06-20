package com.jaoafa.periodmatch2.lib;

import java.sql.*;

public class MySQLDBManager {
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;
    private Connection conn = null;
    private long WAIT_TIMEOUT = -1;
    private long LAST_PACKET = -1;

    public MySQLDBManager(String hostname, String port, String database, String username, String password) throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        user = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        if (conn != null && !conn.isClosed() && conn.isValid(5)) {
            if (WAIT_TIMEOUT != -1 && LAST_PACKET != -1) {
                long diff = (System.currentTimeMillis() - LAST_PACKET) / 1000;
                if (diff < WAIT_TIMEOUT) return conn;
                else
                    System.out.println("MySQL TIMEOUT! WAIT_TIMEOUT: " + WAIT_TIMEOUT + " / DIFF: " + diff);
            }
            LAST_PACKET = System.currentTimeMillis();
            return conn;
        }
        String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + database + "?autoReconnect=true&useUnicode=true&characterEncoding=utf8";
        conn = DriverManager.getConnection(jdbcUrl, user, password);
        if (WAIT_TIMEOUT == -1) WAIT_TIMEOUT = getWaitTimeout();
        LAST_PACKET = System.currentTimeMillis();
        conn.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
        return conn;
    }

    private long getWaitTimeout() {
        try {
            Connection conn = getConnection();
            try (PreparedStatement statement = conn.prepareStatement("show variables like 'wait_timeout'")) {
                try (ResultSet res = statement.executeQuery()) {
                    if (res.next()) {
                        WAIT_TIMEOUT = res.getInt("Value");
                        System.out.println("MySQL WAIT_TIMEOUT: " + WAIT_TIMEOUT);
                    } else WAIT_TIMEOUT = -1;
                }
            }
        } catch (SQLException e) {
            WAIT_TIMEOUT = -1;
        }
        return WAIT_TIMEOUT;
    }
}

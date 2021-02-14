package ru.kpfu.itis.hbaseloader.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Component
public class PhoenixConnectionManager {
    @Value("${application.config.hbase.url}")
    private String url;
    @Value("${application.config.hbase.login")
    private String login;
    @Value("${application.config.hbase.password}")
    private String password;
    private final Properties props;

    public PhoenixConnectionManager() {
        try {
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
            Properties props = new Properties();
            props.setProperty("phoenix.query.timeoutMs", "1200000");
            props.setProperty("hbase.rpc.timeout", "1200000");
            props.setProperty("hbase.client.scanner.timeout.period", "1200000");

            props.setProperty("phoenix.query.threadPoolSize", "128");
            props.setProperty("phoenix.query.queueSize", "5000");
            props.setProperty("phoenix.use.stats.parallelization", "false");
            props.setProperty("hbase.client.scanner.caching", "20000");

            this.props = props;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("org.apache.phoenix.jdbc.PhoenixDriver not found");
        }
    }

    public Connection getConnection() throws SQLException {
        if (login == null && password == null
                || login.isBlank() && password.isBlank()) return DriverManager.getConnection(url, props);
        return DriverManager.getConnection(url, login, password);
    }
}

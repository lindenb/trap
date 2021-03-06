package util;

import global.Data;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Properties;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 *
 * @author nick
 */
public class DBManager {

    private static DataSource dataSource;
    private static Connection connection;
    private static Statement statement;

    public static String dbVersion;
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static HashMap<String, String> dbVersionNameMap = new HashMap<String, String>();

    public static void init() throws Exception {
        initDataFromSystemConfig();

        if (dataSource == null) {
            PoolProperties p = new PoolProperties();
            p.setDriverClassName("com.mysql.jdbc.Driver");
            p.setUrl(dbUrl);
            p.setUsername(dbUser);
            p.setPassword(dbPassword);
            p.setJmxEnabled(true);
            p.setTestWhileIdle(false);
            p.setTestOnBorrow(true);
            p.setValidationQuery("SELECT 1");
            p.setTestOnReturn(false);
            p.setValidationInterval(30000);
            p.setTimeBetweenEvictionRunsMillis(30000);
            p.setMaxActive(100);
            p.setInitialSize(10);
            p.setMaxWait(10000);
            p.setRemoveAbandonedTimeout(60);
            p.setMinEvictableIdleTimeMillis(30000);
            p.setMinIdle(10);
            p.setLogAbandoned(true);
            p.setRemoveAbandoned(true);
            p.setJdbcInterceptors(
                    "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                    + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
            dataSource = new DataSource();
            dataSource.setPoolProperties(p);
            connection = dataSource.getConnection();
            statement = connection.createStatement();
        }

        if (connection == null || connection.isClosed()) {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
        } else if (statement.isClosed()) {
            statement = connection.createStatement();
        }
    }

    private static void initDataFromSystemConfig() {
        try {
            // server config
            InputStream input = new FileInputStream(Data.SYSTEM_CONFIG);
            Properties prop = new Properties();
            prop.load(input);

            dbUrl = prop.getProperty("dburl");
            dbUser = prop.getProperty("dbuser");
            dbPassword = prop.getProperty("dbpassword");
            String dbVersionName = prop.getProperty("dbversionname");
            
            // local config
//            dbUrl = "jdbc:mysql://localhost:3306/vdsdb";
//            dbUser = "test";
//            dbPassword = "test";
//            String dbVersionName = "v1:vdsdb,v2:trap_v2_060117";

            for (String str : dbVersionName.split(",")) {
                String version = str.split(":")[0];
                String name = str.split(":")[1];

                dbVersionNameMap.put(version, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResultSet executeQuery(String sqlQuery) throws SQLException {
        return statement.executeQuery(sqlQuery);
    }

    public static PreparedStatement prepareStatement(String sqlQuery) throws SQLException {
        return connection.prepareStatement(sqlQuery);
    }

    public static String getDBName() {
        return dbVersionNameMap.get(dbVersion);
    }
}

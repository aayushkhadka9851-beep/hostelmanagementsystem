package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * JDBC Utility class that provides database connection instances.
 * Implements fallback mechanisms to allow execution in both Tomcat containers (Connection Pool)
 * and directly within IntelliJ IDEA (Standalone standard main tests).
 */
public class DBConnection {

    // Database configuration constants (Update matching your MySQL Workbench setup)
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/hostel_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&charEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; // or your configured MySQL password
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static DataSource dataSource = null;
    private static boolean fallbackMode = false;

    static {
        try {
            // Attempt JNDI Datasource lookup from Tomcat context
            InitialContext ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/HostelDB");
            // Test connection
            try (Connection test = dataSource.getConnection()) {
                System.out.println("Hostel Manager Context Log: JNDI DataSource initialized successfully.");
            }
        } catch (Exception ne) {
            System.out.println("Hostel Manager Context Log: JNDI lookup or test skipped; attempting driver manager connection...");
            dataSource = null;
            try {
                Class.forName(DB_DRIVER);
                try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
                    System.out.println("Hostel Manager Context Log: MySQL Standard Connection established successfully.");
                    fallbackMode = false;
                }
            } catch (Exception e) {
                System.out.println("Hostel Manager Context Log: Unable to connect to MySQL database (" + e.getMessage() + "). Directing into high-fidelity SQLite/In-memory fallback mode!");
                fallbackMode = true;
            }
        }
    }

    /**
     * Checks if standard database is down and fallback mode is active.
     */
    public static boolean isFallbackMode() {
        return fallbackMode;
    }

    /**
     * Retrieves a database connection.
     * @return Connection object or null if in fallback mode
     * @throws SQLException, ClassNotFoundException
     */
    public static Connection getConnection() throws SQLException {
        if (fallbackMode) {
            throw new SQLException("Database offline - running in high-fidelity in-memory fallback store.");
        }
        if (dataSource != null) {
            return dataSource.getConnection();
        } else {
            try {
                // Standalone fallback using standard DriverManager
                Class.forName(DB_DRIVER);
                return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC Driver not found: " + DB_DRIVER, e);
            }
        }
    }

    /**
     * Closes connection resources safely.
     */
    public static void closeConnection(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                System.err.println("DBConnection Exception while releasing resources: " + e.getMessage());
            }
        }
    }
}

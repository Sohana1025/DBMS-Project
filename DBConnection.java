import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/car_rental_db";
    private static final String USER     = "root";     // change if your MySQL user is different
    private static final String PASSWORD = "Alpha_@123";     // change to your MySQL password

    /**
     * getConnection()
     * ---------------
     * Static method – can be called without creating an object.
     * Loads the MySQL JDBC driver and returns a live Connection object.
     *
     * @return  Connection  – active database connection
     * @throws  SQLException if connection fails (wrong URL/credentials/DB not running)
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load the MySQL JDBC driver class into the JVM.
            // Required for older JDBC versions; harmless in newer ones.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Driver JAR not on classpath → show helpful message
            System.out.println("MySQL Driver not found. Add mysql-connector-java.jar to classpath.");
            e.printStackTrace();
        }

        // DriverManager uses the URL + credentials to create a TCP connection
        // to MySQL and returns a Connection object we can use to run SQL.
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

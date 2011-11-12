//intentionally un-packaged to facilitate testing
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class OracleDb implements DB {
	private Connection conn;

	public OracleDb(String server, String user, String password) {
		try {
			//Load the Oracle JDBC driver
			Class.forName("oracle.jdbc.driver.OracleDriver");

			//Connect to the database
			conn = DriverManager.getConnection("jdbc:oracle:thin:@" + server , user , password);
		} //end of the "try"
		catch (ClassNotFoundException e){
			System.out.println("ERROR: couldn't load database driver");
		} catch (SQLException e) {
			System.out.println("ERROR: couldn't open connection");
			e.printStackTrace();
		}
	}

	public ResultSet execute(final String query) {
		if (query == null) {
			throw new IllegalArgumentException("ERROR: can't execute empty query");
		}
		ResultSet rset = null;
		try {
	    	Statement stmt = conn.createStatement();
	        //System.out.println("                         ***DEBUG*** Executing query=[" + query + "]");
	        rset = stmt.executeQuery(query);
		}
		catch (Exception e) {
			System.out.println("Error executing query [" + query + "]");
		}
		return rset;
	}
}

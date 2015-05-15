package WorkLoader;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

public class Database {
	private Connection connection;
	private Statement st;
	private Jdbc3PoolingDataSource source;
	
	/**
	 * Constructor
	 */
	Database() {
		try {
			Class.forName("org.postgresql.Driver");
			source = new Jdbc3PoolingDataSource();
			source.setDataSourceName("Customer");
			source.setServerName("b139.seng.uvic.ca:44450");
			source.setDatabaseName("customerDB");
			source.setUser("root");
			source.setPassword("password1");
			source.setInitialConnections(1);
			source.setMaxConnections(1);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Passes sql update string to customer database.
	 * @param s
	 * @return 1 means successful update; else failed.
	 */
	private int updateDB(String s) {
		int val = 0;
		try {
			connection = source.getConnection();
			st = connection.createStatement();
			val = st.executeUpdate(s);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				try {connection.close();} catch (SQLException e) {}
			}
		}
		return val;		
	}
	
	public void insertUser(String userID) {
		int val = updateDB("INSERT INTO Account(UserID)"
						+ " VALUES('" + userID + "')");
		if(val != 1) {
			System.out.println("Error with inserting user: " + userID);
		}
	}
}

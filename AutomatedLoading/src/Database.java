import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

public class Database {
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
	public int[] updateDB() {
		int[] val = null;
		Connection connection = null;
		try {
			connection = source.getConnection();
			Statement st = connection.createStatement();
			
			st.addBatch("DELETE FROM Stocks");
			st.addBatch("DELETE FROM StockReserve");
			st.addBatch("DELETE FROM Reserve");
			st.addBatch("UPDATE Account set Dollar=0, Cent=0");
			
			val = st.executeBatch();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				try {connection.close();} catch (SQLException e) {}
			}
		}
		return val;		
	}
	public void closeConnection() {
		try {
			source.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

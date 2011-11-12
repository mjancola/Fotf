//intentionally un-packaged to facilitate testing
import java.sql.ResultSet;


public interface DB {
	public ResultSet execute(final String query);
}

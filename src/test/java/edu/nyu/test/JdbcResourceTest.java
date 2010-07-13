import junit.framework.TestCase;
import edu.nyu.cms.JdbcResourceProvider;

public class JdbcResourceTest extends TestCase {
	public void testUrlToSql() throws Exception {
		assertEquals(
			JdbcResourceProvider.convertUrlToSql(":table/:id", "courses/12345"),
			"SELECT * FROM courses WHERE id='12345'"
		);
	}
}
package edu.nyu.cms;

import java.sql.*;
import javax.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.sling.api.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.commons.datasource.poolservice.*;

// @see http://www.lucamasini.net/Home/sling-and-cq5/accessing-relational-data-as-sling-restful-urls
// @see http://dev.day.com/discussion-groups/content/lists.go/ttadhjzq

/**
 * @scr.component metatype="true" factory="true" name="JdbcResourceProvider" label="JdbcResourceProvider" description="JDBC Resource Provider" getConfigurationFactory=true
 * @scr.service interface="org.apache.sling.api.resource.ResourceProvider"
 * @scr.property name="service.vendor" value="New York University"
 * @scr.property name="service.description" value=""
 * @scr.property name="provider.roots" value="" label="Content Namespace" description="E.g. /content/namespace"
 * @scr.property name="resourceType" value="" label="Resource Type" description="E.g. app/components/type"
 * @scr.property name="jdbc.datasource" value="" label="JDBC Data Source" description="The name of the configured JDBC data source"
 */

public class JdbcResourceProvider implements ResourceProvider {
	private static final Logger log = LoggerFactory.getLogger(JdbcResourceProvider.class);
	
	/** @scr.reference */
	private DataSourcePool dataSourcePool;
	
	private String providerRoot;
	private String providerRootPrefix;
	private String resourceType;
	private Connection conn;
	
	protected void activate(ComponentContext c) throws SQLException {
		providerRoot = c.getProperties().get("provider.roots").toString();
		resourceType = c.getProperties().get("resourceType").toString();

		this.providerRootPrefix = providerRoot.concat("/");
		
		try {
			DataSource ds = (DataSource)this.dataSourcePool.getDataSource(c.getProperties().get("jdbc.datasource").toString()); 
			this.conn = ds.getConnection();			
		} catch (DataSourceNotFoundException e) {
			log.info(e.getMessage());
		}
	}

	protected void deactivate(ComponentContext c) throws SQLException {
		this.conn.close();
		this.conn = null;
		this.providerRoot = null;
		this.providerRootPrefix = null;
		this.resourceType = null;
	}
	
	public Resource getResource(ResourceResolver resourceResolver, HttpServletRequest request, String path) {
		return getResource(resourceResolver, path);
	}
	
	/**
	 * Retrieve a JDBC resource, mapping a REST request with the following URL components: /path/to/root/<table>/<id>
	 */
	public Resource getResource(ResourceResolver resourceResolver, String path) {
		
		// TODO Filter requests to match above format
		
		if (providerRoot.equals(path) || providerRootPrefix.equals(path)) {
			log.info("path " + path + " matches this provider root folder: " + providerRoot);
			return new SyntheticResource(resourceResolver, path, "nt:folder");
		} else if (path.startsWith(providerRootPrefix) && NumberUtils.isNumber(path.substring(providerRootPrefix.length()))) {
			
			Statement stmt = null;
			ResultSet rs = null;
			Resource resource = null;
			
			// TODO Simplify query execution. Possibly use Spring Framework...
			// TODO Generalize SELECT clause
			
			String query = "SELECT * FROM test WHERE id = " + path.substring(providerRootPrefix.length());
			
			try {
				log.info(String.format("[jdbc] Executing query: %s", query));
				
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);
				
				while (rs.next()) {
					ResultSetMetaData resultMeta = rs.getMetaData();
					ResourceMetadata resourceMeta = new ResourceMetadata();

					for (int i = 1; i <= resultMeta.getColumnCount(); i++) {
						resourceMeta.put(resultMeta.getColumnName(i), rs.getObject(i));
						log.info(String.format("[jdbc] %s = %s", resultMeta.getColumnName(i), rs.getObject(i)));
					}

					resourceMeta.setResolutionPath(path);
					resource = new JdbcResource(resourceResolver, resourceMeta, this.resourceType);
				}
				
			} catch (SQLException ex) {
				log.info("SQLException: " + ex.getMessage());
				log.info("SQLState: " + ex.getSQLState());
				log.info("VendorError: " + ex.getErrorCode());
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException sqlEx) {}
					rs = null;
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException sqlEx) {}
					stmt = null;
				}
			}
			
			return resource;
		}

		return null;
	}
	
	// TODO Implement `listChildren`
	
	public Iterator<Resource> listChildren(Resource parent) {
		return null;
	}
}
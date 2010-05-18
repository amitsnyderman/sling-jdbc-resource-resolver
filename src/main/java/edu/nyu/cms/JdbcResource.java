package edu.nyu.cms;

import org.apache.sling.api.resource.*;
import org.apache.sling.api.adapter.*;
import org.apache.sling.api.wrappers.ValueMapDecorator;

public class JdbcResource extends SyntheticResource {
	public JdbcResource(ResourceResolver resourceResolver, ResourceMetadata rm, String resourceType) {
		super(resourceResolver, rm, resourceType);
	}
	
	public JdbcResource(ResourceResolver resourceResolver, String path, String resourceType) {
		super(resourceResolver, path, resourceType);
	}
	
	public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
		return (type == ValueMap.class) ?
			(AdapterType)new ValueMapDecorator(this.getResourceMetadata()) :
			super.adaptTo(type);
	}
}
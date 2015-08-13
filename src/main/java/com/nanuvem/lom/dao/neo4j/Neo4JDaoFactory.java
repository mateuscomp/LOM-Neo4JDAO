package com.nanuvem.lom.dao.neo4j;

import com.nanuvem.lom.api.dao.DaoFactory;
import com.nanuvem.lom.api.dao.EntityDao;
import com.nanuvem.lom.api.dao.EntityTypeDao;
import com.nanuvem.lom.api.dao.PropertyDao;
import com.nanuvem.lom.api.dao.PropertyTypeDao;

public class Neo4JDaoFactory implements DaoFactory {

	private Neo4JPropertyTypeDao propertyTypeDao = null;
	private Neo4JEntityTypeDao entityTypeDao;

	private Neo4JConnector connector;

	public EntityTypeDao createEntityTypeDao() {
		if (this.entityTypeDao == null) {
			this.entityTypeDao = new Neo4JEntityTypeDao(connector);
		}
		return this.entityTypeDao;
	}

	public PropertyTypeDao createPropertyTypeDao() {
		if (this.propertyTypeDao != null) {
			this.propertyTypeDao = new Neo4JPropertyTypeDao(connector,
					(Neo4JEntityTypeDao) createEntityTypeDao());
		}
		return this.propertyTypeDao;
	}

	public EntityDao createEntityDao() {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyDao createPropertyDao() {
		// TODO Auto-generated method stub
		return null;
	}

	public void createDatabaseSchema() {
		// TODO Auto-generated method stub
	}

	public void dropDatabaseSchema() {
		// TODO Auto-generated method stub

	}

}

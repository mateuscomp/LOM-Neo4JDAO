package com.nanuvem.lom.dao.neo4j;

import com.nanuvem.lom.api.dao.DaoFactory;
import com.nanuvem.lom.api.dao.EntityDao;
import com.nanuvem.lom.api.dao.EntityTypeDao;
import com.nanuvem.lom.api.dao.PropertyDao;
import com.nanuvem.lom.api.dao.PropertyTypeDao;

public class Neo4JDaoFactory implements DaoFactory {

	private Neo4JEntityTypeDao entityTypeDao;
	private Neo4JPropertyTypeDao propertyTypeDao;
	private Neo4JEntityDao entityDao;
	private Neo4JPropertyDAO propertyDao;

	private Neo4JConnector connector = new Neo4JConnector();

	public EntityTypeDao createEntityTypeDao() {
		if (this.entityTypeDao == null) {
			this.entityTypeDao = new Neo4JEntityTypeDao(connector);
		}
		return this.entityTypeDao;
	}

	public PropertyTypeDao createPropertyTypeDao() {
		if (this.propertyTypeDao == null) {
			this.propertyTypeDao = new Neo4JPropertyTypeDao(connector,
					(Neo4JEntityTypeDao) createEntityTypeDao());
		}
		return this.propertyTypeDao;
	}

	public EntityDao createEntityDao() {
		if (this.entityDao == null) {
			this.entityDao = new Neo4JEntityDao(connector,
					(Neo4JEntityTypeDao) createEntityTypeDao(), propertyTypeDao);
		}
		return this.entityDao;
	}

	public PropertyDao createPropertyDao() {
		if (this.propertyDao == null) {
			this.propertyDao = new Neo4JPropertyDAO(connector,
					(Neo4JEntityDao) createEntityDao(),
					(Neo4JPropertyTypeDao) propertyTypeDao);
		}
		return this.propertyDao;
	}

	public void createDatabaseSchema() {
		
	}

	public void dropDatabaseSchema() {
		this.connector.dropDatabase();
	}
}
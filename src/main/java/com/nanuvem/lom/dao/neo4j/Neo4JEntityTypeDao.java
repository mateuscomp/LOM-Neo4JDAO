package com.nanuvem.lom.dao.neo4j;

import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.nanuvem.lom.api.EntityType;
import com.nanuvem.lom.api.dao.EntityTypeDao;

public class Neo4JEntityTypeDao implements EntityTypeDao {

	private Neo4JConnector connector;

	public Neo4JEntityTypeDao(Neo4JConnector connector) {
		this.connector = connector;
	}

	public EntityType create(EntityType entity) {
		Transaction tx = connector.iniciarTransacao();

		try {
			Node noEntityType = connector.getGraphDatabaseService()
					.createNode();
			noEntityType.setProperty("version", 0);
			noEntityType.setProperty("namespace", entity.getNamespace());
			noEntityType.setProperty("name", entity.getName());
			tx.success();

			entity.setId(noEntityType.getId());

			return entity;
		} catch (Exception e) {
			tx.finish();
			e.printStackTrace();
		}

		return null;
	}

	public List<EntityType> listAll() {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityType findById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<EntityType> listByFullName(String fragment) {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityType findByFullName(String fullName) {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityType update(EntityType entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public void delete(Long id) {
		// TODO Auto-generated method stub
	}

}

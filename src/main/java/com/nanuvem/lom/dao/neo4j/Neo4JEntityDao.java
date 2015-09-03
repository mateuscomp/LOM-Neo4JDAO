package com.nanuvem.lom.dao.neo4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import com.nanuvem.lom.api.Entity;
import com.nanuvem.lom.api.EntityType;
import com.nanuvem.lom.api.Property;
import com.nanuvem.lom.api.dao.EntityDao;
import com.nanuvem.lom.dao.neo4j.relation.Neo4JRelation;

public class Neo4JEntityDao implements EntityDao {

	private long autoIncrementId;
	private Neo4JConnector connector;

	private Neo4JEntityTypeDao entityTypeDao;

	public Neo4JEntityDao(Neo4JConnector connector,
			Neo4JEntityTypeDao entityTypeDao) {
		this.connector = connector;
		this.autoIncrementId = 0L;
		this.entityTypeDao = entityTypeDao;
	}

	@Override
	public Entity create(Entity entity) {
		Node entityTypeNode = this.entityTypeDao.findNodeById(entity
				.getEntityType().getId());

		try (Transaction tx = connector.iniciarTransacao()) {
			Node noEntity = connector.getGraphDatabaseService().createNode(
					NodeType.ENTITY);
			noEntity.setProperty("id", ++autoIncrementId);
			noEntity.setProperty("version", 0);
			tx.success();

			noEntity.createRelationshipTo(entityTypeNode,
					Neo4JRelation.IS_A_ENTITY_OF_ENTITY_TYPE);
			tx.success();

			entity.setId(autoIncrementId);
			entity.setVersion(0);
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Entity findEntityById(Long id) {
		Entity entity = null;

		String query = "MATCH (e:" + NodeType.ENTITY + " {id: " + id + "})-[r:"
				+ Neo4JRelation.IS_A_ENTITY_OF_ENTITY_TYPE + "]->(et:"
				+ NodeType.ENTITY_TYPE + ") return e, et";

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {

			if (result.hasNext()) {
				Map<String, Object> next = result.next();
				Node nodeE = (Node) next.get("e");
				Node nodeET = (Node) next.get("et");

				EntityType entityType = Neo4JEntityTypeDao
						.newEntityType(nodeET);
				entity = this.newEntity(nodeE);
				entity.setEntityType(entityType);
			}
		}
		List<Property> properties = Neo4JPropertyDAO
				.findPropertiesByEntity(entity);
		if (entity != null) {
			entity.setProperties(properties);
		}

		return entity;
	}

	private Entity newEntity(Node node) {
		Entity entity = new Entity();
		entity.setId((Long) node.getProperty("id"));
		entity.setVersion((Integer) node.getProperty("version"));

		return entity;
	}

	@Override
	public List<Entity> findEntitiesByEntityTypeId(Long entityTypeId) {
		List<Entity> entities = new LinkedList<Entity>();
		String query = "MATCH (e:" + NodeType.ENTITY + ")-[r:"
				+ Neo4JRelation.IS_A_ENTITY_OF_ENTITY_TYPE + "]->(et:"
				+ NodeType.ENTITY_TYPE + " {id: " + entityTypeId
				+ "}) return e, et";
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {
			if (result.hasNext()) {
				Map<String, Object> next = result.next();
				Node nodeE = (Node) next.get("e");
				Node nodeET = (Node) next.get("et");

				EntityType entityType = Neo4JEntityTypeDao
						.newEntityType(nodeET);
				Entity entity = this.newEntity(nodeE);
				entity.setEntityType(entityType);
				entities.add(entity);
			}
		}
		for (Entity entity : entities) {
			List<Property> properties = Neo4JPropertyDAO
					.findPropertiesByEntity(entity);
			if (entity != null) {
				entity.setProperties(properties);
			}
		}
		return entities;
	}

	@Override
	public Entity update(Entity entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Entity> findEntityByNameOfPropertiesTypeAndByValueOfProperties(
			String fullnameEntityType,
			Map<String, String> nameByPropertiesTypesAndValuesOfProperties) {
		// TODO Auto-generated method stub
		return null;
	}
}

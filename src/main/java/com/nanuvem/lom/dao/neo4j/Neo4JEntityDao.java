package com.nanuvem.lom.dao.neo4j;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;

import com.nanuvem.lom.api.Entity;
import com.nanuvem.lom.api.EntityType;
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

			entityTypeNode.createRelationshipTo(noEntity,
					Neo4JRelation.HAS_A_PROPERTY_TYPE);
			tx.success();

			entity.setId(autoIncrementId);
			entity.setVersion(0);
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Entity findEntityById(Long id) {
		Entity entity = null;

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						"MATCH (et:" + NodeType.ENTITY_TYPE + ")-[:"
								+ Neo4JRelation.HAS_A_ENTITY
								+ "]->(e) WHERE e.id=" + id + " return e, et")) {

			Iterator<Node> iterator = result.columnAs("e");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				entity = newEntity(node);
				break;
			}

			if (entity != null) {
				Iterator<Node> iterator2 = result.columnAs("et");
				for (Node node : IteratorUtil.asIterable(iterator2)) {
					entity.setEntityType(newEntityType(node));
					break;
				}
			}
		}

		return entity;
	}

	private EntityType newEntityType(Node node) {
		EntityType entityType = new EntityType();
		entityType.setId((Long) node.getProperty("id"));
		entityType.setVersion((Integer) node.getProperty("version"));
		entityType.setNamespace((String) node.getProperty("namespace"));
		entityType.setName((String) node.getProperty("name"));

		return entityType;
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

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						"MATCH (et:" + NodeType.ENTITY_TYPE + ")-[:"
								+ Neo4JRelation.HAS_A_ENTITY
								+ "]->(e) WHERE et.id=" + entityTypeId
								+ " return e, et")) {

			Iterator<Node> iterator = result.columnAs("e");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				entities.add(newEntity(node));
				break;
			}

			for (Entity entity : entities) {
				Iterator<Node> iterator2 = result.columnAs("et");
				for (Node node : IteratorUtil.asIterable(iterator2)) {
					entity.setEntityType(newEntityType(node));
					break;
				}
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

package com.nanuvem.lom.dao.neo4j;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;

import com.nanuvem.lom.api.EntityType;
import com.nanuvem.lom.api.dao.EntityTypeDao;

public class Neo4JEntityTypeDao implements EntityTypeDao {

	private long autoIncrementId;
	private Neo4JConnector connector;

	public Neo4JEntityTypeDao(Neo4JConnector connector) {
		this.autoIncrementId = 0L;
		this.connector = connector;
	}

	public EntityType create(EntityType entityType) {
		try (Transaction tx = connector.iniciarTransacao()) {
			Node noEntityType = connector.getGraphDatabaseService().createNode(
					NodeType.ENTITY_TYPE);
			noEntityType.setProperty("id", ++autoIncrementId);
			noEntityType.setProperty("version", 0);
			noEntityType.setProperty("namespace", entityType.getNamespace());
			noEntityType.setProperty("name", entityType.getName());
			tx.success();

			entityType.setId(autoIncrementId);
			return entityType;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<EntityType> listAll() {
		List<EntityType> entitiesTypes = new LinkedList<EntityType>();

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						"match (n:" + NodeType.ENTITY_TYPE + ") return n")) {

			Iterator<Node> iterator = result.columnAs("n");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				EntityType entityType = newEntityType(node);
				entitiesTypes.add(entityType);
			}
		}

		if (entitiesTypes.isEmpty()) {
			entitiesTypes = null;
		}
		return entitiesTypes;
	}

	public EntityType findById(Long id) {
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						"match (n:" + NodeType.ENTITY_TYPE + ") WHERE n.id = "
								+ String.valueOf(id) + " return n")) {

			Iterator<Node> iterator = result.columnAs("n");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				return newEntityType(node);
			}
		}
		return null;
	}

	public List<EntityType> listByFullName(String fragment) {
		List<EntityType> entitiesTypes = new LinkedList<EntityType>();

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						"MATCH (n:" + NodeType.ENTITY_TYPE
								+ ") WHERE (n.namespace + '.' + n.name) =~ '.*"
								+ fragment + ".*' return n")) {

			Iterator<Node> iterator = result.columnAs("n");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				EntityType entityType = newEntityType(node);
				entitiesTypes.add(entityType);
			}
		}
		if (entitiesTypes.isEmpty()) {
			entitiesTypes = null;
		}
		return entitiesTypes;
	}

	public EntityType findByFullName(String fullName) {
		String namespace = null;
		try {
			namespace = (fullName != null && !fullName.isEmpty()) ? fullName
					.substring(0, fullName.lastIndexOf(".")) : "";
		} catch (StringIndexOutOfBoundsException e) {
			namespace = null;
		}

		String name;
		try {
			name = (fullName != null && !fullName.isEmpty()) ? fullName
					.substring(fullName.lastIndexOf(".") + 1, fullName.length())
					: "";

		} catch (StringIndexOutOfBoundsException e) {
			name = null;
		}

		List<EntityType> entitiesTypes = new LinkedList<EntityType>();
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						"MATCH (n:" + NodeType.ENTITY_TYPE
								+ ") WHERE n.namespace = '" + namespace
								+ "' AND n.name = '" + name + "' return n")) {

			Iterator<Node> iterator = result.columnAs("n");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				EntityType entityType = newEntityType(node);
				entitiesTypes.add(entityType);
			}
		}

		if (entitiesTypes.isEmpty()) {
			return null;
		}
		return entitiesTypes.get(0);
	}

	public EntityType update(EntityType entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public void delete(Long id) {
		// TODO Auto-generated method stub
	}

	private EntityType newEntityType(Node node) {
		EntityType entityType = new EntityType();
		entityType.setId((Long) node.getProperty("id"));
		entityType.setVersion((Integer) node.getProperty("version"));
		entityType.setNamespace((String) node.getProperty("namespace"));
		entityType.setName((String) node.getProperty("name"));

		return entityType;
	}

	public Node findNodeById(Long id) {
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						"match (n:" + NodeType.ENTITY_TYPE + ") WHERE n.id = "
								+ String.valueOf(id) + " return n")) {

			Iterator<Node> iterator = result.columnAs("n");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				return node;
			}
		}
		return null;
	}
}

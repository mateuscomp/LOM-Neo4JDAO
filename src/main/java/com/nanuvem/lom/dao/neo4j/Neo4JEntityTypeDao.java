package com.nanuvem.lom.dao.neo4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;

import com.nanuvem.lom.api.EntityType;
import com.nanuvem.lom.api.MetadataException;
import com.nanuvem.lom.api.PropertyType;
import com.nanuvem.lom.api.dao.EntityTypeDao;
import com.nanuvem.lom.dao.neo4j.relation.Neo4JRelation;

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
			noEntityType.setProperty("namespace", entityType.getNamespace()
					.toLowerCase());
			noEntityType
					.setProperty("name", entityType.getName().toLowerCase());
			tx.success();

			entityType.setId(autoIncrementId);
			entityType.setVersion(0);
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
						"MATCH (et:" + NodeType.ENTITY_TYPE + ") return et")) {

			Iterator<Node> iterator = result.columnAs("et");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				EntityType entityType = newEntityType(node);
				entitiesTypes.add(entityType);
			}
		}

		return entitiesTypes;
	}

	public EntityType findById(Long id) {
		EntityType entityType = null;
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector
						.getGraphDatabaseService()
						.execute(
								"match (et:"
										+ NodeType.ENTITY_TYPE
										+ " {id: "
										+ id
										+ "}) OPTIONAL MATCH (et)-[r:"
										+ Neo4JRelation.IS_A_PROPERTY_TYPE_OF_ENTITY_TYPE
										+ "]-(pt) return et, pt")) {

			while (result.hasNext()) {
				Map<String, Object> next = result.next();
				Node nodeEt = (Node) next.get("et");
				if (entityType == null) {
					entityType = newEntityType(nodeEt);
				}

				Node propertyTypeNode = (Node) next.get("pt");
				if (propertyTypeNode != null) {
					PropertyType pt = Neo4JPropertyTypeDao
							.newPropertyType(propertyTypeNode);
					entityType.getPropertiesTypes().add(pt);
				}
			}
			tx.success();
		}
		return entityType;
	}

	public List<EntityType> listByFullName(String fragment) {
		List<EntityType> entitiesTypes = new LinkedList<EntityType>();

		String regex = ".*" + fragment.toLowerCase() + ".*";
		String query = "MATCH (et:" + NodeType.ENTITY_TYPE + ") "
				+ "WHERE et.namespace =~ {regex} OR et.name =~ {regex} OR (et.namespace + '.' + et.name) =~ {regex}"
				+ " return et ";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("regex", regex);
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query, params)) {

			Iterator<Node> iterator = result.columnAs("et");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				EntityType entityType = newEntityType(node);
				entitiesTypes.add(entityType);
			}
			tx.success();
		}
		return entitiesTypes;
	}

	// Trazer tbm os PropertyTypes
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

		EntityType entityType = null;
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector
						.getGraphDatabaseService()
						.execute(
								"MATCH (et:"
										+ NodeType.ENTITY_TYPE
										+ "{namespace: '"
										+ namespace.toLowerCase()
										+ "', name: '"
										+ name.toLowerCase()
										+ "'}) "
										+ "OPTIONAL MATCH (et)-[r:"
										+ Neo4JRelation.IS_A_PROPERTY_TYPE_OF_ENTITY_TYPE
										+ "]-(pt)" + " return et, pt")) {

			while (result.hasNext()) {
				Map<String, Object> next = result.next();
				Node nodeEt = (Node) next.get("et");
				if (entityType == null) {
					entityType = newEntityType(nodeEt);
				}

				Node propertyTypeNode = (Node) next.get("pt");
				if (propertyTypeNode != null) {
					PropertyType pt = Neo4JPropertyTypeDao
							.newPropertyType(propertyTypeNode);
					entityType.getPropertiesTypes().add(pt);
				}
			}

		}
		return entityType;
	}

	public EntityType update(EntityType entityType) {
		EntityType entityTypePersisted = this.findById(entityType.getId());

		if (entityTypePersisted == null) {
			throw new MetadataException("Invalid id for Entity "
					+ entityType.getNamespace() + "." + entityType.getName());
		}

		else if (entityTypePersisted.getVersion() > entityType.getVersion()) {
			throw new MetadataException(
					"Updating a deprecated version of Entity "
							+ entityTypePersisted.getNamespace()
							+ "."
							+ entityTypePersisted.getName()
							+ ". Get the Entity again to obtain the newest version and proceed updating.");
		}
		entityType.setVersion(entityTypePersisted.getVersion() + 1);

		String query = "MATCH (n:" + NodeType.ENTITY_TYPE + " {" + "id: "
				+ entityType.getId() + "}) " + " SET" + " n.namespace= '"
				+ entityType.getNamespace().toLowerCase() + "', "
				+ " n.name= '" + entityType.getName().toLowerCase() + "', "
				+ "n.version= " + entityType.getVersion() + " return n";
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {
			tx.success();
		}
		return findById(entityType.getId());
	}

	public void delete(Long id) {
		String query = "MATCH (et:" + NodeType.ENTITY_TYPE
				+ " {id: {id}}) DELETE et";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query, params)) {
			tx.success();
		}
	}

	public static EntityType newEntityType(Node node) {
		EntityType entityType = new EntityType();
		entityType.setId((Long) node.getProperty("id"));

		try {
			entityType.setVersion((Integer) node.getProperty("version"));
		} catch (Exception e) {
			Long version = (Long) node.getProperty("version");
			entityType.setVersion(Integer.parseInt(version.toString()));
		}
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
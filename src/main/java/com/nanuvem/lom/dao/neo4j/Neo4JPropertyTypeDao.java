package com.nanuvem.lom.dao.neo4j;

import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;

import com.nanuvem.lom.api.EntityType;
import com.nanuvem.lom.api.PropertyType;
import com.nanuvem.lom.api.Type;
import com.nanuvem.lom.api.dao.PropertyTypeDao;
import com.nanuvem.lom.dao.neo4j.relation.Neo4JRelation;

public class Neo4JPropertyTypeDao implements PropertyTypeDao {

	private Neo4JConnector connector;
	private long autoIncrementId;

	private Neo4JEntityTypeDao entityTypeDao;

	public Neo4JPropertyTypeDao(Neo4JConnector connector,
			Neo4JEntityTypeDao entityTypeDao) {
		this.connector = connector;
		this.autoIncrementId = 0L;
		this.entityTypeDao = entityTypeDao;
	}

	@Override
	public PropertyType create(PropertyType propertyType) {
		Node entityTypeNode = this.entityTypeDao.findNodeById(propertyType
				.getEntityType().getId());

		try (Transaction tx = connector.iniciarTransacao()) {
			Node noPropertyType = connector.getGraphDatabaseService()
					.createNode(NodeType.PROPERTY_TYPE);

			noPropertyType.setProperty("id", ++autoIncrementId);
			noPropertyType.setProperty("version", 0);
			noPropertyType.setProperty("sequence", propertyType.getSequence());
			noPropertyType.setProperty("name", propertyType.getName());
			noPropertyType.setProperty("type", propertyType.getType()
					.toString());
			noPropertyType.setProperty("configuration",
					propertyType.getConfiguration());
			tx.success();

			entityTypeNode.createRelationshipTo(noPropertyType,
					Neo4JRelation.HAS_A_PROPERTY_TYPE);
			tx.success();

			propertyType.setId(autoIncrementId);
			return propertyType;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public PropertyType findPropertyTypeById(Long id) {
		PropertyType propertyType = null;

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						"MATCH (et:" + NodeType.ENTITY_TYPE + ")-[:"
								+ Neo4JRelation.HAS_A_PROPERTY_TYPE
								+ "]->(pt) return pt, et")) {

			Iterator<Node> iterator = result.columnAs("pt");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				propertyType = newPropertyType(node);
				break;
			}

			if (propertyType != null) {
				Iterator<Node> iterator2 = result.columnAs("et");
				for (Node node : IteratorUtil.asIterable(iterator2)) {
					propertyType.setEntityType(newEntityType(node));
					break;
				}
			}
		}
		return propertyType;
	}

	private EntityType newEntityType(Node node) {
		EntityType entityType = new EntityType();
		entityType.setId((Long) node.getProperty("id"));
		entityType.setVersion((Integer) node.getProperty("version"));
		entityType.setNamespace((String) node.getProperty("namespace"));
		entityType.setName((String) node.getProperty("name"));

		return entityType;
	}

	private PropertyType newPropertyType(Node node) {
		PropertyType propertyType = new PropertyType();
		propertyType.setId((Long) node.getProperty("id"));
		propertyType.setVersion((Integer) node.getProperty("version"));
		propertyType.setName((String) node.getProperty("name"));
		propertyType.setConfiguration((String) node
				.getProperty("configuration"));
		propertyType.setType(Type.getType((String) node.getProperty("type")));
		propertyType.setSequence((Integer) node.getProperty("sequence"));

		return propertyType;
	}

	@Override
	public PropertyType findPropertyTypeByNameAndEntityTypeFullName(
			String propertyTypeName, String entityTypeFullName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PropertyType> findPropertiesTypesByFullNameEntityType(
			String fullnameEntityType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertyType update(PropertyType propertyType) {
		// TODO Auto-generated method stub
		return null;
	}

	public Node findNodeById(Long id) {
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						"match (n:" + NodeType.PROPERTY_TYPE
								+ ") WHERE n.id = " + String.valueOf(id)
								+ " return n")) {

			Iterator<Node> iterator = result.columnAs("n");
			for (Node node : IteratorUtil.asIterable(iterator)) {
				return node;
			}
		}
		return null;
	}

}

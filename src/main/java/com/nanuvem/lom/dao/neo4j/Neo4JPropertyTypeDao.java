package com.nanuvem.lom.dao.neo4j;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

			noPropertyType.createRelationshipTo(entityTypeNode,
					Neo4JRelation.IS_A_PROPERTY_TYPE_OF_ENTITY_TYPE);
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

		String query = "MATCH (pt:PROPERTY_TYPE { id: " + id
				+ "})--(et) RETURN pt, et";
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {

			if (result.hasNext()) {
				Map<String, Object> next = result.next();
				Node nodePT = (Node) next.get("pt");
				Node nodeET = (Node) next.get("et");

				EntityType entityType = Neo4JEntityTypeDao
						.newEntityType(nodeET);
				propertyType = newPropertyType(nodePT);
				propertyType.setEntityType(entityType);
			}
		}
		return propertyType;
	}

	private PropertyType newPropertyType(Node node) {
		PropertyType propertyType = new PropertyType();
		propertyType.setId((Long) node.getProperty("id"));
		
		try {
			propertyType.setVersion((Integer) node.getProperty("version"));
		} catch (Exception e) {
			Long version = (Long) node.getProperty("version");
			propertyType.setVersion(Integer.parseInt(version.toString()));
		}
		
		propertyType.setName((String) node.getProperty("name"));
		propertyType.setConfiguration((String) node
				.getProperty("configuration"));
		propertyType.setType(Type.getType((String) node.getProperty("type")));
		
		try {
			propertyType.setSequence((Integer) node.getProperty("version"));
		} catch (Exception e) {
			Long version = (Long) node.getProperty("sequence");
			propertyType.setSequence(Integer.parseInt(version.toString()));
		}

		return propertyType;
	}

	@Override
	public PropertyType findPropertyTypeByNameAndEntityTypeFullName(
			String propertyTypeName, String entityTypeFullName) {

		String namespace = entityTypeFullName != null ? entityTypeFullName
				.substring(0, entityTypeFullName.lastIndexOf(".")) : "";
		String name = entityTypeFullName != null ? entityTypeFullName
				.substring(entityTypeFullName.lastIndexOf(".") + 1,
						entityTypeFullName.length()) : "";

		PropertyType propertyType = null;

		String query = "MATCH (pt:PROPERTY_TYPE {name: '" + propertyTypeName
				+ "'})-[r:" + Neo4JRelation.IS_A_PROPERTY_TYPE_OF_ENTITY_TYPE
				+ "]->(et:ENTITY_TYPE {namespace: '" + namespace + "', name: '"
				+ name + "'}) RETURN pt, et";

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {

			if (result.hasNext()) {
				Map<String, Object> next = result.next();
				Node nodePT = (Node) next.get("pt");
				Node nodeET = (Node) next.get("et");

				EntityType entityType = Neo4JEntityTypeDao
						.newEntityType(nodeET);
				propertyType = newPropertyType(nodePT);
				propertyType.setEntityType(entityType);
			}
		}
		return propertyType;
	}

	@Override
	public List<PropertyType> findPropertiesTypesByFullNameEntityType(
			String fullnameEntityType) {

		String namespace = fullnameEntityType != null ? fullnameEntityType
				.substring(0, fullnameEntityType.lastIndexOf(".")) : "";
		String name = fullnameEntityType != null ? fullnameEntityType
				.substring(fullnameEntityType.lastIndexOf(".") + 1,
						fullnameEntityType.length()) : "";

		List<PropertyType> propertiesTypes = new LinkedList<PropertyType>();

		String query = "MATCH (pt:PROPERTY_TYPE)-[r:"
				+ Neo4JRelation.IS_A_PROPERTY_TYPE_OF_ENTITY_TYPE
				+ "]->(et:ENTITY_TYPE {namespace: '" + namespace + "', name: '"
				+ name + "'}) RETURN pt, et";

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {

			while (result.hasNext()) {
				Map<String, Object> next = result.next();
				Node nodePT = (Node) next.get("pt");
				Node nodeET = (Node) next.get("et");

				EntityType entityType = Neo4JEntityTypeDao
						.newEntityType(nodeET);
				PropertyType propertyType = new PropertyType();
				propertyType = newPropertyType(nodePT);
				propertyType.setEntityType(entityType);
				propertiesTypes.add(propertyType);
			}
		}
		return propertiesTypes;
	}

	@Override
	public PropertyType update(PropertyType propertyType) {
		String query = "MATCH (n:" + NodeType.PROPERTY_TYPE + " {" + "id: "
				+ propertyType.getId() + "}) " + " SET" + " n.name= '"
				+ propertyType.getName() + "', " + "n.version= "
				+ propertyType.getVersion() + ", " + "n.sequence= "
				+ propertyType.getSequence() + ", " + "n.type= '"
				+ propertyType.getType() + "', " + "n.configuration= '"
				+ propertyType.getConfiguration() + "' " + "return n";

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {
			tx.success();
		}
		return this.findPropertyTypeById(propertyType.getId());
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

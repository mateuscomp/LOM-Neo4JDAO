package com.nanuvem.lom.dao.neo4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import com.nanuvem.lom.api.Entity;
import com.nanuvem.lom.api.Property;
import com.nanuvem.lom.api.PropertyType;
import com.nanuvem.lom.api.dao.PropertyDao;
import com.nanuvem.lom.dao.neo4j.relation.Neo4JRelation;

public class Neo4JPropertyDAO implements PropertyDao {

	private long autoIncrementId;
	private Neo4JConnector connector;

	private Neo4JEntityDao entityDao;
	private Neo4JPropertyTypeDao propertyTypeDao;

	public Neo4JPropertyDAO(Neo4JConnector connector, Neo4JEntityDao entityDao,
			Neo4JPropertyTypeDao propertyTypeDao) {
		this.connector = connector;
		this.entityDao = entityDao;
		this.propertyTypeDao = propertyTypeDao;
		this.autoIncrementId = 0L;
	}

	@Override
	public Property create(Property property) {
		Node entityNode = this.entityDao.findNodeById(property.getEntity()
				.getId());
		Node propertyTypeNode = this.propertyTypeDao.findNodeById(property
				.getPropertyType().getId());

		try (Transaction tx = connector.iniciarTransacao()) {
			Node noProperty = connector.getGraphDatabaseService().createNode(
					NodeType.PROPERTY);
			noProperty.setProperty("id", ++autoIncrementId);
			noProperty.setProperty("version", 0);
			noProperty.setProperty("value", property.getValue());
			tx.success();

			noProperty.createRelationshipTo(entityNode,
					Neo4JRelation.IS_A_PROPERTY_OF_ENTITY);
			tx.success();

			noProperty.createRelationshipTo(propertyTypeNode,
					Neo4JRelation.HAS_A_VALUE_FOR_PROPERTY_TYPE);
			tx.success();
		}

		property.setId(autoIncrementId);
		property.setVersion(0);
		return property;
	}

	@Override
	public Property update(Property property) {
		String query = "MATCH (p:" + NodeType.PROPERTY + " {" + "id: "
				+ property.getId() + "}) " + " SET" + " p.version= '"
				+ property.getVersion() + " return p";
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {
			tx.success();
		}
		return findById(property.getId());
	}

	public Property findById(Long id) {
		String query = "MATCH " + "(p:" + NodeType.PROPERTY + ")-[er]->(e:"
				+ NodeType.ENTITY + "), " + "(p:" + NodeType.PROPERTY
				+ ")-[pr]->(pt:" + NodeType.PROPERTY_TYPE + ")"
				+ " WHERE p.id=" + id + " RETURN p, e, pt";
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {

			if (result.hasNext()) {
				Map<String, Object> next = result.next();

				Node nodeE = (Node) next.get("e");
				Entity entity = this.entityDao.newEntity(nodeE);

				Node nodePT = (Node) next.get("pt");
				PropertyType propertyType = this.propertyTypeDao
						.newPropertyType(nodePT);

				Node nodeP = (Node) next.get("p");
				Property property = newProperty(nodeP);
				property.setEntity(entity);
				property.setPropertyType(propertyType);

				return property;
			}
		}

		return null;
	}

	private static Property newProperty(Node node) {
		Property property = new Property();
		property.setId((Long) node.getProperty("id"));
		try {
			property.setVersion((Integer) node.getProperty("version"));
		} catch (Exception e) {
			Long version = (Long) node.getProperty("version");
			property.setVersion(Integer.parseInt(version.toString()));
		}
		property.setValue((String) node.getProperty("value"));

		return property;
	}

	public static List<Property> findPropertiesByEntity(Entity entity,
			Neo4JConnector connector, Neo4JEntityDao entityDao,
			Neo4JPropertyTypeDao propertyTypeDao) {
		String query = "MATCH " + "(p:" + NodeType.PROPERTY + ")-[er]->(e:"
				+ NodeType.ENTITY + "), " + "(p:" + NodeType.PROPERTY
				+ ")-[pr]->(pt:" + NodeType.PROPERTY_TYPE + ")"
				+ " WHERE e.id=" + entity.getId() + " RETURN p, e, pt";

		List<Property> properties = new LinkedList<Property>();
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {

			while (result.hasNext()) {
				Map<String, Object> next = result.next();

				Node nodeE = (Node) next.get("e");
				Entity entityF = entityDao.newEntity(nodeE);

				Node nodePT = (Node) next.get("pt");
				PropertyType propertyType = propertyTypeDao
						.newPropertyType(nodePT);

				Node nodeP = (Node) next.get("p");
				Property property = newProperty(nodeP);
				property.setEntity(entityF);
				property.setPropertyType(propertyType);

				properties.add(property);
			}
		}
		return properties;
	}
}

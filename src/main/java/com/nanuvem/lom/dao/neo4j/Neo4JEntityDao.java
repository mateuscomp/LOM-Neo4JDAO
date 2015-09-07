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
import com.nanuvem.lom.api.PropertyType;
import com.nanuvem.lom.api.dao.EntityDao;
import com.nanuvem.lom.dao.neo4j.relation.Neo4JRelation;

public class Neo4JEntityDao implements EntityDao {

	private long autoIncrementId;
	private Neo4JConnector connector;

	private Neo4JEntityTypeDao entityTypeDao;
	private Neo4JPropertyTypeDao propertyTypeDao;

	public Neo4JEntityDao(Neo4JConnector connector,
			Neo4JEntityTypeDao entityTypeDao,
			Neo4JPropertyTypeDao propertyTypeDao) {
		this.connector = connector;
		this.autoIncrementId = 0L;
		this.entityTypeDao = entityTypeDao;
		this.propertyTypeDao = propertyTypeDao;
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
		List<Property> properties = Neo4JPropertyDAO.findPropertiesByEntity(
				entity, connector, this, propertyTypeDao);
		if (entity != null) {
			entity.setProperties(properties);
		}

		return entity;
	}

	public Entity newEntity(Node node) {
		Entity entity = new Entity();
		entity.setId((Long) node.getProperty("id"));
		try {
			entity.setVersion((Integer) node.getProperty("version"));
		} catch (Exception e) {
			Long value = (Long) node.getProperty("version");
			entity.setVersion(Integer.parseInt(value.toString()));
		}
		return entity;
	}

	@Override
	public List<Entity> findEntitiesByEntityTypeId(Long entityTypeId) {
		List<Entity> entities = new LinkedList<Entity>();
		String query = "MATCH (et:" + NodeType.ENTITY_TYPE 
				+ ")-[r:"+Neo4JRelation.IS_A_ENTITY_OF_ENTITY_TYPE+"]-(e:ENTITY) "
				+ " OPTIONAL MATCH(e:ENTITY)-[r2:"+Neo4JRelation.IS_A_PROPERTY_OF_ENTITY+"]-(p:"+NodeType.PROPERTY+") WHERE et.id= " + entityTypeId
				+ " return e, et, p";
		
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {
			
			EntityType entityType = null;
			while (result.hasNext()) {
				Map<String, Object> next = result.next();
				
				Node nodeEt = (Node) next.get("et");
				if (entityType == null) {
					entityType = Neo4JEntityTypeDao.newEntityType(nodeEt);
				}

				Node entityNode = (Node) next.get("e");
				if (entityNode != null) {
					Entity entity = newEntity(entityNode);
					entity.setEntityType(entityType);
					entities.add(entity);
				}
			}
			
			for(Entity entity : entities){
				entity.setProperties(this.getProperties(entity));
			}
		}
		return entities;
	}

	@Override
	public Entity update(Entity entity) {
		entity.setVersion(entity.getVersion() + 1);
		String query = "MATCH (e:" + NodeType.ENTITY + " {" + "id: "
				+ entity.getId() + "}) " + " SET" + " e.version= "
				+ entity.getVersion() + " return e";
		
		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {
			tx.success();
		}
		
		return findEntityById(entity.getId());
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

	public Node findNodeById(Long id) {
		String query = "MATCH (e:" + NodeType.ENTITY + " {id: " + id + "})-[r:"
				+ Neo4JRelation.IS_A_ENTITY_OF_ENTITY_TYPE + "]->(et:"
				+ NodeType.ENTITY_TYPE + ") return e, et";

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {

			if (result.hasNext()) {
				Map<String, Object> next = result.next();
				return (Node) next.get("e");
			}
		}
		return null;
	}
	
	private List<Property> getProperties(Entity entity) {
		List<Property> properties = new LinkedList<Property>();
	
		String query = "MATCH (e:" + NodeType.ENTITY + " {id: " + entity.getId() + "})-[r:"
				+ Neo4JRelation.IS_A_PROPERTY_OF_ENTITY + "]-(p:"
				+ NodeType.PROPERTY + ") return e, p";

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {
			
			Entity entityFound = null;
			while (result.hasNext()) {
				Map<String, Object> next = result.next();
				Node nodeE = (Node) next.get("e");
				entityFound = newEntity(nodeE);
				
				Node nodeP = (Node) next.get("p");
				Property property = Neo4JPropertyDAO.newProperty(nodeP);
				properties.add(property);
			}
		}
		
		for(Property property : properties){
			property.setPropertyType(this.getPropertyType(property));
		}
		
		return properties;
	}	

	private PropertyType getPropertyType(Property property) {
		String query = "MATCH (p:" + NodeType.PROPERTY + " {id: " + property.getId() + "})-[r:"+ Neo4JRelation.HAS_A_VALUE_FOR_PROPERTY_TYPE + "]-(pt:" + NodeType.PROPERTY_TYPE + ") return pt, p";

		try (Transaction tx = connector.iniciarTransacao();
				Result result = connector.getGraphDatabaseService().execute(
						query)) {
			
//			System.out.println(result.resultAsString());
		}	
		return null;
	}
}
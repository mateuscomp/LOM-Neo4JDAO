package com.nanuvem.lom.dao.neo4j;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nanuvem.lom.api.Entity;
import com.nanuvem.lom.api.EntityType;
import com.nanuvem.lom.api.Property;
import com.nanuvem.lom.api.PropertyType;
import com.nanuvem.lom.api.Type;

public class AppTest {

	private Neo4JConnector connector;
	private Neo4JEntityTypeDao entityTypeDao;
	private Neo4JPropertyTypeDao propertyTypeDao;
	private Neo4JEntityDao entityDao;
	private Neo4JPropertyDAO propertyDao;

	@Before
	public void init() {
		connector = new Neo4JConnector();
		entityTypeDao = new Neo4JEntityTypeDao(connector);
		propertyTypeDao = new Neo4JPropertyTypeDao(connector, entityTypeDao);
		entityDao = new Neo4JEntityDao(connector, entityTypeDao,
				propertyTypeDao);
		propertyDao = new Neo4JPropertyDAO(connector, entityDao,
				propertyTypeDao);
	}

	@After
	public void after() {
		connector.drop();
	}

	@Test
	public void routine01() {
		System.out.println("========= Creating =========");
		for (int i = 0; i < 2; i++) {
			EntityType entityType = new EntityType();
			entityType.setNamespace("Fernando");
			entityType.setName("Mateus" + i);
			entityType.setVersion(0);
			entityTypeDao.create(entityType);

			System.out.println("EntityType: id = " + entityType.getId());

			for (int j = 0; j < 2; j++) {
				PropertyType pt = new PropertyType();
				pt.setEntityType(entityType);
				pt.setSequence(0);
				pt.setName("propertyType" + j);
				pt.setType(Type.TEXT);
				pt.setConfiguration("\"{mandatory\" : true}");
				propertyTypeDao.create(pt);

				System.out.println("PropertyType: id = " + pt.getId());
			}
			System.out.println("");
		}

		System.out.println("========= CONSULTA 1 =========");
		EntityType entityType = entityTypeDao.findById(1L);
		System.out.println(entityType);
		System.out.println("\n");

		System.out.println("========= CONSULTA 2 =========");
		List<EntityType> resultado = entityTypeDao.listByFullName("ateu");
		if (resultado != null) {
			for (EntityType et : resultado) {
				System.out.println(et);
			}
		}
		System.out.println("\n");

		System.out.println("========= CONSULTA 3 =========");
		System.out.println(entityTypeDao.findByFullName("Fernando.Mateus0"));
		System.out.println("\n");

		System.out.println("========= Update EntityType=========");
		EntityType et = entityTypeDao.findByFullName("Fernando.Mateus0");
		et.setNamespace("Rodrigo");
		et.setName("Vilar");
		EntityType etUpdate = entityTypeDao.update(et);
		System.out.println(etUpdate);
		System.out.println("\n");

		System.out.println("\n");
		System.out.println("========= CONSULTA 4 =========");
		System.out.println(propertyTypeDao.findPropertyTypeById(2L));

		System.out.println("\n");
		System.out.println("========= CONSULTA 5 =========");
		System.out.println(propertyTypeDao
				.findPropertyTypeByNameAndEntityTypeFullName("propertyType1",
						"Rodrigo.Vilar"));

		System.out.println("\n");
		System.out.println("========= CONSULTA 6 =========");
		List<PropertyType> pts = propertyTypeDao
				.findPropertiesTypesByFullNameEntityType("Rodrigo.Vilar");
		for (PropertyType pt : pts) {
			System.out.println(pt);
		}
		System.out.println("\n");

		System.out.println("========= Update EntityType=========");
		PropertyType propertyType = propertyTypeDao
				.findPropertyTypeByNameAndEntityTypeFullName("propertyType1",
						"Rodrigo.Vilar");
		propertyType.setConfiguration("{\"mandatory\": false}");
		System.out.println(propertyTypeDao.update(propertyType));
		System.out.println("\n");

		System.out.println("========= Create Entity=========");
		Entity entity = new Entity();
		entity.setEntityType(entityTypeDao.findById(1L));
		System.out.println(entityDao.create(entity));

		System.out.println("\n");
		System.out.println("========= CONSULTA 7 =========");
		System.out.println(entityDao.findEntityById(1L));

		System.out.println("\n");
		System.out.println("========= CONSULTA 8 =========");
		List<Entity> entities = entityDao.findEntitiesByEntityTypeId(1L);
		for (Entity ent : entities) {
			System.out.println(ent);
		}

		System.out.println("\n");
		System.out.println("========= Update Entity =========");
		Entity eUpdate = entityDao.findEntityById(1L);
		eUpdate.setVersion(5);
		System.out.println(entityDao.update(eUpdate));

		System.out.println("\n");
		System.out.println("========= Create Property =========");
		Property property = new Property();
		property.setEntity(entityDao.findEntityById(1L));
		property.setPropertyType(propertyTypeDao.findPropertyTypeById(1L));
		property.setValue("Value of property");
		System.out.println(propertyDao.create(property));

		System.out.println("\n");
		System.out.println("========= CONSULTA 9 =========");
		System.out.println(propertyDao.findById(1L));

		connector.finalizarTransacao();
		
	}
}
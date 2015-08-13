package com.nanuvem.lom.dao.neo4j;

import java.util.List;

import org.junit.Test;

import com.nanuvem.lom.api.EntityType;
import com.nanuvem.lom.api.PropertyType;
import com.nanuvem.lom.api.Type;

public class AppTest {

	@Test
	public void persistirUmEntityType() {
		Neo4JConnector connector = new Neo4JConnector();
		Neo4JEntityTypeDao entityTypeDao = new Neo4JEntityTypeDao(connector);
		Neo4JPropertyTypeDao propertyTypeDao = new Neo4JPropertyTypeDao(connector, entityTypeDao);

		System.out.println("========= Creating =========");
		for (int i = 0; i < 10; i++) {
			EntityType entityType = new EntityType();
			entityType.setNamespace("Fernando");
			entityType.setName("Mateus");
			entityType.setVersion(0);
			entityTypeDao.create(entityType);
			
			System.out.println("EntityType: id = " + entityType.getId());
			
			for(int j = 0; j < 10; j++){
				PropertyType pt = new PropertyType();
				pt.setEntityType(entityType);
				pt.setSequence(0);
				pt.setName("propertyType " + j);
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
		for (EntityType et : resultado) {
			System.out.println(et);
		}
		System.out.println("\n");

		System.out.println("========= CONSULTA 3 =========");
		System.out.println(entityTypeDao.findByFullName("Fernando.Mateus"));

		System.out.println("\n");
		System.out.println("========= CONSULTA 4 =========");
		System.out.println(propertyTypeDao.findPropertyTypeById(4L));
		
		connector.finalizarTransacao();
	}
}
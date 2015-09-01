package com.nanuvem.lom.dao.neo4j;

import java.util.List;

import org.junit.Test;

import com.nanuvem.lom.api.EntityType;

public class AppTest {

	@Test
	public void persistirUmEntityType() {
		Neo4JConnector connector = new Neo4JConnector();
		Neo4JEntityTypeDao entityTypeDao = new Neo4JEntityTypeDao(connector);
		Neo4JPropertyTypeDao propertyTypeDao = new Neo4JPropertyTypeDao(
				connector, entityTypeDao);

//		System.out.println("========= Creating =========");
//		for (int i = 0; i < 1; i++) {
//			EntityType entityType = new EntityType();
//			entityType.setNamespace("Fernando");
//			entityType.setName("Mateus" + i);
//			entityType.setVersion(0);
//			entityTypeDao.create(entityType);
//
//			System.out.println("EntityType: id = " + entityType.getId());
//
//			for (int j = 0; j < 1; j++) {
//				PropertyType pt = new PropertyType();
//				pt.setEntityType(entityType);
//				pt.setSequence(0);
//				pt.setName("propertyType " + j);
//				pt.setType(Type.TEXT);
//				pt.setConfiguration("\"{mandatory\" : true}");
//				propertyTypeDao.create(pt);
//
//				System.out.println("PropertyType: id = " + pt.getId());
//			}
//			System.out.println("");
//		}

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
		
		System.out.println("========= Update =========");
		EntityType et = entityTypeDao.findByFullName("Fernando.Mateus0");
		et.setNamespace("Rodrigo");
		et.setName("Vilar");
		EntityType etUpdate = entityTypeDao.update(et);
		System.out.println(etUpdate);
		System.out.println("\n");
		

		System.out.println("\n");
		System.out.println("========= CONSULTA 4 =========");
		System.out.println(propertyTypeDao.findPropertyTypeById(1L));

		System.out.println("\n");
		System.out.println("========= CONSULTA 5 =========");
		System.out.println(propertyTypeDao
				.findPropertyTypeByNameAndEntityTypeFullName("propertyType 0",
						"Fernando.Mateus0"));

		connector.finalizarTransacao();
	}
}
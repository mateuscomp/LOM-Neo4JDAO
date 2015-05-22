package com.nanuvem.lom.dao.neo4j;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.nanuvem.lom.api.Type;
import com.nanuvem.lom.dao.neo4j.relation.Neo4JRelation;

public class AppTest {

	private GraphDatabaseService db;

	@Before
	public void init() {
		String separator = System.getProperty("file.separator");
		this.db = new EmbeddedGraphDatabase(System.getProperty("user.home")
				+ separator + "neo4j_database");
	}

	public Node persistirUmEntityType(String namespace, String name) {
		Transaction tx = db.beginTx();

		try {
			Node noEntityType = db.createNode();
			// noEntityType.setProperty("id", 1L);
			noEntityType.setProperty("version", 0);
			noEntityType.setProperty("namespace", namespace);
			noEntityType.setProperty("name", name);
			tx.success();

			System.out.println("ID gerado para o EntityType: "
					+ noEntityType.getId());
			return noEntityType;
		} catch (Exception e) {
			e.printStackTrace();
			tx.finish();
		}
		return null;
	}

	private Node persistirUmPropertyType(String name, Type type,
			String configuration) {
		Transaction tx = db.beginTx();

		try {
			Node noPropertyType = db.createNode();
			// noEntityType.setProperty("id", 1L);
			noPropertyType.setProperty("version", 0);
			noPropertyType.setProperty("name", name);
			noPropertyType.setProperty("type", type.toString());
			noPropertyType.setProperty("configuration", configuration);
			tx.success();

			System.out.println("ID gerado para o PropertyType: "
					+ noPropertyType.getId());

			return noPropertyType;
		} catch (Exception e) {
			tx.finish();
			e.printStackTrace();
		}
		return null;
	}

	@Test
	public void verificarCriacaoDeAOMModel() {
		Node noEntityType = persistirUmEntityType("abc", "a");
		Node noPropertyType = persistirUmPropertyType("pa", Type.TEXT,
				"{\"mandatory\" : true}");

		Transaction tx = db.beginTx();
		try {
			Relationship rel = noPropertyType.createRelationshipTo(
					noEntityType, Neo4JRelation.TEM_UM);

			tx.success();

			System.out.println(rel.getId());
		} catch (Exception e) {
			tx.finish();
			e.printStackTrace();
		}
	}

	@After
	public void finish() {
		this.db.shutdown();
	}
}

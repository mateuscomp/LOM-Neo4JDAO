package com.nanuvem.lom.dao.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class Neo4JConnector {

	private GraphDatabaseService graphDatabaseService;

	public Neo4JConnector() {
		String separator = System.getProperty("file.separator");

		this.graphDatabaseService = new EmbeddedGraphDatabase(
				System.getProperty("user.home") + separator + "neo4j_database");
	}

	public Transaction iniciarTransacao() {
		return this.graphDatabaseService.beginTx();
	}

	public GraphDatabaseService getGraphDatabaseService() {
		return graphDatabaseService;
	}

}

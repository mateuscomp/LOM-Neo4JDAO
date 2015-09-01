package com.nanuvem.lom.dao.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class Neo4JConnector {

	private static final String DB_PATH = "C:\\Users\\Mateus\\Documents\\Neo4j\\neo4j-store";
	
	private GraphDatabaseService graphDatabaseService;

	public Neo4JConnector() {
		this.graphDatabaseService = new GraphDatabaseFactory()
				.newEmbeddedDatabase(DB_PATH);
	}

	public Transaction iniciarTransacao() {
		return this.graphDatabaseService.beginTx();
	}
	
	public void finalizarTransacao(){
		this.graphDatabaseService.shutdown();
	}

	public GraphDatabaseService getGraphDatabaseService() {
		return graphDatabaseService;
	}

}

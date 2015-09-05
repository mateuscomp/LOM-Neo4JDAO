package com.nanuvem.lom.dao.neo4j;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

public class Neo4JConnector {

	private String DB_PATH;

	private GraphDatabaseService graphDatabaseService;

	public Neo4JConnector() {
		DB_PATH = this.createDbPathDirectory();

		this.graphDatabaseService = new GraphDatabaseFactory()
				.newEmbeddedDatabase(DB_PATH);
	}

	private String createDbPathDirectory() {
		String homeOfUser = System.getProperty("user.home");
		String separator = System.getProperty("file.separator");

		String urlDirectoryDB = homeOfUser + separator + "neo4J_db";

		File file = new File(urlDirectoryDB);
		if (!file.exists()) {
			file.mkdir();
		}

		return urlDirectoryDB;
	}

	public Transaction iniciarTransacao() {
		return this.graphDatabaseService.beginTx();
	}

	public void finalizarTransacao() {
		this.graphDatabaseService.shutdown();
	}

	public GraphDatabaseService getGraphDatabaseService() {
		return graphDatabaseService;
	}

	public void dropDatabase() {
		this.graphDatabaseService.shutdown();
//		graphDatabaseService = null;
		try {
			FileUtils.deleteRecursively(new File(DB_PATH));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

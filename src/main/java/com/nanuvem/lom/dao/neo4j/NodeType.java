package com.nanuvem.lom.dao.neo4j;

import org.neo4j.graphdb.Label;

public enum NodeType implements Label{
	ENTITY_TYPE, PROPERTY_TYPE;
}

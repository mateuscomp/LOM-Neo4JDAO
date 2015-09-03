package com.nanuvem.lom.dao.neo4j.relation;

import org.neo4j.graphdb.RelationshipType;

public enum Neo4JRelation implements RelationshipType {
	IS_A_PROPERTY_TYPE_OF_ENTITY_TYPE, IS_A_ENTITY_OF_ENTITY_TYPE, IS_A_PROPERTY_OF_ENTITY, HAS_A_VALUE_FOR_PROPERTY_TYPE;
}

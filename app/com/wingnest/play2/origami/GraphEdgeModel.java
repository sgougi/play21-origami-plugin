/*
 * Copyright since 2013 Shigeru GOUGI (sgougi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wingnest.play2.origami;

import java.util.List;

import javax.persistence.Transient;

import com.wingnest.play2.origami.plugin.utils.GraphDBPropertyUtils;
import com.wingnest.play2.origami.plugin.utils.GraphDBPropertyUtils.Property;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;

public abstract class GraphEdgeModel<OUT extends GraphVertexModel, IN extends GraphVertexModel> extends GraphModel {

	@Transient
	private OUT outVertex = null;
	@Transient
	private IN inVertex = null;

	public <T extends OUT, U extends IN> void setVertexes(final T outVertex, final U inVertex) {
		if ( outVertex == null || inVertex == null )
			throw new IllegalArgumentException("outVertex and inVertex must not be null");
		this.outVertex = outVertex;
		this.inVertex = inVertex;
		final ODocument doc = getDocument();
		doc.field(OGraphDatabase.EDGE_FIELD_OUT, this.outVertex.getDocument());
		doc.field(OGraphDatabase.EDGE_FIELD_IN, this.inVertex.getDocument());
	}

	public <T extends OUT> void setOutVertex(final T outVertex) {
		if ( outVertex == null )
			throw new IllegalArgumentException("outVertex must not be null");
		this.outVertex = outVertex;
		if ( outVertex != null && this.inVertex != null ) {
			setVertexes(outVertex, this.inVertex);
		}
	}

	public <T extends IN> void setInVertex(final T inVertex) {
		if ( inVertex == null )
			throw new IllegalArgumentException("inVertex must not be null");
		this.inVertex = inVertex;
		if ( inVertex != null && this.outVertex != null ) {
			setVertexes(this.outVertex, inVertex);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends IN> T getInVertex() {
		if ( inVertex != null )
			return (T) inVertex;
		final ODocument inDoc = getDocument().field(OGraphDatabase.EDGE_FIELD_IN);
		if ( inDoc == null )
			return null;
		inVertex = GraphDB.documentToModel(inDoc);
		return (T) inVertex;
	}

	@SuppressWarnings("unchecked")
	public <T extends OUT> T getOutVertex() {
		if ( outVertex != null )
			return (T) outVertex;
		final ODocument outDoc = getDocument().field(OGraphDatabase.EDGE_FIELD_OUT);
		if ( outDoc == null )
			return null;
		outVertex = (T) GraphDB.documentToModel(outDoc);
		return (T) outVertex;
	}

	////
	
	@Override
	protected void _save() {
		final ODocument edge = getDocument();
		final List<Property> propertyList = GraphDBPropertyUtils.listProperties(this.getClass());
		GraphDB.serializeFiledsOfPojoToGraphDocument(propertyList, this, edge);
		if ( outVertex != null )
			outVertex.save();
		if ( inVertex != null )
			inVertex.save();
		edge.save();
		GraphDB.serializeFiledsOfGraphDocumentToPojo(propertyList, edge, this);
	}
	
	@Override
	protected void _delete() {
		db().removeEdge(getORID());
	}	

	@Override
	protected void _reload() {
		outVertex = null;
		inVertex = null;		
		final ODocument edge = getDocument();
		edge.reload();
		final List<Property> propertyList = GraphDBPropertyUtils.listProperties(this.getClass());
		GraphDB.serializeFiledsOfGraphDocumentToPojo(propertyList, edge, this);		
	}
	
	protected ODocument createModel() {
		final String schemaName = getSchemaName();
		return db().createEdge(outVertex != null ? outVertex.getDocument() : null, inVertex != null ? inVertex.getDocument() : null, schemaName);
	}	
}

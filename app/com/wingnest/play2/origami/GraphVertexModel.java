/*
 * Copyright since 2013 Shigeru GOUGI
 *                              e-mail:  sgougi@gmail.com
 *                              twitter: @igerugo
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
import java.util.Set;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.wingnest.play2.origami.plugin.utils.GraphDBPropertyUtils;
import com.wingnest.play2.origami.plugin.utils.GraphDBPropertyUtils.Property;

public abstract class GraphVertexModel extends GraphModel {

	public Set<ODocument> getInEdges() {
		return getDocument().field(OGraphDatabase.VERTEX_FIELD_IN);
	}

	public Set<ODocument> getOutEdges() {
		return getDocument().field(OGraphDatabase.VERTEX_FIELD_OUT);
	}
	
	////
	
	@Override
	protected void _save() {
		final ODocument vertex = getDocument();
		final List<Property> propertyList = GraphDBPropertyUtils.listProperties(this.getClass());
		GraphDB.serializeFiledsOfPojoToGraphDocument(propertyList, this, vertex);
		vertex.save();
		GraphDB.serializeFiledsOfGraphDocumentToPojo(propertyList, vertex, this);
	}
	
	@Override
	protected void _delete() {
		db().removeVertex(getORID());
	}

	protected ODocument createModel() {
		final String schemaName = getSchemaName();
		return db().createVertex(schemaName);
	}	
}

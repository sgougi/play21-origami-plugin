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

import static com.wingnest.play2.origami.plugin.OrigamiLogger.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.wingnest.play2.origami.plugin.OrigamiPlugin;
import com.wingnest.play2.origami.plugin.exceptions.OrigamiUnexpectedException;
import com.wingnest.play2.origami.plugin.utils.GraphDBPropertyUtils;
import com.wingnest.play2.origami.plugin.utils.GraphDBPropertyUtils.Property;

import com.wingnest.play2.origami.IdManager.IdHandler;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.command.OCommandResultListener;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseListener;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLAsynchQuery;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE;

final public class GraphDB {

	final static ThreadLocal<OGraphDatabase> localGraphTx = new ThreadLocal<OGraphDatabase>();
	final private static IdManager idManager = new IdManager();

	final private static List<Class<? extends ODatabaseListener>> listeners = new ArrayList<Class<? extends ODatabaseListener>>();
	final private static List<Class<? extends ORecordHook>> graphHooks = new ArrayList<Class<? extends ORecordHook>>();

	// //

	public static List<Class<? extends ODatabaseListener>> getListeners() {
		return listeners;
	}

	public static List<Class<? extends ORecordHook>> getGraphHooks() {
		return graphHooks;
	}

	// //

	public static IdManager getIdManager() {
		return idManager;
	}

	public static void setIdHandler(final IdHandler handler) {
		getIdManager().setIdHandler(handler);
	}

	// //

	public static void begin(final TXTYPE type) {
		final OGraphDatabase db = open();
		if ( db.getTransaction().getStatus() != TXSTATUS.BEGUN ) {
			db.begin(type);
		}
	}

	public static void begin() {
		final OGraphDatabase db = open();
		if ( db.getTransaction().getStatus() != TXSTATUS.BEGUN ) {
			db.begin();
		}
	}

	public static OGraphDatabase beginGraph(final TXTYPE type) {
		begin(type);
		return open();
	}

	public static OGraphDatabase beginGraph() {
		begin();
		return open();
	}

	public static void close() {
		commit();
		if ( hasOGraphDatabase() ) {
			if ( !localGraphTx.get().isClosed() )
				localGraphTx.get().close();
			localGraphTx.set(null);
		}
	}

	public static void commit() {
		if ( hasOpenedOGraphDatabase() && localGraphTx.get().getTransaction().getStatus() == TXSTATUS.BEGUN ) {
			localGraphTx.get().commit();
		}
	}

	public static OGraphDatabase open() {
		final boolean hasOpenedOGraphDatabase = hasOpenedOGraphDatabase();
		if ( !hasOpenedOGraphDatabase ) {
			final OGraphDatabase db = new OGraphDatabase(OrigamiPlugin.url);
			db.setUseCustomTypes(true);
			if ( OrigamiPlugin.url.startsWith("remote:") || db.exists() ) {
				db.open(OrigamiPlugin.user, OrigamiPlugin.passwd);
			} else {
				db.create();
			}
			localGraphTx.set(db);
			registerListeners(db);
			registerHooks(db);
		}
		return localGraphTx.get();
	}

	public static void rollback() {
		if ( hasOpenedOGraphDatabase() && localGraphTx.get().getTransaction().getStatus() == TXSTATUS.BEGUN ) {
			localGraphTx.get().rollback();
		}
	}

	// ////

	@SuppressWarnings("unchecked")
	public static <RET extends OCommandRequest> RET asynchCommand(final String query, final Object... params) {
		return (RET) GraphDB.open().command(new OSQLAsynchQuery<ODocument>(query)).execute(params);
	}

	@SuppressWarnings("unchecked")
	public static <RET extends OCommandRequest> RET asynchCommand(final String query, final OCommandResultListener iResultListener, final Object... params) {
		return (RET) GraphDB.open().command(new OSQLAsynchQuery<ODocument>(query, iResultListener)).execute(params);
	}

	@SuppressWarnings("unchecked")
	public static <RET extends OCommandRequest> RET asynchCommand(final String query, final int iLimit, final OCommandResultListener iResultListener, final Object... params) {
		return (RET) GraphDB.open().command(new OSQLAsynchQuery<ODocument>(query, iLimit, iResultListener)).execute(params);
	}

	@SuppressWarnings("unchecked")
	public static <RET extends OCommandRequest> RET synchCommand(final String query, final Object... params) {
		return (RET) GraphDB.open().command(new OSQLSynchQuery<ODocument>(query)).execute(params);
	}

	@SuppressWarnings("unchecked")
	public static <RET extends OCommandRequest> RET synchCommand(final String query, final int iLimit, final Object... params) {
		return (RET) GraphDB.open().command(new OSQLSynchQuery<ODocument>(query, iLimit)).execute(params);
	}

	public static List<ODocument> synchQuery(final String query, final Object... params) {
		return GraphDB.open().query(new OSQLSynchQuery<ODocument>(query), params);
	}

	public static List<ODocument> synchQuery(final String query, final int iLimit, final Object... params) {
		return GraphDB.open().query(new OSQLSynchQuery<ODocument>(query, iLimit), params);
	}

	public static <T extends GraphModel> List<T> synchQueryModel(final String query, final Object... params) {
		final List<ODocument> docs = GraphDB.open().query(new OSQLSynchQuery<ODocument>(query), params);
		return documentsToModels(docs);
	}

	public static <T extends GraphModel> List<T> synchQueryModel(final String query, int iLimit, final Object... params) {
		final List<ODocument> docs = GraphDB.open().query(new OSQLSynchQuery<ODocument>(query, iLimit), params);
		return documentsToModels(docs);
	}

	// //

	@SuppressWarnings("unchecked")
	public static <T extends GraphModel> T findById(final String id) {
		if ( id == null || id.length() == 0 )
			return null;
		final String rid = getIdManager().decodeId(id);
		return (T) findById(new ORecordId(rid));
	}

	public static ODocument findDocumentById(final ORID id) {
		if ( id == null )
			return null;
		ODatabaseRecordThreadLocal.INSTANCE.get().setDatabaseOwner(open());
		final ODocument doc = open().load(id);
		return doc;
	}

	@SuppressWarnings("unchecked")
	public static <T extends GraphModel> T findById(final ORID id) {
		if ( id == null || id.getClusterPosition().intValue() < 0 )
			return null;
		final ODocument doc = findDocumentById(id);
		if ( doc == null )
			return null;
		return (T) documentToModel(doc);
	}

	// ///

	public static <T extends GraphModel> List<T> documentsToModels(final Iterable<ODocument> docs) {
		final List<T> results = new ArrayList<T>();
		if ( docs == null )
			return results;
		for ( ODocument doc : docs ) {
			results.add(GraphDB.<T> documentToModel(doc));
		}
		return results;
	}

	public static <T extends GraphModel> T documentToModel(final ODocument doc) {
		try {
			final T model;
			final Class<?> entityClass;
			try {
				entityClass = OrigamiPlugin.graphEntityMap.get(doc.getSchemaClass().getName());
				@SuppressWarnings("unchecked")
				final T tm = (T) entityClass.newInstance();
				model = tm;
			} catch ( Exception e ) {
				error(e, "doc = " + doc.toJSON());
				return null;
			}
			final List<Property> propertyList = GraphDBPropertyUtils.listProperties(entityClass);
			serializeFiledsOfGraphDocumentToPojo(propertyList, doc, model);
			return model;
		} catch ( Exception e ) {
			return null;
		}
	}

	public static List<ODocument> getDocumentsByFieldName(final List<ODocument> docs, final String fieldNameOfDocument) {
		final ArrayList<ODocument> lst = new ArrayList<ODocument>();
		if ( docs.size() > 0 ) {
			for ( final ODocument doc : docs ) {
				final Object obj = doc.field(fieldNameOfDocument);
				if ( obj instanceof ODocument ) {
					lst.add((ODocument) obj);
				} else if ( obj instanceof List ) {
					for ( final Object rdoc : (List<?>) obj ) {
						if ( rdoc instanceof ODocument )
							lst.add((ODocument) rdoc);
						else if ( rdoc instanceof ORID )
							lst.add((ODocument) GraphDB.open().load((ORID) rdoc));
						else
							throw new IllegalStateException("bug!? : " + rdoc.getClass().getName());
					}
				} else {
					throw new IllegalStateException("bug!? : " + obj.getClass().getName());
				}
			}
		}
		return lst;
	}

	public static void initializeAttributes() {
		listeners.clear();
		graphHooks.clear();
		localGraphTx.remove();
	}

	// //

	static void serializeFiledsOfPojoToGraphDocument(final List<Property> propertyList, final GraphModel model, final ODocument doc) {
		for ( final Property prop : propertyList ) {
			if ( !prop.isGenerated ) {
				try {
					final Object obj = prop.field.get(model);
					doc.field(prop.field.getName(), obj);
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
		}
	}

	static void serializeFiledsOfGraphDocumentToPojo(final List<Property> propertyList, final ODocument doc, final GraphModel model) {
		for ( final Property prop : propertyList ) {
			if ( !prop.isGenerated ) {
				try {
					final Object obj = doc.field(prop.field.getName());
					if ( obj instanceof OTrackedList ) {
						final OTrackedList<?> tl = (OTrackedList<?>) obj;
						if ( Collection.class.isAssignableFrom(prop.field.getType()) ) {
							@SuppressWarnings("unchecked")
							final Collection<Object> col = (Collection<Object>) prop.field.get(model);
							col.clear();
							for ( final Object mObj : tl ) {
								col.add(mObj);
							}
						} else {
							throw new IllegalStateException("bug!:" + prop.field.getType().getName());
						}
					} else {
						prop.field.set(model, obj);
					}
				} catch ( Exception e ) {
					error(e, prop.field.getName());
				}
			}
		}
		setIdentityPropertyOfPojo(propertyList, doc, model);
	}

	// //

	private static boolean hasOpenedOGraphDatabase() {
		return localGraphTx.get() != null && (!localGraphTx.get().isClosed());
	}

	private static boolean hasOGraphDatabase() {
		return localGraphTx.get() != null;
	}

	@SuppressWarnings("unchecked")
	private static <T> T newInstance(final Class<?> appClass) {
		try {
			return (T) appClass.newInstance();
		} catch ( Exception e ) {
			throw new OrigamiUnexpectedException(e);
		}
	}

	private static void registerHooks(final OGraphDatabase db) {
		for ( final Class<?> hook : graphHooks ) {
			db.registerHook((ORecordHook) newInstance(hook));
		}
	}

	private static void registerListeners(final ODatabase db) {
		for ( final Class<?> listener : listeners ) {
			db.registerListener((ODatabaseListener) newInstance(listener));
		}
	}

	private static void setIdentityPropertyOfPojo(final List<Property> propertyList, final ODocument doc, final GraphModel model) {
		model.orid = doc.getIdentity();
		final Property idProp = GraphDBPropertyUtils.getIdProperty(propertyList);
		if ( idProp != null ) {
			if ( idProp.field.getType().equals(String.class) ) {
				try {
					final String id = getIdManager().encodeId(doc.getIdentity());
					idProp.field.set(model, id);
				} catch ( Exception e ) {
					error(e, "Object: idProp.field.set");
				}
			} else {
				error("unknown type: idProp.field.set: " + idProp.field.getType());
			}
		}
		final Property versionProp = GraphDBPropertyUtils.getVersionProperty(propertyList);
		if ( versionProp != null ) {
			try {
				if ( versionProp.field.getType().equals(Long.class) ) {
					versionProp.field.set(model, (long) doc.getVersion());
				} else if ( versionProp.field.getType().equals(Integer.class) ) {
					versionProp.field.set(model, doc.getVersion());
				}
			} catch ( Exception e ) {
				error(e, "ORID: versionProp.field.set");
			}
		}
	}
}

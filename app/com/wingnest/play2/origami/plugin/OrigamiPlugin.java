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
package com.wingnest.play2.origami.plugin;

import static com.wingnest.play2.origami.plugin.ConfigConsts.*;
import static com.wingnest.play2.origami.plugin.OrigamiLogger.debug;
import static com.wingnest.play2.origami.plugin.OrigamiLogger.error;
import static com.wingnest.play2.origami.plugin.OrigamiLogger.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.collections.CollectionUtils;

import play.Application;
import play.Configuration;
import play.Play;
import play.Plugin;

import com.orientechnologies.orient.client.remote.OEngineRemote;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabaseListener;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.wingnest.play2.origami.GraphDB;
import com.wingnest.play2.origami.GraphEdgeModel;
import com.wingnest.play2.origami.GraphVertexModel;
import com.wingnest.play2.origami.annotations.CompositeIndex;
import com.wingnest.play2.origami.annotations.DisupdateFlag;
import com.wingnest.play2.origami.annotations.Index;
import com.wingnest.play2.origami.plugin.exceptions.OrigamiUnexpectedException;
import com.wingnest.play2.origami.plugin.utils.ZipCompressionUtils;

final public class OrigamiPlugin extends Plugin {

	public static final Map<String, Class<?>> graphEntityMap = new HashMap<String, Class<?>>();
	private static final String ORIENTDB_WWW_PATH = "orientdb.www.path";

	public static String url;
	public static String user;
	public static String passwd;

	final private Application application;
	private static OServer server;

	// /

	public OrigamiPlugin(final Application application) {
		this.application = application;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				serverShutdown();
			}
		});
	}

	@Override
	public void onStart() {
		if ( server == null ) {
			if ( Orient.getHomePath() == null ) {
				System.setProperty(Orient.ORIENTDB_HOME, ".");
			}
			configure();
			serverStart();
		}
		registerGraphClasses();
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////

	synchronized private static void serverStart() {
		if ( url.startsWith("remote:") ) {
			Orient.instance().registerEngine(new OEngineRemote());
		} else {
			if ( url.startsWith("local:") ) {
				final File db = new File(url.substring("local:".length()));
				final Configuration c = Play.application().configuration();
				if ( c.getBoolean(CONF_ENABLE_DB_BACKUP_AT_STARTUP) && db.exists() ) {
					OrigamiLogger.info("create database backup");
					try {
						final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-z");
						ZipCompressionUtils.compress(db, new File(url.substring("local:".length()) + "-" + f.format(new Date()) + ".zip"));
					} catch ( Exception e ) {
						throw new IllegalStateException(e);
					}
				}
			}
			runEmbeddedOrientDB();
		}
	}

	synchronized private static void serverShutdown() {
		GraphDB.initializeAttributes();
		if ( server != null ) {
			OrigamiLogger.debug("call server.shutdown()");
			server.shutdown();
			server = null;
		}
		final Orient inst = Orient.instance();
		if ( inst != null ) {
			OrigamiLogger.debug("call Orient.instance().shutdown()");
			inst.shutdown();
		}
	}

	private static void configure() {
		final Configuration c = Play.application().configuration();
		url = c.getString(CONF_ORIENT_DB_URL, "memory:temp");
		user = c.getString(CONF_ORIENT_DB_USER, "admin");
		passwd = c.getString(CONF_ORIENT_DB_PASSWORD, "admin");
	}

	private static void runEmbeddedOrientDB() {
		FileInputStream fis = null;
		try {
			/* orient server */{
				final String cfile = Play.application().configuration().getString(CONF_ORIENT_DB_CONFIG_FILE);
				final InputStream is;
				if ( cfile != null ) {
					final File aFile = new File(cfile);
					OrigamiLogger.info("db.config in application was used : " + aFile.getAbsolutePath());
					fis = new FileInputStream(new File(cfile));
					is = fis;
				} else {
					OrigamiLogger.info("default db.config in Origami Plugin was used");
					is = OrigamiPlugin.class.getClassLoader().getResourceAsStream("db.config");
				}
				server = OServerMain.create();
				server.startup(is);
				info("OrientDB of embedded mode was started");
			}
			/* web server */{
				final String orientDBWwwPath = Play.application().configuration().getString(CONF_ORIENT_DB_WWW_PATH);
				if ( System.getProperty(ORIENTDB_WWW_PATH) == null && orientDBWwwPath != null ) {
					final File wwwPath = new File(orientDBWwwPath);
					if ( !wwwPath.exists() || !wwwPath.isDirectory() ) {
						final String mes = String.format("www directory not found : %s", wwwPath.getAbsolutePath());
						OrigamiLogger.error(mes);
						throw new IllegalStateException(mes);
					}
					System.setProperty(ORIENTDB_WWW_PATH, wwwPath.getCanonicalPath());
				}
				if ( System.getProperty(ORIENTDB_WWW_PATH) != null ) {
					server.activate();
					OrigamiLogger.info("WWW Server was just activated : %s", System.getProperty(ORIENTDB_WWW_PATH));
				} else {
					OrigamiLogger.info("WWW Server is not activated : application.conf's %s property was not provided", CONF_ORIENT_DB_WWW_PATH);
				}
			}
		} catch ( Exception e ) {
			throw new OrigamiUnexpectedException(e);
		} finally {
			if ( fis != null )
				try {
					fis.close();
				} catch ( Exception dummy ) {
				}
		}
	}

	// ///

	private void registerGraphClasses() {
		final OGraphDatabase db = GraphDB.open();
		try {
			debug("Registering Graph Classes");

			final Set<Class<GraphVertexModel>> vertexClasses = getSubTypesOf("models", GraphVertexModel.class);
			@SuppressWarnings("rawtypes")
			final Set<Class<GraphEdgeModel>> edgeClasses = getSubTypesOf("models", GraphEdgeModel.class);
			@SuppressWarnings("unchecked")
			final Collection<Class<?>> javaClasses = CollectionUtils.union(vertexClasses, edgeClasses);

			final Class<?>[] javaClassArray = javaClasses.toArray(new Class<?>[0]);
			Arrays.sort(javaClassArray, new Comparator<Class<?>>() {
				@Override
				public int compare(Class<?> o1, Class<?> o2) {
					if ( o1.equals(o2) )
						return 0;
					if ( o1.isAssignableFrom(o2) )
						return -1;
					if ( o2.isAssignableFrom(o1) )
						return 1;
					int o1cnt = calSuperclassCount(o1);
					int o2cnt = calSuperclassCount(o2);
					return (o1cnt - o2cnt);
				}
			});

			javaClasses.clear();
			javaClasses.addAll(Arrays.asList(javaClassArray));

			final OSchema schema = db.getMetadata().getSchema();
			for ( final Class<?> javaClass : javaClasses ) {
				final String entityName = javaClass.getSimpleName();
				final OClass oClass;
				if ( GraphVertexModel.class.isAssignableFrom(javaClass) ) {
					final String className = javaClass.getSimpleName();
					debug("Entity: %s", className);
					if ( schema.existsClass(className) ) {
						oClass = schema.getClass(className);
					} else {
						oClass = db.createVertexType(className);
					}
					graphEntityMap.put(className, javaClass);
					final Class<?> sclass = javaClass.getSuperclass();
					if ( javaClasses.contains(sclass) ) {
						final OClass sClass = db.getMetadata().getSchema().getClass(sclass.getSimpleName());
						db.getMetadata().getSchema().getClass(entityName).setSuperClass(sClass);
					}
				} else if ( GraphEdgeModel.class.isAssignableFrom(javaClass) ) {
					final String className = javaClass.getSimpleName();
					debug("Entity: %s", className);
					if ( schema.existsClass(className) ) {
						oClass = schema.getClass(className);
					} else {
						oClass = db.createEdgeType(className);
					}
					graphEntityMap.put(className, javaClass);
					final Class<?> sclass = javaClass.getSuperclass();
					if ( javaClasses.contains(sclass) ) {
						final OClass sClass = db.getMetadata().getSchema().getClass(sclass.getSimpleName());
						db.getMetadata().getSchema().getClass(entityName).setSuperClass(sClass);
					}
				} else {
					throw new IllegalStateException("bug!?");
				}
				maintainProperties(oClass, javaClass);
			}
			debug("Registering Database Listeners");
			for ( final Class<? extends ODatabaseListener> listener : getSubTypesOf("listeners", ODatabaseListener.class) ) {
				debug("Listener: %s", listener.getName());
				GraphDB.getListeners().add(listener);
			}
			debug("Registering Record Hooks");
			for ( final Class<? extends ORecordHook> hook : getSubTypesOf("hooks", ORecordHook.class) ) {
				debug("Hook: %s", hook.getName());
				GraphDB.getGraphHooks().add(hook);
			}
		} catch ( Exception e ) {
			throw new OrigamiUnexpectedException(e);
		} finally {
			db.close();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Set<Class<T>> getSubTypesOf(final String packageName, final Class<T> clazz) {
		final Set<Class<T>> classes = new HashSet<Class<T>>();
		try {
			final Set<String> classNames = new HashSet<String>();
			classNames.addAll(play.libs.Classpath.getTypes(application, packageName));
			for ( final String clazzName : classNames ) {
				final Class<?> c = Class.forName(clazzName, true, application.classloader());
				if ( clazz.isAssignableFrom(c) )
					classes.add((Class<T>) c);
			}
		} catch ( Exception e ) {
			throw new OrigamiUnexpectedException(e);
		}
		return classes;
	}

	@SuppressWarnings("unchecked")
	private void maintainProperties(final OClass oClass, final Class<?> javaClass) {
		final Map<String, Map<String, Object>> compositeIndexMap = new HashMap<String, Map<String, Object>>();
		final Map<String, OIndex<?>> classIndexCache = new HashMap<String, OIndex<?>>();
		final Map<String, OIndex<?>> compositeIndexCache = new HashMap<String, OIndex<?>>();
		final Set<String> wkCurIndexNames = new HashSet<String>();
//		for ( final OProperty prop : oClass.properties() ) {
//			debug("[b] prop name =%s, type = %s", prop.getName(), prop.getType());
//		}
		for ( final OIndex<?> index : oClass.getClassIndexes() ) {
			wkCurIndexNames.add(index.getName());
			if ( index.getName().indexOf('.') > -1 ) {
				classIndexCache.put(index.getName(), index);
			} else {
				compositeIndexCache.put(index.getName(), index);
			}
//			debug("[b] index name =%s, type = %s", index.getName(), index.getType());
		}
		for ( final Field field : javaClass.getDeclaredFields() ) {
			if ( (!Modifier.isPublic(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) || field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Version.class) || field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(DisupdateFlag.class) )
				continue;

			OProperty prop = oClass.getProperty(field.getName());
			final OType type = guessType(field);
			if ( prop == null ) {
				if ( type != null ) {
					debug("create property : %s", field.getName());
					prop = oClass.createProperty(field.getName(), type);
				}
			} else {
				if ( !type.equals(prop.getType()) ) {
					deleteIndex(oClass, oClass.getName() + "." + field.getName(), classIndexCache, compositeIndexCache);
					debug("drop property : %s", field.getName());
					oClass.dropProperty(field.getName());
					debug("create property : %s", field.getName());
					prop = oClass.createProperty(field.getName(), type);
				}
			}
			final Index index = field.getAnnotation(Index.class);
			if ( index != null ) {
				final String indexName = makeIndexName(javaClass, field);
				OIndex<?> oindex = classIndexCache.get(indexName);
				if ( oindex == null ) {
					debug("create Class Index : %s.%s", javaClass.getSimpleName(), field.getName());
					if ( prop != null ) {
						oindex = oClass.createIndex(indexName, index.indexType(), field.getName());
					} else {
						error("could not create Class Index : property(%s.%s) has't type", javaClass.getName(), field.getName());
					}
				}
				if ( oindex != null ) {
					wkCurIndexNames.remove(oindex.getName());
				}
			}
			final CompositeIndex cindex = field.getAnnotation(CompositeIndex.class);
			if ( cindex != null ) {
				final String indexName = javaClass.getSimpleName() + "_" + cindex.indexName();
				Map<String, Object> ci = compositeIndexMap.get(indexName);
				if ( ci == null ) {
					ci = new HashMap<String, Object>();
					ci.put("fields", new HashSet<Field>());
					ci.put("indexType", OClass.INDEX_TYPE.UNIQUE);
				}
				if ( !cindex.indexType().equals(OClass.INDEX_TYPE.UNIQUE) )
					ci.put("indexType", cindex.indexType());
				((Set<Field>) ci.get("fields")).add(field);
				compositeIndexMap.put(indexName, ci);
			}
		}

		for ( final String cindexName : compositeIndexMap.keySet() ) {
			final Map<String, Object> ci = compositeIndexMap.get(cindexName);
			final Set<Field> fields = (Set<Field>) ci.get("fields");
			final String[] fieldNames = new String[fields.size()];
			int i = 0;
			for ( final Field f : fields ) {
				fieldNames[i++] = f.getName();
			}
			final OIndex<?> oindex = compositeIndexCache.get(cindexName);
			if ( oindex != null && !CollectionUtils.isEqualCollection(Arrays.asList(fieldNames), oindex.getDefinition().getFields()) ) {
				debug("recreate composite index : %s", cindexName);
				deleteIndex(oClass, cindexName, classIndexCache, compositeIndexCache);
			} else if ( oindex == null ) {
				debug("create composite index : %s", cindexName);
			}
			oClass.createIndex(cindexName, (OClass.INDEX_TYPE) ci.get("indexType"), fieldNames);
			wkCurIndexNames.remove(cindexName);
		}

		for ( final String indexName : wkCurIndexNames ) {
			final int ind = indexName.indexOf('.');
			if ( ind > -1 ) {
				debug("delete index : %s", indexName);
			} else {
				debug("delete composite index : %s", indexName);
			}
			deleteIndex(oClass, indexName, classIndexCache, compositeIndexCache);
		}

//		for ( final OProperty prop : oClass.properties() ) {
//			debug("[a] prop name =%s, type = %s", prop.getName(), prop.getType());
//		}
//		for ( final OIndex<?> index : oClass.getClassIndexes() ) {
//			debug("[a] class index name =%s, type = %s", index.getName(), index.getType());
//		}
	}

	private void deleteIndex(final OClass oClass, final String indexName, final Map<String, OIndex<?>> classIndexCache, final Map<String, OIndex<?>> compositeIndexCache) {
		final OIndex<?> oindex;
		if ( indexName.indexOf('.') > -1 ) {
			oindex = classIndexCache.get(indexName);
		} else {
			oindex = compositeIndexCache.get(indexName);
		}
		if ( oindex != null ) {
			debug("drop index %s", oindex.getName());
			GraphDB.open().getMetadata().getIndexManager().dropIndex(indexName).flush();
		}
	}

	private OType guessType(final Field field) {
		if ( String.class.isAssignableFrom(field.getType()) )
			return OType.STRING;
		if ( Date.class.isAssignableFrom(field.getType()) )
			return OType.DATETIME;
		if ( Integer.class.isAssignableFrom(field.getType()) || int.class.isAssignableFrom(field.getType()) )
			return OType.INTEGER;
		if ( Long.class.isAssignableFrom(field.getType()) || long.class.isAssignableFrom(field.getType()) )
			return OType.LONG;
		if ( Float.class.isAssignableFrom(field.getType()) || float.class.isAssignableFrom(field.getType()) )
			return OType.FLOAT;
		if ( Double.class.isAssignableFrom(field.getType()) || double.class.isAssignableFrom(field.getType()) )
			return OType.DOUBLE;
		if ( Boolean.class.isAssignableFrom(field.getType()) || boolean.class.isAssignableFrom(field.getType()) )
			return OType.BOOLEAN;
		if ( Byte.class.isAssignableFrom(field.getType()) )
			return OType.BYTE;
		if ( Short.class.isAssignableFrom(field.getType()) || short.class.isAssignableFrom(field.getType()) )
			return OType.SHORT;
		return null;
	}

	private String makeIndexName(final Class<?> javaClass, final Field field) {
		return new StringBuffer().append(javaClass.getSimpleName()).append(".").append(field.getName()).toString();
	}

	private int calSuperclassCount(final Class<?> clazz) {
		int cnt = 0;
		Class<?> sc = clazz.getSuperclass();
		while ( sc != null ) {
			cnt++;
			sc = sc.getSuperclass();
		}
		return cnt;
	}

}
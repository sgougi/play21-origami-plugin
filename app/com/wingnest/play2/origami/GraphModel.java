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

import static com.wingnest.play2.origami.plugin.OrigamiLogger.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Set;

import javax.persistence.Transient;

import com.wingnest.play2.origami.annotations.DisupdateFlag;
import com.wingnest.play2.origami.annotations.SmartDate;
import com.wingnest.play2.origami.plugin.OrigamiLogger;
import com.wingnest.play2.origami.plugin.exceptions.OrigamiUnexpectedException;
import com.wingnest.play2.origami.plugin.utils.GraphDBPropertyUtils;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;

public abstract class GraphModel {

	public static enum SMART_DATE_TYPE {
		CREATED_DATE, UPDATED_DATE
	}
	
	@Transient
	protected ORID orid = null;
	@Transient
	private ODocument doc = null;

	public ODocument getDocument() {
		if ( !loadDocument() ) {
			doc = createModel();
		}
		return doc;
	}

	public String getSchemaName() {
		return this.getClass().getSimpleName();
	}

	public ORID getORID() {
		return orid;
	}

	@SuppressWarnings("unchecked")
	public <T extends GraphModel> T save() {
		try {
			SmartDateUtils.enhance(this);
			_save();
			return (T) this;
		} catch ( RuntimeException e ) {
			error("GraphModel.save: class = %s, orid = %s: %s", this.getClass().getName(), this.getORID() != null ? this.getORID().toString() : "null", e.getMessage());
			throw new OrigamiUnexpectedException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GraphModel> T delete() {
		_delete();
		return (T) this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GraphModel> T reload() {
		try {
			_reload();
			return (T) this;
		} catch ( RuntimeException e ) {
			error("GraphModel.reload: class = %s, orid = %s: %s", this.getClass().getName(), this.getORID() != null ? this.getORID().toString() : "null", e.getMessage());
			throw new OrigamiUnexpectedException(e);
		}
	}	

	public boolean isUnmanaged() {
		return getORID() == null;
	}

	final public Object getORIDAsString() {
		final ORID aOrid = getORID();
		if ( aOrid == null )
			return null;
		return aOrid.toString();
	}

	@Override
	public boolean equals(Object other) {
		if ( other == null ) {
			return false;
		}
		if ( this == other ) {
			return true;
		}
		if ( !this.getClass().isAssignableFrom(other.getClass()) ) {
			return false;
		}
		if ( this.getORIDAsString() == null ) {
			return false;
		}
		return this.getORIDAsString().equals(((GraphModel) other).getORIDAsString());
	}
	
	////

	abstract protected void _delete();

	abstract protected void _save();
	
	abstract protected void _reload();	
	
	abstract protected ODocument createModel();

	protected static OGraphDatabase db() {
		return GraphDB.open();
	}

	////
	
	private boolean loadDocument() {
		if ( orid != null && doc == null ) {
			doc = db().load(orid);
		} else if ( doc == null ) {
			orid = null;
		}
		return doc != null;
	}
	
	////

	private static class SmartDateUtils {

		public static void enhance(GraphModel g) {
			if ( g == null )
				return;
			final Set<Field> fields = GraphDBPropertyUtils.getDeepDeclaredFields(g.getClass());
			for ( final Field field : fields ) {
				field.setAccessible(true);
				doEnhance(field, g, 0);
			}
		}

		private static void doEnhance(Field field, Object g, int depth) {
			final SmartDate sd = field.getAnnotation(SmartDate.class);
			if ( sd != null ) {
				SMART_DATE_TYPE sdt = sd.dateType();
				if( sdt != null ) {
					try {
						if ( sdt.equals(SMART_DATE_TYPE.CREATED_DATE) ) {
							if ( Date.class.isAssignableFrom(field.getType()) ) {
								Object obj = field.get(g);
								if ( obj == null )
									field.set(g, new Date());
							}
						} else if ( sdt.equals(SMART_DATE_TYPE.UPDATED_DATE) ) {
							if ( Date.class.isAssignableFrom(field.getType()) ) {
								Object obj = field.get(g);
								if ( obj == null ) {
									field.set(g, new Date());
								} else if ( !hasDisupdateFlag(g) ) {
									field.set(g, new Date());
								}
							}
						}
					} catch ( Exception e ) {
						error("SmartDateUtils#doEnhance : %s, %s", e.getClass().getName(), e.getMessage());
						throw new OrigamiUnexpectedException(e);
					}
				}
			}
		}

		private static boolean hasDisupdateFlag(Object g) {
			final Set<Field> fields = GraphDBPropertyUtils.getDeepDeclaredFields(g.getClass());
			for ( final Field field : fields ) {
				final DisupdateFlag disupdateFlag = field.getAnnotation(DisupdateFlag.class);
				if ( disupdateFlag != null ) {
					field.setAccessible(true);
					try {
						final boolean b = field.getBoolean(g);
						return b;
					} catch ( Exception e ) {
						throw new OrigamiUnexpectedException(e);
					}
				}
			}
			return false;
		}

	}

}

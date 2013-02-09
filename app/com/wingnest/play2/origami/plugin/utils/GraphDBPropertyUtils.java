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
package com.wingnest.play2.origami.plugin.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.wingnest.play2.origami.annotations.DisupdateFlag;

final public class GraphDBPropertyUtils {

	public static List<Property> listProperties(final Class<?> clazz) {
		final List<Property> properties = new ArrayList<Property>();
		final Set<Field> fields = new LinkedHashSet<Field>();
		Class<?> tclazz = clazz;
		while ( !tclazz.equals(Object.class) ) {
			Collections.addAll(fields, tclazz.getDeclaredFields());
			tclazz = tclazz.getSuperclass();
		}
		for ( final Field f : fields ) {
			if ( Modifier.isTransient(f.getModifiers()) ) {
				continue;
			}
			if ( f.isAnnotationPresent(Transient.class) ) {
				continue;
			}
			if ( f.isAnnotationPresent(DisupdateFlag.class) ) {
				continue;
			}
			final Property mp = buildProperty(f);
			if ( mp != null ) {
				properties.add(mp);
			}
		}
		return properties;
	}

	public static Property getIdProperty(final List<Property> propList) {
		return getGeneratedField(propList, Id.class);
	}

	public static Property getVersionProperty(final List<Property> propList) {
		return getGeneratedField(propList, Version.class);
	}

	private static Property getGeneratedField(final List<Property> propList, Class<? extends Annotation> annoClass) {
		for ( final Property prop : propList ) {
			if ( prop.isGenerated && prop.field.isAnnotationPresent(annoClass) ) {
				return prop;
			}
		}
		return null;
	}

	private static Property buildProperty(final Field field) {
		final Property modelProperty = new Property();
		modelProperty.type = field.getType();
		modelProperty.field = field;
		if ( field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Version.class) ) {
			modelProperty.isGenerated = true;
		}
		return modelProperty;
	}

	public static class Property {
		public Class<?> type;
		public Field field;
		public boolean isGenerated;
	}

	private GraphDBPropertyUtils() {
	}

}

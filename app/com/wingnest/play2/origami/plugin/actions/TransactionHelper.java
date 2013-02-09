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
package com.wingnest.play2.origami.plugin.actions;

import java.lang.annotation.Annotation;

import play.mvc.Http;

import com.wingnest.play2.origami.GraphDB;
import com.wingnest.play2.origami.annotations.Transactional;
import com.wingnest.play2.origami.plugin.OrigamiLogger;

final public class TransactionHelper {

	private enum ProcessType {
		SKIP, OPEN_ONLY, OPEN_AND_TRANSACTION, TRANSACTION
	};

	private static final String KEY_OPEN = "TransactionHelper.open";
	private static final String KEY_TRANSACTION = "TransactionHelper.transaction";

	private ProcessType processType = ProcessType.SKIP;
	final private Annotation annotation;
	final private Http.Context context;

	public TransactionHelper(final Http.Context context, final Annotation annotation) {
		this.context = context;
		this.annotation = annotation;
		if ( !context.flash().containsKey(KEY_OPEN) ) {
			processType = ProcessType.OPEN_ONLY;
			context.flash().put(KEY_OPEN, "on");
		}

		if ( !context.flash().containsKey(KEY_TRANSACTION) ) {
			if ( annotation.annotationType().equals(Transactional.class) ) {
				if ( processType.equals(ProcessType.OPEN_ONLY) ) {
					processType = ProcessType.OPEN_AND_TRANSACTION;
				} else {
					processType = ProcessType.TRANSACTION;
				}
				context.flash().put(KEY_TRANSACTION, "on");
			}
		}
	}

	public void beforeInvocation() {
		if ( processType.equals(ProcessType.SKIP) )
			return;
		if ( processType.equals(ProcessType.OPEN_ONLY) || processType.equals(ProcessType.OPEN_AND_TRANSACTION) ) {
			GraphDB.open();
			if ( processType.equals(ProcessType.OPEN_ONLY) ) {
				return;
			}
		}
		if ( processType.equals(ProcessType.OPEN_AND_TRANSACTION) || processType.equals(ProcessType.TRANSACTION) ) {
			GraphDB.begin(((Transactional) annotation).type());
		}
	}

	public void invocationFinally() {
		if ( processType.equals(ProcessType.OPEN_ONLY) || processType.equals(ProcessType.OPEN_AND_TRANSACTION) ) {
			GraphDB.close();
		}
		context.flash().remove(KEY_OPEN);
		context.flash().remove(KEY_TRANSACTION);
	}

	public void onInvocationException(Exception e) {
		if ( processType.equals(ProcessType.OPEN_AND_TRANSACTION) ) {
			GraphDB.rollback();
			OrigamiLogger.debug("GraphDB:rollback");
		}
	}

	public void onInvocationSuccess() {
		if ( processType.equals(ProcessType.OPEN_AND_TRANSACTION) ) {
			GraphDB.commit();
		}
	}

}

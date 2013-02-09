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

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

final public class GraphDBAction extends Action<Annotation> {

	@Override
	public Result call(final Http.Context context) throws Throwable {
		final TransactionHelper th = new TransactionHelper(context, this.configuration);
		final Result res;
		try {
			th.beforeInvocation();
			res = delegate.call(context);
			th.onInvocationSuccess();
		} catch ( Exception e ) {
			th.onInvocationException(e);
			throw e;
		} finally {
			th.invocationFinally();
		}
		return res;
	}

}

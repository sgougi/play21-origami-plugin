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

import com.orientechnologies.orient.core.id.ORID;

final public class IdManager {

	private static IdHandler idHandler = null;

	public String encodeId(final ORID identity) {
		if( identity.isTemporary() ) {
			throw new IllegalStateException("Temporary object does not ganerates id : " + identity.toString());
		}		
		if ( idHandler == null ) {
			return play.libs.Crypto.encryptAES(identity.toString());
		}
		return idHandler.encode(identity);
	}

	public String decodeId(final String id) {
		if ( idHandler == null ) {
			return play.libs.Crypto.decryptAES(id);
		}
		return idHandler.decode(id);
	}

	public void setIdHandler(final IdHandler handler) {
		idHandler = handler;
	}

	public interface IdHandler {
		String encode(ORID identity);

		String decode(String encodedId);
	}

}

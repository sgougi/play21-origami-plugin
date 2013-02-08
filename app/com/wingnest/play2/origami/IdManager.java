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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.orientechnologies.orient.core.id.ORID;
import com.wingnest.play2.origami.plugin.exceptions.OrigamiUnexpectedException;

final public class IdManager {

	private static IdHandler ID_HANDLER = null;

	public String encodeId(final ORID identity) {
		if( identity.isTemporary() ) {
			throw new IllegalStateException("Temporary object does not ganerates id : " + identity.toString());
		}		
		if ( ID_HANDLER == null ) {
			final String rid = identity.toString();
			return Codec.byteToHexString(rid.getBytes()); 
		}
		return ID_HANDLER.encode(identity);
	}

	public String decodeId(final String id) {
		if ( ID_HANDLER == null ) {
			return new String(Codec.hexStringToByte(id));
		}
		return ID_HANDLER.decode(id);
	}

	public void setIdHandler(final IdHandler handler) {
		ID_HANDLER = handler;
	}

	public interface IdHandler {
		String encode(ORID identity);

		String decode(String encodedId);
	}

	public static class Codec {
		// from play1.2.5
		public static byte[] hexStringToByte(String hexString) {
			try {
				return Hex.decodeHex(hexString.toCharArray());
			} catch ( DecoderException e ) {
				throw new OrigamiUnexpectedException(e);
			}
		}

		public static String byteToHexString(byte[] bytes) {
			return String.valueOf(Hex.encodeHex(bytes));
		}
	}	
}

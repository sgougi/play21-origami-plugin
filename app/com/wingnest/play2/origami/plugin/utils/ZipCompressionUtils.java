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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.wingnest.play2.origami.plugin.exceptions.OrigamiUnexpectedException;

final public class ZipCompressionUtils {

	public static void compress(final File file) throws IOException {
		final ZipCompresser comp = new ZipCompresser(file);
		comp.archive();
	}

	public static void compress(final File file, final File zipFile) throws IOException {
		final ZipCompresser comp = new ZipCompresser(file);
		comp.archive(zipFile);
	}

	public static class ZipCompresser {
		final private File baseFile;
		final private String baseFilePath;

		public ZipCompresser(final File base) {
			super();
			this.baseFile = base;
			this.baseFilePath = base.getAbsolutePath();
		}

		public void archive() {
			archive(null);
		}

		public void archive(File zipfile) {
			if ( zipfile == null )
				zipfile = new File(this.baseFile.getParent(), this.baseFile.getName() + ".zip");

			ZipOutputStream zos = null;
			try {
				zos = new ZipOutputStream(new FileOutputStream(zipfile));
				archive(zos, this.baseFile);
			} catch ( Exception e ) {
				throw new OrigamiUnexpectedException(e);
			} finally {
				try {
					if ( zos != null )
						zos.close();
				} catch ( IOException e ) {
				}
			}
		}

		private void archive(final ZipOutputStream zos, final File file) {
			if ( file.isDirectory() ) {
				final File[] files = file.listFiles();
				for ( final File f : files ) {
					archive(zos, f);
				}
			} else {
				BufferedInputStream fis = null;
				try {
					fis = new BufferedInputStream(new FileInputStream(file));
					final String entryName = file.getAbsolutePath().replace(this.baseFilePath, "").substring(1);
					zos.putNextEntry(new ZipEntry(entryName));
					int ava = 0;
					while ( (ava = fis.available()) > 0 ) {
						byte[] bs = new byte[ava];
						fis.read(bs);
						zos.write(bs);
					}
					zos.closeEntry();
				} catch ( Exception e ) {
					throw new OrigamiUnexpectedException(e);
				} finally {
					try {
						if ( fis != null )
							fis.close();
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private ZipCompressionUtils() {
	}

}
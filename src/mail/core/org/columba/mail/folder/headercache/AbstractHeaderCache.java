//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.headercache;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import org.columba.core.base.BooleanCompressor;
import org.columba.core.gui.base.ColorFactory;
import org.columba.mail.folder.IHeaderListStore;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.IHeaderList;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.AddressParser;
import org.columba.ristretto.parser.ParserException;

/**
 * Provides basic support for saving and loading email headers as fast as
 * possible.
 * <p>
 * It therefor tries to compress the data to make it as small as possible, which
 * improves performance dramatically
 * 
 * @see CachedHeaderfields
 * 
 * @author fdietz
 */
public abstract class AbstractHeaderCache implements IHeaderListStore {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder.headercache");

	protected File headerFile;

	private boolean headerCacheLoaded;

	protected ObjectWriter writer;

	protected ObjectReader reader;

	/**
	 * @param folder
	 */
	public AbstractHeaderCache(File headerFile) {
		this.headerFile = headerFile;

		headerCacheLoaded = false;
	}

	/**
	 * @return
	 */
	public ColumbaHeader createHeaderInstance() {
		return new ColumbaHeader();
	}

	/**
	 * @return
	 */
	public boolean isHeaderCacheLoaded() {
		return headerCacheLoaded;
	}

	/**
	 * Get or (re)create the header cache file.
	 * 
	 * @return the HeaderList
	 * @throws Exception
	 */
	public void restoreHeaderList(IHeaderList headerList) throws IOException {
		// if there exists a ".header" cache-file
		//  try to load the cache
		if (!headerCacheLoaded) {
			if (headerFile.exists()) {
				try {
					load(headerList);
				} catch (Exception e) {
					LOG.severe(e.getMessage());
					LOG.severe("Could not open headercache file: " + headerFile.toString());
					LOG.severe("Falling back to backup file.");
					// failed to load original header-cache file
					// -> use ".old" file as fallback

					File oldFile = headerFile;

					headerFile = new File(headerFile.getAbsolutePath() + ".old");
					if( !headerFile.exists() ) {
						LOG.severe("No Cache found.");
						headerCacheLoaded = true;
					}
					
					try {
						load(headerList);
					} catch (Exception e2) {
						LOG.severe(e2.getMessage());

					} finally {					
						oldFile.delete();
						headerFile.delete();
						
						headerFile = oldFile;

						headerCacheLoaded = true;
					}
				}
			}

			headerCacheLoaded = true;
		}
	}

	/**
	 * @throws Exception
	 */
	public abstract void load(IHeaderList headerList) throws Exception;


	protected void loadHeader(ColumbaHeader h) throws IOException {
		// load boolean headerfields, which are compressed in one int value
		int compressedFlags = 0;
		
		try {
			compressedFlags = ((Integer) reader.readObject()).intValue();
		} catch (ClassNotFoundException e1) {
		}

		for (int i = 0; i < CachedHeaderfields.INTERNAL_COMPRESSED_HEADERFIELDS.length; i++) {
			h.set(CachedHeaderfields.INTERNAL_COMPRESSED_HEADERFIELDS[i],
					BooleanCompressor.decompress(compressedFlags, i));
		}

		// load other internal headerfields, non-boolean type
		String[] columnNames = CachedHeaderfields.INTERNAL_HEADERFIELDS;
		Class[] columnTypes = CachedHeaderfields.INTERNAL_HEADERFIELDS_TYPE;

		for (int j = 0; j < columnNames.length; j++) {
			Object value = null;

			if (columnTypes[j] == Integer.class) {
				value = new Integer(reader.readInt());
			} else if (columnTypes[j] == Date.class) {
				value = new Date(reader.readLong());
			} else if (columnTypes[j] == Color.class) {
				value = ColorFactory.getColor(reader.readInt());
			} else if (columnTypes[j] == Address.class) {
				try {
					value = AddressParser.parseAddress(reader.readString());
				} catch (IndexOutOfBoundsException e) {
				} catch (IllegalArgumentException e) {
				} catch (ParserException e) {
				} finally {
					if (value == null)
						value = "";
				}
			} else
				value = reader.readString();

			if (value != null) {
				h.set(columnNames[j], value);
			}
		}

		//load default headerfields, as defined in RFC822
		columnNames = CachedHeaderfields.getDefaultHeaderfields();

		for (int j = 0; j < columnNames.length; j++) {
			String value = reader.readString();
			if (value != null) {
				h.set(columnNames[j], value);
			}
		}

	}

	protected void saveHeader(ColumbaHeader h) throws IOException {
		// save boolean headerfields, compressing them to one int value
		Boolean[] b = new Boolean[CachedHeaderfields.INTERNAL_COMPRESSED_HEADERFIELDS.length];

		for (int i = 0; i < b.length; i++) {
			b[i] = (Boolean) h
					.get(CachedHeaderfields.INTERNAL_COMPRESSED_HEADERFIELDS[i]);

			// if value doesn't exist, use false as default
			if (b[i] == null) {
				b[i] = Boolean.FALSE;
			}
		}

		writer.writeObject(new Integer(BooleanCompressor.compress(b)));

		// save other internal headerfields, of non-boolean type
		String[] columnNames = CachedHeaderfields.INTERNAL_HEADERFIELDS;
		Class[] columnTypes = CachedHeaderfields.INTERNAL_HEADERFIELDS_TYPE;
		Object o;

		for (int j = 0; j < columnNames.length; j++) {
			o = h.get(columnNames[j]);

			//System.out.println("type="+o.getClass());

			if (columnTypes[j] == Integer.class)
				writer.writeInt(((Integer) o).intValue());
			else if (columnTypes[j] == Date.class) {
				writer.writeLong(((Date) o).getTime());
			} else if (columnTypes[j] == Color.class) {
				writer.writeInt(((Color) o).getRGB());
			} else if (columnTypes[j] == Address.class) {
				if (o instanceof Address)
					writer.writeString(((Address) o).toString());
				else
					writer.writeString((String) o);
			} else
				writer.writeString(o.toString());
		}

		// save default headerfields, as defined in RFC822
		columnNames = CachedHeaderfields.getDefaultHeaderfields();

		for (int j = 0; j < columnNames.length; j++) {
			String v = (String) h.get(columnNames[j]);
			if ( v==null) v = "";
			writer.writeString(v);
		}

	}

	/**
	 * @param b
	 */
	public void setHeaderCacheLoaded(boolean b) {
		headerCacheLoaded = b;
	}

}
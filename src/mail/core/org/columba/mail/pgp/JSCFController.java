// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.pgp;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.columba.api.exception.PluginHandlerNotFoundException;
import org.columba.api.plugin.IExtension;
import org.columba.core.gui.externaltools.ExternalToolsManager;
import org.columba.core.logging.Logging;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ExternalToolsExtensionHandler;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.SecurityItem;
import org.waffel.jscf.JSCFConnection;
import org.waffel.jscf.JSCFDriverManager;
import org.waffel.jscf.JSCFException;
import org.waffel.jscf.gpg.GPGDriver;

/**
 * The <code>JSCFController</code> controls JSCFDrivers and Connections. It
 * chaches for each account the connection to JSCFDrivers. The
 * <code>JSCFController</code> uses the "singleton pattern", which mean, that
 * you should access it, using the <code>getInstcane</code> method.
 * 
 * @author waffel
 */
public class JSCFController {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger.getLogger("org.columba.mail.pgp");

	private static JSCFController myInstance = null;

	private static Map connectionMap;

	/**
	 * Gives a instance of the <code>JSCFController</code> back. If no
	 * instance was created before, a new instance will be created.
	 * 
	 * @return A Instance of <code>JSCFController</code>
	 */
	public static JSCFController getInstance() {
		if (myInstance == null) {
			myInstance = new JSCFController();
			registerDrivers();
			connectionMap = new Hashtable();
		}

		return myInstance;
	}

	private static void registerDrivers() {
		try {
			// at the moment we are only supporting gpg. So let us code hard
			// here the gpg driver
			JSCFDriverManager.registerJSCFDriver(new GPGDriver());
		} catch (JSCFException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Creates a new Connection to the gpg driver, if the connection for the
	 * given <code>userID</code> are not exists. Properties for the connection
	 * are created by using the SecurityItem from the <code>AccountItem</code>.
	 * Properties like PATH and the GPG USERID are stored for the connection, if
	 * the connection are not exists.
	 * 
	 * @param userID
	 *            UserID from which the connection should give back
	 * @return a alrady etablished connection for this user or a newly created
	 *         connection for this userID, if no connection exists for the
	 *         userID
	 * @throws JSCFException
	 *             If there are several Driver problems
	 */
	public JSCFConnection getConnection(String userID) throws JSCFException {
		SecurityItem pgpItem = MailConfig.getInstance().getAccountList()
				.getDefaultAccount().getPGPItem();
		JSCFConnection con = (JSCFConnection) connectionMap.get(userID);

		if (con == null) {
			LOG.fine("no connection for userID (" + userID
					+ ") found. Creating a new Connection.");
			// let us hard coding the gpg for each connection. Later we should
			// support also other variants (like smime)
			con = JSCFDriverManager.getConnection("jscf:gpg:");

			// getting the path to gpg

			ExternalToolsExtensionHandler handler = null;
			String path = null;
			try {
				LOG.fine("try to get the handler");
				handler = (ExternalToolsExtensionHandler) PluginManager
						.getInstance().getHandler(
								"org.columba.core.externaltools");
				LOG.fine("recived Handler ... getting path from it");
				path = ExternalToolsManager.getInstance()
						.getLocationOfExternalTool("gpg").getPath();
				LOG.fine("setting path: " + path);
			} catch (PluginHandlerNotFoundException e) {
				LOG.fine("PluginHandler not found" + e);
				if (Logging.DEBUG) {
					e.printStackTrace();
				}
			}

			/*
			 * if (path == null) { throw new ProgramNotFoundException("invalid
			 * path"); }
			 */
			Properties props = con.getProperties();
			if (path == null) {
				throw new ProgramNotFoundException("invalid path");
			}
			props.put("PATH", path);
			if (handler != null) {
				IExtension extension = handler.getExtension("gpg");

				LOG.fine("gpg userId: " + extension.getMetadata().getId());
			}
			LOG.info("gpg path: " + props.get("PATH"));
			props.put("USERID", pgpItem.get("id"));
			LOG.info("current gpg userID: " + props.get("USERID"));
			con.setProperties(props);
			connectionMap.put(userID, con);
		}

		return con;
	}

	/**
	 * Creates a new JSCFConnection for the current used Account. The current
	 * used Account is determind from the AccountItem. This method calls only
	 * {@link #getConnection(String)}with the <code>id</code> from the
	 * SecurityItem.
	 * 
	 * @return a alrady etablished connection for the current account or a newly
	 *         created connection for the current account, if no connection
	 *         exists for the current account
	 * @throws JSCFException
	 *             If there are several Driver problems
	 */
	public JSCFConnection getConnection() throws JSCFException {
		SecurityItem pgpItem = MailConfig.getInstance().getAccountList()
				.getDefaultAccount().getPGPItem();

		return getConnection(pgpItem.get("id"));
	}
}
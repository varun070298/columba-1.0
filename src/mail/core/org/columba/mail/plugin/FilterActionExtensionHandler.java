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
package org.columba.mail.plugin;

import java.io.InputStream;

import org.columba.core.plugin.ExtensionHandler;

/**
 * @author freddy
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class FilterActionExtensionHandler extends ExtensionHandler {
	public static final String XML_RESOURCE = "/org/columba/mail/plugin/filter_actions.xml";

	public static final String NAME = "org.columba.mail.filteraction";

	/**
	 * Constructor for FilterActionExtensionHandler.
	 * 
	 * @param config
	 */
	public FilterActionExtensionHandler() {
		super(NAME);

		InputStream is = this.getClass().getResourceAsStream(XML_RESOURCE);
		loadExtensionsFromStream(is);
	}
}

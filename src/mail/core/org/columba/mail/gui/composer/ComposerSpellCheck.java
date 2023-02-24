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
//All Rights Reserved.undation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
package org.columba.mail.gui.composer;

import java.io.File;

import org.columba.core.gui.externaltools.ExternalToolsManager;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ExternalToolsExtensionHandler;
import org.columba.mail.config.SpellcheckItem;
import org.columba.mail.spellcheck.ASpellInterface;

public class ComposerSpellCheck {
	private ComposerController composerController;

	private SpellcheckItem spellCheckConfig = null;

	public ComposerSpellCheck(ComposerController composerController) {
		this.composerController = composerController;

		/*
		 * spellCheckConfig =
		 * MailInterface.config.getComposerOptionsConfig().getSpellcheckItem();
		 * ASpellInterface.setAspellExeFilename(
		 * spellCheckConfig.get("executable"));
		 */
	}

	public String checkText(String text) {
		ExternalToolsExtensionHandler handler = null;

		try {
			handler = (ExternalToolsExtensionHandler) PluginManager.getInstance()
					.getHandler(ExternalToolsExtensionHandler.NAME);

			File externalToolFile = ExternalToolsManager.getInstance().getLocationOfExternalTool("aspell");

			if (externalToolFile != null) {
				ASpellInterface
						.setAspellExeFilename(externalToolFile.getPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String checked = ASpellInterface.checkBuffer(text);

		if (checked == null) {
			// Display error ?
			// As it is inmutable
			return text;
		} else {
			return checked;
		}
	}
}

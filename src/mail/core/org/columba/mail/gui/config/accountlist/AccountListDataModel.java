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
package org.columba.mail.gui.config.accountlist;

import javax.swing.table.AbstractTableModel;

import org.columba.mail.config.AccountItem;
import org.columba.mail.config.AccountList;
import org.columba.mail.util.MailResourceLoader;


class AccountListDataModel extends AbstractTableModel {
    final String[] columnNames = {
        MailResourceLoader.getString("dialog", "account", "accountname"), //$NON-NLS-1$
        MailResourceLoader.getString("dialog", "account", "type"), //$NON-NLS-1$
    };
    private AccountList accountList;

    public AccountListDataModel(AccountList list) {
        super();
        this.accountList = list;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return accountList.count();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        AccountItem item = accountList.get(row);

        if (item == null) {
            return "";
        }

        if (col == 0) {
            /*
String description = item.getName();
if ( description == null ) return "";
return description;
*/
            return item;
        } else {
            return item.isPopAccount() ? "POP3" : "IMAP4";
        }
    }

    public Class getColumnClass(int c) {
        if (c == 0) {
            return AccountItem.class;
        } else {
            return String.class;
        }
    }

    /*
public boolean isCellEditable(int row, int col)
{
    if ( col == 1 )
       return true;
    else
       return false;
}
*/
}

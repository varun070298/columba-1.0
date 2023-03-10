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
package org.columba.mail.gui.config.filter;

import javax.swing.table.AbstractTableModel;

import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterList;
import org.columba.mail.util.MailResourceLoader;


class FilterListDataModel extends AbstractTableModel {
    final String[] columnNames = {
        MailResourceLoader.getString("dialog", "filter",
            "description_tableheader"),
        MailResourceLoader.getString("dialog", "filter", "enabled_tableheader")
    };
    private FilterList filterList;

    public FilterListDataModel(FilterList list) {
        super();
        this.filterList = list;
    }

    /** {@inheritDoc} */
    public int getColumnCount() {
        return columnNames.length;
    }

    /** {@inheritDoc} */
    public int getRowCount() {
        return filterList.count();
    }

    /** {@inheritDoc} */
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /** {@inheritDoc} */
    public Object getValueAt(int row, int col) {
        Filter filter = filterList.get(row);

        if (filter == null) {
            return "";
        }

        if (col == 0) {
            // description
            String description = filter.get("description");

            if (description == null) {
                return "";
            }

            return description;
        } else {
            // enabled/disabled
            boolean enabled = filter.getBoolean("enabled");

            return enabled ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    /** {@inheritDoc} */
    public Class getColumnClass(int c) {
        if (c == 0) {
            return String.class;
        } else {
            return Boolean.class;
        }
    }

    /** {@inheritDoc} */
    public boolean isCellEditable(int row, int col) {
        return col == 1;
    }

    /** {@inheritDoc} */
    public void setValueAt(Object value, int row, int col) {
        if (col == 1) {
            Filter filter = filterList.get(row);
            filter.setEnabled(((Boolean) value).booleanValue());
        }
    }

    /**
 * Returns the filter at the specified row/index.
 * @param row the row.
 * @return a Filter;
 */
    public Filter getFilter(int row) {
        return filterList.get(row);
    }

    /**
 * Inserts the filter into the filter list at the end of the filter list.
 * @param newFilter the filter to insert at the end.         *
 * @throws IndexOutOfBoundsException if the index is out of range.
 */
    public void addFilter(Filter newFilter) throws IndexOutOfBoundsException {
        int row = filterList.count();
        filterList.add(newFilter);
        fireTableRowsInserted(row, row);
    }

    /**
 * Inserts the filter into the filter list at the specified index.
 * Filters that are at a position below the index (higher) is moved down.
 * @param newFilter the filter to insert.
 * @param index the positiong in the list to add the filter too.
 */
    public void insertFilter(Filter newFilter, int index) {
        filterList.insert(newFilter, index);
        fireTableRowsInserted(index, index);
    }

    /**
 * Removes the filter from the filter list.
 * @param filter the filter to remove.
 */
    public void removeFilter(Filter filter) {
        filterList.remove(filter);
        fireTableDataChanged();
    }
}

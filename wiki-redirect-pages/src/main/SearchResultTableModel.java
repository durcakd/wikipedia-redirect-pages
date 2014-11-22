package main;

import indexing.lucene.WikiIndex;

import javax.swing.table.AbstractTableModel;

public class SearchResultTableModel extends AbstractTableModel {

		private String[] columnNames = { "" }; // columns names
		private Object[][] data = { { "" } }; // table data

		int rows = 0;
		int allRows = WikiIndex.MAX_HINTS; // max rows

		public synchronized int getColumnCount() {
			return columnNames.length;
		}

		public synchronized int getRowCount() {
			return data.length;
		}

		public synchronized String getColumnName(int column) {
			return columnNames[column];
		}

		public synchronized Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);

		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public SearchResultTableModel() {
		}

		/**
		 * Construct and set table model with parameters
	
		 */
		public SearchResultTableModel(String[] columnNames, Object[][] data) {
			if (null == columnNames) {
				throw new NullPointerException();
			} else {
				this.columnNames = columnNames;
			}
			if (null == data) {
				throw new NullPointerException();
			} else {
				this.data = data;
			}

		}

		/**
		 * Update data in table model, new data are in parameters
		 */
		public void updateTableModel(String[] columnNames, Object[][] data) {
			if (null == columnNames) {
				throw new NullPointerException();
			} else {
				this.columnNames = columnNames;
			}
			if (null == data) {
				throw new NullPointerException();
			} else {
				this.data = data;
			}

			// fire change in table model
			super.fireTableStructureChanged();

		}

	}
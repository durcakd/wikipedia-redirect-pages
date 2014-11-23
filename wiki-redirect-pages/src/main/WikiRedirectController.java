package main;

import indexing.lucene.SpellcheckTool;
import indexing.lucene.WikiIndex;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mapReduce.hadoop.WikiRedirectDriver;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

public class WikiRedirectController {

	public static Logger log = Logger.getLogger(WikiRedirectController.class);

	private Window view; // GUI instance
	private WikiRedirectDriver mapReducer; // Map Reduce job
	private WikiIndex wikiIndex;
	private SpellcheckTool spellCheck;

	private final String outFileName = "sdf";
	private String inputFile;
	private String outputFile;
	private List<Document> docs;
	private SearchResultTableModel tableModel;

	public static String DICTIONARY_1 = "sk.txt";
	public static String DICTIONARY_2 = "slovak.txt";

	public static String DEFAULT_HADOOP_DIR = "defaultHadoop";
	public static String DEFAULT_INDEX_DIR = "defaultIndex";

	//public static String TEST_HADOOP_DIR = "testHadoop";
	//public static String TEST_INDEX_DIR = "testIndex";

	public static String TEST_HADOOP_DIR = "defaultHadoop";
	public static String TEST_INDEX_DIR = "defaultIndex";

	public static String DEFAULT_PARSE_FILE = "skwiki-latest-pages-articles.xml";
	public static String DEFAULT_INDEX_FILE = DEFAULT_HADOOP_DIR + "//part-r-00000";





	public static String ACTUAL_FIELD = WikiIndex.FIELD_TITLE;


	/**
	 * Constructor of Controll class in MVC
	 */
	public WikiRedirectController(Window view) {
		inputFile = null;
		outputFile = null;
		this.view = view;
		this.mapReducer = new WikiRedirectDriver();
		this.wikiIndex = new WikiIndex();
		this.spellCheck = new SpellcheckTool(DICTIONARY_1);
		this.docs = new ArrayList<Document>();
		tableModel = new SearchResultTableModel();

		view.getTable().setModel(tableModel);
		view.getTable().getColumnModel().getColumn(0).setMaxWidth(80);
		view.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		// adding listeners
		view.addSearchBTListener( new SearchListener());
		view.addParseBTListener( new StartParseListener());
		view.addIndexBTListener( new StartIndexListener());
		view.addTableSelectionListener( new SelectionListener());

		ChangeSearchFieldListener changeFieldListener = new ChangeSearchFieldListener();
		view.addTitleRBListener( changeFieldListener);
		view.addTextRBListener( changeFieldListener);

		SpellCheckItemListener spellCheckItemListener = new SpellCheckItemListener(view.getQueryTF());
		SearchTextChangeListener searchTextChangeListener = new SearchTextChangeListener(spellCheckItemListener);

		view.getQueryTF().getDocument().addDocumentListener( searchTextChangeListener);
		view.getQueryCB().addActionListener(spellCheckItemListener );
		
	}

	/**
	 * Listener for starting search in index by text in query text field
	 */
	class SearchListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					String query = view.getQueryTF().getText();
					docs =  wikiIndex.search( ACTUAL_FIELD, query, DEFAULT_INDEX_DIR);
					String[] columnNames = { WikiIndex.FIELD_SCORE, WikiIndex.FIELD_TITLE, WikiIndex.FIELD_TEXT};
					Object[][] data = new Object[docs.size()][];

					// data from docs to dataset
					for (int row = 0; row < docs.size(); row++) {
						data[row] = new Object[columnNames.length];
						
						for(int c=0; c < columnNames.length; c++) {
							data[row][c] = docs.get(row).get( columnNames[c]);
						}
						
					}			
					tableModel.updateTableModel(columnNames, data);	
				}
			});
		}
	}

	/**
	 * Listener for selecting document from table
	 */
	class SelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					int row = view.getTable().getSelectedRow();
					int column = view.getTable().getSelectedColumn();

					// if is something selected
					if (row >= 0 && column >= 0 && docs.size() > row) {
						view.getTextArea().setText("");
						view.getTextArea().append(docs.get(row).get(WikiIndex.FIELD_TITLE));
						view.getTextArea().append("\n \n");
						view.getTextArea().append(docs.get(row).get(WikiIndex.FIELD_TEXT));
						log.info("selected row " + row + " from table");
					} 		
				}
			});
		}
	}

	/**
	 * Listener for starting parse wiki dump file in hadoop
	 */
	class StartParseListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					final JFileChooser fc = new JFileChooser(".");
					int returnVal = fc.showOpenDialog(view);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String filePath = fc.getSelectedFile().getAbsolutePath();
						DEFAULT_INDEX_FILE = TEST_HADOOP_DIR + "//part-r-00000";
						DEFAULT_HADOOP_DIR = TEST_HADOOP_DIR;
						log.info("Opening:          " + filePath);			            
						log.info("Hadoop result in: " + DEFAULT_INDEX_FILE);     

						mapReducer.doWikiMapReduceJob(filePath, TEST_HADOOP_DIR);	
					} else {
						log.info("Open command cancelled by user.");
					}
				}
			});
		}
	}

	/**
	 * Listener for selecting output parsing file and parsing and indexing it 
	 */
	class StartIndexListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					JFileChooser fc = new JFileChooser(".");
					int returnVal = fc.showOpenDialog(view);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String filePath = fc.getSelectedFile().getAbsolutePath();
						DEFAULT_INDEX_DIR = TEST_INDEX_DIR;
						log.info("Opening:          " + filePath);			            
						log.info("index loaction:   " + DEFAULT_INDEX_DIR);     

						wikiIndex.indexFile( filePath, DEFAULT_INDEX_DIR);
					} else {
						log.info("Open command cancelled by user.");
					}
				}
			});
		}
	}

	/**
	 * Listener for changing default document field that will by search for by
	 */
	class ChangeSearchFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (view.getTitleRB().isSelected()) {
						ACTUAL_FIELD = wikiIndex.FIELD_TITLE;
					} else {
						ACTUAL_FIELD = wikiIndex.FIELD_TEXT;	
					}
					log.info("Actual selected search field: " +  ACTUAL_FIELD);
				}
			});
		}
	}
	
	/**
	 * Listener for updating spell check combo box with suggestions
	 */
	class SearchTextChangeListener implements DocumentListener {
		SpellCheckItemListener spellCheckItemListener;
		
		public SearchTextChangeListener(SpellCheckItemListener listener) {
			spellCheckItemListener = listener;
		}
		
		@Override
		public void changedUpdate(DocumentEvent arg0) {
			change();
		}
		@Override
		public void insertUpdate(DocumentEvent arg0) {
			change();
		}
		@Override
		public void removeUpdate(DocumentEvent arg0) {
			change();
		}

		protected void change() {
			if ( null!=spellCheckItemListener) {
				spellCheckItemListener.enableAction(false);
			}
			String query = view.getQueryTF().getText();
			if ( null!=query && !query.isEmpty()) {
				String[] suggestions = spellCheck.searchSuggestions(query);

				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) view.getQueryCB().getModel();
				model.removeAllElements();
				for(String str : suggestions){
					model.addElement( str);
				}

			}
			if ( null!=spellCheckItemListener) {
				spellCheckItemListener.enableAction(true);
			}
		}
	}

	/**
	 * Listener for selecting document from table
	 */
	class SpellCheckItemListener implements ActionListener {
		private boolean isActionEnabled = true;
		private JTextField queryTF;

		public SpellCheckItemListener(JTextField queryTF) {
			this.queryTF = queryTF;
		}
				
		public void enableAction(boolean enable) {
			isActionEnabled = enable;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {

			if( isActionEnabled){
				JComboBox cb = ((JComboBox) e.getSource());
				queryTF.setText(cb.getSelectedItem().toString());
			}
		}
	}

}

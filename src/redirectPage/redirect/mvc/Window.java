package redirectPage.redirect.mvc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;

/**
 * @author David Durcak
 * Information Retrieval - project: Parsing redirection pages
 * 
 * This GUI window
 */
public class Window extends JFrame {

	private JTextField queryTF;
	private JComboBox<String> queryCB;
	private JRadioButton titleRB;
	private JRadioButton textRB;
	private JButton searchBT;
	private JButton parseBT;
	private JButton indexBT;
	private JTextArea textArea;
	private JTable table;
	private JLabel stateLabel;

	public void addSearchBTListener(ActionListener searchBTListener) {
		searchBT.addActionListener(searchBTListener);
	}
	public void addParseBTListener(ActionListener parseBTListener) {
		parseBT.addActionListener(parseBTListener);
	}
	public void addIndexBTListener(ActionListener indexBTListener) {
		indexBT.addActionListener(indexBTListener);
	}
	public void addTitleRBListener(ActionListener titleRBListener) {
		titleRB.addActionListener(titleRBListener);
	}
	public void addTextRBListener(ActionListener textRBListener) {
		textRB.addActionListener(textRBListener);
	}
	public void addTableSelectionListener(
			ListSelectionListener selectionListener) {
		table.getSelectionModel().addListSelectionListener(selectionListener);
	}

	/**
	 * Create the frame.
	 */
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel topPanel = new JPanel();
		JPanel centralPanel = new JPanel();
		JPanel bottomPanel = new JPanel();

		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.X_AXIS));

		JPanel controllPanel1 = new JPanel();
		JPanel controllPanel2 = new JPanel();
		topPanel.add(controllPanel1);
		topPanel.add(controllPanel2);

		// control 1
		queryTF = new JTextField();
		queryTF.setColumns(20);
		queryCB = new JComboBox<String>();
	    queryCB.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXX"); 
		
		parseBT = new JButton("Parse");
		Component horizontalStrut = Box.createHorizontalStrut(200);		
		controllPanel1.add(queryTF);
		controllPanel1.add(queryCB);
		controllPanel1.add(horizontalStrut);
		controllPanel1.add(parseBT);


		// control 2
		searchBT = new JButton("Search");
		ButtonGroup bg = new ButtonGroup( );
		titleRB = new JRadioButton("Title", true);
		textRB = new JRadioButton("Text");
		stateLabel = new JLabel("                            ");
	
		indexBT = new JButton("Index");
		bg.add(titleRB);
		bg.add(textRB);
		Component horizontalStrut_1 = Box.createHorizontalStrut(400);
		controllPanel2.add(searchBT);
		controllPanel2.add(titleRB);
		controllPanel2.add(textRB);
		controllPanel2.add(stateLabel);
		controllPanel2.add(horizontalStrut_1);
		controllPanel2.add(indexBT);

		// central
		textArea = new JTextArea();
		JScrollPane textScroll = new JScrollPane();
		textScroll.setMinimumSize(new Dimension(800, 100));
		textScroll.setViewportView(textArea);
		centralPanel.add(textScroll, BorderLayout.CENTER);	

		contentPane.add(topPanel, BorderLayout.NORTH);
		contentPane.add(centralPanel, BorderLayout.CENTER);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout(0, 0));

		// bottom 
		table = new JTable();

		bottomPanel.add(table, BorderLayout.CENTER);
	}
	// GETTER    SETTERS

	public JTextField getQueryTF() {
		return queryTF;
	}

	public void setQueryTF(JTextField queryTF) {
		this.queryTF = queryTF;
	}

	public JButton getSearchBT() {
		return searchBT;
	}

	public void setSearchBT(JButton searchBT) {
		this.searchBT = searchBT;
	}

	public JRadioButton getTitleRB() {
		return titleRB;
	}

	public void setTitleRB(JRadioButton titleRB) {
		this.titleRB = titleRB;
	}

	public JRadioButton getTextRB() {
		return textRB;
	}

	public void setTextRB(JRadioButton textRB) {
		this.textRB = textRB;
	}

	public JButton getParseBT() {
		return parseBT;
	}

	public void setParseBT(JButton parseBT) {
		this.parseBT = parseBT;
	}

	public JButton getIndexBT() {
		return indexBT;
	}

	public void setIndexBT(JButton indexBT) {
		this.indexBT = indexBT;
	}

	public JTextArea getTextArea() {
		return textArea;
	}

	public void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	public JComboBox<String> getQueryCB() {
		return queryCB;
	}
	public void setQueryCB(JComboBox<String> queryCB) {
		this.queryCB = queryCB;
	}
	
	public void setPromtMsg(String msg) {
		this.stateLabel.setText(msg);;
	}
}
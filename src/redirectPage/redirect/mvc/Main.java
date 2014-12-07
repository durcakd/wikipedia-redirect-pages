package redirectPage.redirect.mvc;

import java.awt.EventQueue;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author David Durcak
 * Information Retrieval - project: Parsing redirection pages
 */
public class Main {
	/**
	 * Main method
	 * 
	 * @param args
	 */
	
	public static void main(String[] args) {
		Logger rootLogger = Logger.getRootLogger();
	    rootLogger.setLevel(Level.INFO);
	    rootLogger.addAppender(new ConsoleAppender(
	    //          new PatternLayout("%-6r [%p] %c - %m%n")));
	              new PatternLayout("%m%n")));
	    
	    
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// create windows
					Window theView = new Window();

					// controller
					WikiRedirectController theController = new WikiRedirectController(theView);

					// set nice Windows design
					try {
						UIManager.setLookAndFeel(UIManager
								.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (UnsupportedLookAndFeelException e) {
						e.printStackTrace();
					}
					SwingUtilities.updateComponentTreeUI(theView);

					// start window
					theView.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}

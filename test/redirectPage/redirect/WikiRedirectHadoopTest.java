package redirectPage.redirect;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import redirectPage.redirect.index.lucene.WikiIndex;
import redirectPage.redirect.mapReduce.hadoop.WikiRedirectDriver;

public class WikiRedirectHadoopTest {
	
    
	public static Logger log = Logger.getLogger(WikiRedirectHadoopTest.class);

	public static String inputData = "data//sample_input_redirect_skwiki-latest-pages-articles.xml";
	public static String hadoop_out_Dir = "outputHadoopTest";
	public static String lucene_index_Dir = "luceneIndexTest";
	
	public static String hadoop_out_Data = hadoop_out_Dir + "/part-r-00000";
	public static String sample_out_Data = "data//sample_output_redirect_skwiki-latest-pages-articles.txt";


	@BeforeClass
	public static void initData() throws Exception {
		Logger rootLogger = Logger.getRootLogger();
	    rootLogger.setLevel(Level.INFO);
	    rootLogger.addAppender(new ConsoleAppender(
	               new PatternLayout("%-6r [%p] %c - %m%n")));
	    
		String[] args = new String[2];
		args[0] = inputData;
		args[1] = hadoop_out_Dir;

		log.info("HADOOP BEFORE");
		WikiRedirectDriver driver = new WikiRedirectDriver();
		int exitCode = ToolRunner.run(driver, args);
		Assert.assertEquals(0, exitCode);
		
		log.info("HADOOP DONE");
	}


	@Test
	public void testHadoopOutput() {

		try {
			BufferedReader in1 = new BufferedReader(new FileReader(sample_out_Data));
			BufferedReader in2 = new BufferedReader(new FileReader(hadoop_out_Data));
			int count = 0; 
			while (in1.ready()) {
				String line1 = in1.readLine();
				if( !in2.ready()) {
					log.error("Hadoop out is shorter. first missing line:" + line1);
					fail("Hadoop out is shorter. first missing line:");
				}
				String line2 = in2.readLine();
				
				if (line1.compareTo(line2) != 0){
					log.error("Lines are not same! Original:" + line1 + " >>>>> <<<<< Output: " +  line2);
					fail("Lines are not same!");
				}
				log.info("Compare ok line " + count + ": " + line1);
				count++;
			}
			if( in2.ready()) {
				String line2 = in2.readLine();
				log.error("Hadoop out is longer, with line:" + line2);
				fail("Hadoop out is longer, with line");
			}
			in1.close();
			in2.close();

		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException", e);
			fail("FileNotFoundException");
		} catch (IOException e) {
			log.error("IOException", e);
			fail("IOException");
		}
		//fail("Not yet implemented");
	}
	
	@After
	public void testLuceneIndex() {
		WikiIndex wikiIndex = new WikiIndex();
		wikiIndex.indexFile( hadoop_out_Data, lucene_index_Dir);
		
		
		Assert.assertEquals(0 , wikiIndex.search("Title", "Heroes", lucene_index_Dir).size());
		Assert.assertEquals(0 , wikiIndex.search("Title", "gorán", lucene_index_Dir).size());
		Assert.assertEquals(0 , wikiIndex.search("Title", "ale", lucene_index_Dir).size());
		Assert.assertEquals(0 , wikiIndex.search("Text", "hrad", lucene_index_Dir).size());
	
		String preklapaciObvod = wikiIndex.search("Title", "Preklápací obvod", lucene_index_Dir).get(0).get(WikiIndex.FIELD_TEXT);
		String klopnyObvod = wikiIndex.search("Title", "Klopný obvod", lucene_index_Dir).get(0).get(WikiIndex.FIELD_TEXT);

		Assert.assertEquals(preklapaciObvod.length(), klopnyObvod.length());
		Assert.assertEquals(preklapaciObvod.length(), 11525);
			
		String bistabilnyPreklapaci = wikiIndex.search("Title", "Bistabilný preklápací obvod", lucene_index_Dir).get(0).get(WikiIndex.FIELD_TEXT);
		String bistabilnyKlopny = wikiIndex.search("Title", "Bistabilný klopný obvod", lucene_index_Dir).get(0).get(WikiIndex.FIELD_TEXT);
		
		Assert.assertEquals(bistabilnyPreklapaci.length(), bistabilnyKlopny.length());
		Assert.assertEquals(bistabilnyPreklapaci.length(), 6452);
			
		String preklapaciT = wikiIndex.search("Title", "Preklápací obvod T", lucene_index_Dir).get(0).get(WikiIndex.FIELD_TEXT);
		String preklapaciJK = wikiIndex.search("Title", "Preklápací obvod JK", lucene_index_Dir).get(0).get(WikiIndex.FIELD_TEXT);
		
		Assert.assertEquals(preklapaciT.length(), 1148);
		Assert.assertEquals(preklapaciJK.length(), 1376);
	}
}

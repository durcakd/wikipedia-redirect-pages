package redirectPage.redirect.index.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import redirectPage.redirect.parsing.WikiRedirectParser;

/**
 * @author David Durcak
 * Information Retrieval - project: Parsing redirection pages
 * 
 * This class is responsible for indexing and searching in that index
 */
public class WikiIndex {

	IndexWriter indexWriter;
	Directory index;
	IndexWriterConfig writerConfig;
	Analyzer analyzer;
	int counterOfWrongRedirect;

	public static Logger log = Logger.getLogger(WikiIndex.class);
	public static int MAX_HINTS = 10;
	public static final String FIELD_TITLE = "Title";
	public static final String FIELD_TEXT = "Text";
	public static final String FIELD_SCORE = "Score";
	public static final String STOP_WORDS_FILE = "stop-words-slovak.txt";

	public WikiIndex () {

		indexWriter = null;
		index = null;
		writerConfig = null;
		analyzer = new CustomAnalyzer(Version.LUCENE_CURRENT);
	}

	public WikiIndex (String stopWordsFile) {
		indexWriter = null;
		index = null;
		writerConfig = null;
		try {
			analyzer = new CustomAnalyzer(Version.LUCENE_CURRENT, stopWordsFile);
		} catch (IOException e) {
			log.error("Error create analyzer with custom stopwords ",e);
			analyzer = new CustomAnalyzer(Version.LUCENE_CURRENT);
		}
	}

	class CustomAnalyzer extends StopwordAnalyzerBase {
		private Version ver;

		public CustomAnalyzer(Version matchVersion){
			super(matchVersion, StandardAnalyzer.STOP_WORDS_SET);
			ver = matchVersion;
		}

		public CustomAnalyzer(Version matchVersion, String stopWordsFile) throws IOException{
			super(matchVersion, StopwordAnalyzerBase.loadStopwordSet( new File(stopWordsFile), matchVersion));
			ver = matchVersion;
		}

		@Override
		protected TokenStreamComponents createComponents(String arg0, Reader reader) {
			final Tokenizer source = new StandardTokenizer(ver, reader);

			TokenStream tokenStream = source;
			tokenStream = new StandardFilter(ver, tokenStream);
			tokenStream = new LowerCaseFilter(ver, tokenStream);
			tokenStream = new StopFilter(ver, tokenStream, getStopwordSet());
			tokenStream = new ASCIIFoldingFilter(tokenStream);
			return new TokenStreamComponents(source, tokenStream);
		}
	}


	public void indexFile( String filePath, String indexDir) {
		counterOfWrongRedirect = 0;
		writerConfig = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
		

		FileInputStream inputStream = null;
		try {
			log.info( "START index File: ");
			index = FSDirectory.open(new File(indexDir));
			indexWriter = new IndexWriter( index, writerConfig);

			inputStream = new FileInputStream( filePath );
			Scanner sc = new Scanner(inputStream, "UTF-8");

			StringBuffer sb = new StringBuffer();

			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				line = line.trim();
				if (line.endsWith(WikiRedirectParser.DOCDEL)) {
					
					sb.append(line.replaceFirst(WikiRedirectParser.DOCDEL, ""));
					String page = sb.toString();

					indexPage( page);

					sb = new StringBuffer();
				} else {
					sb.append(line);
					sb.append("\n");
				}
			}

			log.info( "     index File: ... DONE ");
			// note that Scanner suppresses exceptions
			if (sc.ioException() != null) {
				log.error("SCANNER ERRO io exception:", sc.ioException());
			}
			sc.close();
			indexWriter.close();
			inputStream.close();
		} catch (FileNotFoundException e) {	
			log.error( "Error: not found file: " + filePath, e);
		} catch (IOException e) {
			log.error( "Error: create index writer ", e);
		}
		finally {
			try {
				inputStream.close();
				log.info("INCORRECT REDIRECT COUNT: " + counterOfWrongRedirect);
			} catch (IOException e) {
				log.error( "Error: close file", e);
			}
		}
	} 

	protected void indexPage( String value) {
		String[] mainSplit;
		try{
			mainSplit = value.toString().split(WikiRedirectParser.DEL);
			if (mainSplit.length == 3){
				String text = mainSplit[2].trim();

				String[] titles = mainSplit[0].split(WikiRedirectParser.SUBDEL);
				for(String title: titles) {
					title = title.trim();
					if(!title.isEmpty()){
						if ( null != indexWriter){
							if ( text.trim().isEmpty()) {
								log.info("WRONG-RED: " + title);
								counterOfWrongRedirect++;
							} else {
								addDoc( indexWriter, title, text);
							}
						}
					}
				}
			} else {
				log.info("NOT EXACTLY 3 FIELDS IN RESULT\n" + value);
			}

		}catch (Exception e ) {
			log.error("ERROR  ", e);
			log.info("VALUE\n" + value);
		}
	}

	protected void addDoc(IndexWriter w, String title, String text) {
		Document doc = new Document();
		doc.add(new TextField(FIELD_TITLE, title, Field.Store.YES));
		doc.add(new TextField(FIELD_TEXT, text, Field.Store.YES));
		try {
			w.addDocument(doc);
		} catch (IOException e) {
			log.error( "Error: write to index ", e);
		}
	}

	public List<Document> search( String field, String str, String indexDir) {
		List<Document> resDocs = new ArrayList<Document>();
		File indexFile = new File( indexDir);

		try {
			index = FSDirectory.open(indexFile);

			DirectoryReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);

			Query query = createQuery( field, str);

			TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_HINTS, true);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			System.out.println(str +  " >>>> Found " + hits.length + " hits.");
			for(int i=0;i<hits.length;++i) {
				int docId = hits[i].doc;
				Document doc = searcher.doc(docId);
				doc.add( new TextField(FIELD_SCORE, String.valueOf(hits[i].score), Field.Store.NO));

				resDocs.add(doc);
			}		
			reader.close();

		} catch (IOException e) {
			log.error( "Error: open index ", e);
		} 
		return resDocs;
	}

	protected Query createQuery( String field, String str) {
		try {
			QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, field ,analyzer);		
			return parser.parse(str);

		} catch (ParseException e) {
			log.error( "Error: query parsing: ", e);
		}
		return null;
	}
}

package indexing.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mapReduce.parse.WikiRedirectParser;

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

public class WikiIndex {

	IndexWriter indexWriter;
	Directory index;
	IndexWriterConfig writerConfig;
	Analyzer analyzer;

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
		//analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT,new CharArraySet(Version.LUCENE_45, createStopWord(), true));
		//analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		//analyzer = new PolishAnalyzer(Version.LUCENE_CURRENT); - not work

		analyzer = new CustomAnalyzer(Version.LUCENE_CURRENT);

	}

	class CustomAnalyzer extends StopwordAnalyzerBase {
		private Version ver;
		
		 public CustomAnalyzer(Version matchVersion){
		        super(matchVersion, StandardAnalyzer.STOP_WORDS_SET);
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
		//analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		writerConfig = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
		//writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

		int lcounter = 0;
		int pcounter = 0;

		FileInputStream inputStream = null;
		try {
			log.info( "START index File: ");
			index = FSDirectory.open(new File(indexDir));
			indexWriter = new IndexWriter( index, writerConfig);

			inputStream = new FileInputStream( filePath );
			Scanner sc = new Scanner(inputStream, "UTF-8");

			StringBuffer sb = new StringBuffer();

			while (sc.hasNextLine()) {
				//log.info( "line: " + ++lcounter);
				String line = sc.nextLine();
				line = line.trim();
				if (line.endsWith(WikiRedirectParser.DOCDEL)) {
					//log.info("page  " + ++pcounter);

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
		} catch (FileNotFoundException e) {	
			log.error( "Error: not found file: " + filePath, e);
		} catch (IOException e) {
			log.error( "Error: create index writer ", e);
		}
		finally {
			try {
				inputStream.close();
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
				String title = mainSplit[0].split(WikiRedirectParser.SUBDEL)[0].trim();
				String text = mainSplit[2].trim();
				//log.info( "TITLE:  " + title + "   TEXT:" + text.subSequence(0, 10));
				if ( null != indexWriter){
					addDoc( indexWriter, title, text);
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
		//log.info( "TITLE:  " + title + "   TEXT:" + text.subSequence(0, 5));
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
				//log.info(str +  " >>>> TITLE: " + doc.get("title") + " TEXT:" + doc.get("text"));
				log.info(str +  " >>>> TITLE: " + doc.get("title"));
			}		
			reader.close();

		} catch (IOException e) {
			log.error( "Error: open index ", e);
		} 
		return resDocs;
	}

	protected Query createQuery( String field, String str) {
		try {
			//analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, field ,analyzer);		
			return parser.parse(str);

		} catch (ParseException e) {
			log.error( "Error: query parsing: ", e);
		}
		return null;
	}

	public List<String> createStopWord() {
		List<String> stopwords = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(STOP_WORDS_FILE));
			String line = null;
			while ((line = reader.readLine()) != null) {
				stopwords.add(line);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			log.error( "Error: stopwords file not found :", e);
		} catch (IOException e) {
			log.error( "Error: read stopwords file: ", e);
		}
		return stopwords;
	}





















}

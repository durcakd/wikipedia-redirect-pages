package indexing.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class SpellcheckTool {
	public static Logger log = Logger.getLogger(SpellcheckTool.class);

	public final static int suggestionsNumber = 8;

	private SpellChecker spellChecker = null;


	public SpellcheckTool( String plainDictionaryFile, String indexDir) {
		try {
			File dir = new File(indexDir);	
			Directory index = FSDirectory.open(dir);
			createSpellcheckIndex( index, plainDictionaryFile);

		} catch (IOException e) {
			log.error("Error open spellcheck index directory", e);
		}
	}

	public SpellcheckTool( String plainDictionaryFile) {
		Directory index = new RAMDirectory();
		createSpellcheckIndex( index, plainDictionaryFile);
	}

	private void createSpellcheckIndex( Directory index, String plainDictionaryFile) {
		try {
			Dictionary dictionary = new PlainTextDictionary(new File( plainDictionaryFile));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer );

			spellChecker = new SpellChecker(index);
			spellChecker.indexDictionary( dictionary, writerConfig, true);

		} catch (IOException e) {
			log.error("Error create spellcheck index", e);
		}
	}

	public String[] searchSuggestions(String query) {
		String[] suggestions = null;
		try {
			if (spellChecker.exist(query) ) {
				suggestions = new String[]{query};
			}
			suggestions = spellChecker.suggestSimilar(query, suggestionsNumber);

			if ( null!=suggestions && suggestions.length>0) {
				for (String word : suggestions) {
					System.out.println("Did you mean:" + word);
				}
			} else {
				System.out.println("No suggestions found for word:"+query);
				suggestions = new String[]{"no suggesion"};
			}
		} catch (IOException e) {
			log.error("Error read in spellcheck index", e);
		}
		return suggestions;
	}
}

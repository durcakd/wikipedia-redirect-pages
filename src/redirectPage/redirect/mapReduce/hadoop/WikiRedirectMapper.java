package redirectPage.redirect.mapReduce.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import redirectPage.redirect.parsing.WikiRedirectParser;

/**
 * @author David Durcak
 * Information Retrieval - project: Parsing redirection pages
 * 
 * Hadoop Mapper
 */
public class WikiRedirectMapper extends Mapper<LongWritable, Text, Text, Text> {

	public static Logger log = Logger.getLogger(WikiRedirectMapper.class);
	
	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		WikiRedirectParser parser = new WikiRedirectParser();
		
		String output = parser.parse( value.toString());
		String title = parser.getTitle();
		if (null != title) {
			Text newValue = new Text( output);
			Text newKey   = new Text( title);
			
			context.write( newKey, newValue);
		}	
	}
	
}
package mapReduce.hadoop;

import java.io.IOException;

import mapReduce.parse.WikiRedirectParser;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;



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
			//log.info(">>>****************************************");
			//log.info(">>>KEY:    " + title);
			//log.info(">>>VALUE : " + output);
			
			context.write( newKey, newValue);
		}	
	}
	
}
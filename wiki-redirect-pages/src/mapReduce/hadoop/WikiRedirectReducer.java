package mapReduce.hadoop;

import java.io.IOException;

import mapReduce.parse.WikiRedirectParser;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

public class WikiRedirectReducer extends Reducer<Text, Text, Text, Text> {

	public static Logger log = Logger.getLogger(WikiRedirectReducer.class);
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		/*
		String reds = "";
		String subs = "";
		String text = "";
		
		for (Text value : values) {
			String[] mainSplit = value.toString().split(";||;");
			reds = reds + ";|;" + mainSplit[0];
			subs = subs + ";|;" + mainSplit[1];
			text = text + mainSplit[2];
		}
		
		
		if ( !text.trim().isEmpty() && !subs.trim().isEmpty()) {
			WikiRedirectParser parser = new WikiRedirectParser();
			
			String[] subsArray = subs.split(";|;");
			for (String sub : subsArray ) {

				String subText = ";|; ;|;" + parser.findSubtext(text, sub);
				log.info(">>>===========================================");
				log.info(sub);
				log.info(subText);
				
				context.write( new Text(sub), new Text(subText));
			}
			subs = " ";
		}
		
		StringBuffer resBuf = new StringBuffer();		
		resBuf.append(reds);
		resBuf.append(";||;");
		resBuf.append(subs);
		resBuf.append(";||;");
		resBuf.append(text);
		log.error(resBuf.toString());
		Text reducedValue = new Text( resBuf.toString());
		
		log.info(">>>===========================================");
		log.info(key.toString());
		log.info(reducedValue.toString());
		context.write(key, reducedValue);
		*/
		
		log.info(">>>===========================================");
		context.write(key, new Text("sd"));
		
		
	}
}

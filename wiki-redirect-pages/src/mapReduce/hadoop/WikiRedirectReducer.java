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

		String reds = "";
		String subs = "";
		String text = "";

		for (Text value : values) {
			//log.info("..................");
			//log.info("KEY:   " + key.toString());
			//log.info("VALUE: " + value.toString());
			String[] mainSplit = value.toString().split(WikiRedirectParser.DEL);
			
			//log.info("REDIRECT    " + mainSplit[0]);
			//log.info("SUBREDIRECT " + mainSplit[1]);
			//log.info("TEXT        " + mainSplit[2]);
			if (! mainSplit[0].trim().isEmpty()){
				reds = reds + WikiRedirectParser.SUBDEL + mainSplit[0];
			
			}
			if (! mainSplit[1].trim().isEmpty()){	
				subs = subs + WikiRedirectParser.SUBDEL + mainSplit[1];
			}

			text = text + mainSplit[2].replaceFirst(WikiRedirectParser.DOCDEL, "");
		}


		if ( !text.trim().isEmpty() && !subs.trim().isEmpty()) {
			WikiRedirectParser parser = new WikiRedirectParser();

			//log.info("SUBS: " + subs);
			String[] subsArray = subs.split(WikiRedirectParser.SUBDEL);
			for (String sub : subsArray ) {
				if (!sub.trim().isEmpty()){
					String subText = parser.findSubtext(text, sub);
					//log.info(">>>===========================================");
					//log.info(sub);
					//log.info(subText);
					
					StringBuffer subBuf = new StringBuffer();		
					subBuf.append(" ");
					subBuf.append( WikiRedirectParser.DEL);
					subBuf.append(" ");
					subBuf.append( WikiRedirectParser.DEL);
					subBuf.append(subText);
					subBuf.append( WikiRedirectParser.DOCDEL);
					subText = subBuf.toString();
					
					
					context.write( new Text(sub), new Text(subText));
				}
			}
			subs = " ";
		}

		StringBuffer resBuf = new StringBuffer();		
		resBuf.append(reds);
		resBuf.append( WikiRedirectParser.DEL);
		resBuf.append(subs);
		resBuf.append( WikiRedirectParser.DEL);
		resBuf.append(text);
		resBuf.append( WikiRedirectParser.DOCDEL);
		//log.error(resBuf.toString());
		Text reducedValue = new Text( resBuf.toString());

		//log.info(">>>===========================================");
		//log.info("KEY:   " + key.toString());
		//log.info("VALUE: " + reducedValue.toString());
		context.write(key, reducedValue);



	}
}

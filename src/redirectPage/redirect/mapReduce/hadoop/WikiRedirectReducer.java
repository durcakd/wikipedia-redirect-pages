package redirectPage.redirect.mapReduce.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import redirectPage.redirect.parsing.WikiRedirectParser;

/**
 * @author David Durcak
 * Information Retrieval - project: Parsing redirection pages
 * 
 * Hadoop Reducer
 */
public class WikiRedirectReducer extends Reducer<Text, Text, Text, Text> {

	public static Logger log = Logger.getLogger(WikiRedirectReducer.class);

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

		String reds = "";
		String subs = "";
		String text = "";

		for (Text value : values) {
			String[] mainSplit = value.toString().split(WikiRedirectParser.DEL);

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

			String[] subsArray = subs.split(WikiRedirectParser.SUBDEL);
			for (String sub : subsArray ) {
				if (!sub.trim().isEmpty()){

					String[] pair = sub.split("#");
					String realSubTitle = pair[0];
					String newTitle = pair[1];

					String subText = parser.findSubtext(text, realSubTitle, newTitle, key.toString());

					StringBuffer subBuf = new StringBuffer();		
					subBuf.append(" ");
					subBuf.append( WikiRedirectParser.DEL);
					subBuf.append(" ");
					subBuf.append( WikiRedirectParser.DEL);
					subBuf.append(subText);
					subBuf.append( WikiRedirectParser.DOCDEL);
					subText = subBuf.toString();

					context.write( new Text(newTitle), new Text(subText));
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
		Text reducedValue = new Text( resBuf.toString());

		context.write(key, reducedValue);
	}
}
package mapReduce.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

public class WikiRedirectReducer extends Reducer<Text, Text, Text, Text> {

	public static Logger log = Logger.getLogger(WikiRedirectReducer.class);
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

		int counter = Integer.valueOf(0);
		for (Text value : values) {
			counter++;
		}
		context.write(key, new Text(String.valueOf(counter)));
	}
}

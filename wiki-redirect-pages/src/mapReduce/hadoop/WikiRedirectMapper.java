package mapReduce.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;



public class WikiRedirectMapper extends Mapper<LongWritable, Text, Text, Text> {

	public static Logger log = Logger.getLogger(WikiRedirectMapper.class);
	
	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		log.info("******************************************************");
		String str = value.toString();//.replaceAll("\n", "++");
		log.info("    " + str);
		context.write(new Text("kluc"), value);
	}
}
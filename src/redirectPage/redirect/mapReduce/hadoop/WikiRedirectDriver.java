package redirectPage.redirect.mapReduce.hadoop;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 * @author David Durcak
 * Information Retrieval - project: Parsing redirection pages
 * 
 * This class is responsible for running map reduce job
 * It can work also works as independent jar for hadoop cluster
 */
public class WikiRedirectDriver extends Configured implements Tool{

	public static Logger log = Logger.getLogger(WikiRedirectDriver.class);

	public int run(String[] args) throws Exception
	{
		if(args.length !=2) {
			System.err.println("Usage: MaxTemperatureDriver <input path> <outputpath>");
			System.exit(-1);
		}
		Configuration conf = new Configuration(true);
		// dont need such regex  conf.set("record.delimiter.regex", regex);
		conf.set("textinputformat.record.delimiter","<page>");

		Job job = new Job(conf);
		job.setJarByClass( WikiRedirectDriver.class);
		job.setJobName( "Wiki redirect pages");

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath( job, new Path(args[1]));

		job.setMapperClass( WikiRedirectMapper.class);
		job.setReducerClass( WikiRedirectReducer.class);

		job.setOutputKeyClass( Text.class);
		job.setOutputValueClass( Text.class);
		job.waitForCompletion(true);
		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;	

	}

	public int doWikiMapReduceJob( String inputFilePath, String outputDirPath) {
		try {
			String[] args = new String[2];
			args[0] = inputFilePath;
			args[1] = outputDirPath;

			log.info(" START Wiki MapReduce job ...");
			WikiRedirectDriver driver = new WikiRedirectDriver();
			int exitcode;

			exitcode = ToolRunner.run(driver, args);

			log.info("       Wiki MapReduce job.... DONE");

			return exitcode;
		} catch (Exception e) {
			log.error("Error: hadoop parse: ", e);
		}
		return 1;

	}

	public static void main(String[] args) throws Exception {


		WikiRedirectDriver driver = new WikiRedirectDriver();
		int exitCode = ToolRunner.run(driver, args);
		System.exit(exitCode);
	}
}
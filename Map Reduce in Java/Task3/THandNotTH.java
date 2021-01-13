// =============================================================================
// Program name: THandNotTH.java
// Author:       Animish Andraskar
// =============================================================================
// DESCRIPTION:
// ------------
// This program counts number of words that contain the exact lowercase
// substring 'th' and also the words which do not contain that substring,
// and then stores the counts in the output file
//
// RESULTS:
// --------
// Words containing "th": 11,681
// Words not containing "th": 224,242
// =============================================================================

package org.myorg;
// Essential imports
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

// This class extends Configured since it is the base class with the implementations of
// getConf() and setConf(), which we need to create our instance variable
public class THandNotTH extends Configured implements Tool {
    // This inner class transforms input records into intermediate records, which are processed
    // further by the reducer. The class extends Mapper interface with input key-value
    // as LongWritable, Text and the output key-value as Text, IntWritable
    public static class WordCountMap extends Mapper<LongWritable, Text, Text, IntWritable>
    {
        // Create an instance of IntWritable with value 1, which is the Hadoop variant of Integer
        // wrapper class and has been optimized for serialization in the Hadoop environment
        private final static IntWritable one = new IntWritable(1);
        // Similarly, create an instance of Text object, which is the Hadoop variant of Java String
        private Text word = new Text();
        
        // Override the map method
        @Override
        // This method maps a single input key/value pair into an intermediate key/value pair
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
        {
            // Get each line from the input file passed in by Hadoop as value
            // Each value is a Hadoop Text line. Convert it to Java String
            String line = value.toString();
            // Tokenize each line i.e. split it into words
            StringTokenizer tokenizer = new StringTokenizer(line);
            // While there are tokens present, traverse them one by one
            while(tokenizer.hasMoreTokens())
            {
                // Read the next word from the token
				String currentToken = tokenizer.nextToken();
                // Check if it contains "th" (case-sensitive)
				if (currentToken.contains("th")) {
                    // Set a fixed key for each mapper so that the number of reducers stays 1
                    // for this particular condition
                	word.set("Words containing \"th\"");
                    // For each word satisfying the condition, the reducer will be called with
                    // ("Words containing \"th\"", [1, 1, 1, ....., 1])
	                context.write(word, one);
				} else {
                    // Set a fixed key for each mapper so that the number of reducers stays 1
                    // for this particular condition
					word.set("Words not containing \"th\"");
                    // For each word satisfying the condition, the reducer will be called with
                    // ("Words not containing \"th\"", [1, 1, 1, ....., 1])
                    context.write(word, one);
				}
            }
        }
    }
    
    // This inner class reduces a set of intermediate values which share a key (in this case a fixed string) to a
    // smaller set of values (in this case the word count). Both input and output: key = Text, value = IntWritable
    public static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable>
    {
        // This method reduces values i.e. generate word count for the given fixed string key
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            // Set count as 0
            int count = 0;
            // Traverse each value in provided values
            for(IntWritable value : values)
            {
                // Increment the count by adding 1, which was stored with the key by the Mapper
                count += value.get();
            }
            // Emit (key = "Words containing \"th\"", value = count of words with substring "th")
            context.write(key, new IntWritable(count));
        }
        
    }
    
    public int run(String[] args) throws Exception  {
        // Create a new Hadoop job
        Job job = new Job(getConf());
        // Set its execution jar as compiled THandNotTH class
        job.setJarByClass(THandNotTH.class);
        // Give it a name
        job.setJobName("THandNotTH");
        
        // Set output key data type as Text
        job.setOutputKeyClass(Text.class);
        // Set output value data type as IntWritable
        job.setOutputValueClass(IntWritable.class);
        
        // Set mapper as compiled WordCountMap class
        job.setMapperClass(WordCountMap.class);
        // Set combiner as compiled WordCountReducer class
        job.setCombinerClass(WordCountReducer.class);
        // Set reducer as compiled WordCountReducer class
        job.setReducerClass(WordCountReducer.class);
        
        // Set input data format as TextInputFormat
        job.setInputFormatClass(TextInputFormat.class);
        // Set input data format as TextOuputFormat
        job.setOutputFormatClass(TextOutputFormat.class);
        
        // Map input to the first argument passed to the main() method
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        // Map output to the second argument passed to the main() method
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
        // Wait for completion and store the result
        boolean success = job.waitForCompletion(true);
        // Return 0 if success or else 1
        return success ? 0: 1;
    }
    
    // This is class' static main method, which executes the task by reading
    // user arguments and passing them to the run() method
    public static void main(String[] args) throws Exception {
        // Pass the arguments to ToolRunner's run() method and get the result (0 = success; 1 = failure)
        int result = ToolRunner.run(new THandNotTH(), args);
        // Exit system with result
        System.exit(result);
    }

}

// =============================================================================
// Program name: OaklandCrimeStatsKML.java
// Author:       Animish Andraskar
// =============================================================================
// DESCRIPTION:
// ------------
// This program gets the arguments (<input_file_path>, <output_file_path>),
// sets up and controls the Hadoop job, mapper, reducer
//
// RESULTS:
// --------
// {KML file placed in the directory as Task8Output and a screenshot stored as
//  Task8OutputGoogleEarthScreenshot/Task8OutputGoogleEarthScreenshot.png}
// =============================================================================

package edu.cmu.andrew.aandrask;
// Essential imports
import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

// The primary class
public class OaklandCrimeStatsKML {
    // This is class' static main method, which executes the task by reading
    // user arguments and executes the Hadoop job
    public static void main(String[] args) throws IOException {
        // Check if number of arguments provided is 2
        if (args.length != 2) {
            // If not, raise an error
            System.err.println("Usage: OaklandCrimeStatsKML <input path> <output path>");
            // Exit with result -1
            System.exit(-1);
        }
        // Setup the primary interface to describe a map-reduce job to Hadoop framework for execution
        JobConf conf = new JobConf(OaklandCrimeStatsKML.class);
        // Give it a name
        conf.setJobName("Rapes Plus Robberies");
        // Map first argument to input
        FileInputFormat.addInputPath(conf, new Path(args[0]));
        // Map second argument to output
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));
        // Set the Mapper class for the job
        conf.setMapperClass(OaklandCrimeStatsKMLMapper.class);
        // Set the Reducer class for the job
        conf.setReducerClass(OaklandCrimeStatsKMLReducer.class);
        // Set the key class for the job output data
        conf.setOutputKeyClass(Text.class);
        // Set the value class for job outputs
        conf.setOutputValueClass(Text.class);
        // Run the job
        JobClient.runJob(conf);
     }
}
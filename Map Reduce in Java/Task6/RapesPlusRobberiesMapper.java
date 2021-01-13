// =============================================================================
// Program name: RapesPlusRobberiesMapper.java
// Author:       Animish Andraskar
// =============================================================================
// DESCRIPTION:
// ------------
// This program transforms input records into intermediate records, which are
// processed further by the reducer. The class extends Mapper interface with
// input key-value as LongWritable, Text and the output key-value as Text,
// IntWritable
// =============================================================================

package edu.cmu.andrew.aandrask;
// Essential imports
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

// As mentioned above, this class transforms input records into intermediate records, which are processed
// further by the reducer. The class extends Mapper interface with input key-value as LongWritable, Text
// and the output key-value as Text, IntWritable
public class RapesPlusRobberiesMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {

    // This method maps a single input key/value pair into an intermediate key/value pair
    public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
        // Get line from input file. This was passed in by Hadoop as value.
        // We have no use for the key (file offset) so we are ignoring it.
        String line = value.toString();

        // When the line is split, it will have following components:
        //  0:1 - State Plane (projected, rectilinear) x and y co-ordinates
        //  2   - time
        //  3   - street address
        //  4   - offence type
        //  5   - date
        //  6   - census tract

        // We need only offence type
        // Also, trim the value to remove extra spaces and do better string matching
        String offence = line.split("\t")[4].trim();

        // If the offence is rape or robbery, produce intermediate (crime_type, 1) key-value pair
        if (offence.equalsIgnoreCase("rape") || offence.equalsIgnoreCase("robbery")) {
             // For each rape or robbery crime in the input, reduce will be called with
             // ("Rapes and robberies", [1, 1, 1, ...., 1])
             // They key is a fixed string "Rapes and robberies" and integer 1 will be placed in the iterator
            output.collect(new Text("Rapes and robberies"), new IntWritable(1)); }
        }
}

// =============================================================================
// Program name: OaklandCrimeStatsReducer.java
// Author:       Animish Andraskar
// =============================================================================
// DESCRIPTION:
// ------------
// This program reduces the set of intermediate values which share a key
// (a fixed string) to a smaller set of values (count of crimes within 200mt)
// Both input and output: key = Text, value = IntWritable
// =============================================================================

package edu.cmu.andrew.aandrask;
// Essential imports
import java.io.IOException; import java.util.Iterator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer; import org.apache.hadoop.mapred.Reporter;

// As mentioned above, this class reduces a set of intermediate values which share a key (in this case a fixed string)
// to a smaller set of values (in this case the crime count). Both input and output: key = Text, value = IntWritable
public class OaklandCrimeStatsReducer extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
    // This method reduces values i.e. generate count for the given fixed string key
    public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text,
                       IntWritable> output, Reporter reporter) throws IOException {

        // For each value in the list, increment the count
        int count = 0;
        // Traverse each value
        while (values.hasNext()) {
            // We can ignore the value
            int dummy = values.next().get();
            // Increment the count
            count += 1;
        }
        // Emit (key = crime_type, value = number_of_crime)
        output.collect(key, new IntWritable(count));
    }
}
// =============================================================================
// Program name: OaklandCrimeStatsKMLMapper.java
// Author:       Animish Andraskar
// =============================================================================
// DESCRIPTION:
// ------------
// This program transforms input records into intermediate records, which are
// processed further by the reducer. The class extends Mapper interface with
// input key-value as LongWritable, Text and the output key-value as Text,
// Text
// =============================================================================

package edu.cmu.andrew.aandrask;
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
// and the output key-value as Text, Text
public class OaklandCrimeStatsKMLMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
    // This method maps a single input key/value pair into an intermediate key/value pair
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

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
        //  7:8 - Lat-long

        // We need only x-y coordinates, offence type and lat-longitude
        // Also, trim the values to remove extra spaces and do better string matching
        String xString = line.split("\t")[0].trim(); // x-coordinate
        String yString = line.split("\t")[1].trim(); // y-coordinate
        String offence = line.split("\t")[4].trim(); // offence type
        String lat = line.split("\t")[7].trim(); // latitude
        String lon = line.split("\t")[8].trim(); // longitude

        // If this is first row, it contains header instead of coordinates
        // hence, proceed further only if this is not the very first line
        double x = 0;
        double y = 0;
        if (!xString.equalsIgnoreCase("x")) {
            // Convert String coordinates to double
            x = Double.parseDouble(xString);
            y = Double.parseDouble(yString);
        }

        // If the offence is rape or robbery, produce intermediate (crime_type, 1) key-value pair
        if (offence.equalsIgnoreCase("aggravated assault")) {
            // For each aggrevated assault crime entry in the input,
            // find out its squared distance from 3803 Forbes Avenue in Oakland (1354326.897, 411447.7828)
            // Formula: (x2-x1)^2 + (y2-y1)^2
            double xDiff = 1354326.897 - x; // x2-x1
            double yDiff = 411447.7828 - y; // y2-y1
            double distanceSq = xDiff*xDiff + yDiff*yDiff; // (x2-x1)^2 + (y2-y1)^2

            // Check if this sq. distance is less than 300 meters (984.252 feet) squared
            // Or else, check if it is file header
            if (distanceSq < (984.252 * 984.252)) {
                // The reduce will be called with ({xml_header}, {lat,lon,0})
                // The value string is latitude, longitude and 0 altitude
                // They key is a blank string and a string "[latitude],[longitude],[0]" is placed in the iterator
                output.collect(new Text("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"), new Text(lon + "," + lat + ",0")); }
            }
        }
}
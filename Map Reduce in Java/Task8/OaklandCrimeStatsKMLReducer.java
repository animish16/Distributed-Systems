// =============================================================================
// Program name: OaklandCrimeStatsKMLReducer.java
// Author:       Animish Andraskar
// =============================================================================
// DESCRIPTION:
// ------------
// This program reduces the set of intermediate values which share a key
// (a fixed string) to a smaller set of values (lat-long of crimes within 300mt)
// Both input and output: key = Text, value = Text
// =============================================================================

package edu.cmu.andrew.aandrask;
// Essential imports
import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer; import org.apache.hadoop.mapred.Reporter;

// As mentioned above, this class reduces a set of intermediate values which share a key (in this case a fixed string)
// to a smaller set of values (in this case the crime count). Both input and output: key = Text, value = Text
public class OaklandCrimeStatsKMLReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
    // This method reduces values i.e. generate a KML placemerk String output for Google Earth
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text,
                       Text> output, Reporter reporter) throws IOException {

        // Each value in the list is a unique point to be added in the KML file
        // We will append all these rows into a String named KMLText

        // File header
        String KMLText =  "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n";

        // Counter for placemark name
        int count = 0;
        // Traverse each value in the list
        while (values.hasNext()) {
            // Append the value as a KML Placemerk point
            KMLText += "<Placemark><name>Crime point: " + (++count) + "</name>" +
                "<Point><coordinates>" + values.next().toString() + "</coordinates></Point>" +
                "</Placemark>\n";
        }

        // KML tag closing
        KMLText += "</Document>\n</kml>";
        // Emit (key = {XML header}, value = {KML content})
        output.collect(key, new Text(KMLText));
    }
}